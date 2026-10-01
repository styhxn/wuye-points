package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.canteen.AdminRechargeResponse;
import com.wuye.points.dto.canteen.BindingCreateRequest;
import com.wuye.points.dto.canteen.BindingResponse;
import com.wuye.points.dto.canteen.BindingUpdateRequest;
import com.wuye.points.dto.canteen.CanteenSettingsRequest;
import com.wuye.points.dto.canteen.CanteenSettingsResponse;
import com.wuye.points.dto.canteen.CreateCanteenMerchantRequest;
import com.wuye.points.dto.canteen.DirectedFlowResponse;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.canteen.QuotaAdjustRequest;
import com.wuye.points.dto.canteen.QuotaRecordResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.MerchantService;
import com.wuye.points.entity.CanteenDirectedFlow;
import com.wuye.points.entity.CanteenDirectedRecharge;
import com.wuye.points.entity.CanteenMerchantBinding;
import com.wuye.points.entity.CanteenQuotaPurchase;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CanteenDirectedFlowRepository;
import com.wuye.points.repository.CanteenDirectedRechargeRepository;
import com.wuye.points.repository.CanteenMerchantBindingRepository;
import com.wuye.points.repository.CanteenQuotaPurchaseRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社区食堂-管理端服务：主商家标记、绑定/解绑、供货比例、手续费配置、三类流水查询。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCanteenService {

    private final RoleGuard roleGuard;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final CanteenMerchantBindingRepository bindingRepository;
    private final CanteenQuotaPurchaseRepository quotaPurchaseRepository;
    private final CanteenDirectedRechargeRepository rechargeRepository;
    private final CanteenDirectedFlowRepository flowRepository;
    private final PlatformSettingService platformSettingService;
    private final SnowflakeIdGenerator idGenerator;
    private final MerchantService merchantService;

    // ==================== 主商家标记 ====================

    /** 标记/取消商家为社区食堂主商家 */
    @Transactional
    public void setMainMerchant(String merchantId, boolean main) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (main && !Boolean.TRUE.equals(merchant.getIsCanteenMainMerchant())) {
            merchant.setIsCanteenMainMerchant(true);
            // 标记为主商家即纳入社区食堂体系（类型与主/子商家统一隔离）
            merchant.setMerchantType("canteen");
            merchantRepository.save(merchant);
            log.info("[社区食堂] 标记主商家 merchantId={} operator={}", merchantId, UserContext.currentUserId());
        } else if (!main && Boolean.TRUE.equals(merchant.getIsCanteenMainMerchant())) {
            merchant.setIsCanteenMainMerchant(false);
            // 取消主商家但保留 canteen 类型（仍是食堂商家，仅降级为非主）
            merchantRepository.save(merchant);
            log.info("[社区食堂] 取消主商家 merchantId={} operator={}", merchantId, UserContext.currentUserId());
        }
    }

    /**
     * 管理端创建社区食堂商家（主商家 / 子商家）。实际建库逻辑在 MerchantService.createCanteenMerchant。
     */
    @Transactional
    public MerchantDetailResponse createCanteenMerchant(CreateCanteenMerchantRequest request) {
        return merchantService.createCanteenMerchant(request);
    }

    // ==================== 手动调整可用额度（调试/运营兜底） ====================

    /**
     * 管理端直接给主商家增加可用充值额度（免手续费、无需支付，调试/运营兜底用）。
     * 落一条 status=paid 的额度购买流水，与正常购买到账口径一致（remaining_quota 参与汇总与扣减）。
     *
     * @return 调整后该主商家的可用充值额度总额
     */
    @Transactional
    public BigDecimal adjustQuota(String merchantId, QuotaAdjustRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家不存在"));
        if (!"canteen".equals(merchant.getMerchantType())
                || !Boolean.TRUE.equals(merchant.getIsCanteenMainMerchant())) {
            throw new BizException(ErrorCode.CANTEEN_NOT_MAIN_MERCHANT, "仅社区食堂主商家可调整可用充值额度");
        }

        CanteenQuotaPurchase purchase = new CanteenQuotaPurchase();
        purchase.setId(idGenerator.generateId("cqp"));
        purchase.setMerchantId(merchant.getId());
        purchase.setPropertyCompanyId(merchant.getPropertyCompanyId());
        purchase.setQuotaAmount(request.getAmount());
        purchase.setFeeRate(BigDecimal.ZERO);
        purchase.setFeeAmount(BigDecimal.ZERO);
        purchase.setActualQuota(request.getAmount());
        purchase.setPayAmount(BigDecimal.ZERO);
        purchase.setPaymentRecordId(null);
        purchase.setStatus("paid");
        purchase.setRemainingQuota(request.getAmount());
        purchase.setCompletedAt(LocalDateTime.now());
        quotaPurchaseRepository.save(purchase);

        log.info("[社区食堂] 管理端手动调整额度 main={} amount={} remark={} operator={}",
                merchant.getId(), request.getAmount(), request.getRemark(), UserContext.currentUserId());
        return quotaPurchaseRepository.sumRemainingQuota(merchant.getId());
    }

    // ==================== 绑定管理 ====================

    @Transactional
    public BindingResponse createBinding(BindingCreateRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant main = merchantRepository.findById(request.getMainMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "主商家不存在"));
        Merchant sub = merchantRepository.findById(request.getSubMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "子商家不存在"));
        if (main.getId().equals(sub.getId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "主商家与子商家不能相同");
        }
        if (!"canteen".equals(main.getMerchantType())
                || !Boolean.TRUE.equals(main.getIsCanteenMainMerchant())) {
            throw new BizException(ErrorCode.CANTEEN_NOT_MAIN_MERCHANT, "请先将该商家标记为社区食堂主商家");
        }
        if (!main.getPropertyCompanyId().equals(sub.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "主商家与子商家不属于同一物业公司");
        }

        String propertyCompanyId = main.getPropertyCompanyId();
        if (bindingRepository.findByMainMerchantIdAndSubMerchantIdAndPropertyCompanyId(
                main.getId(), sub.getId(), propertyCompanyId).isPresent()) {
            throw new BizException(ErrorCode.CANTEEN_BINDING_EXISTS);
        }

        CanteenMerchantBinding binding = new CanteenMerchantBinding();
        binding.setId(idGenerator.generateId("cmb"));
        binding.setMainMerchantId(main.getId());
        binding.setSubMerchantId(sub.getId());
        binding.setPropertyCompanyId(propertyCompanyId);
        binding.setSupplyDiscountRate(request.getSupplyDiscountRate() != null
                ? request.getSupplyDiscountRate() : new BigDecimal("0.2000"));
        binding.setStatus(CanteenService.BINDING_STATUS_ACTIVE);
        binding = bindingRepository.save(binding);

        log.info("[社区食堂] 绑定子商家 main={} sub={} rate={} operator={}",
                main.getId(), sub.getId(), binding.getSupplyDiscountRate(), UserContext.currentUserId());
        return toBindingResponse(binding, main, sub);
    }

    /** 解绑 = 软停用（历史订单退款仍可用订单快照体系） */
    @Transactional
    public void deleteBinding(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        CanteenMerchantBinding binding = bindingRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.CANTEEN_BINDING_NOT_FOUND));
        binding.setStatus("inactive");
        bindingRepository.save(binding);
        log.info("[社区食堂] 解绑绑定 bindingId={} operator={}", id, UserContext.currentUserId());
    }

    @Transactional
    public BindingResponse updateBindingRate(String id, BindingUpdateRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        CanteenMerchantBinding binding = bindingRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.CANTEEN_BINDING_NOT_FOUND));
        binding.setSupplyDiscountRate(request.getSupplyDiscountRate() != null
                ? request.getSupplyDiscountRate() : binding.getSupplyDiscountRate());
        binding = bindingRepository.save(binding);
        return toBindingResponse(binding, null, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<BindingResponse> listBindings(int page, int pageSize,
                                                      String mainMerchantId, String subMerchantId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Specification<CanteenMerchantBinding> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(mainMerchantId)) {
                predicates.add(cb.equal(root.get("mainMerchantId"), mainMerchantId));
            }
            if (StringUtils.hasText(subMerchantId)) {
                predicates.add(cb.equal(root.get("subMerchantId"), subMerchantId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<CanteenMerchantBinding> bindingPage = bindingRepository.findAll(spec, pageable);

        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        bindingPage.getContent().stream()
                                .flatMap(b -> java.util.stream.Stream.of(b.getMainMerchantId(), b.getSubMerchantId()))
                                .distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));

        List<BindingResponse> list = bindingPage.getContent().stream()
                .map(b -> toBindingResponse(b,
                        merchantMap.get(b.getMainMerchantId()), merchantMap.get(b.getSubMerchantId())))
                .toList();
        return PageResponse.of(list, page, pageSize, bindingPage.getTotalElements());
    }

    // ==================== 配置 ====================

    @Transactional(readOnly = true)
    public CanteenSettingsResponse getSettings() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return CanteenSettingsResponse.builder()
                .quotaFeeRate(platformSettingService.getCanteenQuotaFeeRate())
                .build();
    }

    @Transactional
    public CanteenSettingsResponse updateSettings(CanteenSettingsRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (request.getQuotaFeeRate() != null) {
            platformSettingService.setCanteenQuotaFeeRate(request.getQuotaFeeRate());
        }
        return getSettings();
    }

    // ==================== 三类流水查询 ====================

    /** 额度购买流水（管理端，按商家/状态/时间） */
    @Transactional(readOnly = true)
    public PageResponse<QuotaRecordResponse> listQuotaPurchases(int page, int pageSize,
                                                                String merchantId, String status,
                                                                String startDate, String endDate) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Specification<CanteenQuotaPurchase> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<CanteenQuotaPurchase> purchasePage = quotaPurchaseRepository.findAll(spec, pageable);
        List<QuotaRecordResponse> list = purchasePage.getContent().stream()
                .map(p -> QuotaRecordResponse.builder()
                        .id(p.getId())
                        .type("purchase")
                        .amount(p.getPayAmount())
                        .feeAmount(p.getFeeAmount())
                        .actualQuota(p.getActualQuota())
                        .status(p.getStatus())
                        .remark("购买充值额度")
                        .createdAt(DateTimeUtil.toIsoUtc(p.getCreatedAt()))
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, purchasePage.getTotalElements());
    }

    /** 定向充值流水（管理端，按商家/用户/时间） */
    @Transactional(readOnly = true)
    public PageResponse<AdminRechargeResponse> listRecharges(int page, int pageSize,
                                                             String mainMerchantId, String residentId,
                                                             String startDate, String endDate) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Specification<CanteenDirectedRecharge> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(mainMerchantId)) {
                predicates.add(cb.equal(root.get("mainMerchantId"), mainMerchantId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<CanteenDirectedRecharge> rechargePage = rechargeRepository.findAll(spec, pageable);

        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        rechargePage.getContent().stream().map(CanteenDirectedRecharge::getMainMerchantId)
                                .distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
        Map<String, Resident> residentMap = residentRepository.findAllById(
                        rechargePage.getContent().stream().map(CanteenDirectedRecharge::getResidentId)
                                .distinct().toList())
                .stream().collect(Collectors.toMap(Resident::getId, Function.identity()));

        List<AdminRechargeResponse> list = rechargePage.getContent().stream()
                .map(r -> {
                    Merchant main = merchantMap.get(r.getMainMerchantId());
                    Resident user = residentMap.get(r.getResidentId());
                    return AdminRechargeResponse.builder()
                            .id(r.getId())
                            .mainMerchantId(r.getMainMerchantId())
                            .mainMerchantName(main != null ? main.getName() : null)
                            .residentId(r.getResidentId())
                            .residentName(user != null ? user.getName() : null)
                            .residentPhone(user != null ? user.getPhone() : null)
                            .amount(r.getAmount())
                            .balanceAfter(r.getBalanceAfter())
                            .operatorId(r.getOperatorId())
                            .remark(r.getRemark())
                            .createdAt(DateTimeUtil.toIsoUtc(r.getCreatedAt()))
                            .build();
                })
                .toList();
        return PageResponse.of(list, page, pageSize, rechargePage.getTotalElements());
    }

    /** 定向余额消费/退款流水（管理端，按商家/用户/类型/时间） */
    @Transactional(readOnly = true)
    public PageResponse<DirectedFlowResponse> listFlows(int page, int pageSize,
                                                        String mainMerchantId, String merchantId,
                                                        String residentId, String flowType,
                                                        String startDate, String endDate) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Specification<CanteenDirectedFlow> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(mainMerchantId)) {
                predicates.add(cb.equal(root.get("mainMerchantId"), mainMerchantId));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(flowType)) {
                predicates.add(cb.equal(root.get("flowType"), flowType));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<CanteenDirectedFlow> flowPage = flowRepository.findAll(spec, pageable);

        Map<String, Merchant> merchantMap = merchantRepository.findAllById(
                        flowPage.getContent().stream().flatMap(f -> java.util.stream.Stream.of(f.getMainMerchantId(), f.getMerchantId()))
                                .distinct().toList())
                .stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));

        List<DirectedFlowResponse> list = flowPage.getContent().stream()
                .map(f -> DirectedFlowResponse.builder()
                        .id(f.getId())
                        .flowType(f.getFlowType())
                        .mainMerchantName(merchantMap.containsKey(f.getMainMerchantId())
                                ? merchantMap.get(f.getMainMerchantId()).getName() : null)
                        .amount(f.getAmount())
                        .balanceAfter(f.getBalanceAfter())
                        .orderNo(null)
                        .merchantName(merchantMap.containsKey(f.getMerchantId())
                                ? merchantMap.get(f.getMerchantId()).getName() : null)
                        .createdAt(DateTimeUtil.toIsoUtc(f.getCreatedAt()))
                        .build())
                .toList();
        return PageResponse.of(list, page, pageSize, flowPage.getTotalElements());
    }

    // ==================== 内部辅助 ====================

    private BindingResponse toBindingResponse(CanteenMerchantBinding binding, Merchant main, Merchant sub) {
        return BindingResponse.builder()
                .id(binding.getId())
                .mainMerchantId(binding.getMainMerchantId())
                .mainMerchantName(main != null ? main.getName() : null)
                .subMerchantId(binding.getSubMerchantId())
                .subMerchantName(sub != null ? sub.getName() : null)
                .supplyDiscountRate(binding.getSupplyDiscountRate())
                .status(binding.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(binding.getCreatedAt()))
                .build();
    }

    private LocalDateTime parseStart(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "开始日期格式不正确，需：yyyy-MM-dd");
        }
    }

    private LocalDateTime parseEnd(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value).plusDays(1).atStartOfDay();
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "结束日期格式不正确，需：yyyy-MM-dd");
        }
    }
}
