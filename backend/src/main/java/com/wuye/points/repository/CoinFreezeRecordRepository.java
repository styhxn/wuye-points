package com.wuye.points.repository;

import com.wuye.points.entity.CoinFreezeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 物业币冻结/解冻记录 Repository
 */
@Repository
public interface CoinFreezeRecordRepository extends JpaRepository<CoinFreezeRecord, String> {

    List<CoinFreezeRecord> findByResidentId(String residentId);

    List<CoinFreezeRecord> findByResidentIdAndAction(String residentId, String action);

    List<CoinFreezeRecord> findByPropertyCompanyId(String propertyCompanyId);

    Optional<CoinFreezeRecord> findByIdAndResidentId(String id, String residentId);

    long countByPropertyCompanyIdAndAction(String propertyCompanyId, String action);

    Page<CoinFreezeRecord> findByPropertyCompanyIdAndAction(String propertyCompanyId, String action, Pageable pageable);

    Page<CoinFreezeRecord> findByAction(String action, Pageable pageable);
}
