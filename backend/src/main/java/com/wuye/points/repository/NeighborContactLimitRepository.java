package com.wuye.points.repository;

import com.wuye.points.entity.NeighborContactLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 邻居私聊每日限制 Repository
 * 注：resident_id 已隐含 propertyCompanyId
 */
@Repository
public interface NeighborContactLimitRepository extends JpaRepository<NeighborContactLimit, String> {

    Optional<NeighborContactLimit> findByResidentIdAndContactDate(String residentId, LocalDate contactDate);
}
