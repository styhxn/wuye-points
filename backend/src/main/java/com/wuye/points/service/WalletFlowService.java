package com.wuye.points.service;

import com.wuye.points.dto.distribution.CalculationResult;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.WalletFlow;
import com.wuye.points.repository.WalletFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包流水账本服务（规范§7.4 / §9.5 / §10.5）。
 *
 * <p>每次分账结算、配送结算、取消/退款冲正都在同一事务内写入不可变流水：
 * <ul>
 *   <li>分账结算键：{@code distribution:settle:{orderId}:{entryType}:{ownerType}:{ownerId}}（§9.4）</li>
 *   <li>配送结算键：{@code delivery:settle:{deliveryId}}（§9.3）</li>
 *   <li>冲正键：{@code distribution:reverse:{orderId}:{entryType}:{ownerType}:{ownerId}}（§10.5）</li>
 * </ul>
 * 幂等键唯一约束保证重复调用不重复入账；冲正产生负数流水，不删除原流水。</p>
 *
 * <p>余额口径维持实时聚合（与既有体系一致），本表作为官方审计账本（§15.2）。</p>
 *
 * <p><b>事务说明</b>：流水的物理写入由 {@link WalletFlowWriter} 在
 * {@code REQUIRES_NEW} 独立事务中完成，目的是让「并发幂等竞争」不至于把整个分账事务
 * 拖入 rollback-only。业务侧仍应在自己的事务内调用本服务，且尽量放在流程末尾。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletFlowService {

    /** 公司（平台）固定主体 ID（规范§7.4 ownerType=platform） */
    public static final String PLATFORM_OWNER_ID = "platform";

    private static final String BALANCE_WITHDRAWABLE = "withdrawable";

    private final WalletFlowRepository walletFlowRepository;
    private final WalletFlowWriter walletFlowWriter;

    /**
     * 分账结算入账（规范§9.4）：商家商品收入、商家配送补贴扣减、公司/物业/管理盘三级份额、
     * 公司配送收入、成本击穿。与 {@code distribution_records} 同事务写入，仅首次结算落账。
     */
    @Transactional
    public void recordDistribution(CalculationResult r, DistributionRecord record, Order order) {
        String orderId = order.getId();
        String recordId = record.getId();
        String merchantId = record.getMerchantId();

        // 商家：商品收入 + 配送补贴扣减，净额 = merchantShare（规范§4.4）
        flow(recordId, orderId, null, "merchant", merchantId, "merchant_goods_income",
                r.getMerchantGoodsShare(),
                "distribution:settle:" + orderId + ":merchant_goods_income:merchant:" + merchantId, null);
        flow(recordId, orderId, null, "merchant", merchantId, "merchant_delivery_subsidy",
                r.getMerchantDeliverySubsidy().negate(),
                "distribution:settle:" + orderId + ":merchant_delivery_subsidy:merchant:" + merchantId, null);

        // 公司（平台）：商品平台盘份额 + 配送收入（§8.6 两条链路分开记录）
        flow(recordId, orderId, null, "platform", PLATFORM_OWNER_ID, "platform_product_share",
                r.getPlatformShare(),
                "distribution:settle:" + orderId + ":platform_product_share:platform:" + PLATFORM_OWNER_ID, null);
        flow(recordId, orderId, null, "platform", PLATFORM_OWNER_ID, "platform_delivery_share",
                r.getPlatformDeliveryShare(),
                "distribution:settle:" + orderId + ":platform_delivery_share:platform:" + PLATFORM_OWNER_ID, null);

        // 物业：商品平台盘份额；成本击穿按承担方落账（负流水）
        flow(recordId, orderId, null, "property", record.getPropertyCompanyId(), "property_product_share",
                r.getPropertyShare(),
                "distribution:settle:" + orderId + ":property_product_share:property:" + record.getPropertyCompanyId(), null);

        // 物业/公司活动覆盖承担方时，为承担方记录配送补贴成本流水（§2.5 / §5.2）：
        // 商家承担走 merchant_delivery_subsidy，物业/公司承担走 delivery_subsidy_cost，不得“只展示承担方、后端不落账”
        String sponsor = r.getDeliverySubsidySponsor();
        if ("property".equals(sponsor) && isPositive(r.getDeliverySubsidyAmount())) {
            flow(recordId, orderId, null, "property", record.getPropertyCompanyId(), "delivery_subsidy_cost",
                    r.getDeliverySubsidyAmount().negate(),
                    "distribution:settle:" + orderId + ":delivery_subsidy_cost:property:" + record.getPropertyCompanyId(), null);
        } else if ("platform".equals(sponsor) && isPositive(r.getDeliverySubsidyAmount())) {
            flow(recordId, orderId, null, "platform", PLATFORM_OWNER_ID, "delivery_subsidy_cost",
                    r.getDeliverySubsidyAmount().negate(),
                    "distribution:settle:" + orderId + ":delivery_subsidy_cost:platform:" + PLATFORM_OWNER_ID, null);
        }
        if (isPositive(r.getDeficitAmount())) {
            String sponsorType = deficitSponsorType(r.getDeficitSponsor());
            String sponsorId = "platform".equals(sponsorType)
                    ? PLATFORM_OWNER_ID : record.getPropertyCompanyId();
            flow(recordId, orderId, null, sponsorType, sponsorId, "deficit_cost",
                    r.getDeficitAmount().negate(),
                    "distribution:settle:" + orderId + ":deficit_cost:" + sponsorType + ":" + sponsorId, null);
        }

        // 管理盘三级（§4.3）
        flow(recordId, orderId, null, "coordinator", record.getCoordinatorId(), "coordinator_share",
                r.getCoordinatorShare(),
                "distribution:settle:" + orderId + ":coordinator_share:coordinator:" + record.getCoordinatorId(), null);
        flow(recordId, orderId, null, "sector_leader", record.getSectorLeaderId(), "sector_leader_share",
                r.getSectorLeaderShare(),
                "distribution:settle:" + orderId + ":sector_leader_share:sector_leader:" + record.getSectorLeaderId(), null);
        flow(recordId, orderId, null, "individual_leader", record.getIndividualLeaderId(), "individual_leader_share",
                r.getIndividualLeaderShare(),
                "distribution:settle:" + orderId + ":individual_leader_share:individual_leader:" + record.getIndividualLeaderId(), null);
    }

    /**
     * 取消/退款冲正（规范§10.5）：对原分账流水逐条生成负数冲正流水，不删除原流水。
     * 由 {@code reverseForCancelledOrder} 调用；冲正流水键带 entryType 保证每主体多条流水不冲突。
     */
    @Transactional
    public void recordReversal(String orderId, DistributionRecord originalRecord) {
        List<WalletFlow> originals = walletFlowRepository.findByDistributionRecordId(originalRecord.getId());
        for (WalletFlow original : originals) {
            flow(originalRecord.getId(), orderId, original.getDeliveryId(),
                    original.getOwnerType(), original.getOwnerId(), "distribution_reversal",
                    original.getAmount().negate(),
                    "distribution:reverse:" + orderId + ":" + original.getEntryType()
                            + ":" + original.getOwnerType() + ":" + original.getOwnerId(),
                    original.getId());
        }
    }

    /**
     * 配送结算入账（规范§9.3）：配送员收入写入配送员流水。
     * 配送员可提现以配送单状态实时聚合，本流水作为配送员收入审计账本，幂等键 {@code delivery:settle:{deliveryId}}。
     */
    @Transactional
    public void recordCourierDelivery(Delivery delivery) {
        if (delivery == null || !StringUtils.hasText(delivery.getCourierId())) {
            return;
        }
        flow(null, delivery.getOrderId(), delivery.getId(), "courier", delivery.getCourierId(),
                "courier_delivery_income", delivery.getCourierEarning(),
                "delivery:settle:" + delivery.getId(), null);
    }

    /**
     * 商家自配结算入账：商家配送费分成写入商家流水。
     * 幂等键 {@code delivery:merchant:settle:{deliveryId}}（§9.5）。
     */
    @Transactional
    public void recordMerchantDelivery(Delivery delivery, String merchantId) {
        if (delivery == null || !StringUtils.hasText(merchantId)) {
            return;
        }
        BigDecimal amount = delivery.getMerchantDeliveryFeeShare();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        flow(null, delivery.getOrderId(), delivery.getId(), "merchant", merchantId,
                "merchant_delivery_fee_share", amount,
                "delivery:merchant:settle:" + delivery.getId(), null);
    }

    /**
     * 单条流水写入：零金额跳过；幂等键已存在跳过；负数金额用于扣减/冲正。
     *
     * <p>实际写入委托 {@link WalletFlowWriter} 在独立事务中完成，
     * 使并发幂等竞争不影响业务主事务（详见该类注释）。
     */
    private void flow(String distributionRecordId, String orderId, String deliveryId,
                      String ownerType, String ownerId, String entryType,
                      BigDecimal amount, String idempotencyKey, String refRecordId) {
        if (!StringUtils.hasText(ownerId)) {
            return; // 未挂接负责人（无对应角色）时不产生流水
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        if (walletFlowRepository.existsByIdempotencyKey(idempotencyKey)) {
            return; // 幂等（§9.5）
        }
        walletFlowWriter.write(distributionRecordId, orderId, deliveryId, ownerType, ownerId,
                entryType, amount, BALANCE_WITHDRAWABLE, idempotencyKey, refRecordId);
    }

    /**
     * 社区食堂-购买额度手续费落账（平台收入）：ownerType=platform，entryType=canteen_quota_fee。
     * 幂等键 {@code canteen:quota_fee:{purchaseId}}，回调重复时不会重复入账。
     */
    @Transactional
    public void recordCanteenQuotaFee(String merchantId, String purchaseId, BigDecimal feeAmount) {
        flow(null, null, null, "platform", PLATFORM_OWNER_ID, "canteen_quota_fee",
                feeAmount, "canteen:quota_fee:" + purchaseId, null);
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private String deficitSponsorType(String deficitSponsor) {
        return "platform".equals(deficitSponsor) ? "platform" : "property";
    }
}
