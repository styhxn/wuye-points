package com.wuye.points.repository;

import com.wuye.points.entity.ServiceSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 服务订阅 Repository
 * 注：resident_id / service_id 已隐含 propertyCompanyId
 */
@Repository
public interface ServiceSubscriptionRepository extends JpaRepository<ServiceSubscription, String> {

    Page<ServiceSubscription> findByResidentId(String residentId, Pageable pageable);

    Page<ServiceSubscription> findByResidentIdAndStatus(String residentId, String status, Pageable pageable);

    boolean existsByServiceIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
            String serviceId, String residentId, String status, LocalDate date);

    List<ServiceSubscription> findByEndDateBetweenAndStatus(LocalDate start, LocalDate end, String status);

    List<ServiceSubscription> findAllByServiceIdAndResidentIdAndStatus(
            String serviceId, String residentId, String status);

    /**
     * 到期兜底：把已过 end_date 的 active 订阅批量置 expired。
     * 订阅幂等只看 end_date &gt;= 今天，缺这一步会让 active 行越积越多，
     * 取消订阅时同一 (service, resident) 命中多行。
     */
    @Modifying
    @Query("update ServiceSubscription s set s.status = 'expired' "
            + "where s.status = 'active' and s.endDate is not null and s.endDate < :today")
    int expireOverdue(@Param("today") LocalDate today);
}
