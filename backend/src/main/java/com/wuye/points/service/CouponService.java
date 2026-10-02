package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.coupon.AvailableCouponResponse;
import com.wuye.points.dto.coupon.CouponTemplateResponse;
import com.wuye.points.dto.coupon.CreateCouponTemplateRequest;
import com.wuye.points.dto.coupon.GrantCouponRequest;
import com.wuye.points.dto.coupon.UpdateCouponTemplateRequest;
import com.wuye.points.dto.coupon.UserCouponResponse;
import com.wuye.points.entity.CouponTemplate;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.UserCoupon;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CouponTemplateRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.UserCouponRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 优惠券服务。
 *
 * <p>券类型：满减 full_reduction / 折扣 discount / 立减 instant_reduction；
 * 适用范围：平台券 platform（全平台通用）+ 商家券 merchant（限指定商家）。</p>
 *
 * <p>分账口径：优惠金额由商家承担；订单 {@code totalAmount} 保持原商品金额不变，
 * 另记 {@code couponAmount}，平台佣金基数按 {@code totalAmount - couponAmount}（实付商品金额）计算。</p>
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    public static final String TYPE_FULL_REDUCTION = "full_reduction";
    public static final String TYPE_DISCOUNT = "discount";
    public static final String TYPE_INSTANT_REDUCTION = "instant_reduction";

    public static final String SCOPE_PLATFORM = "platform";
    public static final String SCOPE_MERCHANT = "merchant";

    public static final String STATUS_UNUSED = "unused";
    public static final String STATUS_LOCKED = "locked";
    public static final String STATUS_USED = "used";
    public static final String STATUS_EXPIRED = "expired";

    private static final Set<String> VALID_TYPES =
            Set.of(TYPE_FULL_REDUCTION, TYPE_DISCOUNT, TYPE_INSTANT_REDUCTION);
    private static final Set<String> VALID_SCOPES = Set.of(SCOPE_PLATFORM, SCOPE_MERCHANT);
    private static final Set<String> VALID_VALID_TYPES = Set.of("fixed", "relative");
    private static final Set<String> VALID_TEMPLATE_STATUS = Set.of("active", "inactive");

    private final CouponTemplateRepository templateRepository;
    private final UserCouponRepository userCouponRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final MerchantSupport merchantSupport;
    private final SnowflakeIdGenerator idGenerator;

    // ==================================================================
    // 券计算
    // ==================================================================

    /** 计算券在给定「商品金额」下的抵扣额（不含配送费），不改变券状态。 */
    public BigDecimal computeDiscount(UserCoupon coupon, BigDecimal productAmount) {
        BigDecimal amount = productAmount == null ? BigDecimal.ZERO : productAmount;
        BigDecimal minSpend = coupon.getMinSpendAmount() == null ? BigDecimal.ZERO : coupon.getMinSpendAmount();
        if (amount.compareTo(minSpend) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if (TYPE_DISCOUNT.equals(coupon.getType())) {
            BigDecimal rate = coupon.getDiscountRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.TEN) >= 0) {
                return BigDecimal.ZERO;
            }
            // 8.5 折 => 抵扣 = 金额 * (1 - 8.5/10)
            discount = amount.multiply(BigDecimal.TEN.subtract(rate))
                    .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null
                    && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getValueAmount() == null ? BigDecimal.ZERO : coupon.getValueAmount();
        }
        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }
        return discount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isExpired(UserCoupon coupon, LocalDateTime now) {
        return coupon.getExpireAt() != null && coupon.getExpireAt().isBefore(now);
    }

    private boolean suitableForMerchant(UserCoupon coupon, String merchantId) {
        return coupon.getMerchantId() == null || coupon.getMerchantId().equals(merchantId);
    }

    // ==================================================================
    // 用户端：领券中心 / 领取 / 我的券 / 下单可用券
    // ==================================================================

    @Transactional
    public List<CouponTemplateResponse> listClaimable(String merchantId) {
        Resident resident = currentResident();
        List<CouponTemplate> templates =
                templateRepository.findClaimable(resident.getPropertyCompanyId(), merchantId);
        List<CouponTemplateResponse> responses = new ArrayList<>();
        for (CouponTemplate template : templates) {
            long held = userCouponRepository.countActiveHeld(template.getId(), resident.getId());
            boolean claimedByMe = held >= (template.getPerUserLimit() == null ? 1 : template.getPerUserLimit());
            responses.add(toTemplateResponse(template, claimedByMe));
        }
        return responses;
    }

    @Transactional
    public UserCouponResponse claim(String templateId) {
        Resident resident = currentResident();
        CouponTemplate template = templateRepository.findByIdForUpdate(templateId)
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_TEMPLATE_NOT_FOUND));
        if (!"active".equals(template.getStatus())) {
            throw new BizException(ErrorCode.COUPON_NOT_CLAIMABLE);
        }
        boolean scopeVisible = SCOPE_PLATFORM.equals(template.getScope())
                || (SCOPE_MERCHANT.equals(template.getScope())
                    && resident.getPropertyCompanyId() != null
                    && resident.getPropertyCompanyId().equals(template.getPropertyCompanyId()));
        if (!scopeVisible) {
            throw new BizException(ErrorCode.COUPON_NOT_CLAIMABLE);
        }
        LocalDateTime now = LocalDateTime.now();
        if (template.getTotalQuantity() != null
                && template.getClaimedQuantity() >= template.getTotalQuantity()) {
            throw new BizException(ErrorCode.COUPON_STOCK_EXHAUSTED);
        }
        int perUserLimit = template.getPerUserLimit() == null ? 1 : template.getPerUserLimit();
        if (userCouponRepository.countActiveHeld(template.getId(), resident.getId()) >= perUserLimit) {
            throw new BizException(ErrorCode.COUPON_CLAIM_LIMIT_EXCEEDED);
        }
        UserCoupon coupon = buildUserCoupon(template, resident, "claim", null, now);
        userCouponRepository.save(coupon);
        template.setClaimedQuantity(template.getClaimedQuantity() + 1);
        templateRepository.save(template);
        return toUserCouponResponse(coupon);
    }

    @Transactional
    public List<UserCouponResponse> listMyCoupons(String status) {
        Resident resident = currentResident();
        String normalized = StringUtils.hasText(status) ? status : null;
        LocalDateTime now = LocalDateTime.now();
        List<UserCoupon> coupons = userCouponRepository.findByResident(resident.getId(), normalized);
        List<UserCouponResponse> responses = new ArrayList<>();
        for (UserCoupon coupon : coupons) {
            if (STATUS_UNUSED.equals(coupon.getStatus()) && isExpired(coupon, now)) {
                coupon.setStatus(STATUS_EXPIRED);
                userCouponRepository.save(coupon);
            }
            responses.add(toUserCouponResponse(coupon));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<AvailableCouponResponse> listAvailable(String merchantId, BigDecimal productAmount) {
        Resident resident = currentResident();
        BigDecimal amount = productAmount == null ? BigDecimal.ZERO : productAmount;
        LocalDateTime now = LocalDateTime.now();
        List<AvailableCouponResponse> responses = new ArrayList<>();
        for (UserCoupon coupon : userCouponRepository.findByResident(resident.getId(), STATUS_UNUSED)) {
            if (isExpired(coupon, now) || !suitableForMerchant(coupon, merchantId)) {
                continue;
            }
            BigDecimal discount = computeDiscount(coupon, amount);
            responses.add(AvailableCouponResponse.builder()
                    .id(coupon.getId())
                    .templateId(coupon.getTemplateId())
                    .name(coupon.getName())
                    .type(coupon.getType())
                    .scope(coupon.getMerchantId() == null ? SCOPE_PLATFORM : SCOPE_MERCHANT)
                    .merchantId(coupon.getMerchantId())
                    .valueAmount(coupon.getValueAmount())
                    .discountRate(coupon.getDiscountRate())
                    .maxDiscountAmount(coupon.getMaxDiscountAmount())
                    .minSpendAmount(coupon.getMinSpendAmount())
                    .expireAt(DateTimeUtil.toLocalDateTimeString(coupon.getExpireAt()))
                    .discountAmount(discount)
                    .usable(discount.compareTo(BigDecimal.ZERO) > 0)
                    .build());
        }
        responses.sort((a, b) -> b.getDiscountAmount().compareTo(a.getDiscountAmount()));
        return responses;
    }

    /**
     * 试算：不改变券状态，返回该券在本单商品金额下的抵扣额（券无效/不适用时返回 0）。
     */
    @Transactional(readOnly = true)
    public BigDecimal previewDiscount(String userCouponId, String residentId, String merchantId,
                                      BigDecimal productAmount) {
        if (!StringUtils.hasText(userCouponId)) {
            return BigDecimal.ZERO;
        }
        UserCoupon coupon = userCouponRepository.findById(userCouponId).orElse(null);
        if (coupon == null || !coupon.getResidentId().equals(residentId)) {
            return BigDecimal.ZERO;
        }
        if (!STATUS_UNUSED.equals(coupon.getStatus()) || isExpired(coupon, LocalDateTime.now())) {
            return BigDecimal.ZERO;
        }
        if (!suitableForMerchant(coupon, merchantId)) {
            return BigDecimal.ZERO;
        }
        return computeDiscount(coupon, productAmount);
    }

    // ==================================================================
    // 订单集成：锁定 / 核销 / 释放
    // ==================================================================

    /**
     * 下单支付时锁定券并返回抵扣额（不改变订单金额，仅将券置为 locked）。
     * 校验：归属、状态、有效期、商家适用范围、使用门槛。
     */
    @Transactional
    public BigDecimal lockForOrder(String userCouponId, String residentId, String merchantId,
                                   BigDecimal productAmount, String orderId) {
        if (!StringUtils.hasText(userCouponId)) {
            return BigDecimal.ZERO;
        }
        UserCoupon coupon = userCouponRepository.findByIdForUpdate(userCouponId)
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_NOT_FOUND));
        if (!coupon.getResidentId().equals(residentId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "优惠券不属于当前用户");
        }
        if (STATUS_USED.equals(coupon.getStatus())) {
            throw new BizException(ErrorCode.COUPON_ALREADY_USED);
        }
        if (!STATUS_UNUSED.equals(coupon.getStatus())) {
            throw new BizException(ErrorCode.COUPON_UNAVAILABLE);
        }
        LocalDateTime now = LocalDateTime.now();
        if (isExpired(coupon, now)) {
            coupon.setStatus(STATUS_EXPIRED);
            userCouponRepository.save(coupon);
            throw new BizException(ErrorCode.COUPON_EXPIRED);
        }
        if (!suitableForMerchant(coupon, merchantId)) {
            throw new BizException(ErrorCode.COUPON_SCOPE_MISMATCH);
        }
        BigDecimal discount = computeDiscount(coupon, productAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.COUPON_MIN_SPEND_NOT_MET);
        }
        coupon.setStatus(STATUS_LOCKED);
        coupon.setOrderId(orderId);
        userCouponRepository.save(coupon);
        return discount;
    }

    /** 支付成功：将订单锁定的券置为已使用。 */
    @Transactional
    public void markUsedByOrder(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (UserCoupon coupon : userCouponRepository.findByOrderId(orderId)) {
            if (STATUS_LOCKED.equals(coupon.getStatus())) {
                coupon.setStatus(STATUS_USED);
                coupon.setUsedAt(now);
                userCouponRepository.save(coupon);
            }
        }
    }

    /** 订单取消/退款：释放券回用户账户（未使用/已使用均复位为 unused）。 */
    @Transactional
    public void releaseByOrder(String orderId) {
        if (!StringUtils.hasText(orderId)) {
            return;
        }
        for (UserCoupon coupon : userCouponRepository.findByOrderId(orderId)) {
            coupon.setStatus(STATUS_UNUSED);
            coupon.setOrderId(null);
            coupon.setUsedAt(null);
            userCouponRepository.save(coupon);
        }
    }

    // ==================================================================
    // 管理端：模板 CRUD / 定向发放
    // ==================================================================

    @Transactional
    public CouponTemplateResponse createTemplate(CreateCouponTemplateRequest request) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        CouponTemplate template = new CouponTemplate();
        template.setId(idGenerator.generateId("cpt"));
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setType(request.getType());
        template.setScope(request.getScope());
        template.setValidType(request.getValidType());
        template.setPerUserLimit(request.getPerUserLimit());
        template.setTotalQuantity(request.getTotalQuantity());
        template.setClaimedQuantity(0);
        template.setValueAmount(request.getValueAmount());
        template.setDiscountRate(request.getDiscountRate());
        template.setMaxDiscountAmount(request.getMaxDiscountAmount());
        template.setMinSpendAmount(request.getMinSpendAmount() == null
                ? BigDecimal.ZERO : request.getMinSpendAmount());
        template.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        template.setCreatedBy(UserContext.currentUserId());

        applyScopeAndValidity(template, request.getMerchantId(),
                request.getValidStartAt(), request.getValidEndAt(), request.getValidDays());
        validateTemplate(template);

        templateRepository.save(template);
        return toTemplateResponse(template, false);
    }

    @Transactional
    public CouponTemplateResponse updateTemplate(String id, UpdateCouponTemplateRequest request) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        CouponTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_TEMPLATE_NOT_FOUND));
        assertCanManageTemplate(template);

        boolean issued = template.getClaimedQuantity() != null && template.getClaimedQuantity() > 0;
        if (issued && (request.getValueAmount() != null
                || request.getDiscountRate() != null
                || request.getMaxDiscountAmount() != null
                || request.getMinSpendAmount() != null)) {
            throw new BizException(ErrorCode.COUPON_TEMPLATE_IN_USE);
        }

        if (StringUtils.hasText(request.getName())) {
            template.setName(request.getName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getValueAmount() != null) {
            template.setValueAmount(request.getValueAmount());
        }
        if (request.getDiscountRate() != null) {
            template.setDiscountRate(request.getDiscountRate());
        }
        if (request.getMaxDiscountAmount() != null) {
            template.setMaxDiscountAmount(request.getMaxDiscountAmount());
        }
        if (request.getMinSpendAmount() != null) {
            template.setMinSpendAmount(request.getMinSpendAmount());
        }
        if (request.getValidStartAt() != null) {
            template.setValidStartAt(DateTimeUtil.parseLocalDateTime(request.getValidStartAt()));
        }
        if (request.getValidEndAt() != null) {
            template.setValidEndAt(DateTimeUtil.parseLocalDateTime(request.getValidEndAt()));
        }
        if (request.getValidDays() != null) {
            template.setValidDays(request.getValidDays());
        }
        if (request.getTotalQuantity() != null) {
            template.setTotalQuantity(request.getTotalQuantity());
        }
        if (request.getPerUserLimit() != null) {
            template.setPerUserLimit(request.getPerUserLimit());
        }
        if (StringUtils.hasText(request.getStatus())) {
            template.setStatus(request.getStatus());
        }
        validateTemplate(template);

        templateRepository.save(template);
        return toTemplateResponse(template, false);
    }

    @Transactional(readOnly = true)
    public CouponTemplateResponse getTemplate(String id) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        CouponTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_TEMPLATE_NOT_FOUND));
        return toTemplateResponse(template, false);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponTemplateResponse> listTemplates(int page, int pageSize,
                                                              String scope, String status,
                                                              String merchantId, String propertyCompanyId) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String scopedPropertyCompanyId = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
        Page<CouponTemplate> result = templateRepository.findAllForAdmin(
                scopedPropertyCompanyId, scope, status, merchantId,
                PageRequest.of(Math.max(page, 1) - 1, pageSize));
        List<CouponTemplateResponse> list = result.getContent().stream()
                .map(t -> toTemplateResponse(t, false))
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public CouponTemplateResponse updateTemplateStatus(String id, String status) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        if (!VALID_TEMPLATE_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "状态只能为active或inactive");
        }
        CouponTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_TEMPLATE_NOT_FOUND));
        assertCanManageTemplate(template);
        template.setStatus(status);
        templateRepository.save(template);
        return toTemplateResponse(template, false);
    }

    /**
     * 定向发放。管理员可发放任意券；商家端仅可发放自己的商家券。
     */
    @Transactional
    public int grant(GrantCouponRequest request) {
        String role = roleGuard.normalizeRole(UserContext.currentRole());
        boolean merchantSide = "merchant".equals(role);
        if (merchantSide) {
            merchantSupport.requireCurrentMerchant();
        } else {
            roleGuard.requireAnyRole("platform_admin", "property_admin");
        }

        CouponTemplate template = templateRepository.findByIdForUpdate(request.getTemplateId())
                .orElseThrow(() -> new BizException(ErrorCode.COUPON_TEMPLATE_NOT_FOUND));
        if (!"active".equals(template.getStatus())) {
            throw new BizException(ErrorCode.COUPON_NOT_CLAIMABLE);
        }
        if (merchantSide) {
            Merchant merchant = merchantSupport.requireCurrentMerchant();
            if (!SCOPE_MERCHANT.equals(template.getScope())
                    || !merchant.getId().equals(template.getMerchantId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "仅可发放本商家的商家券");
            }
        } else {
            assertCanManageTemplate(template);
        }

        int perUser = request.getQuantity() == null || request.getQuantity() < 1 ? 1 : request.getQuantity();
        LocalDateTime now = LocalDateTime.now();
        int granted = 0;
        for (String residentId : request.getResidentIds()) {
            Resident resident = residentRepository.findById(residentId).orElse(null);
            if (resident == null) {
                continue;
            }
            int perUserLimit = template.getPerUserLimit() == null ? 1 : template.getPerUserLimit();
            long held = userCouponRepository.countActiveHeld(template.getId(), resident.getId());
            int allowed = (int) Math.max(0, perUserLimit - held);
            int toGrant = Math.min(perUser, allowed);
            for (int i = 0; i < toGrant; i++) {
                if (template.getTotalQuantity() != null
                        && template.getClaimedQuantity() >= template.getTotalQuantity()) {
                    break;
                }
                UserCoupon coupon = buildUserCoupon(template, resident, "grant",
                        UserContext.currentUserId(), now);
                userCouponRepository.save(coupon);
                template.setClaimedQuantity(template.getClaimedQuantity() + 1);
                granted++;
            }
        }
        if (granted > 0) {
            templateRepository.save(template);
        }
        return granted;
    }

    /** 商家端：查看本商家的商家券模板列表。 */
    @Transactional(readOnly = true)
    public List<CouponTemplateResponse> listMerchantTemplates() {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        Page<CouponTemplate> result = templateRepository.findAllForAdmin(
                merchant.getPropertyCompanyId(), SCOPE_MERCHANT, null, merchant.getId(),
                PageRequest.of(0, 100));
        return result.getContent().stream()
                .map(t -> toTemplateResponse(t, false))
                .toList();
    }

    // ==================================================================
    // 内部辅助
    // ==================================================================

    private Resident currentResident() {
        String userId = UserContext.currentUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        return residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    private void applyScopeAndValidity(CouponTemplate template, String merchantId,
                                       String startAt, String endAt, Integer validDays) {
        if (SCOPE_MERCHANT.equals(template.getScope())) {
            if (!StringUtils.hasText(merchantId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "商家券必须指定merchantId");
            }
            Merchant merchant = merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
            template.setMerchantId(merchant.getId());
            template.setPropertyCompanyId(merchant.getPropertyCompanyId());
        } else if (SCOPE_PLATFORM.equals(template.getScope())) {
            if (!roleGuard.isPlatformAdmin()) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "仅平台管理员可创建平台券");
            }
            template.setMerchantId(null);
            template.setPropertyCompanyId(null);
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "适用范围只能为platform或merchant");
        }
        if ("fixed".equals(template.getValidType())) {
            template.setValidStartAt(DateTimeUtil.parseLocalDateTime(startAt));
            template.setValidEndAt(DateTimeUtil.parseLocalDateTime(endAt));
        } else if ("relative".equals(template.getValidType())) {
            template.setValidDays(validDays);
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "有效期类型只能为fixed或relative");
        }
    }

    private void validateTemplate(CouponTemplate template) {
        if (!VALID_TYPES.contains(template.getType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "券类型不合法");
        }
        if (!VALID_SCOPES.contains(template.getScope())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "适用范围不合法");
        }
        if (TYPE_DISCOUNT.equals(template.getType())) {
            BigDecimal rate = template.getDiscountRate();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0
                    || rate.compareTo(BigDecimal.TEN) >= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "折扣率需在 0~10 之间（8.5 表示 8.5 折）");
            }
        } else if (template.getValueAmount() == null
                || template.getValueAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "面额必须大于0");
        }
        if (!VALID_VALID_TYPES.contains(template.getValidType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "有效期类型不合法");
        }
        if ("fixed".equals(template.getValidType())) {
            if (template.getValidStartAt() == null || template.getValidEndAt() == null) {
                throw new BizException(ErrorCode.PARAM_MISSING, "固定有效期需提供开始与结束时间");
            }
            if (!template.getValidEndAt().isAfter(template.getValidStartAt())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "有效期结束时间必须晚于开始时间");
            }
        } else if (template.getValidDays() == null || template.getValidDays() < 1) {
            throw new BizException(ErrorCode.PARAM_MISSING, "相对有效期需提供有效天数");
        }
        if (template.getMinSpendAmount() == null) {
            template.setMinSpendAmount(BigDecimal.ZERO);
        }
        if (template.getPerUserLimit() == null || template.getPerUserLimit() < 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "每人限领至少为1");
        }
        if (!VALID_TEMPLATE_STATUS.contains(template.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "状态不合法");
        }
    }

    /** 商家券归属校验：property_admin 仅能管理本物业的商家券。 */
    private void assertCanManageTemplate(CouponTemplate template) {
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (SCOPE_PLATFORM.equals(template.getScope())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权管理平台券");
        }
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (propertyCompanyId == null || !propertyCompanyId.equals(template.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private UserCoupon buildUserCoupon(CouponTemplate template, Resident resident,
                                       String source, String grantedBy, LocalDateTime now) {
        UserCoupon coupon = new UserCoupon();
        coupon.setId(idGenerator.generateId("ucp"));
        coupon.setTemplateId(template.getId());
        coupon.setResidentId(resident.getId());
        coupon.setMerchantId(template.getMerchantId());
        coupon.setPropertyCompanyId(template.getPropertyCompanyId());
        coupon.setStatus(STATUS_UNUSED);
        coupon.setSource(source);
        coupon.setGrantedBy(grantedBy);
        coupon.setName(template.getName());
        coupon.setType(template.getType());
        coupon.setValueAmount(template.getValueAmount());
        coupon.setDiscountRate(template.getDiscountRate());
        coupon.setMaxDiscountAmount(template.getMaxDiscountAmount());
        coupon.setMinSpendAmount(template.getMinSpendAmount() == null
                ? BigDecimal.ZERO : template.getMinSpendAmount());
        coupon.setExpireAt(resolveExpireAt(template, now));
        coupon.setClaimedAt(now);
        return coupon;
    }

    private LocalDateTime resolveExpireAt(CouponTemplate template, LocalDateTime now) {
        if ("relative".equals(template.getValidType())) {
            int days = template.getValidDays() == null ? 0 : template.getValidDays();
            return now.plusDays(days);
        }
        return template.getValidEndAt();
    }

    private CouponTemplateResponse toTemplateResponse(CouponTemplate template, boolean claimedByMe) {
        String merchantName = null;
        if (StringUtils.hasText(template.getMerchantId())) {
            merchantName = merchantRepository.findById(template.getMerchantId())
                    .map(Merchant::getName).orElse(null);
        }
        Integer remaining = template.getTotalQuantity() == null
                ? null
                : Math.max(0, template.getTotalQuantity() - template.getClaimedQuantity());
        return CouponTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .type(template.getType())
                .scope(template.getScope())
                .merchantId(template.getMerchantId())
                .merchantName(merchantName)
                .valueAmount(template.getValueAmount())
                .discountRate(template.getDiscountRate())
                .maxDiscountAmount(template.getMaxDiscountAmount())
                .minSpendAmount(template.getMinSpendAmount())
                .validType(template.getValidType())
                .validStartAt(DateTimeUtil.toLocalDateTimeString(template.getValidStartAt()))
                .validEndAt(DateTimeUtil.toLocalDateTimeString(template.getValidEndAt()))
                .validDays(template.getValidDays())
                .totalQuantity(template.getTotalQuantity())
                .claimedQuantity(template.getClaimedQuantity())
                .remainingQuantity(remaining)
                .perUserLimit(template.getPerUserLimit())
                .status(template.getStatus())
                .claimedByMe(claimedByMe)
                .createdAt(DateTimeUtil.toLocalDateTimeString(template.getCreatedAt()))
                .build();
    }

    private UserCouponResponse toUserCouponResponse(UserCoupon coupon) {
        String merchantName = StringUtils.hasText(coupon.getMerchantId())
                ? merchantRepository.findById(coupon.getMerchantId()).map(Merchant::getName).orElse(null)
                : null;
        boolean expired = STATUS_EXPIRED.equals(coupon.getStatus())
                || (coupon.getExpireAt() != null && coupon.getExpireAt().isBefore(LocalDateTime.now()));
        return UserCouponResponse.builder()
                .id(coupon.getId())
                .templateId(coupon.getTemplateId())
                .name(coupon.getName())
                .type(coupon.getType())
                .scope(coupon.getMerchantId() == null ? SCOPE_PLATFORM : SCOPE_MERCHANT)
                .merchantId(coupon.getMerchantId())
                .merchantName(merchantName)
                .valueAmount(coupon.getValueAmount())
                .discountRate(coupon.getDiscountRate())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minSpendAmount(coupon.getMinSpendAmount())
                .status(coupon.getStatus())
                .source(coupon.getSource())
                .expireAt(DateTimeUtil.toLocalDateTimeString(coupon.getExpireAt()))
                .claimedAt(DateTimeUtil.toLocalDateTimeString(coupon.getClaimedAt()))
                .usedAt(DateTimeUtil.toLocalDateTimeString(coupon.getUsedAt()))
                .orderId(coupon.getOrderId())
                .expired(expired)
                .build();
    }
}