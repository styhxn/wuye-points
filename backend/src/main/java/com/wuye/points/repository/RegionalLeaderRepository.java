package com.wuye.points.repository;

import com.wuye.points.entity.RegionalLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 区域负责人 Repository（id 前缀 rl_）
 */
@Repository
public interface RegionalLeaderRepository extends JpaRepository<RegionalLeader, String>,
        JpaSpecificationExecutor<RegionalLeader> {

    List<RegionalLeader> findBySectorLeaderId(String sectorLeaderId);

    List<RegionalLeader> findByCoordinatorId(String coordinatorId);

    List<RegionalLeader> findByPropertyCompanyId(String propertyCompanyId);

    Optional<RegionalLeader> findByResidentId(String residentId);

    Optional<RegionalLeader> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countBySectorLeaderIdAndStatus(String sectorLeaderId, String status);

    long countByCoordinatorIdAndStatus(String coordinatorId, String status);
}
