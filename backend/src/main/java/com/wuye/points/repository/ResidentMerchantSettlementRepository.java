package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ResidentMerchantSettlementRepository extends JpaRepository<ResidentMerchantSettlement, String> {

    Page<ResidentMerchantSettlement> findByResidentIdOrderByCreatedAtDesc(String residentId, Pageable pageable);

    boolean existsByOrderId(String orderId);

    Page<ResidentMerchantSettlement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ResidentMerchantSettlement> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}
