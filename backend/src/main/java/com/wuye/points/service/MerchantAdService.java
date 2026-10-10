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
    private static final Set<String> PAYMENT_METHODS = Set.of("free", "coin", "point", "wechat", "mock");
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
    private final PropertyCoinService propertyCoinService;
    private final MerchantPointService merchantPointService;
    private final WebSocketService webSocketService;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PayChannel wechatPayService;

    /** 模拟支付开关：仅开发环境（app.payment.mock-enabled=true）允许 mock 支付方式（fail-closed 默认 false） */
    @Value("${app.payment.mock-enabled:false}")
    private boolean mockPayEnabled;

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
        applyImageUrls(ad, request.getImageUrls(), merchant.getId());
        ad.setProductId(request.getProductId());
        ad.setWeekStartDate(weekStart);
        ad.setDurationDays(request.getDurationDays());
        ad.setPaymentMethod(request.getPaymentMethod());

        BigDecimal payAmount = resolvePayAmount(request.getDurationDays(), request.getPaymentMethod(),
                quota, merchant, merchantUser, ad.getId());
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
                quota, merchant, merchantUser, ad.getId());
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
        if (!wechatPayService.isEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "在线支付未开通，请使用物业币或商家积分支付");
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
            case "mock" -> {
                // 模拟支付仅限开发环境；生产必须走真实微信或积分/物业币，否则等于免费投放
                if (!mockPayEnabled) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "模拟支付仅在开发环境可用");
                }
            }
            case "wechat" -> {
                // 现金部分由微信预下单处理（initiateAdChannelPayment），此处仅计价
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
                .imageUrls(parseStringList(ad.getImageUrls()))
                .productId(ad.getProductId())
                .recipientCount(ad.getRecipientCount() == null ? 0 : ad.getRecipientCount())
                .durationDays(ad.getDurationDays())
                .startDate(ad.getStartDate() != null ? ad.getStartDate().format(DATE_FMT) : null)
                .endDate(ad.getEndDate() != null ? ad.getEndDate().format(DATE_FMT) : null)
                .payAmount(ad.getPayAmount())
                .paymentMethod(ad.getPaymentMethod())
                .status(ad.getStatus() != null ? ad.getStatus() : "active")
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
