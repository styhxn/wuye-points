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
@Table(name = "merchant_message_services")
@EqualsAndHashCode(callSuper = true)
public class MerchantMessageServiceConfig extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /** 1km/3km/5km/district/city */
    @Column(name = "service_radius", length = 20)
    private String serviceRadius;

    @Column(name = "category_ids", columnDefinition = "json")
    private String categoryIds;
}
