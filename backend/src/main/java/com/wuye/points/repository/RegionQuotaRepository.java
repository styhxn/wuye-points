package com.wuye.points.repository;

import com.wuye.points.entity.RegionQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionQuotaRepository extends JpaRepository<RegionQuota, String> {

    List<RegionQuota> findByPropertyCompanyIdOrderByPeriodStartDesc(String propertyCompanyId);

    List<RegionQuota> findByPropertyCompanyIdAndSectorOrderByPeriodStartDesc(String propertyCompanyId, String sector);
}
