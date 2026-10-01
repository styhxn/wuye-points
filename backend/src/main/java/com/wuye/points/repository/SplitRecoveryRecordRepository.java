package com.wuye.points.repository;

import com.wuye.points.entity.SplitRecoveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SplitRecoveryRecordRepository extends JpaRepository<SplitRecoveryRecord, String>,
        JpaSpecificationExecutor<SplitRecoveryRecord> {

    Optional<SplitRecoveryRecord> findBySplitRecordId(String splitRecordId);

    List<SplitRecoveryRecord> findByStatus(String status);
}
