package com.wuye.points.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.priceapproval.AuditPriceApprovalRequest;
import com.wuye.points.dto.priceapproval.CreatePriceApprovalRequest;
import com.wuye.points.dto.priceapproval.PriceApprovalDetailResponse;
import com.wuye.points.dto.priceapproval.PriceApprovalListItemResponse;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PriceApproval;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PriceApprovalRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceApprovalService {

    private static final Set<String> APPROVAL_STATUS = Set.of("pending", "approved", "rejected");

    private static final BigDecimal MIN_PRODUCT_PRICE = new BigDecimal("0.01");

    /** 审批落库金额上限（配送费等），与商家侧字段口径保持一致 */
    private static final BigDecimal MAX_APPROVAL_AMOUNT = new BigDecimal("9999.99");

    private final PriceApprovalRepository priceApprovalRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final RoleGuard roleGuard;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public PriceApprovalDetailResponse createPriceApproval(CreatePriceApprovalRequest request) {
        // 板块可进且可提交：platform_admin 或 物业领导；property_operator 不可
        requireModuleAccess();

        String propertyCompanyId = resolveCreatePropertyCompanyId(request.getPropertyCompanyId());
        if (!propertyCompanyRepository.existsById(propertyCompanyId)) {
            throw new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND);
        }

        PriceApproval approval = new PriceApproval();
        approval.setId(idGenerator.generateId("pa"));
        approval.setPropertyCompanyId(propertyCompanyId);
        approval.setApplicantId(UserContext.currentUserId());
        approval.setApplicantRole(StringUtils.hasText(UserContext.currentRole())
                ? UserContext.currentRole() : "unknown");
        approval.setItemType(request.getItemType());
        approval.setItemId(request.getItemId());
        approval.setOldValue(request.getOldValue());
        approval.setNewValue(request.getNewValue());
        approval.setReason(request.getReason());
        approval.setStatus("pending");
        priceApprovalRepository.save(approval);

        return toDetail(approval);
    }

    @Transactional
    public PriceApprovalDetailResponse auditPriceApproval(String id, AuditPriceApprovalRequest request) {
        // 仅物业领导可审批
        requirePropertyLeaderAuditor();
        adminSupport.validateIdPrefix(id, "pa_");

        PriceApproval approval = getAccessibleApprovalForAudit(id);
        if (!"pending".equals(approval.getStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }

        boolean approved = Boolean.TRUE.equals(request.getApproved());
        if (approved) {
            // 审批通过：按 itemType 落库生效（失败则整单回滚，审批仍为 pending）
            applyApprovedChanges(approval);
        }

        approval.setStatus(approved ? "approved" : "rejected");
        approval.setApproverId(UserContext.currentUserId());
        approval.setApproverRemark(request.getRemark());
        approval.setApprovedAt(LocalDateTime.now());
        priceApprovalRepository.save(approval);

        return toDetail(approval);
    }

    /**
     * 审批通过后按 itemType 落库。
     * 当前支持：resident_share_rate（分成比例）、product_price（商品价格）、
     * merchant_distribution（商家平台抽佣）、delivery_fee（商家配送费）。
     *
     * <p>未落库的类型（merchant_ad / property_fee_price）在此显式告警，
     * 避免「审批单显示已通过、业务参数一点没动」再次无声发生。</p>
     */
    private void applyApprovedChanges(PriceApproval approval) {
        switch (approval.getItemType() == null ? "" : approval.getItemType()) {
            case "resident_share_rate" -> applyResidentShareRate(approval);
            case "product_price" -> applyProductPrice(approval);
            case "merchant_distribution" -> applyMerchantDistribution(approval);
            case "delivery_fee" -> applyDeliveryFee(approval);
            default -> log.warn("[价格审批] itemType={} 无落库实现，审批状态已置为 approved 但业务参数未变更 id={} itemId={}",
                    approval.getItemType(), approval.getId(), approval.getItemId());
        }
    }

    /**
     * 板块负责人审批商家分成（merchant_distribution）：
     * 需求"个体负责人设置分成比例，提交后等待区块负责人同意"。
     * 仅允许审批申请人个体负责人归属自己的申请。
     */
    @Transactional
    public PriceApprovalDetailResponse auditAsSectorLeader(
            String approvalId, String sectorLeaderId, boolean approved, String remark) {
        PriceApproval approval = priceApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "审批记录不存在"));
        if (!"merchant_distribution".equals(approval.getItemType())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "板块负责人仅可审批商家分成申请");
        }
        if (!"pending".equals(approval.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该审批已处理，不可重复操作");
        }
        IndividualLeader applicant = individualLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        approval.getApplicantId(), approval.getPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.INDIVIDUAL_LEADER_NOT_FOUND, "申请人个体负责人不存在"));
        if (!sectorLeaderId.equals(applicant.getSectorLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "该个体负责人不归属当前板块负责人");
        }

        if (approved) {
            applyApprovedChanges(approval);
        }
        approval.setStatus(approved ? "approved" : "rejected");
        approval.setApproverId(UserContext.currentUserId());
        approval.setApproverRemark(remark);
        approval.setApprovedAt(LocalDateTime.now());
        priceApprovalRepository.save(approval);
        return toDetail(approval);
    }

    /**
     * 商家平台抽佣审批落库：
     * itemId = merchants.id，newValue = 平台抽佣比例（0~1），
     * 落 {@code merchant_property_configs.commission_rate}（分账计算真正读取的字段，按商家×物业公司）。
     *
     * <p>此前这里写的是 {@code merchants.individual_leader_rate}——该列全仓库无任何读取方
     * （分账用的个体负责人比例在 {@code property_companies.individual_leader_rate}），
     * 所以「物业领导点了通过、抽佣一点没变」。同时值格式有两种来源，都必须能吃下：
     * 个体负责人接口落纯数字（{@code 0.1}），管理端「发起申请」落 JSON（{@code {"rate":0.1}}）。</p>
     */
    private void applyMerchantDistribution(PriceApproval approval) {
        Merchant merchant = requireMerchantInApprovalProperty(approval);
        BigDecimal rate = parseRate(approval.getNewValue());

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), approval.getPropertyCompanyId())
                .orElseGet(() -> {
                    MerchantPropertyConfig created = new MerchantPropertyConfig();
                    created.setId(idGenerator.generateId("mpc"));
                    created.setMerchantId(merchant.getId());
                    created.setPropertyCompanyId(approval.getPropertyCompanyId());
                    created.setStatus("active");
                    return created;
                });
        config.setCommissionRate(rate);
        merchantPropertyConfigRepository.save(config);

        // 商家归属的个体负责人绑定保持原行为（申请人是在职个体负责人时才绑）
        individualLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        approval.getApplicantId(), approval.getPropertyCompanyId(), "active")
                .ifPresent(leader -> merchant.setIndividualLeaderId(leader.getId()));
        merchantRepository.save(merchant);
    }

    /**
     * 商家配送费审批落库：itemId = merchants.id，
     * newValue = {@code {"amount":1}}（管理端「发起申请」）或纯数字。
     */
    private void applyDeliveryFee(PriceApproval approval) {
        Merchant merchant = requireMerchantInApprovalProperty(approval);
        BigDecimal amount = parseAmount(approval.getNewValue());
        merchant.setDeliveryFee(amount);
        merchantRepository.save(merchant);
    }

    private Merchant requireMerchantInApprovalProperty(PriceApproval approval) {
        String merchantId = approval.getItemId();
        if (!StringUtils.hasText(merchantId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "itemId 必须为商家ID");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!StringUtils.hasText(approval.getPropertyCompanyId())
                || !approval.getPropertyCompanyId().equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "商家不属于该审批所属物业公司");
        }
        return merchant;
    }

    /** 抽佣比例：接受纯数字或 {@code {"rate":x}} / {@code {"commissionRate":x}}，范围 0~1。 */
    private BigDecimal parseRate(String value) {
        BigDecimal rate = readNumber(value, "rate", "commissionRate");
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分成比例必须在 0~1 之间");
        }
        return rate;
    }

    /** 金额：接受纯数字或 {@code {"amount":x}}，范围 0~9999.99。 */
    private BigDecimal parseAmount(String value) {
        BigDecimal amount = readNumber(value, "amount", "deliveryFee");
        if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(MAX_APPROVAL_AMOUNT) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "金额必须在 0~9999.99 之间");
        }
        return amount;
    }

    private BigDecimal readNumber(String raw, String... jsonFields) {
        if (!StringUtils.hasText(raw)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 不能为空");
        }
        String text = raw.trim();
        if (text.startsWith("{")) {
            JsonNode root;
            try {
                root = objectMapper.readTree(text);
            } catch (Exception e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "newValue 格式不正确");
            }
            for (String field : jsonFields) {
                JsonNode node = root.path(field);
                if (node.isNumber() || node.isTextual()) {
                    return toDecimal(node.asText());
                }
            }
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "newValue 需为 " + String.join("/", jsonFields) + " 的数值");
        }
        return toDecimal(text);
    }

    private BigDecimal toDecimal(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 必须为数字");
        }
    }

    /**
     * 商品价格审批落库：
     * itemId = products.id（prd_ 开头），解析 newValue 写入 price/memberPrice/pointPrice。
     * 兼容简写纯数字 "30.00" → 仅更新 price；JSON 对象可部分更新。
     * 校验失败抛出异常，整单不生效（审批保持 pending）。
     */
    private void applyProductPrice(PriceApproval approval) {
        String productId = approval.getItemId();
        if (!StringUtils.hasText(productId) || !productId.startsWith("prd_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "itemId 必须为商品ID(prd_开头)");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getDeletedAt() != null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Merchant merchant = merchantRepository.findById(product.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!StringUtils.hasText(approval.getPropertyCompanyId())
                || !approval.getPropertyCompanyId().equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "商品不属于该审批所属物业公司");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(approval.getNewValue());
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 必须为合法JSON或数字");
        }
        if (root == null || root.isNull()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 不能为空");
        }

        BigDecimal price = null;
        BigDecimal memberPrice = null;
        Integer pointPrice = null;

        if (root.isNumber()) {
            // 兼容简写："30.00"
            price = readProductMoney(root, "price");
        } else if (root.isObject()) {
            if (root.has("price")) {
                price = readProductMoney(root.get("price"), "price");
            }
            if (root.has("memberPrice")) {
                memberPrice = readProductMoney(root.get("memberPrice"), "memberPrice");
            }
            if (root.has("pointPrice")) {
                pointPrice = readPointPrice(root.get("pointPrice"));
            }
            if (price == null && memberPrice == null && pointPrice == null) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "newValue 需包含 price/memberPrice/pointPrice 之一");
            }
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 必须为JSON对象或数字");
        }

        BigDecimal effectivePrice = price != null ? price : product.getPrice();
        BigDecimal effectiveMemberPrice = memberPrice != null ? memberPrice : product.getMemberPrice();
        if (effectiveMemberPrice != null && effectiveMemberPrice.compareTo(effectivePrice) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "业主价不能高于原价");
        }

        if (price != null) {
            product.setPrice(price);
        }
        if (memberPrice != null) {
            product.setMemberPrice(memberPrice);
        }
        if (pointPrice != null) {
            product.setPointPrice(pointPrice);
        }
        productRepository.save(product);
    }

    private BigDecimal readProductMoney(JsonNode node, String field) {
        if (node == null || !node.isNumber()) {
            throw new BizException(ErrorCode.PARAM_INVALID, field + " 必须为数值");
        }
        BigDecimal value = node.decimalValue();
        if (value.compareTo(MIN_PRODUCT_PRICE) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, field + " 必须大于0");
        }
        return value;
    }

    private Integer readPointPrice(JsonNode node) {
        if (node == null || !node.isNumber() || !node.canConvertToInt()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "pointPrice 必须为整数");
        }
        int value = node.intValue();
        if (value < 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分兑换价必须大于0");
        }
        return value;
    }

    /**
     * 业主/商家/物业币/共享积分分成比例审批落库：
     * itemId = property_companies.id，解析 newValue JSON 写入四项分成字段。
     * 兼容手工发起的简写：{"rate":0.1} 或 {"residentPointShareRate":0.1} → 仅更新业主分成。
     * 校验合并后四项合计 ≤ 0.30；校验失败抛出异常，整单不生效。
     */
    private void applyResidentShareRate(PriceApproval approval) {
        String companyId = approval.getItemId();
        if (!StringUtils.hasText(companyId) || !companyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "itemId 必须为物业公司ID(pc_开头)");
        }
        PropertyCompany company = propertyCompanyRepository.findById(companyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        JsonNode root;
        try {
            root = objectMapper.readTree(approval.getNewValue());
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 必须为合法JSON");
        }
        if (root == null || !root.isObject()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newValue 必须为JSON对象");
        }

        BigDecimal resident = null;
        BigDecimal merchant = null;
        BigDecimal coin = null;
        BigDecimal shared = null;
        if (root.has("rate") && root.has("residentPointShareRate")) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "rate 与 residentPointShareRate 不能同时出现");
        }
        if (root.has("rate")) {
            resident = readRate(root.get("rate"), "rate");
        }
        if (root.has("residentPointShareRate")) {
            resident = readRate(root.get("residentPointShareRate"), "residentPointShareRate");
        }
        if (root.has("merchantPointShareRate")) {
            merchant = readRate(root.get("merchantPointShareRate"), "merchantPointShareRate");
        }
        if (root.has("coinPointShareRate")) {
            coin = readRate(root.get("coinPointShareRate"), "coinPointShareRate");
        }
        if (root.has("sharedPointShareRate")) {
            shared = readRate(root.get("sharedPointShareRate"), "sharedPointShareRate");
        }
        if (resident == null && merchant == null && coin == null && shared == null) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "newValue 需包含 rate 或 residentPointShareRate/merchantPointShareRate/coinPointShareRate/sharedPointShareRate 之一");
        }

        // 合并后四项合计校验：业主/商家/物业币/共享积分分成比例为积分池内部子分配，合计必须恰好为 100%（1.0）
        BigDecimal newResident = resident != null ? resident : defaultZero(company.getResidentPointShareRate());
        BigDecimal newMerchant = merchant != null ? merchant : defaultZero(company.getMerchantPointShareRate());
        BigDecimal newCoin = coin != null ? coin : defaultZero(company.getCoinPointShareRate());
        BigDecimal newShared = shared != null ? shared : defaultZero(company.getSharedPointShareRate());
        BigDecimal total = newResident.add(newMerchant).add(newCoin).add(newShared);
        if (total.compareTo(BigDecimal.ZERO) < 0 || total.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.INVALID_SHARE_RATE_TOTAL,
                    "业主/商家/物业币/共享积分分成比例合计必须在 0% ~ 100% 之间");
        }
        if (total.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.0001")) > 0) {
            throw new BizException(ErrorCode.INVALID_SHARE_RATE_TOTAL,
                    "业主/商家/物业币/共享积分分成比例合计必须为100%");
        }

        if (resident != null) {
            company.setResidentPointShareRate(resident);
        }
        if (merchant != null) {
            company.setMerchantPointShareRate(merchant);
        }
        if (coin != null) {
            company.setCoinPointShareRate(coin);
        }
        if (shared != null) {
            company.setSharedPointShareRate(shared);
        }
        propertyCompanyRepository.save(company);
    }

    private BigDecimal readRate(JsonNode node, String field) {
        if (node == null || !node.isNumber()) {
            throw new BizException(ErrorCode.PARAM_INVALID, field + " 必须为数值");
        }
        BigDecimal value = node.decimalValue();
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, field + " 不能为负数");
        }
        return value;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * 价格审批板块入口：platform_admin 或 物业领导。
     * property_operator / 其他角色 403。
     */
    private void requireModuleAccess() {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin() && permissionGuard.isCurrentPropertyLeader()) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED, "仅平台管理员或物业领导可使用价格审批");
    }

    /** 审批权限：仅物业领导（platform_admin 可看可提，不可批） */
    private void requirePropertyLeaderAuditor() {
        if (roleGuard.isPropertyAdmin() && permissionGuard.isCurrentPropertyLeader()) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED, "仅物业领导可审批价格修改");
    }

    private String resolveCreatePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (!StringUtils.hasText(requestedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "platform_admin 提交时必须指定 propertyCompanyId");
            }
            adminSupport.validateIdPrefix(requestedPropertyCompanyId, "pc_");
            return requestedPropertyCompanyId;
        }
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "当前用户未绑定物业公司");
        }
        return propertyCompanyId;
    }

    @Transactional(readOnly = true)
    public PageResponse<PriceApprovalListItemResponse> listPriceApprovals(
            int page, int pageSize, String status, String propertyCompanyId) {
        // 列表：平台管理员 + 物业领导；操作员不可
        requireModuleAccess();
        validateStatus(status);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");

        String scopedPropertyCompanyId;
        if (roleGuard.isPlatformAdmin()) {
            scopedPropertyCompanyId = StringUtils.hasText(propertyCompanyId) ? propertyCompanyId : null;
            if (scopedPropertyCompanyId != null) {
                adminSupport.validateIdPrefix(scopedPropertyCompanyId, "pc_");
            }
        } else {
            scopedPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!StringUtils.hasText(scopedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "当前用户未绑定物业公司");
            }
        }

        Page<PriceApproval> approvalPage;
        if (scopedPropertyCompanyId == null && StringUtils.hasText(status)) {
            approvalPage = priceApprovalRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (scopedPropertyCompanyId == null) {
            approvalPage = priceApprovalRepository.findAll(pageable);
        } else if (StringUtils.hasText(status)) {
            approvalPage = priceApprovalRepository.findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                    scopedPropertyCompanyId, status, pageable);
        } else {
            approvalPage = priceApprovalRepository.findByPropertyCompanyIdOrderByCreatedAtDesc(
                    scopedPropertyCompanyId, pageable);
        }

        Map<String, String> itemNames = resolveItemNames(approvalPage.getContent());
        List<PriceApprovalListItemResponse> list = approvalPage.getContent().stream()
                .map(approval -> toListItem(approval, itemNames))
                .toList();

        return PageResponse.of(list, page, pageSize, approvalPage.getTotalElements());
    }

    private PriceApproval getAccessibleApprovalForAudit(String id) {
        PriceApproval approval = priceApprovalRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "价格审批记录不存在"));
        // 审批人仅为物业领导，只能批本公司
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (currentPropertyCompanyId == null
                || !currentPropertyCompanyId.equals(approval.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权审批其他物业公司的记录");
        }
        return approval;
    }

    private PriceApprovalListItemResponse toListItem(PriceApproval approval, Map<String, String> itemNames) {
        return PriceApprovalListItemResponse.builder()
                .id(approval.getId())
                .propertyCompanyId(approval.getPropertyCompanyId())
                .applicantId(approval.getApplicantId())
                .applicantRole(approval.getApplicantRole())
                .itemType(approval.getItemType())
                .itemId(approval.getItemId())
                .itemName(itemNames.get(approval.getItemId()))
                .oldValue(approval.getOldValue())
                .newValue(approval.getNewValue())
                .reason(approval.getReason())
                .status(approval.getStatus())
                .approverId(approval.getApproverId())
                .approverRemark(approval.getApproverRemark())
                .approvedAt(DateTimeUtil.toIsoUtc(approval.getApprovedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(approval.getCreatedAt()))
                .build();
    }

    private PriceApprovalDetailResponse toDetail(PriceApproval approval) {
        return toDetail(approval, resolveItemNames(List.of(approval)));
    }

    private PriceApprovalDetailResponse toDetail(PriceApproval approval, Map<String, String> itemNames) {
        return PriceApprovalDetailResponse.builder()
                .id(approval.getId())
                .propertyCompanyId(approval.getPropertyCompanyId())
                .applicantId(approval.getApplicantId())
                .applicantRole(approval.getApplicantRole())
                .itemType(approval.getItemType())
                .itemId(approval.getItemId())
                .itemName(itemNames.get(approval.getItemId()))
                .oldValue(approval.getOldValue())
                .newValue(approval.getNewValue())
                .reason(approval.getReason())
                .status(approval.getStatus())
                .approverId(approval.getApproverId())
                .approverRemark(approval.getApproverRemark())
                .approvedAt(DateTimeUtil.toIsoUtc(approval.getApprovedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(approval.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(approval.getUpdatedAt()))
                .build();
    }

    /**
     * 「对象」列的名字：按 itemId 前缀定位（{@code mch_}=商家、{@code prd_}/{@code p_}=商品、
     * {@code pc_}=物业公司），批量回查一次；查不到名字的键缺失，前端回退显示 ID。
     */
    private Map<String, String> resolveItemNames(List<PriceApproval> approvals) {
        List<String> merchantIds = new ArrayList<>();
        List<String> productIds = new ArrayList<>();
        List<String> companyIds = new ArrayList<>();
        for (PriceApproval approval : approvals) {
            String itemId = approval.getItemId();
            if (!StringUtils.hasText(itemId)) {
                continue;
            }
            if (itemId.startsWith("mch_")) {
                merchantIds.add(itemId);
            } else if (itemId.startsWith("prd_") || itemId.startsWith("p_")) {
                productIds.add(itemId);
            } else if (itemId.startsWith("pc_")) {
                companyIds.add(itemId);
            }
        }

        Map<String, String> names = new HashMap<>();
        if (!merchantIds.isEmpty()) {
            merchantRepository.findAllById(merchantIds)
                    .forEach(merchant -> names.put(merchant.getId(), merchant.getName()));
        }
        if (!productIds.isEmpty()) {
            productRepository.findAllById(productIds)
                    .forEach(product -> names.put(product.getId(), product.getName()));
        }
        if (!companyIds.isEmpty()) {
            propertyCompanyRepository.findAllById(companyIds)
                    .forEach(company -> names.put(company.getId(), company.getName()));
        }
        return names;
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !APPROVAL_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }
}
