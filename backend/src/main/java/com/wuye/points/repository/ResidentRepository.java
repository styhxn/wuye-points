package com.wuye.points.repository;

import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 业主/住户 Repository
 * 注：findByOpenid / findByPhone 用于登录态识别，openid 与 phone 全局唯一，无需 propertyCompanyId
 */
@Repository
public interface ResidentRepository extends JpaRepository<Resident, String>, JpaSpecificationExecutor<Resident> {

    Optional<Resident> findByOpenid(String openid);

    Optional<Resident> findByPhone(String phone);

    List<Resident> findByFamilyId(String familyId);

    List<Resident> findByPropertyCompanyId(String propertyCompanyId);

    long countByPropertyCompanyId(String propertyCompanyId);

    List<Resident> findByPropertyCompanyIdAndRole(String propertyCompanyId, String role);

    Optional<Resident> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    boolean existsByPhoneAndPropertyCompanyId(String phone, String propertyCompanyId);

    List<Resident> findByPropertyCompanyIdAndBuildingAndUnitAndStatus(
            String propertyCompanyId, String building, String unit, String status);

    List<Resident> findByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);

    long countByPropertyCompanyIdAndCreatedAtAfter(String propertyCompanyId, LocalDateTime createdAt);

    long countByPropertyCompanyIdAndCreatedAtBetween(String propertyCompanyId, LocalDateTime start, LocalDateTime end);
}
