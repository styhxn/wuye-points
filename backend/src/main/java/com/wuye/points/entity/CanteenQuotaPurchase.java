package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社区食堂-商家额度购买流水（id 前缀 cqp_）
 * 主商家购买额度 → 微信支付给平台 → 回调自动到账；手续费内扣。
 * 每笔已到账记录维护 remaining_quota（剩余可充值额度），定向充值时按时间先后扣减。
 */
@Data
@Entity
@Table(name = "canteen_quota_purchases")
@EqualsAndHashCode(callSuper = true)
public class CanteenQuotaPurchase extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 购买商家（必须为主商家） */
    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    /** 所属物业公司 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 申请购买额度金额（= 微信支付金额） */
    @Column(name = "quota_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal quotaAmount;

    /** 手续费比例快照（0.02=2%） */
    @Column(name = "fee_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal feeRate;

    /** 手续费金额 = quota_amount × fee_rate */
    @Column(name = "fee_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal feeAmount;

    /** 实际到账额度 = quota_amount - fee_amount */
    @Column(name = "actual_quota", precision = 12, scale = 2, nullable = false)
    private BigDecimal actualQuota;

    /** 微信下单支付金额 */
    @Column(name = "pay_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal payAmount;

    /** 关联支付流水ID（payment_records.id） */
    @Column(name = "payment_record_id", length = 32)
    private String paymentRecordId;

    /** pending 待支付 / paid 已到账 / refunded 已退款 / closed 已关闭 */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** 剩余可用额度（到账后初始=actual_quota，充值后递减） */
    @Column(name = "remaining_quota", precision = 12, scale = 2, nullable = false)
    private BigDecimal remainingQuota;

    /** 支付完成/到账时间 */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
