package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家搜索关键词
 */
@Data
@Entity
@Table(name = "merchant_keywords", uniqueConstraints = {
        @UniqueConstraint(name = "uk_merchant_keyword", columnNames = {"merchant_id", "keyword"})
})
@EqualsAndHashCode(callSuper = true)
public class MerchantKeyword extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "keyword", length = 100, nullable = false)
    private String keyword;

    /** 权重，越大越优先匹配 */
    @Column(name = "weight")
    private Integer weight;

    @Column(name = "status", length = 20)
    private String status;
}
