package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家动态表 mp_xxxxxxxx
 */
@Data
@Entity
@Table(name = "merchant_posts")
@EqualsAndHashCode(callSuper = true)
public class MerchantPost extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** JSON 数组 */
    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** draft/published/archived */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "view_count")
    private Integer viewCount;
}
