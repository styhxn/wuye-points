package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台级配置（JSON 文件持久化，避免新增数据库表）
 */
@Slf4j
@Service
public class PlatformSettingService {

    private static final String KEY_CONSULTATION_COMMISSION_RATE = "consultation.commissionRate";
    private static final String KEY_RESIDENT_MERCHANT_DEFAULT_DEPOSIT = "residentMerchant.defaultDepositAmount";
    private static final String KEY_RESIDENT_MERCHANT_COMMISSION_RATE = "residentMerchant.platformCommissionRate";
    private static final String KEY_RESIDENT_MERCHANT_DISTRIBUTOR_COMMISSION_RATE =
            "residentMerchant.distributorProductCommissionRate";
    private static final String KEY_RESIDENT_MERCHANT_REFUND_WINDOW_DAYS = "residentMerchant.refundWindowDays";
    private static final String KEY_RESIDENT_MERCHANT_MARKUP_MIN_RATE = "residentMerchant.markupMinRate";
    private static final String KEY_RESIDENT_MERCHANT_MARKUP_MAX_RATE = "residentMerchant.markupMaxRate";
    private static final String KEY_AD_DAY_PRICE = "ad.dayPrice";
    private static final String KEY_AD_FREE_QUOTA_ENABLED = "ad.freeQuotaEnabled";
    private static final String KEY_AD_FREE_QUOTA_PER_WEEK = "ad.freeQuotaPerWeek";

    // ===== 结算/分账模式开关 =====
    private static final String KEY_WECHAT_SPLIT_ENABLED = "settlement.wechatSplitEnabled";
    private static final String KEY_LEDGER_FALLBACK_ENABLED = "settlement.ledgerFallbackEnabled";
    private static final String KEY_AUTO_WITHDRAWAL_ENABLED = "settlement.autoWithdrawalEnabled";
    private static final String KEY_PLATFORM_WITHDRAWAL_FEE_ENABLED = "settlement.platformWithdrawalFeeEnabled";
    private static final String KEY_PLATFORM_WITHDRAWAL_FEE_RATE = "settlement.platformWithdrawalFeeRate";
    private static final String KEY_PLATFORM_WITHDRAWAL_FEE_SHARE_RATE = "settlement.platformWithdrawalFeeShareRate";
    private static final String KEY_FEE_FOR_MERCHANT = "settlement.feeForMerchant";
    private static final String KEY_FEE_FOR_ROLE = "settlement.feeForRole";
    private static final String KEY_FEE_FOR_PROPERTY = "settlement.feeForProperty";
    private static final String KEY_FEE_FOR_COIN = "settlement.feeForCoin";

    // ===== 商家核实 + 冻结期配置 =====
    private static final String KEY_ORDER_MERCHANT_VERIFICATION_ENABLED = "order.merchantVerificationEnabled";
    private static final String KEY_ORDER_MERCHANT_VERIFICATION_HOURS = "order.merchantVerificationHours";
    private static final String KEY_ORDER_FREEZE_DAYS = "order.freezeDays";
    private static final String KEY_ORDER_REFUND_WINDOW_DAYS = "order.refundWindowDays";

    // ===== 归属开关 / 提现粒度 / 分成维度 =====
    private static final String KEY_REWARD_ATTRIBUTION_MODE = "reward.attributionMode";
    private static final String KEY_WITHDRAWAL_GRANULARITY = "settlement.withdrawalGranularity";
    private static final String KEY_SHARE_DIMENSION = "distribution.shareDimension";

    /** 归属模式：property=归属物业积分池 / platform=归属平台 */
    public static final String ATTRIBUTION_MODE_PROPERTY = "property";
    public static final String ATTRIBUTION_MODE_PLATFORM = "platform";

    /** 提现粒度：per_role=按角色 / merchant=按商家 / resident=按人 */
    public static final String GRANULARITY_PER_ROLE = "per_role";
    public static final String GRANULARITY_MERCHANT = "merchant";
    public static final String GRANULARITY_RESIDENT = "resident";

