package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.merchantad.MerchantAdSettingsResponse;
import com.wuye.points.dto.admin.merchantad.UpdateMerchantAdSettingsRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchantad.CreateMerchantAdRequest;
import com.wuye.points.dto.merchantad.MerchantAdListItemResponse;
import com.wuye.points.dto.merchantad.MerchantAdPackageResponse;
import com.wuye.points.dto.merchantad.MerchantAdQuoteResponse;
import com.wuye.points.dto.merchantad.MerchantAdQuotaResponse;
import com.wuye.points.dto.merchantad.TargetedMerchantAdRequest;
import com.wuye.points.entity.AgeBracketConfig;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantAd;
import com.wuye.points.entity.MerchantAdQuota;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.AgeBracketConfigRepository;
import com.wuye.points.repository.MerchantAdQuotaRepository;
import com.wuye.points.repository.MerchantAdRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantAdService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> TARGET_GENDER_VALUES = Set.of("male", "female", "all");
    private static final Set<String> PAYMENT_METHODS = Set.of("free", "coin", "point", "wechat", "mock");
    private static final int MAX_DURATION_DAYS = 30;

    private final RoleGuard roleGuard;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantAdRepository merchantAdRepository;
    private final MerchantAdQuotaRepository merchantAdQuotaRepository;
    private final AgeBracketConfigRepository ageBracketConfigRepository;
    private final MessageRepository messageRepository;
    private final PlatformSettingService platformSettingService;
    private final PropertyCoinService propertyCoinService;
    private final MerchantPointService merchantPointService;
    private final WebSocketService webSocketService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<MerchantAdPackageResponse> listPackages() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        return List.of(
                MerchantAdPackageResponse.builder()
                        .id("map_basic")
                        .name("基础包")
                        .weeklyQuota(5)
                        .price(new BigDecimal("99.00"))
                        .build(),
                MerchantAdPackageResponse.builder()
                        .id("map_standard")
                        .name("标准包")
                        .weeklyQuota(10)
                        .price(new BigDecimal("179.00"))
                        .build(),
                MerchantAdPackageResponse.builder()
                        .id("map_premium")
                        .name("高级包")
                        .weeklyQuota(20)
                        .price(new BigDecimal("299.00"))
                        .build());
    }

    /**
     * 广告位按天计价试算：days × 单日价。
     */
    @Transactional(readOnly = true)
    public MerchantAdQuoteResponse quote(int durationDays) {
        validateDurationDays(durationDays);
        MerchantAdQuota quota = currentWeekQuotaOrNull();
        int freeQuotaRemaining = resolveFreeQuotaRemaining(quota);
        return MerchantAdQuoteResponse.builder()
                .durationDays(durationDays)
                .dayPrice(platformSettingService.getAdDayPrice())
                .payAmount(platformSettingService.getAdDayPrice()
                        .multiply(BigDecimal.valueOf(durationDays)))
                .freeQuotaRemaining(freeQuotaRemaining)
                .freeQuotaEnabled(platformSettingService.isAdFreeQuotaEnabled())
                .build();
    }

    @Transactional(readOnly = true)
    public MerchantAdQuotaResponse getQuota() {
        Merchant merchant = requireCurrentMerchant();
        return buildQuotaResponse(merchant);
    }

    /**
     * D6 管理端按 merchantId 查询当前广告周额度(只读)。
     * 不创建额度记录,无额度时返回全 0。
     */
    @Transactional(readOnly = true)
    public MerchantAdQuotaResponse getQuotaByMerchantId(String merchantId) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!merchant.getPropertyCompanyId().equals(propertyCompanyId)) {
                throw new BizException(ErrorCode.PROPERTY_OPERATOR_OUT_OF_SCOPE);
            }
        }
        return buildQuotaResponse(merchant);
    }

    private MerchantAdQuotaResponse buildQuotaResponse(Merchant merchant) {
        LocalDate weekStart = currentWeekStart();
        MerchantAdQuota quota = merchantAdQuotaRepository
                .findByMerchantIdAndWeekStartDate(merchant.getId(), weekStart).orElse(null);

        int freeQuota = platformSettingService.getAdFreeQuotaPerWeek();
        if (!platformSettingService.isAdFreeQuotaEnabled()) {
            freeQuota = 0;
        }
        int purchasedQuota = quota != null ? defaultInt(quota.getPurchasedQuota()) : 0;
        int usedCount = quota != null ? defaultInt(quota.getUsedCount()) : 0;
        int freeUsedCount = quota != null ? defaultInt(quota.getFreeUsedCount()) : 0;
        int freeQuotaRemaining = Math.max(freeQuota + purchasedQuota - freeUsedCount, 0);

        return MerchantAdQuotaResponse.builder()
                .weekStart(weekStart.format(DATE_FMT))
                .weekEnd(weekStart.plusDays(6).format(DATE_FMT))
                .freeQuota(freeQuota)
                .purchasedQuota(purchasedQuota)
                .usedCount(usedCount)
                .freeUsedCount(freeUsedCount)
                .remainingCount(freeQuotaRemaining)
                .freeQuotaRemaining(freeQuotaRemaining)
                .freeQuotaEnabled(platformSettingService.isAdFreeQuotaEnabled())
                .freeQuotaPerWeek(platformSettingService.getAdFreeQuotaPerWeek())
                .dayPrice(platformSettingService.getAdDayPrice())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<MerchantAdListItemResponse> listAds(int page, int pageSize) {
        Merchant merchant = requireCurrentMerchant();
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, Math.min(Math.max(pageSize, 1), 100));
        Page<MerchantAd> result = merchantAdRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId(), pageable);
        List<MerchantAdListItemResponse> list = result.getContent().stream()
                .map(ad -> toListItem(ad))
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    /**
     * 发布广告（广告位按天计价）。
     * 支付方式：free=免费额度 / coin=物业币实时扣 / point=商家积分实时扣 / wechat,mock=模拟支付。
     * 支付成功后设置投放期（startDate~endDate），并向服务范围住户 fan-out 聊天消息（messageType=merchant_ad）。
     */
    @Transactional
    public MerchantAdListItemResponse createAd(CreateMerchantAdRequest request) {
        Merchant merchant = requireCurrentMerchant();
        Resident merchantUser = requireCurrentMerchantUser();
        validateDurationDays(request.getDurationDays());

        LocalDate weekStart = currentWeekStart();
        MerchantAdQuota quota = getOrCreateQuota(merchant.getId(), weekStart);

        MerchantAd ad = new MerchantAd();
        ad.setId(idGenerator.generateId("mad"));
        ad.setMerchantId(merchant.getId());
        ad.setTitle(request.getTitle().trim());
        ad.setContent(request.getContent().trim());
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            try {
                ad.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
            } catch (Exception e) {
                // 原先静默写成 "[]"，用户上传的图片被无声丢弃且无任何日志。
                // 改为让创建失败：宁可报错让用户重试，也不能存一条"看起来成功但图片没了"的广告。
                log.error("[商家广告] 图片序列化失败 merchantId={}", merchant.getId(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, "广告图片保存失败，请重试");
            }
        }
        ad.setProductId(request.getProductId());
        ad.setWeekStartDate(weekStart);
        ad.setDurationDays(request.getDurationDays());
        ad.setPaymentMethod(request.getPaymentMethod());

        BigDecimal payAmount = resolvePayAmount(request.getDurationDays(), request.getPaymentMethod(),
                quota, merchant, merchantUser, ad.getId());
        ad.setStartDate(LocalDate.now());
        ad.setEndDate(ad.getStartDate().plusDays(request.getDurationDays() - 1L));
        ad.setPayAmount(payAmount);
        merchantAdRepository.save(ad);

        // 免费额度投放：占用免费条数；付费投放：不占用免费名额
        if ("free".equals(request.getPaymentMethod())) {
            quota.setFreeUsedCount(defaultInt(quota.getFreeUsedCount()) + 1);
        }
        quota.setUsedCount(defaultInt(quota.getUsedCount()) + 1);
        merchantAdQuotaRepository.save(quota);

        List<Resident> targets = resolveAdTargets(merchant.getPropertyCompanyId(), null, null, null);
        int recipientCount = fanOutAd(ad, merchantUser, targets);
        ad.setRecipientCount(recipientCount);
        merchantAdRepository.save(ad);

        return toListItem(ad);
    }

    @Transactional
    public MerchantAdListItemResponse createTargetedAd(TargetedMerchantAdRequest request) {
        Merchant merchant = requireCurrentMerchant();
        Resident merchantUser = requireCurrentMerchantUser();
        if (!merchant.getId().equals(request.getMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "只能为本商家创建定向广告");
        }
        if (StringUtils.hasText(request.getTargetGender())
                && !TARGET_GENDER_VALUES.contains(request.getTargetGender())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "targetGender不合法");
        }
        validateDurationDays(request.getDurationDays());

        LocalDate weekStart = currentWeekStart();
        MerchantAdQuota quota = getOrCreateQuota(merchant.getId(), weekStart);

        MerchantAd ad = new MerchantAd();
        ad.setId(idGenerator.generateId("mad"));
        ad.setMerchantId(merchant.getId());
        ad.setTitle(request.getTitle().trim());
        ad.setContent(request.getContent().trim());
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            try {
                ad.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
            } catch (Exception e) {
                // 原先静默写成 "[]"，用户上传的图片被无声丢弃且无任何日志。
                // 改为让创建失败：宁可报错让用户重试，也不能存一条"看起来成功但图片没了"的广告。
                log.error("[商家广告] 图片序列化失败 merchantId={}", merchant.getId(), e);
                throw new BizException(ErrorCode.SYSTEM_ERROR, "广告图片保存失败，请重试");
            }
        }
        ad.setProductId(request.getProductId());
        ad.setWeekStartDate(weekStart);
        ad.setDurationDays(request.getDurationDays());
        ad.setPaymentMethod(request.getPaymentMethod());

        BigDecimal payAmount = resolvePayAmount(request.getDurationDays(), request.getPaymentMethod(),
                quota, merchant, merchantUser, ad.getId());
        ad.setStartDate(LocalDate.now());
        ad.setEndDate(ad.getStartDate().plusDays(request.getDurationDays() - 1L));
        ad.setPayAmount(payAmount);
        ad.setTargetBuildings(toJsonString(request.getTargetBuildings()));
        ad.setTargetGender(StringUtils.hasText(request.getTargetGender()) ? request.getTargetGender() : null);
        ad.setTargetAgeBrackets(toJsonString(request.getTargetAgeBrackets()));
        merchantAdRepository.save(ad);

        if ("free".equals(request.getPaymentMethod())) {
            quota.setFreeUsedCount(defaultInt(quota.getFreeUsedCount()) + 1);
        }
        quota.setUsedCount(defaultInt(quota.getUsedCount()) + 1);
        merchantAdQuotaRepository.save(quota);

        List<Resident> targets = resolveAdTargets(merchant.getPropertyCompanyId(),
                request.getTargetBuildings(), request.getTargetGender(), request.getTargetAgeBrackets());
        int recipientCount = fanOutAd(ad, merchantUser, targets);
        ad.setRecipientCount(recipientCount);
        merchantAdRepository.save(ad);

        return toListItem(ad);
    }

    /**
     * 结算金额：免费额度投放=0；付费按 单日价×天数。
     */
    private BigDecimal resolvePayAmount(int durationDays, String paymentMethod, MerchantAdQuota quota,
                                        Merchant merchant, Resident merchantUser, String adId) {
        if (!PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod不合法");
        }
        if ("free".equals(paymentMethod)) {
            int freeQuotaRemaining = resolveFreeQuotaRemaining(quota);
            if (freeQuotaRemaining <= 0) {
                throw new BizException(ErrorCode.MERCHANT_AD_QUOTA_INSUFFICIENT, "本周免费额度已用完，请选择付费投放");
            }
            return BigDecimal.ZERO;
        }

        BigDecimal payAmount = platformSettingService.getAdDayPrice()
                .multiply(BigDecimal.valueOf(durationDays));
        switch (paymentMethod) {
            case "coin" -> {
                // 余额不足/冻结由 spendCoins 内部校验并抛出对应错误码
                propertyCoinService.spendCoins(
                        merchantUser, payAmount, "ad_purchase", adId,
                        "商家广告位购买", merchantUser.getId(), false);
            }
            case "point" -> merchantPointService.spendPoints(
                    merchant.getId(), merchant.getPropertyCompanyId(), payAmount, "商家广告位购买");
            case "wechat", "mock" -> {
                // 模拟微信支付成功（与保证金支付同套路，真实微信支付暂未接入）
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod不合法");
        }
        return payAmount;
    }

    /**
     * 广告 fan-out：向目标住户各写入一条 merchant_ad 消息（聊天列表小横条）+ WS 推送。
     * 返回实际推送条数。
     */
    private int fanOutAd(MerchantAd ad, Resident merchantUser, List<Resident> targets) {
        if (targets == null || targets.isEmpty()) {
            return 0;
        }
        String content = ad.getTitle() + "\n" + ad.getContent();
        int sent = 0;
        for (Resident target : targets) {
            if (target.getId().equals(merchantUser.getId())) {
                continue;
            }
            Message message = new Message();
            message.setId(idGenerator.generateId("msg"));
            message.setFromId(merchantUser.getId());
            message.setToId(target.getId());
            message.setMessageType("merchant_ad");
            message.setChatType("private");
            message.setContent(content);
            message.setReadStatus("unread");
            message.setPropertyCompanyId(target.getPropertyCompanyId());
            message.setRelatedTaskId(ad.getId());
            messageRepository.save(message);
            try {
                webSocketService.sendToUser(target.getId(), "message.new",
                        Map.of("messageType", "merchant_ad", "title", ad.getTitle()));
            } catch (Exception ignored) {
                // 推送失败不影响落库
            }
            sent++;
        }
        return sent;
    }

    /**
     * 解析广告投放目标住户：默认全物业 active 住户；定向广告按楼栋/性别/年龄段过滤。
     */
    private List<Resident> resolveAdTargets(String propertyCompanyId,
                                            List<String> buildings,
                                            String gender,
                                            List<String> ageBracketIds) {
        List<Resident> residents = residentRepository
                .findByPropertyCompanyIdAndStatus(propertyCompanyId, "active");
        residents = residents.stream()
                .filter(r -> r.getDeletedAt() == null)
                .toList();

        if (buildings != null && !buildings.isEmpty()) {
            Set<String> buildingSet = new HashSet<>(buildings);
            residents = residents.stream()
                    .filter(r -> StringUtils.hasText(r.getBuilding()) && buildingSet.contains(r.getBuilding()))
                    .toList();
        }
        if ("male".equals(gender)) {
            residents = residents.stream()
                    .filter(r -> Integer.valueOf(1).equals(r.getGender()))
                    .toList();
        } else if ("female".equals(gender)) {
            residents = residents.stream()
                    .filter(r -> Integer.valueOf(2).equals(r.getGender()))
                    .toList();
        }
        if (ageBracketIds != null && !ageBracketIds.isEmpty()) {
            List<AgeBracketConfig> brackets = ageBracketConfigRepository.findAllById(ageBracketIds);
            if (brackets.isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "年龄段配置不存在");
            }
            residents = residents.stream()
                    .filter(r -> matchesAnyBracket(r.getAge(), brackets))
                    .toList();
        }
        return residents;
    }

    private boolean matchesAnyBracket(Integer age, List<AgeBracketConfig> brackets) {
        if (age == null) {
            return false;
        }
        for (AgeBracketConfig bracket : brackets) {
            int min = bracket.getMinAge() != null ? bracket.getMinAge() : 0;
            Integer max = bracket.getMaxAge();
            if (age >= min && (max == null || age <= max)) {
                return true;
            }
        }
        return false;
    }

    private void validateDurationDays(Integer durationDays) {
        if (durationDays == null || durationDays < 1 || durationDays > MAX_DURATION_DAYS) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "展示天数须在1-" + MAX_DURATION_DAYS + "天之间");
        }
    }

    private int resolveFreeQuotaRemaining(MerchantAdQuota quota) {
        int freeQuota = platformSettingService.isAdFreeQuotaEnabled()
                ? platformSettingService.getAdFreeQuotaPerWeek() : 0;
        int purchasedQuota = quota != null ? defaultInt(quota.getPurchasedQuota()) : 0;
        int freeUsedCount = quota != null ? defaultInt(quota.getFreeUsedCount()) : 0;
        return Math.max(freeQuota + purchasedQuota - freeUsedCount, 0);
    }

    private MerchantAdQuota currentWeekQuotaOrNull() {
        Merchant merchant = requireCurrentMerchant();
        return merchantAdQuotaRepository
                .findByMerchantIdAndWeekStartDate(merchant.getId(), currentWeekStart())
                .orElse(null);
    }

    @Transactional
    public MerchantAdQuotaResponse addPurchasedQuota(String merchantId, int purchasedQuota) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        if (purchasedQuota <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "purchasedQuota须大于0");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!roleGuard.isPlatformAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!merchant.getPropertyCompanyId().equals(propertyCompanyId)) {
                throw new BizException(ErrorCode.PROPERTY_OPERATOR_OUT_OF_SCOPE);
            }
        }

        LocalDate weekStart = currentWeekStart();
        MerchantAdQuota quota = getOrCreateQuota(merchantId, weekStart);
        quota.setPurchasedQuota(defaultInt(quota.getPurchasedQuota()) + purchasedQuota);
        merchantAdQuotaRepository.save(quota);
        return buildQuotaResponse(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantAdSettingsResponse getAdSettings() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        return MerchantAdSettingsResponse.builder()
                .dayPrice(platformSettingService.getAdDayPrice())
                .freeQuotaEnabled(platformSettingService.isAdFreeQuotaEnabled())
                .freeQuotaPerWeek(platformSettingService.getAdFreeQuotaPerWeek())
                .build();
    }

    @Transactional
    public MerchantAdSettingsResponse updateAdSettings(UpdateMerchantAdSettingsRequest request) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        platformSettingService.updateAdSettings(
                request.getDayPrice(),
                request.getFreeQuotaEnabled(),
                request.getFreeQuotaPerWeek());
        return getAdSettings();
    }

    private MerchantAdListItemResponse toListItem(MerchantAd ad) {
        return MerchantAdListItemResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .content(ad.getContent())
                .imageUrls(parseImages(ad.getImageUrls()))
                .productId(ad.getProductId())
                .recipientCount(ad.getRecipientCount() == null ? 0 : ad.getRecipientCount())
                .durationDays(ad.getDurationDays())
                .startDate(ad.getStartDate() != null ? ad.getStartDate().format(DATE_FMT) : null)
                .endDate(ad.getEndDate() != null ? ad.getEndDate().format(DATE_FMT) : null)
                .payAmount(ad.getPayAmount())
                .paymentMethod(ad.getPaymentMethod())
                .createdAt(DateTimeUtil.toLocalDateTimeString(ad.getCreatedAt()))
                .build();
    }

    private String toJsonString(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "JSON序列化失败");
        }
    }

    private MerchantAdQuota getOrCreateQuota(String merchantId, LocalDate weekStart) {
        return merchantAdQuotaRepository.findByMerchantIdAndWeekStartDate(merchantId, weekStart)
                .orElseGet(() -> {
                    MerchantAdQuota created = new MerchantAdQuota();
                    created.setId(idGenerator.generateId("maq"));
                    created.setMerchantId(merchantId);
                    created.setWeekStartDate(weekStart);
                    created.setFreeQuota(platformSettingService.getAdFreeQuotaPerWeek());
                    created.setPurchasedQuota(0);
                    created.setUsedCount(0);
                    created.setFreeUsedCount(0);
                    return merchantAdQuotaRepository.save(created);
                });
    }

    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private List<String> parseImages(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Merchant requireCurrentMerchant() {
        Resident merchantUser = requireCurrentMerchantUser();
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private Resident requireCurrentMerchantUser() {
        roleGuard.requireAnyRole("merchant");
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }
}
