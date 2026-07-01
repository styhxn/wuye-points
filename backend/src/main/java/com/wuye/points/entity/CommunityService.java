package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区服务表
 * 对应 schema.sql 中 community_services 表（id 前缀 svc_）
 */
@Data
@Entity
@Table(name = "community_services")
@EqualsAndHashCode(callSuper = true)
public class CommunityService extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_urls", columnDefinition = "json")
    private String coverUrls;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "member_price", precision = 10, scale = 2)
    private BigDecimal memberPrice;

    @Column(name = "price_unit", length = 20)
    private String priceUnit;

    @Column(name = "provider_id", length = 32)
    private String providerId;

    @Column(name = "provider_type", length = 20)
    private String providerType;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "rank_order")
    private Integer rankOrder;

    @Column(name = "is_subscription")
    private Boolean isSubscription;

    @Column(name = "status", length = 20)
    private String status;
}