    /** 分成维度：default=四级分成 / simple=平台+物业两级 */
    public static final String SHARE_DIMENSION_DEFAULT = "default";
    public static final String SHARE_DIMENSION_SIMPLE = "simple";

    /** 提现手续费适用类别（用于按类别开关） */
    public static final String FEE_CATEGORY_MERCHANT = "merchant";
    public static final String FEE_CATEGORY_ROLE = "role";
    public static final String FEE_CATEGORY_PROPERTY = "property";
    public static final String FEE_CATEGORY_COIN = "coin";

    private final ObjectMapper objectMapper;
    private final Path settingsFile;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public PlatformSettingService(
            ObjectMapper objectMapper,
            @Value("${app.upload.path:/data/uploads}") String uploadPath) {
        this.objectMapper = objectMapper;
        this.settingsFile = Path.of(uploadPath, "platform-settings.json");
        loadFromDisk();
    }

    public BigDecimal getConsultationCommissionRate() {
        return getDecimal(KEY_CONSULTATION_COMMISSION_RATE, BigDecimal.ZERO);
    }

    public void setConsultationCommissionRate(BigDecimal rate) {
        if (rate != null && (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("commissionRate需在0-1之间");
        }
        putDecimal(KEY_CONSULTATION_COMMISSION_RATE, rate == null ? BigDecimal.ZERO : rate);
    }

    public BigDecimal getResidentMerchantDefaultDeposit() {
        return getDecimal(KEY_RESIDENT_MERCHANT_DEFAULT_DEPOSIT, new BigDecimal("500.00"));
    }

    /**
     * 业主商户销售自有商品的平台佣金比例（默认 20%）。
     */
    public BigDecimal getResidentMerchantCommissionRate() {
        return getDecimal(KEY_RESIDENT_MERCHANT_COMMISSION_RATE, new BigDecimal("0.20"));
    }

    /**
     * 业主商户销售一级经销商货品的平台佣金比例（默认 10%）。
     * 需求：经销商产品卖出后自动按该比例扣除手续费，剩余差价归业主商户。
     */
    public BigDecimal getResidentMerchantDistributorCommissionRate() {
        return getDecimal(KEY_RESIDENT_MERCHANT_DISTRIBUTOR_COMMISSION_RATE, new BigDecimal("0.10"));
    }

    public void setResidentMerchantDistributorCommissionRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("经销商货品佣金比例需在0-1之间");
        }
        putDecimal(KEY_RESIDENT_MERCHANT_DISTRIBUTOR_COMMISSION_RATE, rate);
    }

    public Integer getResidentMerchantRefundWindowDays() {
        return getInt(KEY_RESIDENT_MERCHANT_REFUND_WINDOW_DAYS, 7);
    }

    /**
     * 业主小店加价下限（默认 5%）。
     */
    public BigDecimal getResidentMerchantMarkupMinRate() {
        return getDecimal(KEY_RESIDENT_MERCHANT_MARKUP_MIN_RATE, new BigDecimal("0.05"));
    }

    /**
     * 业主小店加价上限（默认 10%）。
     */
    public BigDecimal getResidentMerchantMarkupMaxRate() {
        return getDecimal(KEY_RESIDENT_MERCHANT_MARKUP_MAX_RATE, new BigDecimal("0.10"));
    }

    public BigDecimal getAdDayPrice() {
        return getDecimal(KEY_AD_DAY_PRICE, new BigDecimal("10.00"));
    }

    public boolean isAdFreeQuotaEnabled() {
        return getBool(KEY_AD_FREE_QUOTA_ENABLED, true);
    }

    public Integer getAdFreeQuotaPerWeek() {
        return getInt(KEY_AD_FREE_QUOTA_PER_WEEK, 1);
    }

    public void updateAdSettings(BigDecimal dayPrice, Boolean freeQuotaEnabled, Integer freeQuotaPerWeek) {
        if (dayPrice != null) {
            if (dayPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("广告单日价不能为负数");
            }
            putDecimal(KEY_AD_DAY_PRICE, dayPrice);
        }
        if (freeQuotaEnabled != null) {
            putBool(KEY_AD_FREE_QUOTA_ENABLED, freeQuotaEnabled);
        }
        if (freeQuotaPerWeek != null) {
            if (freeQuotaPerWeek < 0) {
                throw new IllegalArgumentException("每周免费条数不能为负数");
            }
            putInt(KEY_AD_FREE_QUOTA_PER_WEEK, freeQuotaPerWeek);
        }
    }

    public void updateResidentMerchantSettings(
            BigDecimal defaultDepositAmount,
            BigDecimal platformCommissionRate,
            Integer refundWindowDays,
            BigDecimal markupMinRate,
            BigDecimal markupMaxRate) {
        if (defaultDepositAmount != null) {
            putDecimal(KEY_RESIDENT_MERCHANT_DEFAULT_DEPOSIT, defaultDepositAmount);
        }
        if (platformCommissionRate != null) {
            putDecimal(KEY_RESIDENT_MERCHANT_COMMISSION_RATE, platformCommissionRate);
        }
        if (refundWindowDays != null) {
            putInt(KEY_RESIDENT_MERCHANT_REFUND_WINDOW_DAYS, refundWindowDays);
        }
        // 加价上下限：先整体校验再落盘，避免校验失败却已写入部分值
        if (markupMinRate != null
                && (markupMinRate.compareTo(BigDecimal.ZERO) < 0 || markupMinRate.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("markupMinRate需在0-1之间");
        }
        if (markupMaxRate != null
                && (markupMaxRate.compareTo(BigDecimal.ZERO) < 0 || markupMaxRate.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException("markupMaxRate需在0-1之间");
        }
        BigDecimal effectiveMin = markupMinRate != null ? markupMinRate : getResidentMerchantMarkupMinRate();
        BigDecimal effectiveMax = markupMaxRate != null ? markupMaxRate : getResidentMerchantMarkupMaxRate();
        if (effectiveMin.compareTo(effectiveMax) > 0) {
            throw new IllegalArgumentException("加价下限不能高于上限");
        }
        if (markupMinRate != null) {
            putDecimal(KEY_RESIDENT_MERCHANT_MARKUP_MIN_RATE, markupMinRate);
        }
        if (markupMaxRate != null) {
            putDecimal(KEY_RESIDENT_MERCHANT_MARKUP_MAX_RATE, markupMaxRate);
        }
    }

    // ===== 社区食堂配置 =====
    private static final String KEY_CANTEEN_QUOTA_FEE_RATE = "canteen.quotaFeeRate";

    /** 社区食堂购买充值额度手续费比例（内扣，默认 2%） */
    public BigDecimal getCanteenQuotaFeeRate() {
        return getDecimal(KEY_CANTEEN_QUOTA_FEE_RATE, new BigDecimal("0.02"));
    }

    public void setCanteenQuotaFeeRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("社区食堂手续费比例需在0-1之间");
        }
        putDecimal(KEY_CANTEEN_QUOTA_FEE_RATE, rate);
    }

    // ===== 结算/分账模式配置 =====

    public boolean isWechatSplitEnabled() {
        // 默认开启：wechat.pay.enabled=true 即走微信普通分账（真分账），未配置收款方时资金解冻回平台，不丢钱。
        return getBool(KEY_WECHAT_SPLIT_ENABLED, true);
    }

    public boolean isLedgerFallbackEnabled() {
        return getBool(KEY_LEDGER_FALLBACK_ENABLED, true);
    }

    public boolean isAutoWithdrawalEnabled() {
        return getBool(KEY_AUTO_WITHDRAWAL_ENABLED, false);
    }

    public boolean isPlatformWithdrawalFeeEnabled() {
        return getBool(KEY_PLATFORM_WITHDRAWAL_FEE_ENABLED, true);
    }

    public BigDecimal getPlatformWithdrawalFeeRate() {
        return getDecimal(KEY_PLATFORM_WITHDRAWAL_FEE_RATE, new BigDecimal("0.0060"));
    }

    public BigDecimal getPlatformWithdrawalFeeShareRate() {
        return getDecimal(KEY_PLATFORM_WITHDRAWAL_FEE_SHARE_RATE, new BigDecimal("1.0000"));
    }

    public boolean isFeeForMerchant() {
        return getBool(KEY_FEE_FOR_MERCHANT, true);
    }

    public boolean isFeeForRole() {
        return getBool(KEY_FEE_FOR_ROLE, true);
    }

    public boolean isFeeForProperty() {
        return getBool(KEY_FEE_FOR_PROPERTY, true);
    }

    public boolean isFeeForCoin() {
        return getBool(KEY_FEE_FOR_COIN, true);
    }

    // ===== 归属开关 / 提现粒度 / 分成维度 =====

    /**
     * 奖励金/积分归属模式：property(归属物业积分池，默认) / platform(归属平台)。
     */
    public String getRewardAttributionMode() {
        String raw = cache.get(KEY_REWARD_ATTRIBUTION_MODE);
        return ATTRIBUTION_MODE_PLATFORM.equalsIgnoreCase(raw)
                ? ATTRIBUTION_MODE_PLATFORM : ATTRIBUTION_MODE_PROPERTY;
    }

    public void setRewardAttributionMode(String mode) {
        if (!ATTRIBUTION_MODE_PROPERTY.equals(mode) && !ATTRIBUTION_MODE_PLATFORM.equals(mode)) {
            throw new IllegalArgumentException("归属模式必须为 property 或 platform");
        }
        cache.put(KEY_REWARD_ATTRIBUTION_MODE, mode);
        persistToDisk();
    }

    /**
     * 提现粒度：per_role(按角色，默认) / merchant(按商家) / resident(按人)。
     */
    public String getWithdrawalGranularity() {
        String raw = cache.get(KEY_WITHDRAWAL_GRANULARITY);
        if (GRANULARITY_MERCHANT.equals(raw) || GRANULARITY_RESIDENT.equals(raw)) {
            return raw;
        }
        return GRANULARITY_PER_ROLE;
    }

    public void setWithdrawalGranularity(String granularity) {
        if (!GRANULARITY_PER_ROLE.equals(granularity)
                && !GRANULARITY_MERCHANT.equals(granularity)
                && !GRANULARITY_RESIDENT.equals(granularity)) {
            throw new IllegalArgumentException("提现粒度必须为 per_role、merchant 或 resident");
        }
        cache.put(KEY_WITHDRAWAL_GRANULARITY, granularity);
        persistToDisk();
    }

    /**
     * 分成维度：default(四级分成，默认) / simple(平台+物业两级)。
     */
    public String getShareDimension() {
        return SHARE_DIMENSION_SIMPLE.equals(cache.get(KEY_SHARE_DIMENSION))
                ? SHARE_DIMENSION_SIMPLE : SHARE_DIMENSION_DEFAULT;
    }

    public void setShareDimension(String dimension) {
        if (!SHARE_DIMENSION_DEFAULT.equals(dimension) && !SHARE_DIMENSION_SIMPLE.equals(dimension)) {
            throw new IllegalArgumentException("分成维度必须为 default 或 simple");
        }
        cache.put(KEY_SHARE_DIMENSION, dimension);
        persistToDisk();
    }

    // ===== 商家核实 + 冻结期配置 =====

    public boolean isMerchantVerificationEnabled() {
        return getBool(KEY_ORDER_MERCHANT_VERIFICATION_ENABLED, true);
    }

    public int getMerchantVerificationHours() {
        return getInt(KEY_ORDER_MERCHANT_VERIFICATION_HOURS, 24);
    }

    public int getFreezeDays() {
        return getInt(KEY_ORDER_FREEZE_DAYS, 7);
    }

    public int getRefundWindowDays() {
        return getInt(KEY_ORDER_REFUND_WINDOW_DAYS, 3);
    }

    public void updateOrderConfig(Boolean merchantVerificationEnabled,
                                   Integer merchantVerificationHours,
                                   Integer freezeDays,
                                   Integer refundWindowDays) {
        if (merchantVerificationEnabled != null) {
            putBool(KEY_ORDER_MERCHANT_VERIFICATION_ENABLED, merchantVerificationEnabled);
        }
        if (merchantVerificationHours != null) {
            if (merchantVerificationHours < 1 || merchantVerificationHours > 168) {
                throw new IllegalArgumentException("商家核实时限范围应为1-168小时");
            }
            putInt(KEY_ORDER_MERCHANT_VERIFICATION_HOURS, merchantVerificationHours);
        }
        if (freezeDays != null) {
            if (freezeDays < 1 || freezeDays > 30) {
                throw new IllegalArgumentException("冻结期范围应为1-30天");
            }
            putInt(KEY_ORDER_FREEZE_DAYS, freezeDays);
        }
        if (refundWindowDays != null) {
            if (refundWindowDays < 1 || refundWindowDays > 30) {
                throw new IllegalArgumentException("退款窗口期范围应为1-30天");
            }
            int effectiveFreezeDays = freezeDays != null ? freezeDays : getFreezeDays();
            if (refundWindowDays > effectiveFreezeDays) {
                throw new IllegalArgumentException("退款窗口期不能超过冻结期");
            }
            putInt(KEY_ORDER_REFUND_WINDOW_DAYS, refundWindowDays);
        }
    }

    public boolean isFeeForCategory(String category) {
        return switch (category) {
            case FEE_CATEGORY_MERCHANT -> isFeeForMerchant();
            case FEE_CATEGORY_ROLE -> isFeeForRole();
            case FEE_CATEGORY_PROPERTY -> isFeeForProperty();
            case FEE_CATEGORY_COIN -> isFeeForCoin();
            default -> false;
        };
    }

    /**
     * 解析实际提现手续费率：
     * 1) 平台提现手续费总开关关闭 → 0；
     * 2) 该类别手续费开关关闭 → 0；
     * 3) 否则优先用物业公司自定义费率，未设置则用全局可调费率。
     */
    public BigDecimal resolveWithdrawalFeeRate(BigDecimal propertyOverrideRate, String category) {
        if (!isPlatformWithdrawalFeeEnabled()) {
            return BigDecimal.ZERO;
        }
        if (!isFeeForCategory(category)) {
            return BigDecimal.ZERO;
        }
        if (propertyOverrideRate != null && propertyOverrideRate.compareTo(BigDecimal.ZERO) >= 0) {
            return propertyOverrideRate;
        }
        return getPlatformWithdrawalFeeRate();
    }

    /**
     * 解析平台手续费分成比例：优先用物业公司自定义，未设置则用全局默认。
     */
    public BigDecimal resolvePlatformFeeShareRate(BigDecimal propertyOverrideRate) {
        if (propertyOverrideRate != null && propertyOverrideRate.compareTo(BigDecimal.ZERO) >= 0) {
            return propertyOverrideRate;
        }
        return getPlatformWithdrawalFeeShareRate();
    }

    /**
     * 批量更新结算/分账模式配置（仅更新非 null 字段）。
     */
    public void updateSettlementConfig(SettlementConfigUpdate update) {
        if (update.wechatSplitEnabled() != null) {
            putBool(KEY_WECHAT_SPLIT_ENABLED, update.wechatSplitEnabled());
        }
        if (update.ledgerFallbackEnabled() != null) {
            putBool(KEY_LEDGER_FALLBACK_ENABLED, update.ledgerFallbackEnabled());
        }
        if (update.autoWithdrawalEnabled() != null) {
            putBool(KEY_AUTO_WITHDRAWAL_ENABLED, update.autoWithdrawalEnabled());
        }
        if (update.platformWithdrawalFeeEnabled() != null) {
            putBool(KEY_PLATFORM_WITHDRAWAL_FEE_ENABLED, update.platformWithdrawalFeeEnabled());
        }
        if (update.platformWithdrawalFeeRate() != null) {
            BigDecimal rate = update.platformWithdrawalFeeRate();
            if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("平台提现手续费率需在 0~1 之间");
            }
            putDecimal(KEY_PLATFORM_WITHDRAWAL_FEE_RATE, rate);
        }
        if (update.platformWithdrawalFeeShareRate() != null) {
            BigDecimal share = update.platformWithdrawalFeeShareRate();
            if (share.compareTo(BigDecimal.ZERO) < 0 || share.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("平台提现手续费分成比例需在 0~1 之间");
            }
            putDecimal(KEY_PLATFORM_WITHDRAWAL_FEE_SHARE_RATE, share);
        }
        if (update.feeForMerchant() != null) {
            putBool(KEY_FEE_FOR_MERCHANT, update.feeForMerchant());
        }
        if (update.feeForRole() != null) {
            putBool(KEY_FEE_FOR_ROLE, update.feeForRole());
        }
        if (update.feeForProperty() != null) {
            putBool(KEY_FEE_FOR_PROPERTY, update.feeForProperty());
        }
        if (update.feeForCoin() != null) {
            putBool(KEY_FEE_FOR_COIN, update.feeForCoin());
        }
        // 互斥校验：真分账(微信)与记账兜底至少启用一种
        boolean splitEnabled = update.wechatSplitEnabled() != null ? update.wechatSplitEnabled() : isWechatSplitEnabled();
        boolean ledger = update.ledgerFallbackEnabled() != null ? update.ledgerFallbackEnabled() : isLedgerFallbackEnabled();
        if (!splitEnabled && !ledger) {
            throw new IllegalArgumentException("真分账(微信)与记账兜底至少需启用一种结算模式");
        }
    }

    /**
     * 结算/分账模式配置更新载荷（字段均可为 null，仅更新非 null 项）。
     */
    public record SettlementConfigUpdate(
            Boolean wechatSplitEnabled,
            Boolean ledgerFallbackEnabled,
            Boolean autoWithdrawalEnabled,
            Boolean platformWithdrawalFeeEnabled,
            BigDecimal platformWithdrawalFeeRate,
            BigDecimal platformWithdrawalFeeShareRate,
            Boolean feeForMerchant,
            Boolean feeForRole,
            Boolean feeForProperty,
            Boolean feeForCoin) {
    }

    private BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String raw = cache.get(key);
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int getInt(String key, int defaultValue) {
        String raw = cache.get(key);
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private boolean getBool(String key, boolean defaultValue) {
        String raw = cache.get(key);
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(raw.trim());
    }

    private void putBool(String key, boolean value) {
        cache.put(key, String.valueOf(value));
        persistToDisk();
    }

    private void putDecimal(String key, BigDecimal value) {
        cache.put(key, value.stripTrailingZeros().toPlainString());
        persistToDisk();
    }

    private void putInt(String key, int value) {
        cache.put(key, String.valueOf(value));
        persistToDisk();
    }

    private void loadFromDisk() {
        if (!Files.exists(settingsFile)) {
            return;
        }
        try {
            String json = Files.readString(settingsFile);
            Map<String, String> loaded = objectMapper.readValue(json, new TypeReference<>() {});
            if (loaded != null) {
                cache.putAll(loaded);
            }
        } catch (IOException ex) {
            log.warn("加载平台配置失败: {}", settingsFile, ex);
        }
    }

    private void persistToDisk() {
        try {
            Files.createDirectories(settingsFile.getParent());
            Map<String, String> snapshot = new HashMap<>(cache);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile.toFile(), snapshot);
        } catch (IOException ex) {
            log.warn("保存平台配置失败: {}", settingsFile, ex);
        }
    }
}
