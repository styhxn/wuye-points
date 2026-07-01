package com.wuye.points.repository;

import com.wuye.points.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
