package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "community_property_bindings")
@EqualsAndHashCode(callSuper = true)
public class CommunityPropertyBinding extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "community_entity_id", length = 32, nullable = false)
    private String communityEntityId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "permission_level", length = 20, nullable = false)
    private String permissionLevel;

    @Column(name = "bound_at")
    private LocalDateTime boundAt;
}
