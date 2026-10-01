package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertycompany.AdminAccountResponse;
import com.wuye.points.dto.propertycompany.CommunityItemResponse;
import com.wuye.points.dto.propertycompany.CommunityListResponse;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyRequest;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyResponse;
import com.wuye.points.dto.propertycompany.UpdateCoinUseConditionRequest;
import com.wuye.points.dto.propertycompany.PropertyCompanyCurrentResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyListItemResponse;
import com.wuye.points.dto.propertycompany.UpdatePropertyCompanyRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.DeliveryRule;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.enums.CoinUseCondition;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.DeliveryRuleRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyCompanySpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PasswordUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PropertyCompanyService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "name", "updatedAt");
    private static final BigDecimal DEFAULT_PROPERTY_SHARE_RATE = new BigDecimal("0.3000");
    private static final BigDecimal DEFAULT_COORDINATOR_SHARE_RATE = new BigDecimal("0.7000");
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_INDIVIDUAL_LEADER_RATE = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_WITHDRAWAL_FEE_RATE = new BigDecimal("0.0060");
    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final BigDecimal DEFAULT_DELIVERY_COURIER_SHARE_RATE = new BigDecimal("1.0000");
    private static final BigDecimal DEFAULT_DELIVERY_COURIER_PER_ORDER = new BigDecimal("0.35");
    /** 分成比例求和容差：校验「三档合计 100%」时允许的浮点误差（2026-09-08 错误码 97006） */
    private static final BigDecimal RATE_TOLERANCE = new BigDecimal("0.0001");
    /** 新建物业公司初始管理员密码（与测试种子账号规则一致） */
    public static final String INITIAL_ADMIN_PASSWORD = "admin123456";

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final PointPoolRepository pointPoolRepository;
    private final DeliveryRuleRepository deliveryRuleRepository;
    private final OrderRepository orderRepository;
    private final PropertyCompanyConverter propertyCompanyConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PropertyCompanyCurrentResponse getCurrentConfig() {
        roleGuard.requireAnyRole("resident", "merchant", "courier", "activity_leader", "technician");

        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            Resident resident = residentRepository.findById(UserContext.currentUserId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            propertyCompanyId = resident.getPropertyCompanyId();
        }
        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        if (!"active".equals(company.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED, "物业公司已停用");
        }
        return propertyCompanyConverter.toCurrentResponse(company);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyCompanyListItemResponse> listPropertyCompanies(
            int page,
            int pageSize,
            String keyword,
            String status,
            String sort) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateStatus(status);

        String scopedId = null;
        if (roleGuard.isPropertyAdmin()) {
            scopedId = UserContext.currentPropertyCompanyId();
        }

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<PropertyCompany> companyPage = propertyCompanyRepository.findAll(
                PropertyCompanySpecifications.withFilters(keyword, status, scopedId),
                pageable);

        List<PropertyCompanyListItemResponse> list = companyPage.getContent().stream()
                .map(company -> propertyCompanyConverter.toListItem(
                        company,
                        communityRepository.countByPropertyCompanyId(company.getId()),
                        merchantRepository.countByPropertyCompanyId(company.getId()),
                        residentRepository.countByPropertyCompanyId(company.getId())))
                .toList();

        return PageResponse.of(list, page, pageSize, companyPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PropertyCompanyDetailResponse getPropertyCompany(String id) {
        validatePropertyCompanyId(id);
        PropertyCompany company = getReadableCompany(id);
        return buildDetail(company);
    }

    @Transactional(readOnly = true)
    public CommunityListResponse listCommunities(String id) {
        validatePropertyCompanyId(id);
        PropertyCompany company = getReadableCompany(id);
        List<CommunityItemResponse> communities = communityRepository.findByPropertyCompanyId(company.getId()).stream()
                .map(propertyCompanyConverter::toCommunityItem)
                .toList();
        return CommunityListResponse.builder()
                .propertyCompanyId(company.getId())
                .list(communities)
                .build();
    }

    @Transactional
    public CreatePropertyCompanyResponse createPropertyCompany(CreatePropertyCompanyRequest request) {
        roleGuard.requireAnyRole("platform_admin");

        // 业务校验前置：重名/重复手机号返回可预期的 400，而非落到唯一约束的 500
        String companyName = request.getName().trim();
        if (propertyCompanyRepository.existsByName(companyName)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业公司名称已存在");
        }
        Set<String> communityNames = new java.util.HashSet<>();
        for (CreatePropertyCompanyRequest.CreateCommunityRequest item : request.getCommunities()) {
            if (!communityNames.add(item.getName().trim())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "小区名称重复: " + item.getName().trim());
            }
        }

        PropertyCompany company = new PropertyCompany();
        company.setId(idGenerator.generateId("pc"));
        company.setName(companyName);
        company.setLogoUrl(request.getLogoUrl());
        company.setContactPhone(request.getContactPhone().trim());
        company.setAddress(request.getAddress());
        applyDefaultConfigs(company);
        company.setStatus("active");
        propertyCompanyRepository.save(company);

        List<CommunityItemResponse> communities = new ArrayList<>();
        for (CreatePropertyCompanyRequest.CreateCommunityRequest item : request.getCommunities()) {
            Community community = new Community();
            community.setId(idGenerator.generateId("com"));
            community.setPropertyCompanyId(company.getId());
            community.setName(item.getName().trim());
            community.setAddress(item.getAddress());
            community.setTotalBuildings(item.getTotalBuildings());
            community.setTotalUnits(item.getTotalUnits());
            community.setStatus("active");
            communityRepository.save(community);
            communities.add(propertyCompanyConverter.toCommunityItem(community));
        }

        AdminAccountResponse adminAccount = createAdminAccount(company.getId(), request.getAdminAccount());

        initializeDefaults(company);

        return CreatePropertyCompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .status(company.getStatus())
                .communities(communities)
                .adminAccount(adminAccount)
                .defaultConfigs("已自动初始化默认配置项")
                .createdAt(DateTimeUtil.toIsoUtc(company.getCreatedAt()))
                .build();
    }

    @Transactional
    public PropertyCompanyDetailResponse updatePropertyCompany(String id, UpdatePropertyCompanyRequest request) {
        validatePropertyCompanyId(id);
        PropertyCompany company = getManageableCompany(id);
        applyUpdate(company, request);
        // 2026-09-08：仅当本次请求涉及一级分账比例时才做三档合计校验，
        // 避免历史数据（platform=0 且 property+coordinator=1）阻塞配送费、状态等无关字段的保存
        validateDistributionRates(company, request);
        propertyCompanyRepository.save(company);
        return buildDetail(company);
    }

    /**
     * 设置物业币使用条件（§79.2 PUT /admin/property-companies/{id}/coin-use-condition）。
     * 角色：property_admin / platform_admin；property_admin 仅可操作本物业公司。
     */
    @Transactional
    public PropertyCompanyDetailResponse updateCoinUseCondition(String id, UpdateCoinUseConditionRequest request) {
        validatePropertyCompanyId(id);
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyCompany company = getManageableCompany(id);

        CoinUseCondition condition = CoinUseCondition.fromCode(request.getCondition());
        if (condition == CoinUseCondition.NONE && !CoinUseCondition.NONE.getCode().equals(request.getCondition())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "condition值无效，应为 none/point_threshold/property_fee_paid/manual_issue");
        }
        if (condition == CoinUseCondition.POINT_THRESHOLD && request.getPointThreshold() == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "condition=point_threshold 时必须填写 pointThreshold");
        }

        company.setCoinUseCondition(condition.getCode());
        company.setCoinPointThreshold(request.getPointThreshold());
        propertyCompanyRepository.save(company);
        return buildDetail(company);
    }

    @Transactional
    public void deletePropertyCompany(String id) {
        roleGuard.requireAnyRole("platform_admin");
        validatePropertyCompanyId(id);

        // 2026-09-08：硬删除拒绝时返回具体关联类型与数量，便于前端直接展示原因
        long communityCount = communityRepository.countByPropertyCompanyId(id);
        long residentCount = residentRepository.countByPropertyCompanyId(id);
        long merchantCount = merchantRepository.countByPropertyCompanyId(id);
        long orderCount = orderRepository.countByPropertyCompanyId(id);
        String relatedDetail = buildRelatedDetail(communityCount, residentCount, merchantCount, orderCount);
        if (!relatedDetail.isEmpty()) {
            throw new BizException(ErrorCode.PROPERTY_COMPANY_HAS_RELATED_DATA,
                    "该物业公司存在关联数据，不可删除（" + relatedDetail + "）。"
                            + "如仅需在列表隐藏，请改用 PATCH /admin/property-companies/{id}/status 置为 inactive");
        }

        PropertyCompany company = propertyCompanyRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        deliveryRuleRepository.findByPropertyCompanyId(id).ifPresent(deliveryRuleRepository::delete);
        pointPoolRepository.findByPropertyCompanyId(id).ifPresent(pointPoolRepository::delete);
        propertyCompanyRepository.delete(company);
    }

    private AdminAccountResponse createAdminAccount(
            String propertyCompanyId,
            CreatePropertyCompanyRequest.CreateAdminAccountRequest accountRequest) {
        String phone = accountRequest.getPhone().trim();
        if (residentRepository.findByPhone(phone).isPresent()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "管理员手机号已被使用: " + PhoneUtil.mask(phone));
        }

        Resident admin = new Resident();
        admin.setId(idGenerator.generateId("res"));
        admin.setName(accountRequest.getName().trim());
        admin.setPhone(phone);
        // 初始密码与种子账号规则一致（admin123456，BCrypt 加密），管理员可凭此走 /auth/admin-login
        admin.setPassword(PasswordUtil.hashPassword(INITIAL_ADMIN_PASSWORD));
        admin.setRole("property_admin");
        admin.setUserType("owner");
        admin.setPropertyCompanyId(propertyCompanyId);
        admin.setStatus("active");
        admin.setWechatBound(false);
        admin.setPointBalance(0);
        admin.setCoinBalance(BigDecimal.ZERO);
        admin.setCoinFrozen(false);
        admin.setCoinHidden(false);
        residentRepository.save(admin);
        return propertyCompanyConverter.toAdminAccount(admin, accountRequest.getEmail(), INITIAL_ADMIN_PASSWORD);
    }

    private void initializeDefaults(PropertyCompany company) {
        PointPool pool = new PointPool();
        pool.setId(idGenerator.generateId("pp"));
        pool.setPropertyCompanyId(company.getId());
        pool.setBalance(BigDecimal.ZERO);
        pool.setTotalIn(BigDecimal.ZERO);
        pool.setTotalOut(BigDecimal.ZERO);
        pointPoolRepository.save(pool);

        if (deliveryRuleRepository.findByPropertyCompanyId(company.getId()).isEmpty()) {
            DeliveryRule rule = new DeliveryRule();
            rule.setId(idGenerator.generateId("drl"));
            rule.setName("默认配送规则");
            rule.setBaseFee(defaultDecimal(company.getDeliveryBaseFee(), BigDecimal.ZERO));
            rule.setPerKgFee(BigDecimal.ZERO);
            rule.setFreeThreshold(BigDecimal.ZERO);
            rule.setCourierShareRate(defaultDecimal(
                    company.getDeliveryCourierShareRate(), DEFAULT_DELIVERY_COURIER_SHARE_RATE));
            rule.setCourierPerOrder(defaultDecimal(
                    company.getDeliveryCourierPerOrder(), DEFAULT_DELIVERY_COURIER_PER_ORDER));
            rule.setTimeoutMinutes(30);
            rule.setPropertyCompanyId(company.getId());
            rule.setStatus("active");
            deliveryRuleRepository.save(rule);
        }
    }

    private void applyDefaultConfigs(PropertyCompany company) {
        company.setPropertyShareRate(DEFAULT_PROPERTY_SHARE_RATE);
        company.setCoordinatorShareRate(DEFAULT_COORDINATOR_SHARE_RATE);
        company.setSectorLeaderRate(DEFAULT_SECTOR_LEADER_RATE);
        company.setIndividualLeaderRate(DEFAULT_INDIVIDUAL_LEADER_RATE);
        company.setCoinDisplayEnabled(true);
        // point_enabled / coin_enabled 服务器列为 NOT NULL 且 Hibernate 插入显式 NULL 会绕过列默认值，
        // 必须显式赋值，否则创建物业公司 400「coin_enabled cannot be null」（2026-09-07 前端问题 G 根因）
        company.setPointEnabled(true);
        company.setCoinEnabled(true);
        company.setCoinIssueMode("auto");
        company.setCoinExpiryDays(365);
        company.setCoinFreezeDefault(false);
        company.setDeliveryBaseFee(BigDecimal.ZERO);
        company.setDeliveryCourierShareRate(DEFAULT_DELIVERY_COURIER_SHARE_RATE);
        company.setDeliveryCourierPerOrder(DEFAULT_DELIVERY_COURIER_PER_ORDER);
        company.setWithdrawalFeeRate(DEFAULT_WITHDRAWAL_FEE_RATE);
        company.setPointToFeeRate(DEFAULT_POINT_TO_FEE_RATE);
        company.setTwoYearClearEnabled(true);
        company.setNeighborDailyContactLimit(2);
    }

    private void applyUpdate(PropertyCompany company, UpdatePropertyCompanyRequest request) {
        if (StringUtils.hasText(request.getName())) {
            company.setName(request.getName().trim());
        }
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }
        if (StringUtils.hasText(request.getContactPhone())) {
            company.setContactPhone(request.getContactPhone().trim());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            validateStatus(request.getStatus());
            company.setStatus(request.getStatus());
        }

        boolean configUpdate = hasConfigUpdate(request);
        if (configUpdate) {
            assertCanUpdateConfig(company.getId());
        }

        if (request.getPlatformShareRate() != null) {
            // 「我们公司」一档为平台级配置，仅平台管理员可写；物业侧请走 /admin/platform-share-rates 只读查看
            if (!roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "仅平台管理员可修改我们公司（平台）分成比例");
            }
            company.setPlatformShareRate(request.getPlatformShareRate());
        }
        if (request.getPropertyShareRate() != null) {
            company.setPropertyShareRate(request.getPropertyShareRate());
        }
        if (request.getCoordinatorShareRate() != null) {
            company.setCoordinatorShareRate(request.getCoordinatorShareRate());
        }
        if (request.getSectorLeaderRate() != null) {
            company.setSectorLeaderRate(request.getSectorLeaderRate());
        }
        if (request.getIndividualLeaderRate() != null) {
            company.setIndividualLeaderRate(request.getIndividualLeaderRate());
        }
        if (request.getCoinDisplayEnabled() != null) {
            company.setCoinDisplayEnabled(request.getCoinDisplayEnabled());
        }
        if (StringUtils.hasText(request.getCoinIssueMode())) {
            validateCoinIssueMode(request.getCoinIssueMode());
            company.setCoinIssueMode(request.getCoinIssueMode());
        }
        if (request.getCoinExpiryDays() != null) {
            company.setCoinExpiryDays(request.getCoinExpiryDays());
        }
        if (request.getCoinFreezeDefault() != null) {
            company.setCoinFreezeDefault(request.getCoinFreezeDefault());
        }
        if (request.getDeliveryBaseFee() != null) {
            company.setDeliveryBaseFee(request.getDeliveryBaseFee());
        }
        if (request.getDeliveryCourierShareRate() != null) {
            company.setDeliveryCourierShareRate(request.getDeliveryCourierShareRate());
        }
        if (request.getDeliveryCourierPerOrder() != null) {
            company.setDeliveryCourierPerOrder(request.getDeliveryCourierPerOrder());
        }
        if (request.getWithdrawalFeeRate() != null) {
            company.setWithdrawalFeeRate(request.getWithdrawalFeeRate());
        }
        if (request.getPointToFeeRate() != null) {
            company.setPointToFeeRate(request.getPointToFeeRate());
        }
        if (request.getTwoYearClearEnabled() != null) {
            company.setTwoYearClearEnabled(request.getTwoYearClearEnabled());
        }
        if (request.getNeighborDailyContactLimit() != null) {
            company.setNeighborDailyContactLimit(request.getNeighborDailyContactLimit());
        }
    }

    private PropertyCompanyDetailResponse buildDetail(PropertyCompany company) {
        List<CommunityItemResponse> communities = communityRepository.findByPropertyCompanyId(company.getId()).stream()
                .map(propertyCompanyConverter::toCommunityItem)
                .toList();
        List<AdminAccountResponse> adminAccounts = residentRepository
                .findByPropertyCompanyIdAndRole(company.getId(), "property_admin").stream()
                .map(resident -> propertyCompanyConverter.toAdminAccount(resident, null))
                .toList();
        return propertyCompanyConverter.toDetail(company, communities, adminAccounts);
    }

    private PropertyCompany getReadableCompany(String id) {
        PropertyCompany company = propertyCompanyRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return company;
        }
        if (roleGuard.isPropertyAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId != null && currentPropertyCompanyId.equals(company.getId())) {
                return company;
            }
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权访问该物业公司");
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private PropertyCompany getManageableCompany(String id) {
        PropertyCompany company = propertyCompanyRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return company;
        }
        if (roleGuard.isPropertyAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId != null && currentPropertyCompanyId.equals(company.getId())) {
                return company;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertCanUpdateConfig(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId.equals(currentPropertyCompanyId)) {
                return;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    /**
     * 商品平台盘一级三档分成比例校验（2026-09-08 前端问题：旧规则「物业+统筹=100%」已废弃）。
     *
     * <p>现行口径（API v8.1）：
     * <pre>
     * platformShareRate + propertyShareRate + coordinatorShareRate = 1.0000
     * </pre>
     * 其中 platformShareRate（我们公司）取本次 body 传入值，未传则用已落库值。
     * 校验失败返回错误码 {@code 97006}。
     *
     * <p>sectorLeaderRate / individualLeaderRate 是管理盘内部的级联系数（默认 0.6 / 0.5），
     * 二者相加并无业务含义，故只做 0~1 区间校验，<b>不再</b>校验二者之和。
     */
    private void validateDistributionRates(PropertyCompany company, UpdatePropertyCompanyRequest request) {
        boolean touched = request.getPlatformShareRate() != null
                || request.getPropertyShareRate() != null
                || request.getCoordinatorShareRate() != null;
        if (!touched) {
            return;
        }

        BigDecimal platform = requireRate(company.getPlatformShareRate(), "我们公司（平台）分成比例");
        BigDecimal property = requireRate(company.getPropertyShareRate(), "物业公司分成比例");
        BigDecimal coordinator = requireRate(company.getCoordinatorShareRate(), "管理盘分成比例");

        BigDecimal sum = platform.add(property).add(coordinator);
        if (sum.subtract(BigDecimal.ONE).abs().compareTo(RATE_TOLERANCE) > 0) {
            throw new BizException(ErrorCode.SHARE_RATE_PRIMARY_TOTAL_INVALID,
                    "商品平台盘一级分成比例合计必须为100%（我们公司 "
                            + toPercent(platform) + "% + 物业 " + toPercent(property)
                            + "% + 管理盘 " + toPercent(coordinator) + "% = "
                            + toPercent(sum) + "%）");
        }

        // 级联系数仅校验区间，不做相加校验
        if (company.getSectorLeaderRate() != null) {
            requireRate(company.getSectorLeaderRate(), "板块负责人分成比例");
        }
        if (company.getIndividualLeaderRate() != null) {
            requireRate(company.getIndividualLeaderRate(), "个体负责人分成比例");
        }
    }

    private BigDecimal requireRate(BigDecimal value, String label) {
        BigDecimal rate = value == null ? BigDecimal.ZERO : value;
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, label + "必须在0~100%之间");
        }
        return rate;
    }

    private String toPercent(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
    }

    private String buildRelatedDetail(long communityCount, long residentCount,
                                      long merchantCount, long orderCount) {
        List<String> parts = new ArrayList<>();
        if (communityCount > 0) {
            parts.add("小区 " + communityCount + " 个");
        }
        if (residentCount > 0) {
            parts.add("住户 " + residentCount + " 个");
        }
        if (merchantCount > 0) {
            parts.add("商家 " + merchantCount + " 个");
        }
        if (orderCount > 0) {
            parts.add("订单 " + orderCount + " 个");
        }
        return String.join("、", parts);
    }

    private boolean hasConfigUpdate(UpdatePropertyCompanyRequest request) {
        return request.getPlatformShareRate() != null
                || request.getPropertyShareRate() != null
                || request.getCoordinatorShareRate() != null
                || request.getSectorLeaderRate() != null
                || request.getIndividualLeaderRate() != null
                || request.getCoinDisplayEnabled() != null
                || request.getCoinIssueMode() != null
                || request.getCoinExpiryDays() != null
                || request.getCoinFreezeDefault() != null
                || request.getDeliveryBaseFee() != null
                || request.getDeliveryCourierShareRate() != null
                || request.getDeliveryCourierPerOrder() != null
                || request.getWithdrawalFeeRate() != null
                || request.getPointToFeeRate() != null
                || request.getTwoYearClearEnabled() != null
                || request.getNeighborDailyContactLimit() != null;
    }

    private void validatePropertyCompanyId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "物业公司ID格式不正确");
        }
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !RoleGuard.PROPERTY_COMPANY_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private void validateCoinIssueMode(String coinIssueMode) {
        if (!RoleGuard.COIN_ISSUE_MODES.contains(coinIssueMode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "coinIssueMode值无效");
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!SORTABLE_FIELDS.contains(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }
}
