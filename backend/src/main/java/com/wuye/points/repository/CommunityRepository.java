package com.wuye.points.repository;

import com.wuye.points.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小区 Repository
 */
@Repository
public interface CommunityRepository extends JpaRepository<Community, String> {

    List<Community> findByPropertyCompanyId(String propertyCompanyId);

    long countByPropertyCompanyId(String propertyCompanyId);
}
