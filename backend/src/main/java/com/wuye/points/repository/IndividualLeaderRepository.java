package com.wuye.points.repository;

import com.wuye.points.entity.IndividualLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 个体负责人 Repository
 * 注：sector_leader_id 已隐含 propertyCompanyId
 */
@Repository
public interface IndividualLeaderRepository extends JpaRepository<IndividualLeader, String>,
        JpaSpecificationExecutor<IndividualLeader> {

    List<IndividualLeader> findBySectorLeaderId(String sectorLeaderId);

    Optional<IndividualLeader> findFirstBySectorLeaderIdAndSectorAndStatus(
            String sectorLeaderId, String sector, String status);

    Optional<IndividualLeader> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countBySectorLeaderIdAndStatus(String sectorLeaderId, String status);

    Optional<IndividualLeader> findByResidentIdAndPropertyCompanyIdAndSectorLeaderId(
            String residentId, String propertyCompanyId, String sectorLeaderId);
}
