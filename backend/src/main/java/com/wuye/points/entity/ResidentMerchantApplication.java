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
}
