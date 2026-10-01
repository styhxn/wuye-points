package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "community_property_permissions")
@EqualsAndHashCode(callSuper = true)
public class CommunityPropertyPermission extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "binding_id", length = 32, nullable = false)
    private String bindingId;

    @Column(name = "allowed_modules", columnDefinition = "json")
    private String allowedModules;
}
