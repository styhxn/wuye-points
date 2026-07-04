package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物业费表 fee_xxxxxxxx
 */
@Data
@Entity
@Table(name = "property_fees")
@EqualsAndHashCode(callSuper = true)
public class PropertyFee extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "family_id", length = 32)
    private String familyId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "fee_type", length = 30, nullable = false)
    private String feeType;

    @Column(name = "period", length = 20, nullable = false)
    private String period;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "point_deducted")
    private Integer pointDeducted;

    @Column(name = "coin_deducted", precision = 10, scale = 2)
    private BigDecimal coinDeducted;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "auto_deduct")
    private Boolean autoDeduct;
}
