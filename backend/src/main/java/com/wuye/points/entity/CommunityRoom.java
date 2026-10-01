package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 社区房号主表（楼宇结构主数据）。
 *
 * <p>作为独立的“房间”一等公民，房号不再依赖 residents 表聚合投影。
 * 空房即 status='vacant' 的物理房间，可由物业在楼栋管理中新增。
 * 占用状态（occupied）由 {@link com.wuye.points.service.CommunityRoomService#syncRoom} 依据 residents 实时维护。
 */
@Entity
@Table(name = "community_rooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_community_room",
                columnNames = {"community_id", "building", "unit", "room"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRoom extends BaseEntity {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    @Column(name = "building", length = 20, nullable = false)
    private String building;

    /** 单元，统一以空串表示“无单元”，保证唯一约束与查询一致 */
    @Column(name = "unit", length = 20, nullable = false)
    @Builder.Default
    private String unit = "";

    @Column(name = "floor", length = 20)
    private String floor;

    @Column(name = "room", length = 20, nullable = false)
    private String room;

    /** vacant 空置 / occupied 已入住 / locked 锁定 */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "vacant";

    @Column(name = "resident_id", length = 32)
    private String residentId;
}
