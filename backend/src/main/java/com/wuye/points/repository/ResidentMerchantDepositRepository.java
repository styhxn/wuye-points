package com.wuye.points.repository;

import com.wuye.points.entity.ResidentMerchantDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ResidentMerchantDepositRepository extends JpaRepository<ResidentMerchantDeposit, String>,
        JpaSpecificationExecutor<ResidentMerchantDeposit> {

    Optional<ResidentMerchantDeposit> findByApplicationId(String applicationId);

    Optional<ResidentMerchantDeposit> findFirstByResidentIdOrderByCreatedAtDesc(String residentId);

    Page<ResidentMerchantDeposit> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ResidentMerchantDeposit> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
