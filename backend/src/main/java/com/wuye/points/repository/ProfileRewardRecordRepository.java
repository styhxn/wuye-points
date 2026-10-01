package com.wuye.points.repository;

import com.wuye.points.entity.ProfileRewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 信息采集奖励发放记录 Repository
 */
@Repository
public interface ProfileRewardRecordRepository extends JpaRepository<ProfileRewardRecord, String> {

    List<ProfileRewardRecord> findByResidentId(String residentId);

    boolean existsByResidentIdAndFieldName(String residentId, String fieldName);
}
