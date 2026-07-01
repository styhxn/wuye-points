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
 * 商家表（挂接到各物业）
 */
@Data
@Entity
@Table(name = "merchants")
@EqualsAndHashCode(callSuper = true)
public class Merchant extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 关联平台商家库ID */
    @Column(name = "platform_merchant_id", length = 32, nullable = false)
    private String platformMerchantId;

    /** 挂接的物业公司 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 商家名称（冗余，可各物业不同） */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 封面图片数组 */
    @Column(name = "cover_urls", columnDefinition = "json")
    private String coverUrls;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** 专属收款二维码 */
    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    /** official_certified/property_certified/normal */
    @Column(name = "merchant_level", length = 30)
    private String merchantLevel;

    /** 分级排序权重（官方>物业>普通） */
    @Column(name = "level_weight")
    private Integer levelWeight;

    @Column(name = "category", length = 50)
    private String category;

    /** pending_audit/approved/rejected */
    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    /** 手动拖拽排序值 */
    @Column(name = "rank_order")
    private Integer rankOrder;

    @Column(name = "business_hours", length = 50)
    private String businessHours;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "address", length = 200)
    private String address;

    /** 商家设定的配送费 */
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    /** 满额免配送费 */
    @Column(name = "free_delivery_threshold", precision = 10, scale = 2)
    private BigDecimal freeDeliveryThreshold;

    @Column(name = "status", length = 20)
    private String status;
}
