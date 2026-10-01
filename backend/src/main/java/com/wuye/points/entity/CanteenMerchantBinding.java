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
 * 社区食堂-主商家子商家绑定表（id 前缀 cmb_）
 */
@Data
@Entity
@Table(name = "canteen_merchant_bindings")
@EqualsAndHashCode(callSuper = true)
public class CanteenMerchantBinding extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 主商家ID（餐饮公司，统一开票主体） */
    @Column(name = "main_merchant_id", length = 32, nullable = false)
    private String mainMerchantId;

    /** 子商家ID（麻辣烫/米线/烧烤等供货商家） */
    @Column(name = "sub_merchant_id", length = 32, nullable = false)
    private String subMerchantId;

    /** 所属物业公司 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 供货优惠比例（0.20=子商家给主商家供货8折），第一版仅记录用于人工对账 */
    @Column(name = "supply_discount_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal supplyDiscountRate;

    /** active 启用 / inactive 停用 */
    @Column(name = "status", length = 20, nullable = false)
    private String status;
}
