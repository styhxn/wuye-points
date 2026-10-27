package com.wuye.points.repository;

import com.wuye.points.entity.MerchantAdQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MerchantAdQuotaRepository extends JpaRepository<MerchantAdQuota, String> {

    Optional<MerchantAdQuota> findByMerchantIdAndWeekStartDate(String merchantId, LocalDate weekStartDate);

    /** 商家累计已用免费额度（跨周汇总，用于「免费额度永久仅一次」判定） */
    @Query("SELECT COALESCE(SUM(q.freeUsedCount), 0) FROM MerchantAdQuota q WHERE q.merchantId = :merchantId")
    long sumFreeUsedCountByMerchantId(@Param("merchantId") String merchantId);
}
