package com.wuye.points.repository;

import com.wuye.points.entity.ProjectLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 项目负责人 Repository（id 前缀 pl_）
 */
@Repository
public interface ProjectLeaderRepository extends JpaRepository<ProjectLeader, String>,
        JpaSpecificationExecutor<ProjectLeader> {

    List<ProjectLeader> findByRegionalLeaderId(String regionalLeaderId);

    List<ProjectLeader> findByPropertyCompanyId(String propertyCompanyId);

    Optional<ProjectLeader> findByResidentId(String residentId);

    Optional<ProjectLeader> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countByRegionalLeaderIdAndStatus(String regionalLeaderId, String status);
}
