package com.wuye.points.repository;

import com.wuye.points.entity.AgeBracketConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgeBracketConfigRepository extends JpaRepository<AgeBracketConfig, String> {

    List<AgeBracketConfig> findByPropertyCompanyIdIsNullAndStatusOrderBySortOrderAsc(String status);

    List<AgeBracketConfig> findByPropertyCompanyIdIsNullOrderBySortOrderAsc();

    List<AgeBracketConfig> findByPropertyCompanyIdOrderBySortOrderAsc(String propertyCompanyId);

    List<AgeBracketConfig> findByPropertyCompanyIdOrPropertyCompanyIdIsNullOrderBySortOrderAsc(
            String propertyCompanyId);
}
