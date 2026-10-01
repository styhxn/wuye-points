package com.wuye.points.repository;

import com.wuye.points.entity.CommunityRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityRoomRepository extends JpaRepository<CommunityRoom, String> {

    List<CommunityRoom> findByCommunityIdOrderByBuildingAscUnitAscRoomAsc(String communityId);

    List<CommunityRoom> findByCommunityIdAndBuildingOrderByUnitAscRoomAsc(String communityId, String building);

    List<CommunityRoom> findByCommunityIdAndBuildingAndUnitOrderByRoomAsc(String communityId, String building, String unit);

    Optional<CommunityRoom> findFirstByCommunityIdAndBuildingAndUnitAndRoom(String communityId, String building, String unit, String room);

    boolean existsByCommunityIdAndBuildingAndUnitAndRoom(String communityId, String building, String unit, String room);

    long countByCommunityId(String communityId);

    @Modifying
    @Query("DELETE FROM CommunityRoom r WHERE r.communityId = :communityId AND r.id = :id")
    void deleteByCommunityIdAndId(@Param("communityId") String communityId, @Param("id") String id);
}
