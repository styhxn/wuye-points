package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台级商家库（基础信息一次录入）
 */
@Data
@Entity
@Table(name = "platform_merchants")
@EqualsAndHashCode(callSuper = true)
public class PlatformMerchant extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 商家名称 */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** 外卖/周围商家/自营/农家/外卖小吃/团购 */
    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "business_hours", length = 50)
    private String businessHours;

    @Column(name = "status", length = 20)
    private String status;
}
