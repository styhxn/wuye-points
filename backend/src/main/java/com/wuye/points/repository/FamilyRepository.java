package com.wuye.points.repository;

import com.wuye.points.entity.Family;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 家庭 Repository
 */
@Repository
public interface FamilyRepository extends JpaRepository<Family, String> {

    List<Family> findByPropertyCompanyId(String propertyCompanyId);

    Optional<Family> findByOwnerId(String ownerId);

    Optional<Family> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countByPropertyCompanyId(String propertyCompanyId);

    /** 悲观锁查询（SELECT ... FOR UPDATE），用于并发扣减场景 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Family f WHERE f.id = :id")
    Optional<Family> findByIdForUpdate(@Param("id") String id);
}
