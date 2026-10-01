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
 * 支付流水表 pr_xxxxxxxx
 *
 * <p>微信（微信）真实支付的两段式中间态：
 * 业务侧（订单/物业费/充值等）下单时先落一条 pending 流水并调微信预下单，
 * 微信支付成功回调（4.10）后由 PaymentCompletionService 幂等完成业务。
 * 记录点/币/现金拆分快照，供回调确认时还原支付明细。
 */
@Data
@Entity
@Table(name = "payment_records")
@EqualsAndHashCode(callSuper = true)
public class PaymentRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 业务类型：order / property_fee / coin_recharge / service_order / service_subscription / activity_subscription */
    @Column(name = "biz_type", length = 32, nullable = false)
    private String bizType;

    /** 业务单ID（订单ID/物业费ID/订阅ID等） */
    @Column(name = "biz_id", length = 32)
    private String bizId;

    @Column(name = "resident_id", length = 32)
    private String residentId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    /** 支付方式：wechat / mixed */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    /** 应付总额（元） */
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    /** 积分抵扣（快照） */
    @Column(name = "point_used")
    private Integer pointUsed;

    /** 物业币抵扣（快照） */
    @Column(name = "coin_used", precision = 10, scale = 2)
    private BigDecimal coinUsed;

    /** 社区食堂定向余额抵扣（快照，回调还原用） */
    @Column(name = "directed_balance_used", precision = 10, scale = 2)
    private BigDecimal directedBalanceUsed;

    /** 现金部分（元）＝微信下单金额 */
    @Column(name = "cash_amount", precision = 10, scale = 2)
    private BigDecimal cashAmount;

    /** pending 待支付 / paid 已支付 / closed 已关闭 / refunding 退款中 / refunded 已退款 / failed 失败 */
    @Column(name = "status", length = 20)
    private String status;

    /** 微信订单号 out_trade_no（预下单返回） */
    @Column(name = "out_trade_no", length = 64)
    private String outTradeNo;

    /** 终端流水号 terminal_trace（默认取流水ID，作为查询/退款凭据） */
    @Column(name = "trace_no", length = 64)
    private String traceNo;

    /** 微信订单号（回调返回，建议入库） */
    @Column(name = "channel_trade_no", length = 64)
    private String channelTradeNo;

    /** 收单通道侧订单号 */
    @Column(name = "channel_order_no", length = 64)
    private String channelOrderNo;

    /** 该笔支付实际入账的微信二级商户号（电商收付通模式，按订单商家路由） */
    @Column(name = "sub_mchid", length = 32)
    private String subMchid;

    /** 微信退款单号 out_refund_no */
    @Column(name = "out_refund_no", length = 64)
    private String outRefundNo;

    /** 回调原始报文（对账审计用） */
    @Column(name = "notify_body", length = 4000)
    private String notifyBody;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
