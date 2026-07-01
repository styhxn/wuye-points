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
 * 商品表
 */
@Data
@Entity
@Table(name = "products")
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    /** 业主专属折扣价 */
    @Column(name = "member_price", precision = 10, scale = 2)
    private BigDecimal memberPrice;

    /** 积分兑换价格 */
    @Column(name = "point_price")
    private Integer pointPrice;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "category", length = 50)
    private String category;

    /** active/inactive/sold_out */
    @Column(name = "status", length = 20)
    private String status;
}
