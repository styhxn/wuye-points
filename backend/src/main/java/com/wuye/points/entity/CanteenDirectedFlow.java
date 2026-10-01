package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区食堂-定向余额消费/退款流水（id 前缀 cdf_）
 * flow_type: consume_deduct 消费扣减 / refund_back 退款退回
 * amount 恒为正数，方向由 flow_type 表达；balance_after 为变动后余额快照。
 */
@Data
@Entity
@Table(name = "canteen_directed_flows")
@EqualsAndHashCode(callSuper = true)
public class CanteenDirectedFlow extends BaseEntity {

    public static final String FLOW_CONSUME_DEDUCT = "consume_deduct";
    public static final String FLOW_REFUND_BACK = "refund_back";

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 用户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 所属主商家体系 */
    @Column(name = "main_merchant_id", length = 32, nullable = false)
    private String mainMerchantId;

    /** 实际消费商家（主商家或子商家） */
    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    /** 关联订单ID */
    @Column(name = "order_id", length = 32)
    private String orderId;

    /** consume_deduct 消费扣减 / refund_back 退款退回 */
    @Column(name = "flow_type", length = 20, nullable = false)
    private String flowType;

    /** 发生金额（正数） */
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 变动后定向余额快照 */
    @Column(name = "balance_after", precision = 12, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
}
