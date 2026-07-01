package com.wuye.points.repository;

import com.wuye.points.entity.SectorLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 板块负责人 Repository
 * 注：coordinator_id 已隐含 propertyCompanyId
 */
@Repository
public interface SectorLeaderRepository extends JpaRepository<SectorLeader, String>,
        JpaSpecificationExecutor<SectorLeader> {

    List<SectorLeader> findByCoordinatorId(String coordinatorId);

    Optional<SectorLeader> findByCoordinatorIdAndSector(String coordinatorId, String sector);

    Optional<SectorLeader> findFirstByPropertyCompanyIdAndSectorAndCoordinatorIdAndStatus(
            String propertyCompanyId, String sector, String coordinatorId, String status);

    Optional<SectorLeader> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    Optional<SectorLeader> findByResidentIdAndPropertyCompanyIdAndStatus(
            String residentId, String propertyCompanyId, String status);

    Optional<SectorLeader> findByResidentIdAndPropertyCompanyIdAndSector(
            String residentId, String propertyCompanyId, String sector);

    long countByCoordinatorIdAndStatus(String coordinatorId, String status);

    long countByPropertyCompanyIdAndCoordinatorIdAndStatus(
            String propertyCompanyId, String coordinatorId, String status);
}
