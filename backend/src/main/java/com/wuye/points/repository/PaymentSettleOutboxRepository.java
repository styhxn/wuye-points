package com.wuye.points.repository;

import com.wuye.points.entity.PaymentSettleOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentSettleOutboxRepository extends JpaRepository<PaymentSettleOutbox, String> {

    Optional<PaymentSettleOutbox> findByRecordId(String recordId);

    /**
     * 扫描可处理的 outbox：状态为 PENDING/FAILED 且已过退避期（nextRetryAt 为空或 <= now）。
     */
    @Query("SELECT o FROM PaymentSettleOutbox o WHERE o.status IN :statuses "
            + "AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) ORDER BY o.createdAt ASC")
    List<PaymentSettleOutbox> findDue(@Param("statuses") List<String> statuses, @Param("now") LocalDateTime now);

    /**
     * 回收卡死过久的 PROCESSING 记录：JVM 在 finalize 中途崩溃（markProcessing 已落库、markDone 未落库）时，
     * 该状态不被 {@link #findDue} 覆盖，需按更新时间超时回收重新入队（业务 complete* 均幂等，可安全重放）。
     */
    @Query("SELECT o FROM PaymentSettleOutbox o WHERE o.status = :status "
            + "AND o.updatedAt < :cutoff ORDER BY o.updatedAt ASC")
    List<PaymentSettleOutbox> findStuckProcessing(@Param("status") String status, @Param("cutoff") LocalDateTime cutoff);

    /**
     * 原子回收卡死 PROCESSING：仅在「仍为 PROCESSING 且确实超时」时重置回 PENDING，
     * 并同步推进乐观锁版本（version = version + 1），使仍持有旧快照的并发线程后续写入必然失败，
     * 从而把抢占语义从「读-改-写 + 冲突重试」收敛为单条条件 UPDATE。
     *
     * @return 受影响行数：1=本线程抢到回收权；0=已被并发回收/状态已变更，应静默跳过
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE payment_settle_outbox SET status = :pending, next_retry_at = NULL, "
            + "updated_at = :now, version = version + 1 "
            + "WHERE id = :id AND status = :processing AND updated_at < :cutoff", nativeQuery = true)
    int reclaimIfStuck(@Param("id") String id,
                       @Param("processing") String processing,
                       @Param("pending") String pending,
                       @Param("cutoff") LocalDateTime cutoff,
                       @Param("now") LocalDateTime now);
}
