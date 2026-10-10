package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PaymentSettleOutbox;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PaymentSettleOutboxRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 支付回调统一收口（微信支付 APIv3）。
 *
 * <p><b>设计：回调只做“快路径”</b>——验签 + 解密 + 标流水 paid + 落库 outbox + 立即应答 {@code SUCCESS}；
 * 业务 finalize（冻结分账、完成订单、返积分/物业币、建配送单、算分账等）由 {@link PaymentSettleService}
 * 异步处理。这样保证在微信要求的 <b>5 秒</b>内必须应答，避免重推风暴与“钱已扣但订单仍显示待支付”的时序问题。</p>
 *
 * <p>幂等：流水已 paid 或 outbox 已存在时直接应答 SUCCESS；异常回滚并返回 FAIL，由微信按策略重推。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompletionService {

    /** 回调金额与预下单金额不一致时的终态：不入账、不自动关单，留待人工对账（不会被 pending 扫描误收尾） */
    private static final String STATUS_AMOUNT_MISMATCH = "amount_mismatch";

    private final PayChannel payChannel;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentSettleOutboxRepository outboxRepository;
    private final SnowflakeIdGenerator snowflake;
    private final ApplicationEventPublisher eventPublisher;
    private final WechatNotifyService wechatNotifyService;
    private final ObjectMapper objectMapper;

    /**
     * 微信支付结果通知（APIv3）：验签 + 解密 resource + 标记已支付 + 触发 finalize。
     *
     * @return 返给微信的应答体，调用方直接 HTTP 200 返回
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, String> handleWechatNotify(String rawBody, HttpHeaders headers) {
        try {
            Map<String, Object> decoded = wechatNotifyService.decrypt(rawBody, headers);

            String outTradeNo = (String) decoded.get("out_trade_no");
            String transactionId = (String) decoded.get("transaction_id");
            String tradeState = (String) decoded.get("trade_state");
            if (!"SUCCESS".equals(tradeState)) {
                log.info("微信支付未成功 trade_state={} outTradeNo={}", tradeState, outTradeNo);
                return Map.of("code", "SUCCESS", "message", "已接收");
            }

            PaymentRecord record = paymentRecordRepository.findByOutTradeNo(outTradeNo).orElse(null);
            if (record == null) {
                log.warn("微信回调无对应支付流水: outTradeNo={}", outTradeNo);
                return Map.of("code", "SUCCESS", "message", "无对应流水");
            }
            if (isAlreadyPaid(record)) {
                return Map.of("code", "SUCCESS", "message", "重复通知");
            }
            // 金额校验（官方规范要求）：回调 amount.total 必须与本地预下单金额（cashAmount）一致。
            // 不一致说明数据异常或串单，一律不入账（置 amount_mismatch 待人工对账，ERROR 日志告警），
            // 避免「按错误金额发货、少收/多收却照常履约」。应答 SUCCESS 停止重推——重推不会改变结果。
            Integer paidFen = extractPaidFen(decoded);
            if (!isAmountMatched(paidFen, record)) {
                String msg = "支付金额与预下单金额不一致（微信total=" + paidFen + "分，本地cash="
                        + record.getCashAmount() + "元），已拦截入账待人工对账";
                record.setStatus(STATUS_AMOUNT_MISMATCH);
                record.setErrorMsg(msg);
                record.setChannelTradeNo(transactionId);
                paymentRecordRepository.save(record);
                log.error("微信回调金额校验失败 recordId={} outTradeNo={} {}",
                        record.getId(), outTradeNo, msg);
                return Map.of("code", "SUCCESS", "message", "金额校验失败，待人工对账");
            }
            // 竞态防御：流水本地已关单（PendingPaymentScheduler 30min 收尾）但微信实际支付成功
            // （30~120min 窗口内用户完成支付）。订单已取消，不能置 paid 走 finalize（无人履约），
            // 应立即原路退款，避免「钱已扣、单已取消、资金滞留」。
            if ("closed".equals(record.getStatus())) {
                handleClosedButPaid(record);
                return Map.of("code", "SUCCESS", "message", "已关单，触发原路退款");
            }
            payChannel.markPaidFromQuery(record); // 仅置 PAID；channelTradeNo 由 query 路径补，见下行
            record.setChannelTradeNo(transactionId);
            paymentRecordRepository.save(record);
            settle(record);
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (WechatNotifyVerifyException e) {
            log.warn("微信支付回调验签/解密失败: {}", e.getMessage());
            return Map.of("code", "FAIL", "message", e.getMessage());
        } catch (Exception e) {
            log.error("微信支付回调处理异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    private boolean isAlreadyPaid(PaymentRecord record) {
        return "paid".equalsIgnoreCase(record.getStatus()) || "PAID".equals(record.getStatus());
    }

    /** 从回调明文中取微信实收金额（amount.total，单位分）；缺失或格式异常返回 null。 */
    private Integer extractPaidFen(Map<String, Object> decoded) {
        Object amountObj = decoded.get("amount");
        if (!(amountObj instanceof Map<?, ?> amount)) {
            return null;
        }
        Object total = amount.get("total");
        if (total instanceof Number n) {
            return n.intValue();
        }
        try {
            return total == null ? null : Integer.valueOf(String.valueOf(total));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 校验微信实收金额（分）与本地预下单金额（cashAmount 元）一致；金额缺失视为校验不通过（fail-closed）。 */
    private boolean isAmountMatched(Integer paidFen, PaymentRecord record) {
        if (paidFen == null) {
            return false;
        }
        BigDecimal cash = record.getCashAmount() != null ? record.getCashAmount() : BigDecimal.ZERO;
        int expectedFen = cash.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        return paidFen == expectedFen;
    }

    /**
     * 本地已关单的流水收到支付成功回调：订单已取消，资金必须原路退回。
     * 退款申请成功置 refunding（RefundQueryScheduler 轮询落 refunded/refund_failed）；
     * 失败置 refund_failed 待人工对账，绝不静默吞掉资金。
     */
    private void handleClosedButPaid(PaymentRecord record) {
        BigDecimal cash = record.getCashAmount() != null ? record.getCashAmount() : BigDecimal.ZERO;
        if (cash.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("已关单流水回调到账但无现金部分，无需退款 recordId={}", record.getId());
            return;
        }
        try {
            String outRefundNo = payChannel.refund(record, cash, "支付超时关单后到账，系统自动原路退款");
            record.setOutRefundNo(outRefundNo);
            record.setStatus("refunding");
            paymentRecordRepository.save(record);
            log.warn("已关单流水回调到账，已自动发起原路退款 recordId={} cash={} outRefundNo={}",
                    record.getId(), cash, outRefundNo);
        } catch (Exception e) {
            record.setStatus("refund_failed");
            record.setErrorMsg("关单后到账自动退款失败，需人工处理: " + e.getMessage());
            paymentRecordRepository.save(record);
            log.error("已关单流水回调到账，自动退款失败，需人工对账 recordId={}", record.getId(), e);
        }
    }

    private void settle(PaymentRecord record) {
        if (outboxRepository.findByRecordId(record.getId()).isEmpty()) {
            outboxRepository.save(PaymentSettleOutbox.pending(record, snowflake.generateId("pso_")));
        }
        eventPublisher.publishEvent(new PaymentSettledEvent(record.getId()));
    }
}
