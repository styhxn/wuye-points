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
@Table(name = "directed_message_tasks")
@EqualsAndHashCode(callSuper = true)
public class DirectedMessageTask extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "sender_id", length = 32, nullable = false)
    private String senderId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "official_sender_type", length = 30, nullable = false)
    private String officialSenderType;

    @Column(name = "title", length = 128, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    @Column(name = "filter_buildings", columnDefinition = "json")
    private String filterBuildings;

    @Column(name = "filter_gender", length = 10)
    private String filterGender;

    @Column(name = "filter_age_bracket_ids", columnDefinition = "json")
    private String filterAgeBracketIds;

    /** 按商家消费筛选（商家ID数组JSON） */
    @Column(name = "merchant_ids", columnDefinition = "json")
    private String merchantIds;

    /** 按产品购买筛选（产品ID数组JSON） */
    @Column(name = "product_ids", columnDefinition = "json")
    private String productIds;

    @Column(name = "recipient_count", nullable = false)
    private Integer recipientCount;

    @Column(name = "read_count", nullable = false)
    private Integer readCount;

    @Column(name = "status", length = 20)
    private String status;
}
