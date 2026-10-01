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
 * 商家-小区距离配置表（管理端配置）。
 * 商品/商家列表按当前用户 communityId 查表写入 distanceKm；无配置则不展示距离。
 */
@Data
@Entity
@Table(name = "merchant_community_distances")
@EqualsAndHashCode(callSuper = true)
public class MerchantCommunityDistance extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    /** 冗余物业公司ID，便于按物业检索 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 商家距该小区的公里数 */
    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    /** 最后配置人ID */
    @Column(name = "operator_id", length = 32)
    private String operatorId;
}
