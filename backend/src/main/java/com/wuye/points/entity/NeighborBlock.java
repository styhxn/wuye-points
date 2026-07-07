package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邻居屏蔽记录表 nb_xxxxxxxx
 */
@Data
@Entity
@Table(name = "neighbor_blocks")
@EqualsAndHashCode(callSuper = true)
public class NeighborBlock extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "blocker_id", length = 32, nullable = false)
    private String blockerId;

    @Column(name = "blocked_id", length = 32, nullable = false)
    private String blockedId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;
}
