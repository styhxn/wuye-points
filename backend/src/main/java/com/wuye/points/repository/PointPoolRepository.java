package com.wuye.points.repository;

import com.wuye.points.entity.PointPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 积分池 Repository
 */
@Repository
public interface PointPoolRepository extends JpaRepository<PointPool, String> {

    Optional<PointPool> findByPropertyCompanyId(String propertyCompanyId);
}
