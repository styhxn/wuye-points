package com.wuye.points.repository;

import com.wuye.points.entity.BuildingLeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 单元楼长 Repository
 */
@Repository
public interface BuildingLeaderRepository extends JpaRepository<BuildingLeader, String>,
        JpaSpecificationExecutor<BuildingLeader> {

    /** 某个小区+单元当前启用的楼长（订单绑定/分成结算用） */
    Optional<BuildingLeader> findFirstByCommunityIdAndUnitAndStatusOrderByCreatedAtDesc(
            String communityId, String unit, String status);

    /** 某住户在某物业下最近一条指定状态的楼长记录 */
    Optional<BuildingLeader> findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
            String residentId, String propertyCompanyId, String status);

    /** 校验某小区+单元是否已有启用中的楼长 */
    boolean existsByCommunityIdAndUnitAndStatus(String communityId, String unit, String status);

    /** 某住户名下全部楼长记录（统一收益聚合用） */
    List<BuildingLeader> findByResidentId(String residentId);

    /** 批量判定会话对方是否楼长（会话列表置顶标识用） */
    List<BuildingLeader> findByResidentIdInAndPropertyCompanyIdAndStatus(
            java.util.Collection<String> residentIds, String propertyCompanyId, String status);

    /** 校验某住户是否已是启用中的楼长 */
    boolean existsByResidentIdAndPropertyCompanyIdAndStatus(
            String residentId, String propertyCompanyId, String status);
}