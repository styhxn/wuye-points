package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家分类字典
 * 前端入驻下拉 / 商家列表筛选均从此表取合法值
 */
@Data
@Entity
@Table(name = "merchant_category_dict")
@EqualsAndHashCode(callSuper = true)
public class MerchantCategoryDict extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 分类名称（中文，如"外卖"、"酒店民宿"） */
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    /** 排序权重（升序） */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** active/inactive */
    @Column(name = "status", length = 20)
    private String status;
}
