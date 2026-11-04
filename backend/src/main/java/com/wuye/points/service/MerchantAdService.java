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
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.repository.AgeBracketConfigRepository;
import com.wuye.points.repository.MerchantAdQuotaRepository;
import com.wuye.points.repository.MerchantAdRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    /** v8.21：广告只保留免费额度与微信现金支付；物业币/商家积分/模拟支付全部下线 */
    private static final Set<String> PAYMENT_METHODS = Set.of("free", "wechat");
    private static final Set<String> LEGACY_PAYMENT_METHODS = Set.of("coin", "point", "mock");
    private static final int MAX_DURATION_DAYS = 30;
    /** 重新发起支付时，作废超过该时长的旧 pending 流水（即时重试窗口，兜底于 PendingPaymentScheduler） */
    private static final int STALE_PENDING_MINUTES = 5;

    private final RoleGuard roleGuard;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantAdRepository merchantAdRepository;
    private final MerchantAdQuotaRepository merchantAdQuotaRepository;
    private final AgeBracketConfigRepository ageBracketConfigRepository;
    private final MessageRepository messageRepository;
    private final PlatformSettingService platformSettingService;
    private final WebSocketService webSocketService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PayChannel wechatPayService;

    /**
     * 广告微信支付开关（默认开启）。
     * 微信支付通道未配置时仍由 PayChannel 拒绝，不会出现「未收款却已投放」。
     */
    @Value("${app.merchant-ad.wechat-payment-enabled:true}")
    private boolean adWechatPaymentEnabled;

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
        Merchant merchant = requireCurrentMerchant();
        int freeQuotaRemaining = resolveFreeQuotaRemaining(merchant.getId());
        return MerchantAdQuoteResponse.builder()
                .durationDays(durationDays)
                .dayPrice(platformSettingService.getAdDayPrice())
                .payAmount(platformSettingService.getAdDayPrice()
                        .multiply(BigDecimal.valueOf(durationDays)))
                .freeQuotaRemaining(freeQuotaRemaining)
                .freeQuotaEnabled(platformSettingService.isAdFreeQuotaEnabled())
                .wechatPaymentEnabled(isAdWechatPaymentAvailable())
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

        int freeQuota = platformSettingService.isAdFreeQuotaEnabled()
                ? platformSettingService.getAdFreeQuotaPerWeek() : 0;
        int purchasedQuota = quota != null ? defaultInt(quota.getPurchasedQuota()) : 0;
        int usedCount = quota != null ? defaultInt(quota.getUsedCount()) : 0;
        int freeUsedCount = quota != null ? defaultInt(quota.getFreeUsedCount()) : 0;
        // v8.21：免费额度永久仅一次；已购买的周额度仍按周单独计算，不混入免费口径
        int freeQuotaRemaining = resolveFreeQuotaRemaining(merchant.getId());

        return MerchantAdQuotaResponse.builder()
                .weekStart(weekStart.format(DATE_FMT))
                .weekEnd(weekStart.plusDays(6).format(DATE_FMT))
                .freeQuota(freeQuota)
                .purchasedQuota(purchasedQuota)
                .usedCount(usedCount)
                .freeUsedCount(freeUsedCount)
                .remainingCount(freeQuotaRemaining + purchasedQuota)
                .freeQuotaRemaining(freeQuotaRemaining)
                .freeQuotaEnabled(platformSettingService.isAdFreeQuotaEnabled())
                .freeQuotaPerWeek(platformSettingService.getAdFreeQuotaPerWeek())
                .dayPrice(platformSettingService.getAdDayPrice())
                .wechatPaymentEnabled(isAdWechatPaymentAvailable())
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
        List<String> adIds = result.getContent().stream().map(MerchantAd::getId).toList();
        Map<String, PaymentRecord> latestPayments = adIds.isEmpty()
                ? Map.of()
                : paymentRecordRepository
                        .findByBizTypeAndBizIdInOrderByCreatedAtDesc(PaymentBizType.AD_PURCHASE, adIds)
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(
                                PaymentRecord::getBizId, r -> r, (first, ignored) -> first));
        List<MerchantAdListItemResponse> list = result.getContent().stream()
                .map(ad -> toListItem(ad, latestPayments.get(ad.getId())))
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

        assertAdPaymentMethodAllowed(request.getPaymentMethod());
        MerchantAd ad = new MerchantAd();
        ad.setId(idGenerator.generateId("mad"));
        ad.setMerchantId(merchant.getId());
        ad.setTitle(request.getTitle().trim());
        ad.setContent(request.getContent().trim());
        applyImageUrls(ad, request.getImageUrls(), merchant.getId());
        ad.setProductId(request.getProductId());
        ad.setWeekStartDate(weekStart);
        ad.setDurationDays(request.getDurationDays());
        ad.setPaymentMethod(request.getPaymentMethod());

        BigDecimal payAmount = resolvePayAmount(request.getDurationDays(), request.getPaymentMethod(),
                merchant.getId());
        ad.setPayAmount(payAmount);

        // 付费广告（wechat）：先落 pending_payment，微信回调确认后才投放；未收款绝不投放
        if (isChannelPayment(request.getPaymentMethod()) && payAmount.compareTo(BigDecimal.ZERO) > 0) {
            return initiateAdChannelPayment(ad, merchant, merchantUser, request.getOpenid());
        }

        List<Resident> targets = resolveAdTargets(merchant.getPropertyCompanyId(), null, null, null);
        activateAd(ad, quota, request.getPaymentMethod(), merchantUser, targets);
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

        assertAdPaymentMethodAllowed(request.getPaymentMethod());
        MerchantAd ad = new MerchantAd();
        ad.setId(idGenerator.generateId("mad"));
        ad.setMerchantId(merchant.getId());
        ad.setTitle(request.getTitle().trim());
        ad.setContent(request.getContent().trim());
        applyImageUrls(ad, request.getImageUrls(), merchant.getId());
        ad.setProductId(request.getProductId());
        ad.setWeekStartDate(weekStart);
        ad.setDurationDays(request.getDurationDays());
        ad.setPaymentMethod(request.getPaymentMethod());

        BigDecimal payAmount = resolvePayAmount(request.getDurationDays(), request.getPaymentMethod(),
                merchant.getId());
        ad.setPayAmount(payAmount);
        ad.setTargetBuildings(toJsonString(request.getTargetBuildings()));
        ad.setTargetGender(StringUtils.hasText(request.getTargetGender()) ? request.getTargetGender() : null);
        ad.setTargetAgeBrackets(toJsonString(request.getTargetAgeBrackets()));

        // 付费广告（wechat）：先落 pending_payment，微信回调确认后才投放；未收款绝不投放
        if (isChannelPayment(request.getPaymentMethod()) && payAmount.compareTo(BigDecimal.ZERO) > 0) {
            return initiateAdChannelPayment(ad, merchant, merchantUser, request.getOpenid());
        }

        List<Resident> targets = resolveAdTargets(merchant.getPropertyCompanyId(),
                request.getTargetBuildings(), request.getTargetGender(), request.getTargetAgeBrackets());
        activateAd(ad, quota, request.getPaymentMethod(), merchantUser, targets);
        return toListItem(ad);
    }

    /** 是否为真实微信通道支付（现金部分需预下单，不可直接投放）。 */
    private boolean isChannelPayment(String paymentMethod) {
        return "wechat".equals(paymentMethod);
    }

    /** 广告微信支付可用 = 开关开启且直连商户通道已配置（广告为平台自营收款，不分账）。 */
    private boolean isAdWechatPaymentAvailable() {
        return adWechatPaymentEnabled && wechatPayService.isDirectEnabled();
    }

    /**
     * 支付方式白名单校验。
     * 物业币/商家积分/模拟支付已下线，旧版客户端继续提交时返回明确业务错误，便于排查。
     */
    private void assertAdPaymentMethodAllowed(String paymentMethod) {
        if (LEGACY_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "广告已不支持物业币、商家积分或模拟支付，请使用免费额度或微信支付");
        }
        if (!PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod不合法");
        }
        // 微信支付未配置时 fail-closed，绝不回退为「直接投放」
        if ("wechat".equals(paymentMethod) && !adWechatPaymentEnabled) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "广告微信支付暂未开放，当前仅可使用免费额度");
        }
    }

    /**
     * 投放生效：设置投放期、占额度、向目标住户 fan-out。
     * 同步支付（free/coin/point/mock）与微信回调 {@link #completeChannelPayment(PaymentRecord)}
     * 共用，保证两条路径口径一致。
     */
    private void activateAd(MerchantAd ad, MerchantAdQuota quota, String paymentMethod,
                            Resident merchantUser, List<Resident> targets) {
        ad.setStartDate(LocalDate.now());
        ad.setEndDate(ad.getStartDate().plusDays(ad.getDurationDays() - 1L));
        ad.setStatus("active");
        merchantAdRepository.save(ad);

        // 免费额度投放：占用免费条数；付费投放：不占用免费名额
        if ("free".equals(paymentMethod)) {
            quota.setFreeUsedCount(defaultInt(quota.getFreeUsedCount()) + 1);
        }
        quota.setUsedCount(defaultInt(quota.getUsedCount()) + 1);
        merchantAdQuotaRepository.save(quota);

        int recipientCount = fanOutAd(ad, merchantUser, targets);
        ad.setRecipientCount(recipientCount);
        merchantAdRepository.save(ad);
    }

    /**
     * 付费广告微信预下单：广告先以 pending_payment 落库（不投放、不占额度），
     * 由微信回调 {@link #completeChannelPayment(PaymentRecord)} 激活投放（幂等）。
     * fail-closed：微信支付未配置直接拒绝，绝不回退为「直接投放」。
     */
    private MerchantAdListItemResponse initiateAdChannelPayment(MerchantAd ad, Merchant merchant,
                                                                Resident merchantUser, String requestOpenId) {
        if (!wechatPayService.isDirectEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "在线支付未开通，请稍后再试或使用免费额度");
        }
        ad.setStatus("pending_payment");
        merchantAdRepository.save(ad);
        // 作废该广告超时未支付的旧流水，避免用户卡在「支付处理中」
        closeStalePendingAdPayments(ad.getId());
        // 防重复提交：同一广告已有待支付流水时拒绝，避免重复扣款
        if (!paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                PaymentBizType.AD_PURCHASE, ad.getId(), "pending").isEmpty()) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "广告支付处理中，请勿重复提交");
        }
        // 前置校验 openid：缺失时不落任何支付流水
        String openId = resolveOpenId(requestOpenId, merchantUser);
        PaymentRecord record = wechatPayService.createIntent(
                PaymentBizType.AD_PURCHASE, ad.getId(), merchantUser.getId(),
                merchant.getPropertyCompanyId(), "wechat", ad.getPayAmount(),
                0, BigDecimal.ZERO, ad.getPayAmount());
        WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "商家广告投放");
        MerchantAdListItemResponse item = toListItem(ad);
        item.setPaymentRecordId(record.getId());
        item.setPayParams(payParams);
        return item;
    }

    /**
     * 微信支付回调确认后广告投放落账（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。
     * 计费口径与同步路径一致：占本周额度 usedCount+1（付费投放不占免费名额），并 fan-out 到目标住户。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        MerchantAd ad = merchantAdRepository.findById(record.getBizId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_AD_NOT_FOUND));
        if ("active".equals(ad.getStatus())) {
            return; // 幂等：已投放
        }
        MerchantAdQuota quota = getOrCreateQuota(ad.getMerchantId(), ad.getWeekStartDate());
        Resident merchantUser = residentRepository.findById(record.getResidentId()).orElse(null);
        if (merchantUser == null) {
            // 兜底：发送者住户已不存在时仍激活投放，仅跳过 fan-out，避免已付费广告丢失
            ad.setStartDate(LocalDate.now());
            ad.setEndDate(ad.getStartDate().plusDays(ad.getDurationDays() - 1L));
            ad.setStatus("active");
            ad.setRecipientCount(0);
            merchantAdRepository.save(ad);
            quota.setUsedCount(defaultInt(quota.getUsedCount()) + 1);
            merchantAdQuotaRepository.save(quota);
            log.warn("[商家广告] 支付成功后发送者住户缺失，跳过 fan-out adId={}", ad.getId());
            return;
        }
        List<Resident> targets = resolveAdTargets(record.getPropertyCompanyId(),
                parseStringList(ad.getTargetBuildings()), ad.getTargetGender(),
                parseStringList(ad.getTargetAgeBrackets()));
        activateAd(ad, quota, ad.getPaymentMethod(), merchantUser, targets);
    }

    /**
     * 作废该广告下超时未支付的预下单流水，让用户可立即重新发起支付。
     * 兜底于 {@code PendingPaymentScheduler}（30 分钟），这里给出更短的即时重试窗口。
     */
    private void closeStalePendingAdPayments(String adId) {
        List<PaymentRecord> stale = paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                        PaymentBizType.AD_PURCHASE, adId, "pending").stream()
                .filter(r -> r.getCreatedAt() != null
                        && r.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(STALE_PENDING_MINUTES)))
                .toList();
        for (PaymentRecord record : stale) {
            record.setStatus("closed");
            record.setErrorMsg("重新发起支付，原流水作废");
            paymentRecordRepository.save(record);
        }
        if (!stale.isEmpty()) {
            log.info("[商家广告] 作废超时支付流水 adId={} count={}", adId, stale.size());
        }
    }

    /** 解析微信小程序 openid：请求显式传入优先，否则取用户绑定 openid，两者皆无则报错。 */
    private String resolveOpenId(String requestOpenId, Resident merchantUser) {
        if (StringUtils.hasText(requestOpenId)) {
            return requestOpenId;
        }
        if (StringUtils.hasText(merchantUser.getOpenid())) {
            return merchantUser.getOpenid();
        }
        throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
    }

    /** 图片URL序列化落库（失败即报错，绝不静默丢图）。 */
    private void applyImageUrls(MerchantAd ad, List<String> imageUrls, String merchantId) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        try {
            ad.setImageUrls(objectMapper.writeValueAsString(imageUrls));
        } catch (Exception e) {
            // 原先静默写成 "[]"，用户上传的图片被无声丢弃且无任何日志。
            // 改为让创建失败：宁可报错让用户重试，也不能存一条"看起来成功但图片没了"的广告。
            log.error("[商家广告] 图片序列化失败 merchantId={}", merchantId, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "广告图片保存失败，请重试");
        }
    }

    /**
     * 结算金额：免费额度投放=0；付费按 单日价×天数。
     */
    private BigDecimal resolvePayAmount(int durationDays, String paymentMethod, String merchantId) {
        if ("free".equals(paymentMethod)) {
            int freeQuotaRemaining = resolveFreeQuotaRemaining(merchantId);
            if (freeQuotaRemaining <= 0) {
                throw new BizException(ErrorCode.MERCHANT_AD_QUOTA_INSUFFICIENT,
                        "免费额度已使用过（仅限一次），请选择微信支付投放");
            }
            return BigDecimal.ZERO;
        }

        // free/wechat 已在上方白名单校验；wechat 的现金部分由预下单处理，此处仅计价
        return platformSettingService.getAdDayPrice()
                .multiply(BigDecimal.valueOf(durationDays));
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

    /**
     * 免费额度剩余：v8.21 起改为「永久仅一次」，不再按周重置。
     * 历史上跨周使用过免费额度即视为已用尽；已购买的额度不混入免费额度口径。
     */
    private int resolveFreeQuotaRemaining(String merchantId) {
        if (!platformSettingService.isAdFreeQuotaEnabled()) {
            return 0;
        }
        long freeUsedTotal = merchantAdQuotaRepository.sumFreeUsedCountByMerchantId(merchantId);
        return freeUsedTotal > 0 ? 0 : Math.max(platformSettingService.getAdFreeQuotaPerWeek(), 0);
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
        return toListItem(ad, paymentRecordRepository
                .findFirstByBizTypeAndBizIdOrderByCreatedAtDesc(PaymentBizType.AD_PURCHASE, ad.getId())
                .orElse(null));
    }

    private MerchantAdListItemResponse toListItem(MerchantAd ad, PaymentRecord latestPayment) {
        String status = resolveDisplayStatus(ad);
        return MerchantAdListItemResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .content(ad.getContent())
                .imageUrls(parseStringList(ad.getImageUrls()))
                .productId(ad.getProductId())
                .recipientCount(ad.getRecipientCount() == null ? 0 : ad.getRecipientCount())
                .durationDays(ad.getDurationDays())
                .startDate(ad.getStartDate() != null ? ad.getStartDate().format(DATE_FMT) : null)
                .endDate(ad.getEndDate() != null ? ad.getEndDate().format(DATE_FMT) : null)
                .payAmount(ad.getPayAmount())
                .paymentMethod(ad.getPaymentMethod())
                .payStatus(resolvePayStatus(status, latestPayment))
                .status(status)
                .remainingDays(resolveRemainingDays(ad, status))
                .canRepay(canRepay(ad, status, latestPayment))
                .createdAt(DateTimeUtil.toLocalDateTimeString(ad.getCreatedAt()))
                .paymentRecordId(latestPayment != null ? latestPayment.getId() : null)
                .build();
    }

    /**
     * 列表展示状态：数据库只存 pending_payment/active/cancelled，
     * ended 为「已投放但超过 endDate」的派生态，不落库（避免依赖定时任务）。
     */
    private String resolveDisplayStatus(MerchantAd ad) {
        String raw = ad.getStatus() != null ? ad.getStatus() : "active";
        if ("active".equals(raw) && ad.getEndDate() != null
                && ad.getEndDate().isBefore(LocalDate.now())) {
            return "ended";
        }
        return raw;
    }

    /** 支付状态：优先以最近一笔支付流水为准；无流水时按广告状态推断（免费额度投放=已支付）。 */
    private String resolvePayStatus(String displayStatus, PaymentRecord latestPayment) {
        if (latestPayment != null) {
            if ("paid".equals(latestPayment.getStatus())) {
                return "paid";
            }
            if ("refunding".equals(latestPayment.getStatus())
                    || "refunded".equals(latestPayment.getStatus())) {
                return "refunded";
            }
            return "unpaid";
        }
        return "active".equals(displayStatus) || "ended".equals(displayStatus) ? "paid" : "unpaid";
    }

    private Integer resolveRemainingDays(MerchantAd ad, String displayStatus) {
        if (!"active".equals(displayStatus) || ad.getEndDate() == null) {
            return 0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), ad.getEndDate()) + 1;
        return (int) Math.max(days, 0);
    }

    /** 可补付：待支付 / 支付超时取消，且为微信现金广告、尚未产生已支付流水。 */
    private boolean canRepay(MerchantAd ad, String displayStatus, PaymentRecord latestPayment) {
        boolean statusRepayable = "pending_payment".equals(displayStatus) || "cancelled".equals(displayStatus);
        boolean wechatAmount = "wechat".equals(ad.getPaymentMethod())
                && ad.getPayAmount() != null
                && ad.getPayAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean notPaid = latestPayment == null || !"paid".equals(latestPayment.getStatus());
        return statusRepayable && wechatAmount && notPaid;
    }

    /**
     * 待支付广告补付（v8.23）：返回新的 payParams，前端直接调起 wx.requestPayment。
     *
     * <p>同一广告的并发补付用行锁串行化；5 分钟内的待支付流水复用（微信允许原商户单号重新预下单），
     * 超时流水关闭后新建，避免触发 payment_records 的「同一业务仅一条 pending」唯一键。</p>
     */
    @Transactional
    public MerchantAdListItemResponse repayAd(String adId) {
        Merchant merchant = requireCurrentMerchant();
        MerchantAd ad = merchantAdRepository.findByIdForUpdate(adId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_AD_NOT_FOUND));
        if (!merchant.getId().equals(ad.getMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他商家的广告");
        }
        if (!"wechat".equals(ad.getPaymentMethod())
                || ad.getPayAmount() == null
                || ad.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该广告无需支付");
        }

        List<PaymentRecord> records = paymentRecordRepository
                .findByBizTypeAndBizIdOrderByCreatedAtDesc(PaymentBizType.AD_PURCHASE, ad.getId());
        PaymentRecord paid = records.stream()
                .filter(r -> "paid".equals(r.getStatus()))
                .findFirst().orElse(null);
        if (paid != null) {
            // 微信已收款但回调/结算尚未把广告置 active：这里幂等补投，不重复收费
            completeChannelPayment(paid);
            return toListItem(ad, paid);
        }
        if ("active".equals(ad.getStatus()) || "ended".equals(ad.getStatus())) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "广告已投放，无需重复支付");
        }
        if (!adWechatPaymentEnabled) {
            throw new BizException(ErrorCode.PARAM_INVALID, "广告微信支付暂未开放");
        }
        if (!wechatPayService.isDirectEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "在线支付未开通，暂无法补付");
        }

        ad.setStatus("pending_payment");
        merchantAdRepository.save(ad);
        closeStalePendingAdPayments(ad.getId());

        Resident merchantUser = requireCurrentMerchantUser();
        String openId = resolveOpenId(null, merchantUser);
        PaymentRecord pending = paymentRecordRepository
                .findFirstByBizTypeAndBizIdAndStatusOrderByCreatedAtDesc(
                        PaymentBizType.AD_PURCHASE, ad.getId(), "pending")
                .orElse(null);
        PaymentRecord record = pending != null
                ? pending
                : wechatPayService.createIntent(
                        PaymentBizType.AD_PURCHASE, ad.getId(), merchantUser.getId(),
                        merchant.getPropertyCompanyId(), "wechat", ad.getPayAmount(),
                        0, BigDecimal.ZERO, ad.getPayAmount());
        WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "商家广告投放");
        MerchantAdListItemResponse item = toListItem(ad, record);
        item.setPaymentRecordId(record.getId());
        item.setPayParams(payParams);
        return item;
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

    private List<String> parseStringList(String json) {
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
