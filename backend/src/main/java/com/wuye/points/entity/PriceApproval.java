package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 价格审批记录表
 * 对应 price_approvals 表（id 前缀 pa_）
 */
@Data
@Entity
@Table(name = "price_approvals")
@EqualsAndHashCode(callSuper = true)
public class PriceApproval extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "applicant_id", length = 32, nullable = false)
    private String applicantId;

    @Column(name = "applicant_role", length = 30, nullable = false)
    private String applicantRole;

    /** 审批类型: delivery_fee/product_price/merchant_ad等 */
    @Column(name = "item_type", length = 30, nullable = false)
    private String itemType;

    @Column(name = "item_id", length = 32)
    private String itemId;

    @Column(name = "old_value", length = 500)
    private String oldValue;

    @Column(name = "new_value", length = 500, nullable = false)
    private String newValue;

    @Column(name = "reason", length = 500)
    private String reason;

    /** pending/approved/rejected */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "approver_id", length = 32)
    private String approverId;

    @Column(name = "approver_remark", length = 500)
    private String approverRemark;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
