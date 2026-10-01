package com.wuye.points.repository;

import com.wuye.points.entity.NavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavigationItemRepository extends JpaRepository<NavigationItem, String> {

    List<NavigationItem> findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc();

    List<NavigationItem> findByPropertyCompanyIdOrderBySortOrderAsc(String propertyCompanyId);

    List<NavigationItem> findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc(String propertyCompanyId);
}
