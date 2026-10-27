package com.wuye.points.repository;

import com.wuye.points.entity.PointPool;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 积分池 Repository
 */
@Repository
public interface PointPoolRepository extends JpaRepository<PointPool, String> {

    Optional<PointPool> findByPropertyCompanyId(String propertyCompanyId);

    /** 过期入池 / 积分池变动统一行锁，避免与分账/清零任务并发覆盖。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointPool p WHERE p.propertyCompanyId = :propertyCompanyId")
    Optional<PointPool> findByPropertyCompanyIdForUpdate(@Param("propertyCompanyId") String propertyCompanyId);
}
