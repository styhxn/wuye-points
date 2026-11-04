package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.roleaccount.CreateRoleAccountRequest;
import com.wuye.points.dto.admin.roleaccount.CreateRoleAccountResponse;
import com.wuye.points.dto.admin.roleaccount.DeleteRoleAccountResponse;
import com.wuye.points.dto.admin.roleaccount.DisableRoleAccountResponse;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.PasswordUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 管理端直接创建 / 删除业务角色账号。
 * <p>适用角色：merchant（商家）、activity_leader（活动组长）、technician（技工）、courier（配送员）。
 * <p>用于数据被误删后的快速恢复：一键创建「账号 + 密码 + 关联店铺/绑定」，避免手改 SQL。
 * <p>权限：物业管理员（property_admin，含物业领导/物业操作员）、平台管理员。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoleAccountService {

    private static final Set<String> SUPPORTED_ROLES = Set.of(
            "merchant", "activity_leader", "technician", "courier");

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    /** 落库列精度上限：超出会被 MySQL 判为 out-of-range，冒出来是一个无法定位的 409。 */
    private static final BigDecimal MAX_COMMISSION_RATE = BigDecimal.ONE;
    private static final BigDecimal MAX_POINT_EXCHANGE_RATE = new BigDecimal("999999.9999");
    private static final BigDecimal MAX_DELIVERY_FEE = new BigDecimal("99999999.99");

    private static final Pattern DUPLICATE_PATTERN =
            Pattern.compile("Duplicate entry '([^']*)' for key '([^']+)'");
    private static final Pattern NOT_NULL_PATTERN =
            Pattern.compile("Column '([^']+)' cannot be null");
    private static final Pattern FOREIGN_KEY_PATTERN =
            Pattern.compile("CONSTRAINT `([^`]+)` FOREIGN KEY");

    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final MerchantRepository merchantRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final MerchantBindingService merchantBindingService;
    private final MerchantConverter merchantConverter;
    private final MerchantCategoryService merchantCategoryService;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final ActivityLeaderIdentityRepository activityLeaderIdentityRepository;

    /**
     * 直接创建业务角色账号。
     * <ul>
     *   <li>merchant：创建账号 + PlatformMerchant + Merchant(approved/active) + 物业参数配置，直接可用、可直接登录</li>
     *   <li>activity_leader：创建账号 + 自动开通 group_leader 小店</li>
     *   <li>technician：创建账号 + 自动开通 technician 小店（社区服务收入提现）</li>
     *   <li>courier：创建账号（配送员）</li>
     * </ul>
     */
    @Transactional
    public CreateRoleAccountResponse createAccount(CreateRoleAccountRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        String role = request.getRole();
        if (!SUPPORTED_ROLES.contains(role)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "仅支持创建角色: merchant/activity_leader/technician/courier");
        }

        String propertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        adminSupport.validateIdPrefix(propertyCompanyId, "pc_");
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        // 小区外键与金额列精度都在库里硬约束：不前置校验的话，会以「数据冲突，请刷新后重试」的 409 冒出来，
        // 运营看不到是哪个字段错。这里一律转成带字段名的 400。
        validateCommunity(request.getCommunityId(), propertyCompanyId);
        if ("merchant".equals(role)) {
            validateMerchantParams(request);
        }

        if (residentRepository.existsByPhoneAndPropertyCompanyId(request.getPhone(), propertyCompanyId)) {
            throw new BizException(ErrorCode.RESIDENT_DUPLICATE_PHONE);
        }

        // 1. 创建账号
        Resident resident = new Resident();
        resident.setId(idGenerator.generateId("res"));
        resident.setPhone(request.getPhone());
        resident.setName(request.getName().trim());
        resident.setPassword(PasswordUtil.hashPassword(request.getPassword()));
        // 2026-10-21 组长标签化：组长是住户子身份，role 恒为 resident，身份建 activity_leader_identities 记录
        resident.setRole("activity_leader".equals(role) ? "resident" : role);
        resident.setUserType("owner");
        resident.setGender(0);
        resident.setHasChildren(false);
        resident.setPropertyCompanyId(propertyCompanyId);
        if (StringUtils.hasText(request.getCommunityId())) {
            adminSupport.validateIdPrefix(request.getCommunityId(), "com_");
            resident.setCommunityId(request.getCommunityId());
        }
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setCoinFrozen(false);
        resident.setCoinHidden(false);
        resident.setWechatBound(false);
        resident.setStatus(ResidentStatus.active.name());
        persist("residents（账号）", residentRepository, resident);

        // 2. 按角色创建关联店铺/绑定/身份
        String merchantId = null;
        switch (role) {
            case "merchant" -> merchantId = createMerchantShop(resident, request);
            case "activity_leader" -> {
                merchantBindingService.ensureGroupLeaderMerchant(resident.getId());
                createLeaderIdentity(resident, "appoint", null);
            }
            case "technician" -> merchantBindingService.ensureTechnicianMerchant(resident.getId());
            case "courier" -> { /* 配送员无额外绑定 */ }
        }

        return CreateRoleAccountResponse.builder()
                .residentId(resident.getId())
                .name(resident.getName())
                .phone(resident.getPhone())
                .role(role)
                .propertyCompanyId(propertyCompanyId)
                .merchantId(merchantId)
                .status(resident.getStatus())
                .build();
    }

    /** 小区必须存在且属于该物业公司，否则 residents 外键会以 409「数据冲突」冒出来。 */
    private void validateCommunity(String communityId, String propertyCompanyId) {
        if (!StringUtils.hasText(communityId)) {
            return;
        }
        adminSupport.validateIdPrefix(communityId, "com_");
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        if (!propertyCompanyId.equals(community.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "小区不属于该物业公司");
        }
    }

    /** 商家数值参数按落库列精度校验：抽佣比例是小数不是百分数，填成 10 会撞 decimal(5,4) 上限。 */
    private void validateMerchantParams(CreateRoleAccountRequest request) {
        requireInRange(request.getCommissionRate(), MAX_COMMISSION_RATE,
                "commissionRate", "抽佣比例须为 0~1 的小数（0.10 表示 10%）");
        requireInRange(request.getPointExchangeRate(), MAX_POINT_EXCHANGE_RATE,
                "pointExchangeRate", "积分兑换比须为 0~999999.9999");
        requireInRange(request.getDeliveryFee(), MAX_DELIVERY_FEE,
                "deliveryFee", "配送费须为 0~99999999.99");
    }

    private static void requireInRange(BigDecimal value, BigDecimal max, String field, String hint) {
        if (value == null) {
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(max) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, field + " 超出可保存范围：" + hint);
        }
    }

    /**
     * 逐表落库，并把数据库约束冲突翻译成「哪张表、哪个约束」。
     *
     * <p>全局兜底处理器刻意不把表名/索引名回显（防泄露），代价是运营只看到「数据冲突，请刷新后重试」，
     * 谁都定位不了根因。建号是管理端专用接口，把约束名给出去没有泄露风险，
     * 且同文件的 {@code deleteAccount} 早就是这个口径。用 {@code saveAndFlush} 是为了让异常能归到具体某张表
     * ——批量 INSERT 会推迟到事务末尾统一 flush，那时抛出来的异常分不出是谁。</p>
     */
    private <T> void persist(String table, JpaRepository<T, ?> repository, T entity) {
        try {
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            Throwable root = e.getMostSpecificCause();
            String detail = root != null && root.getMessage() != null ? root.getMessage() : String.valueOf(e.getMessage());
            log.error("[业务角色建号] 写入 {} 失败: {}", table, detail, e);
            throw new BizException(ErrorCode.ROLE_ACCOUNT_PERSIST_FAILED,
                    "写入「" + table + "」失败：" + describeDbFailure(detail));
        }
    }

    private static String describeDbFailure(String detail) {
        Matcher duplicate = DUPLICATE_PATTERN.matcher(detail);
        if (duplicate.find()) {
            return "唯一键 " + duplicate.group(2) + " 冲突，重复值 " + duplicate.group(1);
        }
        Matcher notNull = NOT_NULL_PATTERN.matcher(detail);
        if (notNull.find()) {
            return "字段 " + notNull.group(1) + " 不能为空";
        }
        Matcher foreignKey = FOREIGN_KEY_PATTERN.matcher(detail);
        if (foreignKey.find()) {
            return "外键 " + foreignKey.group(1) + " 指向的记录不存在";
        }
        return "数据库约束冲突（详见服务端日志）";
    }

    /**
     * 创建活动组组长身份记录（标签化后 appoint 来源）。
     */
    private void createLeaderIdentity(Resident resident, String source, String applicationId) {
        ActivityLeaderIdentity identity = new ActivityLeaderIdentity();
        identity.setId(idGenerator.generateId("ali"));
        identity.setResidentId(resident.getId());
        identity.setPropertyCompanyId(resident.getPropertyCompanyId());
        identity.setSource(source);
        identity.setApplicationId(applicationId);
        identity.setStatus("active");
        identity.setCreatedBy(UserContext.currentUserId());
        activityLeaderIdentityRepository.save(identity);
    }

    /**
     * 当前账号是否属于业务角色（merchant/technician/courier 或持组长身份的 resident）。
     * 2026-10-21 组长标签化：组长 role 归一为 resident，按 identity 表 active 记录判定。
     */
    private boolean isBusinessRoleAccount(Resident resident) {
        if (SUPPORTED_ROLES.contains(resident.getRole())) {
            return true;
        }
        return "resident".equals(resident.getRole())
                && activityLeaderIdentityRepository.existsByResidentIdAndPropertyCompanyIdAndStatus(
                        resident.getId(), resident.getPropertyCompanyId(), "active");
    }

    /**
     * 撤销该账号的组长身份（删号/禁用/软删时联动，避免身份残留）。
     */
    private void revokeLeaderIdentityIfAny(String residentId, String propertyCompanyId) {
        activityLeaderIdentityRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        residentId, propertyCompanyId, "active")
                .ifPresent(identity -> {
                    identity.setStatus("revoked");
                    identity.setRevokedBy(UserContext.currentUserId());
                    identity.setRevokedAt(LocalDateTime.now());
                    activityLeaderIdentityRepository.save(identity);
                });
    }

    /**
     * 创建商家完整店铺链：平台商家库 + 物业挂接商家 + 物业参数配置。
     * 直接置为 approved/active，创建后即可在商家端登录、上架商品、下单。
     */
    private String createMerchantShop(Resident resident, CreateRoleAccountRequest request) {
        String category = request.getCategory();
        if (!StringUtils.hasText(category)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "商家品类 category 不能为空");
        }
        if (!merchantCategoryService.activeCategoryNames().contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "category值无效，可选品类见 GET /merchant-categories");
        }

        PlatformMerchant platformMerchant = new PlatformMerchant();
        platformMerchant.setId(idGenerator.generateId("pm"));
        platformMerchant.setName(resident.getName());
        platformMerchant.setDescription(request.getDescription());
        platformMerchant.setContactPhone(resident.getPhone());
        platformMerchant.setAddress(request.getAddress());
        platformMerchant.setBusinessHours(request.getBusinessHours());
        platformMerchant.setStatus("active");
        persist("platform_merchants（平台商家库）", platformMerchantRepository, platformMerchant);

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(platformMerchant.getId());
        merchant.setPropertyCompanyId(resident.getPropertyCompanyId());
        merchant.setName(resident.getName());
        merchant.setDescription(request.getDescription());
        merchant.setCoverUrls(request.getCoverUrls() != null && !request.getCoverUrls().isEmpty()
                ? merchantConverter.serializeCoverUrls(request.getCoverUrls()) : null);
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(0);
        merchant.setCategory(category);
        merchant.setAuditStatus("approved");
        merchant.setRankOrder(0);
        merchant.setBusinessHours(request.getBusinessHours());
        merchant.setContactPhone(resident.getPhone());
        merchant.setResidentId(resident.getId());
        merchant.setAddress(request.getAddress());
        merchant.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO);
        merchant.setDeliveryScope("both");
        merchant.setStatus("active");
        merchant.setServeAllCommunities(true);
        merchant.setMerchantSource("platform");
        persist("merchants（物业商家）", merchantRepository, merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(resident.getPropertyCompanyId());
        config.setCommissionRate(request.getCommissionRate() != null
                ? request.getCommissionRate() : DEFAULT_COMMISSION_RATE);
        config.setPointExchangeRate(request.getPointExchangeRate() != null
                ? request.getPointExchangeRate() : DEFAULT_POINT_EXCHANGE_RATE);
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        persist("merchant_property_configs（抽佣配置）", merchantPropertyConfigRepository, config);

        return merchant.getId();
    }

    /**
     * 直接删除业务角色账号（硬删除 / 物理删除）。
     * <p>彻底删除 residents 记录（非软删除），并同步删除创建账号时生成的关联店铺链
     * （Merchant + PlatformMerchant + MerchantPropertyConfig，含 group_leader / technician 小店），
     * 避免残留孤儿数据。账号手机号随物理删除释放，可再次用于新建账号。
     * <p>若账号已存在业务数据（订单 / 积分 / 提现 / 维修 / 家庭成员等），外键约束会阻止硬删，
     * 此时抛 {@link ErrorCode#ROLE_ACCOUNT_HAS_BUSINESS_DATA} 并提示先处理业务数据或使用账号注销流程。
     */
    @Transactional
    public DeleteRoleAccountResponse deleteAccount(String residentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(residentId, "res_");

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        if (!isBusinessRoleAccount(resident)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该账号不属于业务角色（merchant/activity_leader/technician/courier），无法通过此接口删除。"
                            + "如需删除区域负责人/物业领导/普通住户，请改用 DELETE /residents/{id}（软删除）");
        }

        try {
            // 1) 先删创建账号时生成的店铺链（避免孤儿店铺）
            deleteMerchantChainByResident(residentId);
            merchantPropertyConfigRepository.flush();
            merchantRepository.flush();
            platformMerchantRepository.flush();

            // 2) 物理删除账号主体（手机号随之释放，可再次用于新建）
            residentRepository.delete(resident);
            residentRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.ROLE_ACCOUNT_HAS_BUSINESS_DATA,
                    "该账号存在关联业务数据（订单/积分/提现/维修/家庭成员等），无法彻底删除；"
                            + "请改用 POST /admin/role-accounts/{id}/disable 禁用账号，"
                            + "或 POST /admin/role-accounts/{id}/soft-delete 软删除账号，历史业务数据将完整保留");
        }

        return DeleteRoleAccountResponse.builder()
                .residentId(resident.getId())
                .deleted(true)
                .message("账号已彻底删除（含关联店铺）")
                .build();
    }

    /**
     * 禁用/停用业务角色账号（软性停用，不删除业务数据）。
     * <p>将账号 status 置为 disabled，并连带停用其名下仍在营业（active）的店铺（status=stopped），
     * 防止账号已停用但店铺仍可接单。账号不可登录/接单，但历史订单/积分/提现/维修/家庭成员等数据完整保留。
     * <p>适用于「存在关联业务数据、无法硬删除」的账号处理场景。
     */
    @Transactional
    public DisableRoleAccountResponse disableAccount(String residentId, String reason) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(residentId, "res_");

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        if (ResidentStatus.deleted.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ROLE_ACCOUNT_ALREADY_DELETED, "该账号已删除，无法禁用");
        }
        if (!isBusinessRoleAccount(resident)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该账号不属于业务角色（merchant/activity_leader/technician/courier），无法使用本接口禁用。"
                            + "请改用 PATCH /residents/{id}/status");
        }

        resident.setStatus(ResidentStatus.disabled.name());
        residentRepository.save(resident);
        revokeLeaderIdentityIfAny(residentId, resident.getPropertyCompanyId());

        String merchantStatus = stopActiveMerchantShops(residentId);

        return DisableRoleAccountResponse.builder()
                .residentId(residentId)
                .status(ResidentStatus.disabled.name())
                .merchantStatus(merchantStatus)
                .message("账号已禁用（关联店铺已停用），历史业务数据完整保留")
                .build();
    }

    /**
     * 软删除业务角色账号（标记删除，不物理删除、不释放手机号）。
     * <p>将账号 status 置为 deleted 并记录 deletedAt，连带停用其名下仍在营业（active）的店铺（status=stopped）。
     * 历史业务数据完整保留，账号列表自动隐藏。适用于「存在关联业务数据、无法硬删除」的账号处理场景。
     */
    @Transactional
    public DeleteRoleAccountResponse softDeleteAccount(String residentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        adminSupport.validateIdPrefix(residentId, "res_");

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        if (ResidentStatus.deleted.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ROLE_ACCOUNT_ALREADY_DELETED, "该账号已删除");
        }
        if (!isBusinessRoleAccount(resident)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该账号不属于业务角色（merchant/activity_leader/technician/courier），无法使用本接口软删除。"
                            + "请改用 DELETE /residents/{id}");
        }

        resident.setStatus(ResidentStatus.deleted.name());
        resident.setDeletedAt(LocalDateTime.now());
        residentRepository.save(resident);
        revokeLeaderIdentityIfAny(residentId, resident.getPropertyCompanyId());

        // 软删仅停用店铺（保留记录便于审计），不物理删除
        stopActiveMerchantShops(residentId);

        return DeleteRoleAccountResponse.builder()
                .residentId(residentId)
                .deleted(true)
                .message("账号已软删除（关联店铺已停用），历史业务数据完整保留")
                .build();
    }

    /**
     * 停用指定账号名下仍在营业（active）的店铺，避免账号已禁用/软删但店铺仍可接单。
     * 仅对 active 店铺置为 stopped（新增店铺停用态，接单前置校验 status=active 自然排除），不物理删除，保留记录便于审计。
     *
     * @return 关联店铺最终状态描述：none（无店铺）/ stopped（已停用）/ 其他（店铺原有状态，未改动）
     */
    private String stopActiveMerchantShops(String residentId) {
        List<Merchant> shops = merchantRepository.findByResidentId(residentId);
        if (shops.isEmpty()) {
            return "none";
        }
        boolean changed = false;
        for (Merchant shop : shops) {
            if ("active".equals(shop.getStatus())) {
                shop.setStatus("stopped");
                merchantRepository.save(shop);
                changed = true;
            }
        }
        return changed ? "stopped" : shops.get(0).getStatus();
    }

    /**
     * 删除指定住户名下的全部关联商家店铺链（Merchant + PlatformMerchant + MerchantPropertyConfig）。
     * 覆盖 merchant 角色店铺、group_leader 小店、technician 小店；配送员无店铺，循环为空。
     * 删除顺序：config → merchant → platformMerchant（先删子表，避免外键拒绝）。
     */
    private void deleteMerchantChainByResident(String residentId) {
        for (Merchant merchant : merchantRepository.findByResidentId(residentId)) {
            merchantPropertyConfigRepository.findByMerchantId(merchant.getId())
                    .ifPresent(merchantPropertyConfigRepository::delete);
            merchantRepository.delete(merchant);
            if (StringUtils.hasText(merchant.getPlatformMerchantId())) {
                platformMerchantRepository.findById(merchant.getPlatformMerchantId())
                        .ifPresent(platformMerchantRepository::delete);
            }
        }
    }

    private String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (!StringUtils.hasText(requestedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "平台管理员创建账号必须指定 propertyCompanyId");
            }
            return requestedPropertyCompanyId;
        }
        String current = com.wuye.points.context.UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(current)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "当前用户未关联物业公司");
        }
        return current;
    }
}
