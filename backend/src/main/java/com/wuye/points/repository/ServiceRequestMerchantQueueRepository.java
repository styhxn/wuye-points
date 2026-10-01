package com.wuye.points.repository;

import com.wuye.points.entity.ServiceRequestMerchantQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestMerchantQueueRepository extends JpaRepository<ServiceRequestMerchantQueue, String> {

    Page<ServiceRequestMerchantQueue> findByMerchantIdAndStatusOrderByNotifiedAtDesc(
            String merchantId, String status, Pageable pageable);

    List<ServiceRequestMerchantQueue> findByRequestIdOrderByQueueOrderAsc(String requestId);

    List<ServiceRequestMerchantQueue> findByStatusAndTimeoutAtBefore(String status, LocalDateTime timeoutAt);

    Optional<ServiceRequestMerchantQueue> findByRequestIdAndMerchantId(String requestId, String merchantId);
}
