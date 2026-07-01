package com.wuye.points.repository;

import com.wuye.points.entity.ActivityGroupSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 活动组订阅 Repository
 */
@Repository
public interface ActivityGroupSubscriptionRepository extends JpaRepository<ActivityGroupSubscription, String> {

    Page<ActivityGroupSubscription> findByResidentId(String residentId, Pageable pageable);

    Page<ActivityGroupSubscription> findByResidentIdAndStatus(
            String residentId, String status, Pageable pageable);

    boolean existsByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
            String groupId, String residentId, String status, LocalDate date);

    Optional<ActivityGroupSubscription> findFirstByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
            String groupId, String residentId, String status, LocalDate date);
}
