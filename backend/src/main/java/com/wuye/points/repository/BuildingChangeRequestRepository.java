package com.wuye.points.repository;

import com.wuye.points.entity.BuildingChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 楼栋变更申请 Repository
 */
@Repository
public interface BuildingChangeRequestRepository extends JpaRepository<BuildingChangeRequest, String>,
        JpaSpecificationExecutor<BuildingChangeRequest> {

    List<BuildingChangeRequest> findByResidentIdOrderByAppliedAtDesc(String residentId);

    Optional<BuildingChangeRequest> findFirstByResidentIdAndStatusInOrderByAppliedAtDesc(
            String residentId, List<String> statuses);
}
