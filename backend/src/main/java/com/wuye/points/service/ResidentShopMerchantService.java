package com.wuye.points.service;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 业主小店店铺主体管理。
 *
 * <p>业主商户审核通过（active）时自动创建 merchant_source='resident_shop' 的商家记录，
 * 作为业主小店订单的归属主体（订单 merchantId 指向它，配送费/佣金按该主体配置计算）。
 * 店铺主体同时挂 platform_merchants 与 merchant_property_configs（commission=平台业主小店佣金率默认 10%，
 * point_exchange=100，配送费默认 0、无满免），参照 {@link MerchantBindingService#ensureMerchant} 三表联建模式。
 *
 * <p>退出/暂停/关闭时店铺主体置非营业（status=inactive）；再次激活时恢复 active。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentShopMerchantService {

    public static final String MERCHANT_SOURCE = "resident_shop";

    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    private final MerchantRepository merchantRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ResidentRepository residentRepository;
    private final PlatformSettingService platformSettingService;
    private final MerchantConverter merchantConverter;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 审核通过/激活时确保店铺主体存在（幂等）：
     * 已绑定 merchantId → 直接复用并恢复营业；否则按 手机号+物业+resident_shop 查找复用；都没有才三表新建。
     *
     * @return 店铺主体商家记录
     */
    @Transactional
    public Merchant ensureShopMerchant(ResidentMerchantApplication application) {
        if (StringUtils.hasText(application.getMerchantId())) {
            Merchant bound = merchantRepository.findById(application.getMerchantId()).orElse(null);
            if (bound != null) {
                if (!"active".equals(bound.getStatus())) {
                    bound.setStatus("active");
                    merchantRepository.save(bound);
                }
                return bound;
            }
        }

        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Optional<Merchant> existing = merchantRepository.findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                resident.getPhone(), application.getPropertyCompanyId(), MERCHANT_SOURCE);
        if (existing.isPresent()) {
            Merchant merchant = existing.get();
            if (!"active".equals(merchant.getStatus())) {
                merchant.setStatus("active");
                merchantRepository.save(merchant);
            }
            application.setMerchantId(merchant.getId());
            return merchant;
        }

        String shopName = resolveShopName(application, resident);

        PlatformMerchant platformMerchant = new PlatformMerchant();
        platformMerchant.setId(idGenerator.generateId("pmm"));
        platformMerchant.setName(shopName);
        platformMerchant.setContactPhone(resolveContactPhone(application, resident));
        if (StringUtils.hasText(application.getDescription())) {
            platformMerchant.setDescription(application.getDescription());
        }
        if (StringUtils.hasText(application.getBusinessHours())) {
            platformMerchant.setBusinessHours(application.getBusinessHours());
        }
        if (StringUtils.hasText(application.getCoverUrl())) {
            platformMerchant.setCoverUrl(application.getCoverUrl());
        }
        platformMerchant.setStatus("active");
        platformMerchantRepository.save(platformMerchant);

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(platformMerchant.getId());
        merchant.setPropertyCompanyId(application.getPropertyCompanyId());
        merchant.setName(shopName);
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(0);
        merchant.setCategory("业主小店");
        merchant.setAuditStatus("approved");
        merchant.setRankOrder(0);
        merchant.setContactPhone(resolveContactPhone(application, resident));
        merchant.setResidentId(resident.getId());
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchant.setDeliveryScope("both");
        merchant.setStatus("active");
        merchant.setServeAllCommunities(true);
        merchant.setMerchantSource(MERCHANT_SOURCE);
        if (StringUtils.hasText(application.getDescription())) {
            merchant.setDescription(application.getDescription());
        }
        if (StringUtils.hasText(application.getBusinessHours())) {
            merchant.setBusinessHours(application.getBusinessHours());
        }
        if (StringUtils.hasText(application.getCoverUrl())) {
            merchant.setCoverUrls(merchantConverter.serializeCoverUrls(
                    List.of(application.getCoverUrl())));
        }
        merchantRepository.save(merchant);

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(merchant.getPropertyCompanyId());
        // 平台佣金率：沿用业主小店结算口径（getResidentMerchantDistributorCommissionRate，默认 0.10）
        config.setCommissionRate(platformSettingService.getResidentMerchantDistributorCommissionRate());
        config.setPointExchangeRate(DEFAULT_POINT_EXCHANGE_RATE);
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        application.setMerchantId(merchant.getId());
        log.info("业主小店店铺主体已创建: merchantId={}, applicationId={}, residentId={}, shopName={}",
                merchant.getId(), application.getId(), resident.getId(), shopName);
        return merchant;
    }

    /**
     * 退出（quitting/closed）/暂停（suspended）时店铺主体置非营业。
     * 未绑定店铺主体时静默跳过（兼容存量数据）。
     */
    @Transactional
    public void deactivateShopMerchant(ResidentMerchantApplication application) {
        if (!StringUtils.hasText(application.getMerchantId())) {
            return;
        }
        merchantRepository.findById(application.getMerchantId()).ifPresent(merchant -> {
            if (!"inactive".equals(merchant.getStatus())) {
                merchant.setStatus("inactive");
                merchantRepository.save(merchant);
                log.info("业主小店店铺主体已置非营业: merchantId={}, applicationId={}",
                        merchant.getId(), application.getId());
            }
        });
    }

    /**
     * 店主修改店铺资料后同步店铺主体（merchant + platform_merchants）：
     * 店名/简介/营业时间/联系电话/封面，保持管理端与商家详情页看到的值与店主编辑一致。
     * 未绑定店铺主体时静默跳过（兼容存量数据）。
     */
    @Transactional
    public void syncShopProfile(ResidentMerchantApplication application) {
        if (!StringUtils.hasText(application.getMerchantId())) {
            return;
        }
        Resident resident = residentRepository.findById(application.getResidentId()).orElse(null);
        if (resident == null) {
            return;
        }
        String shopName = resolveShopName(application, resident);
        String contactPhone = resolveContactPhone(application, resident);
        String description = StringUtils.hasText(application.getDescription())
                ? application.getDescription() : null;
        String businessHours = StringUtils.hasText(application.getBusinessHours())
                ? application.getBusinessHours() : null;

        Merchant merchant = merchantRepository.findById(application.getMerchantId()).orElse(null);
        if (merchant == null) {
            return;
        }
        merchant.setName(shopName);
        merchant.setContactPhone(contactPhone);
        merchant.setDescription(description);
        merchant.setBusinessHours(businessHours);
        if (StringUtils.hasText(application.getCoverUrl())) {
            merchant.setCoverUrls(merchantConverter.serializeCoverUrls(
                    List.of(application.getCoverUrl())));
        }
        merchantRepository.save(merchant);

        if (StringUtils.hasText(merchant.getPlatformMerchantId())) {
            platformMerchantRepository.findById(merchant.getPlatformMerchantId()).ifPresent(pm -> {
                pm.setName(shopName);
                pm.setContactPhone(contactPhone);
                pm.setDescription(description);
                pm.setBusinessHours(businessHours);
                if (StringUtils.hasText(application.getCoverUrl())) {
                    pm.setCoverUrl(application.getCoverUrl());
                }
                platformMerchantRepository.save(pm);
            });
        }
        log.info("业主小店店铺资料已同步: merchantId={}, shopName={}", application.getMerchantId(), shopName);
    }

    /** 店铺联系电话：未设置时回退店主手机号（与普通商家口径一致） */
    private String resolveContactPhone(ResidentMerchantApplication application, Resident resident) {
        if (StringUtils.hasText(application.getContactPhone())) {
            return application.getContactPhone().trim();
        }
        return resident.getPhone();
    }

    private String resolveShopName(ResidentMerchantApplication application, Resident resident) {
        if (StringUtils.hasText(application.getShopName())) {
            return application.getShopName().trim();
        }
        String base = StringUtils.hasText(resident.getName()) ? resident.getName() : resident.getPhone();
        return (base == null ? "业主" : base) + "的小店";
    }
}
