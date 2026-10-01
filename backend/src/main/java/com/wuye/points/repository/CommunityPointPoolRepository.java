package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPointPool;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 小区建设积分池 Repository
 */
@Repository
public interface CommunityPointPoolRepository extends JpaRepository<CommunityPointPool, String> {

    Optional<CommunityPointPool> findByCommunityId(String communityId);

    /**
     * 悲观写锁查询，确保并发场景下 balance/totalIn/totalOut 的原子更新
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CommunityPointPool> findWithLockByCommunityId(String communityId);

    List<CommunityPointPool> findByPropertyCompanyId(String propertyCompanyId);
}
