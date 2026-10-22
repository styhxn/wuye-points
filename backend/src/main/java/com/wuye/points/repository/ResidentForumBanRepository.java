package com.wuye.points.repository;

import com.wuye.points.entity.ResidentForumBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentForumBanRepository
        extends JpaRepository<ResidentForumBan, String>, JpaSpecificationExecutor<ResidentForumBan> {

    /** 当前生效中的禁言（永久或未到期）；一名住户同一时间最多一条 */
    Optional<ResidentForumBan> findFirstByResidentIdAndStatusOrderByBannedAtDesc(
            String residentId, String status);

    List<ResidentForumBan> findByStatusAndExpiresAtNotNullAndExpiresAtLessThanEqual(
            String status, LocalDateTime now);

    long countByResidentIdAndStatus(String residentId, String status);
}
