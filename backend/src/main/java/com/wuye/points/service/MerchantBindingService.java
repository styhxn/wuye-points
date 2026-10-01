package com.wuye.points.service;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PlatformMerchant;
import com.wuye.points.entity.Resident;
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
import java.util.Optional;

/**
 * 组长/技工自动绑定商家店铺。
 * <p>
 * 规则：
 * <ul>
 *   <li>组长（activity_leader）自动绑定 source=group_leader 的商家，用于在 /products 上架真实商品</li>
 *   <li>技工（technician）自动绑定 source=technician 的商家，用于社区服务收入提现</li>
 *   <li>同一住户在同一物业下只绑定一家同来源商家</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantBindingService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");

    private final MerchantRepository merchantRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ResidentRepository residentRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public Merchant ensureGroupLeaderMerchant(String residentId) {
        return ensureMerchant(residentId, "group_leader");
    }

    @Transactional
    public Merchant ensureTechnicianMerchant(String residentId) {
        return ensureMerchant(residentId, "technician");
    }

    @Transactional
    public Merchant ensureMerchant(String residentId, String merchantSource) {
        if (!StringUtils.hasText(residentId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户ID不能为空");
        }
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "住户未绑定物业公司");
        }

        Optional<Merchant> existing = merchantRepository
                .findByContactPhoneAndPropertyCompanyIdAndMerchantSource(
                        resident.getPhone(), resident.getPropertyCompanyId(), merchantSource);
        if (existing.isPresent()) {
            return existing.get();
        }

        PlatformMerchant platformMerchant = new PlatformMerchant();
        platformMerchant.setId(idGenerator.generateId("pm"));
        platformMerchant.setName(buildMerchantName(resident, merchantSource));
        platformMerchant.setContactPhone(resident.getPhone());
        platformMerchant.setStatus("active");
        platformMerchantRepository.save(platformMerchant);

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(platformMerchant.getId());
        merchant.setPropertyCompanyId(resident.getPropertyCompanyId());
        merchant.setName(platformMerchant.getName());
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(0);
        merchant.setCategory(resolveCategory(merchantSource));
        merchant.setAuditStatus("approved");
        merchant.setRankOrder(0);
        merchant.setContactPhone(resident.getPhone());
        merchant.setResidentId(resident.getId());
        merchant.setDeliveryFee(BigDecimal.ZERO);
        merchant.setDeliveryScope("both");
        merchant.setStatus("active");
        merchant.setServeAllCommunities(true);
        merchant.setMerchantSource(merchantSource);
        merchantRepository.save(merchant);
        log.info("自动创建商家店铺: merchantId={}, merchantSource={}, residentId={}, propertyCompanyId={}",
                merchant.getId(), merchantSource, resident.getId(), resident.getPropertyCompanyId());

        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setId(idGenerator.generateId("mpc"));
        config.setMerchantId(merchant.getId());
        config.setPropertyCompanyId(merchant.getPropertyCompanyId());
        config.setCommissionRate(DEFAULT_COMMISSION_RATE);
        config.setPointExchangeRate(DEFAULT_POINT_EXCHANGE_RATE);
        config.setCoinRebateRate(BigDecimal.ZERO);
        config.setCoinRebateEnabled(false);
        config.setStatus("active");
        merchantPropertyConfigRepository.save(config);

        return merchant;
    }

    private String buildMerchantName(Resident resident, String merchantSource) {
        String base = StringUtils.hasText(resident.getName()) ? resident.getName() : resident.getPhone();
        return switch (merchantSource) {
            case "group_leader" -> base + "的小店";
            case "technician" -> base + "的服务";
            default -> base + "的店铺";
        };
    }

    private String resolveCategory(String merchantSource) {
        return switch (merchantSource) {
            case "group_leader" -> "活动组";
            case "technician" -> "社区服务";
            default -> "其他";
        };
    }
}
