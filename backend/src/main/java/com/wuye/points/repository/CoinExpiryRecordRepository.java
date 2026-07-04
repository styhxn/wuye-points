package com.wuye.points.repository;

import com.wuye.points.entity.CoinExpiryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 物业币过期清零记录 Repository
 */
@Repository
public interface CoinExpiryRecordRepository extends JpaRepository<CoinExpiryRecord, String>,
        JpaSpecificationExecutor<CoinExpiryRecord> {

    List<CoinExpiryRecord> findByPropertyCompanyId(String propertyCompanyId);
}
