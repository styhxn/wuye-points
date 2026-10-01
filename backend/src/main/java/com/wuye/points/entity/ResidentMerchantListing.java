package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "resident_merchant_listings")
@EqualsAndHashCode(callSuper = true)
public class ResidentMerchantListing extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_merchant_id", length = 32, nullable = false)
    private String residentMerchantId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "distributor_product_id", length = 32, nullable = false)
    private String distributorProductId;

    @Column(name = "retail_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal retailPrice;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "share_count")
    private Integer shareCount;
}
