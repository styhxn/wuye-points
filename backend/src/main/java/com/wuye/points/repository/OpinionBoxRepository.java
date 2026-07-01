package com.wuye.points.repository;

import com.wuye.points.entity.OpinionBox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 意见箱 Repository
 */
@Repository
public interface OpinionBoxRepository extends JpaRepository<OpinionBox, String>, JpaSpecificationExecutor<OpinionBox> {

    Page<OpinionBox> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<OpinionBox> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    long countByFamilyIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String familyId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}
