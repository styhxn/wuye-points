package com.wuye.points.repository;

import com.wuye.points.entity.MerchantAdQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MerchantAdQuotaRepository extends JpaRepository<MerchantAdQuota, String> {

    Optional<MerchantAdQuota> findByMerchantIdAndWeekStartDate(String merchantId, LocalDate weekStartDate);
}
