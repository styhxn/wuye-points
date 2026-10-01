package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物业联系方式配置（全局单条）
 * 管理端可配置，前端在用户未绑定楼栋时展示
 */
@Data
@Entity
@Table(name = "property_contact_configs")
@EqualsAndHashCode(callSuper = true)
public class PropertyContactConfig extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_name", length = 100)
    private String propertyName;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_mobile", length = 50)
    private String contactMobile;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "service_hours", length = 100)
    private String serviceHours;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "enabled")
    private Boolean enabled;
}
