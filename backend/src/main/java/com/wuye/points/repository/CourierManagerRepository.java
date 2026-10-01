package com.wuye.points.repository;

import com.wuye.points.entity.CourierManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 快递负责人 Repository
 */
@Repository
public interface CourierManagerRepository extends JpaRepository<CourierManager, String>,
        JpaSpecificationExecutor<CourierManager> {

    Optional<CourierManager> findByResidentIdAndPropertyCompanyId(String residentId, String propertyCompanyId);

    List<CourierManager> findByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);

    List<CourierManager> findByPropertyCompanyIdAndCommunityIdAndStatus(
            String propertyCompanyId, String communityId, String status);

    Optional<CourierManager> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    Optional<CourierManager> findByResidentIdAndPropertyCompanyIdAndStatus(
            String residentId, String propertyCompanyId, String status);

    long countByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);
}
