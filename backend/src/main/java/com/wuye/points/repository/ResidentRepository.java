package com.wuye.points.repository;

import com.wuye.points.entity.Resident;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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

    Optional<Resident> findByPhoneAndPropertyCompanyId(String phone, String propertyCompanyId);

    List<Resident> findByCourierManagerId(String courierManagerId);

    Optional<Resident> findByIdAndCourierManagerId(String id, String courierManagerId);

    long countByCourierManagerId(String courierManagerId);

    /** 悲观锁查询（SELECT ... FOR UPDATE），用于并发扣减场景 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resident r WHERE r.id = :id")
    Optional<Resident> findByIdForUpdate(@Param("id") String id);

    /** 2026-09-08：板块负责人列表 keyword（姓名/手机号）模糊匹配，返回命中的住户ID */
    @Query("SELECT r.id FROM Resident r WHERE r.name LIKE CONCAT('%', :keyword, '%')"
            + " OR r.phone LIKE CONCAT('%', :keyword, '%')")
    List<String> findIdsByNameContainingOrPhoneContaining(@Param("keyword") String keyword);
}
