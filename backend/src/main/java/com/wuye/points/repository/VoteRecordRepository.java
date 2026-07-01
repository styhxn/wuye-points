package com.wuye.points.repository;

import com.wuye.points.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 投票记录 Repository
 * 注：opinion_box_id 已隐含 propertyCompanyId
 * 表字段 opinion_box_id 对应实体属性 opinionBoxId
 */
@Repository
public interface VoteRecordRepository extends JpaRepository<VoteRecord, String> {

    List<VoteRecord> findByOpinionBoxId(String opinionBoxId);

    Optional<VoteRecord> findByResidentIdAndOpinionBoxId(String residentId, String opinionBoxId);

    Optional<VoteRecord> findByOpinionBoxIdAndFamilyId(String opinionBoxId, String familyId);

    List<VoteRecord> findByOpinionBoxIdInAndFamilyId(Collection<String> opinionBoxIds, String familyId);

    boolean existsByOpinionBoxIdAndFamilyId(String opinionBoxId, String familyId);
}
