package com.wuye.points.repository;

import com.wuye.points.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付流水仓库
 */
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {

    Optional<PaymentRecord> findByOutTradeNo(String outTradeNo);

    Optional<PaymentRecord> findByBizId(String bizId);

    List<PaymentRecord> findByBizTypeAndBizIdAndStatus(String bizType, String bizId, String status);

    List<PaymentRecord> findByBizTypeAndResidentIdAndStatus(String bizType, String residentId, String status);

    /** 查询超时未支付流水（供关闭/兜底调度） */
    List<PaymentRecord> findByStatusAndCreatedAtBefore(String status, LocalDateTime before);

    /** 按状态查询流水（供退款 4.9 轮询） */
    List<PaymentRecord> findByStatus(String status);
}
