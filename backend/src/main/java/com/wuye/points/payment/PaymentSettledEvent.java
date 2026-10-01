package com.wuye.points.payment;

import org.springframework.context.ApplicationEvent;

/**
 * 支付流水已标 paid，可异步 finalize 业务的事件。
 * 由 {@code PaymentCompletionService} 在事务提交后发布，{@code PaymentSettleService} 监听并处理。
 */
public class PaymentSettledEvent extends ApplicationEvent {

    private final String recordId;

    public PaymentSettledEvent(String recordId) {
        super(recordId);
        this.recordId = recordId;
    }

    public String getRecordId() {
        return recordId;
    }
}
