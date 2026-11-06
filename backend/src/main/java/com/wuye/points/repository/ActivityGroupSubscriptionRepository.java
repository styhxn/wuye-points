package com.wuye.points.repository;

import com.wuye.points.entity.ActivityGroupSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 活动组订阅 Repository
 */
@Repository
public interface ActivityGroupSubscriptionRepository extends JpaRepository<ActivityGroupSubscription, String> {

    List<ActivityGroupSubscription> findByGroupId(String groupId);

    List<ActivityGroupSubscription> findByGroupIdAndResidentIdAndStatus(
            String groupId, String residentId, String status);

    void deleteByGroupId(String groupId);

    Page<ActivityGroupSubscription> findByResidentId(String residentId, Pageable pageable);

    Page<ActivityGroupSubscription> findByResidentIdAndStatus(
            String residentId, String status, Pageable pageable);

    boolean existsByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
            String groupId, String residentId, String status, LocalDate date);

    Optional<ActivityGroupSubscription> findFirstByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
            String groupId, String residentId, String status, LocalDate date);

    List<ActivityGroupSubscription> findByEndDateBetweenAndStatus(LocalDate start, LocalDate end, String status);

    /** 到期兜底：与 {@code ServiceSubscriptionRepository.expireOverdue} 同理，避免 active 行长期堆积。 */
    @Modifying
    @Query("update ActivityGroupSubscription s set s.status = 'expired' "
            + "where s.status = 'active' and s.endDate is not null and s.endDate < :today")
    int expireOverdue(@Param("today") LocalDate today);
}
