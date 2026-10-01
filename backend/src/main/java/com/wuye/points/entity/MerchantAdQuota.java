package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "merchant_ad_quota")
@EqualsAndHashCode(callSuper = true)
public class MerchantAdQuota extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "free_quota", nullable = false)
    private Integer freeQuota;

    @Column(name = "purchased_quota", nullable = false)
    private Integer purchasedQuota;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    /** 本周免费额度已用条数（付费广告不占用） */
    @Column(name = "free_used_count", nullable = false)
    private Integer freeUsedCount;
}
