package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快递负责人表
 * 对应 courier_managers 表（id 前缀 cm_）
 */
@Data
@Entity
@Table(name = "courier_managers")
@EqualsAndHashCode(callSuper = true)
public class CourierManager extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    /** 负责区域描述 */
    @Column(name = "responsible_area", length = 500)
    private String responsibleArea;

    /** 配送时间配置JSON */
    @Column(name = "delivery_time_config", columnDefinition = "json")
    private String deliveryTimeConfig;

    @Column(name = "status", length = 20)
    private String status;

    /** 快递负责人分成比例（配送费中归负责人的比例，0~1） */
    @Column(name = "share_rate", precision = 10, scale = 4)
    private java.math.BigDecimal shareRate;
}
