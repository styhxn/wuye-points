package com.wuye.points.repository;

import com.wuye.points.entity.ActivityGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 活动组成员 Repository
 */
@Repository
public interface ActivityGroupMemberRepository extends JpaRepository<ActivityGroupMember, String> {

    List<ActivityGroupMember> findByGroupId(String groupId);

    List<ActivityGroupMember> findByGroupIdAndStatus(String groupId, String status);

    Optional<ActivityGroupMember> findByGroupIdAndResidentId(String groupId, String residentId);

    List<ActivityGroupMember> findByGroupIdInAndResidentIdAndStatus(
            Collection<String> groupIds, String residentId, String status);

    boolean existsByGroupIdAndResidentIdAndStatus(String groupId, String residentId, String status);
}
