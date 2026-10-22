package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resident_merchant_applications")
@EqualsAndHashCode(callSuper = true)
public class ResidentMerchantApplication extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "deposit_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "status", length = 30)
    private String status;

    /** 店铺对外展示：public（对外，任何人可查看）/private（不对外，默认） */
    @Column(name = "visibility", length = 10, nullable = false)
    private String visibility;

    @Column(name = "audit_remark", length = 200)
    private String auditRemark;

    @Column(name = "audited_by", length = 32)
    private String auditedBy;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "quit_requested_at")
    private LocalDateTime quitRequestedAt;

    /** 店铺名称（订单/买家看到的店名，默认「业主姓名的小店」） */
    @Column(name = "shop_name", length = 100)
    private String shopName;

    /** 店铺主体商家ID（审核通过时自动创建，merchantSource=resident_shop） */
    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    /** 店铺简介（对齐普通商家 description） */
    @Column(name = "description", length = 500)
    private String description;

    /** 营业时间（对齐普通商家 businessHours，如 08:00-22:00） */
    @Column(name = "business_hours", length = 50)
    private String businessHours;

    /** 联系电话（对齐普通商家 contactPhone；不传则回退为店主手机号） */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** 店铺封面图URL（对齐普通商家封面） */
    @Column(name = "cover_url", length = 500)
    private String coverUrl;
}
