package com.wuye.points.repository;

import com.wuye.points.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {

    Optional<IdempotencyRecord> findByUserIdAndRequestKey(String userId, String requestKey);

    long deleteByExpiresAtBefore(LocalDateTime before);
}
