package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业主商城商品类目字典
 * 业主商城预置筛选项（食品饮料、日用百货、服饰鞋包、家居用品）统一从此表下发，
 * 不再依赖前端本地字符串，保证全用户一致。
 */
@Data
@Entity
@Table(name = "product_category_dict")
@EqualsAndHashCode(callSuper = true)
public class ProductCategoryDict extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 类目名称（中文，如"食品饮料"、"日用百货"） */
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    /** 排序权重（升序） */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** active/inactive */
    @Column(name = "status", length = 20)
    private String status;
}
