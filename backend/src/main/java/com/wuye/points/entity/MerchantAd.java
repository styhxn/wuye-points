package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "merchant_ads")
@EqualsAndHashCode(callSuper = true)
public class MerchantAd extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "title", length = 128, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_urls", columnDefinition = "json")
    private String imageUrls;

    @Column(name = "product_id", length = 32)
    private String productId;

    @Column(name = "recipient_count")
    private Integer recipientCount;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    /** 广告展示天数（按天计价） */
    @Column(name = "duration_days")
    private Integer durationDays;

    /** 投放开始日期 */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** 投放结束日期 */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** 支付金额（0=免费额度投放） */
    @Column(name = "pay_amount", precision = 10, scale = 2)
    private BigDecimal payAmount;

    /** 支付方式: free/coin/point/wechat/mock */
    @Column(name = "payment_method", length = 10)
    private String paymentMethod;

    /** 定向广告-目标楼栋（JSON数组） */
    @Column(name = "target_buildings", columnDefinition = "json")
    private String targetBuildings;

    /** 定向广告-目标性别: male/female/all */
    @Column(name = "target_gender", length = 10)
    private String targetGender;

    /** 定向广告-目标年龄段（JSON数组） */
    @Column(name = "target_age_brackets", columnDefinition = "json")
    private String targetAgeBrackets;
}
