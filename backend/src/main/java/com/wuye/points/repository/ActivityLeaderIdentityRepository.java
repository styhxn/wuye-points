package com.wuye.points.repository;

import com.wuye.points.entity.ActivityLeaderIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityLeaderIdentityRepository
        extends JpaRepository<ActivityLeaderIdentity, String> {

    Optional<ActivityLeaderIdentity> findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId, String status);

    List<ActivityLeaderIdentity> findAllByResidentIdAndPropertyCompanyIdAndStatus(
            String residentId, String propertyCompanyId, String status);

    boolean existsByResidentIdAndPropertyCompanyIdAndStatus(
            String residentId, String propertyCompanyId, String status);
}
