package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 一级代理申请表
 * 用户自助申请成为一级货源经销商（对应「一级代理申请」入口），由管理端审核。
 * 审核通过后由后端在同一事务内创建/关联 merchants 载体并置 is_distributor=1、audit_status=approved。
 */
@Data
@Entity
@Table(name = "distributor_applications")
@EqualsAndHashCode(callSuper = true)
public class DistributorApplication extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 申请人住户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 物业公司ID（冗余，便于查询与数据隔离） */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 联系人姓名 */
    @Column(name = "contact_name", length = 50, nullable = false)
    private String contactName;

    /** 联系电话 */
    @Column(name = "contact_phone", length = 20, nullable = false)
    private String contactPhone;

    /** 经营品类 fresh/daily/grain/other */
    @Column(name = "business_scope", length = 30, nullable = false)
    private String businessScope;

    /** 服务范围 community/city */
    @Column(name = "service_scope", length = 20, nullable = false)
    private String serviceScope;

    /** 备注 */
    @Column(name = "remark", length = 200)
    private String remark;

    /** pending/approved/rejected */
    @Column(name = "audit_status", length = 20, nullable = false)
    private String auditStatus;

    /** 审核拒绝原因（rejected 时填写） */
    @Column(name = "reject_reason", length = 200)
    private String rejectReason;

    /** 审核通过后创建/关联的商家载体ID */
    @Column(name = "distributor_merchant_id", length = 32)
    private String distributorMerchantId;

    /** 审核人ID */
    @Column(name = "audited_by", length = 32)
    private String auditedBy;

    /** 审核时间 */
    @Column(name = "audited_at")
    private java.time.LocalDateTime auditedAt;
}
