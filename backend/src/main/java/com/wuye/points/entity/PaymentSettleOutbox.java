package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 支付回调业务 finalize 发件箱（Transactional Outbox）。
 *
 * <p>回调只做“标流水 paid + 写本条记录”，随后由 {@code PaymentSettleService} 异步完成真正的业务
 * （冻结分账、完成订单、返积分/物业币等）。本表保证：即使 JVM 崩溃或异步线程池拒绝，事件也不丢失，
 * 由 {@code PaymentSettleScheduler} 兜底扫描重试。</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_settle_outbox")
public class PaymentSettleOutbox extends BaseEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_FAILED_PERMANENT = "FAILED_PERMANENT";

    private static final int MAX_ERROR_LEN = 2000;

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "record_id", length = 32, nullable = false)
    private String recordId;

    @Column(name = "biz_type", length = 32, nullable = false)
    private String bizType;

    @Column(name = "biz_id", length = 32)
    private String bizId;

    @Column(name = "status", length = 20, nullable = false)
    private String status = STATUS_PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Version
    private Long version;

    public static PaymentSettleOutbox pending(PaymentRecord record, String id) {
        PaymentSettleOutbox o = new PaymentSettleOutbox();
        o.id = id;
        o.recordId = record.getId();
        o.bizType = record.getBizType();
        o.bizId = record.getBizId();
        o.status = STATUS_PENDING;
        return o;
    }

    /** 是否可被事件/调度器处理：处于待处理或失败待重试，且已过退避期。 */
    public boolean isDue(LocalDateTime now) {
        if (STATUS_PENDING.equals(status) || STATUS_FAILED.equals(status)) {
            return nextRetryAt == null || !nextRetryAt.isAfter(now);
        }
        return false;
    }

    public void markProcessing() {
        this.status = STATUS_PROCESSING;
    }

    public void markDone() {
        this.status = STATUS_DONE;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    /** 回收卡死过久的 PROCESSING（JVM 在 markProcessing 后、markDone 前崩溃时遗留），重置回 PENDING 重新入队。 */
    public void resetToPending() {
        this.status = STATUS_PENDING;
        this.nextRetryAt = null;
    }

    /**
     * 标记失败并登记重试信息。
     *
     * @param error    异常信息（自动截断）
     * @param nextRetryAt 下次重试时间（为空表示达到最大重试，转入 FAILED_PERMANENT）
     * @param maxRetry 最大重试次数
     */
    public void markFailed(String error, LocalDateTime nextRetryAt, int maxRetry) {
        this.retryCount++;
        this.lastError = truncate(error, MAX_ERROR_LEN);
        if (this.retryCount >= maxRetry) {
            this.status = STATUS_FAILED_PERMANENT;
            this.nextRetryAt = null;
        } else {
            this.status = STATUS_FAILED;
            this.nextRetryAt = nextRetryAt;
        }
    }

    private static String truncate(String s, int n) {
        if (s == null) {
            return null;
        }
        return s.length() <= n ? s : s.substring(0, n);
    }
}
