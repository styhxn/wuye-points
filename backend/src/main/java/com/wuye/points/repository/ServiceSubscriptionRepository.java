package com.wuye.points.repository;

import com.wuye.points.entity.ServiceSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
