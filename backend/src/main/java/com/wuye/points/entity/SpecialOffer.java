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
@Table(name = "special_offers")
@EqualsAndHashCode(callSuper = true)
public class SpecialOffer extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "target_type", length = 30, nullable = false)
    private String targetType;

    @Column(name = "target_tags", columnDefinition = "json")
    private String targetTags;

    @Column(name = "publisher_id", length = 32, nullable = false)
    private String publisherId;

    @Column(name = "publisher_role", length = 30, nullable = false)
    private String publisherRole;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "discount_info", length = 500)
    private String discountInfo;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", length = 20)
    private String status;
}
