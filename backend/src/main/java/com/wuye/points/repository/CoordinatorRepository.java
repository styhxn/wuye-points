package com.wuye.points.repository;

import com.wuye.points.entity.Coordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 统筹 Repository
 */
@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, String>,
        JpaSpecificationExecutor<Coordinator> {

    List<Coordinator> findByPropertyCompanyId(String propertyCompanyId);

    /**
     * 查询某物业下负责某板块的统筹（coordinators 表无 sector 字段，通过 SectorLeader 联表查询）
     */
    @Query("SELECT c FROM Coordinator c WHERE c.propertyCompanyId = :propertyCompanyId " +
           "AND c.id IN (SELECT s.coordinatorId FROM SectorLeader s WHERE s.sector = :sector)")
    List<Coordinator> findByPropertyCompanyIdAndSector(@Param("propertyCompanyId") String propertyCompanyId,
                                                       @Param("sector") String sector);

    Optional<Coordinator> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    Optional<Coordinator> findByResidentIdAndPropertyCompanyId(String residentId, String propertyCompanyId);

    Optional<Coordinator> findFirstByPropertyCompanyIdAndStatusOrderByCommissionRateDesc(
            String propertyCompanyId, String status);
}
