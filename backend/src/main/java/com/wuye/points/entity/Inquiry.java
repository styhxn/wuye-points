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

/**
 * 询价记录表
 * 对应 inquiries 表（id 前缀 inq_）
 */
@Data
@Entity
@Table(name = "inquiries")
@EqualsAndHashCode(callSuper = true)
public class Inquiry extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "inquirer_id", length = 32, nullable = false)
    private String inquirerId;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "product_id", length = 32)
    private String productId;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "quoted_price", precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Column(name = "quoted_remark", length = 500)
    private String quotedRemark;

    /** pending/quoted/accepted/rejected/closed */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "quoted_at")
    private LocalDateTime quotedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
