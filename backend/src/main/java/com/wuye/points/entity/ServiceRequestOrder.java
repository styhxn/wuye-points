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
@Table(name = "service_request_orders")
@EqualsAndHashCode(callSuper = true)
public class ServiceRequestOrder extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "community_id", length = 32)
    private String communityId;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** 服务地址（自动带出楼栋+单元+房号，可覆盖） */
    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "preferred_time")
    private LocalDateTime preferredTime;

    @Column(name = "matched_category_id", length = 32)
    private String matchedCategoryId;

    @Column(name = "current_merchant_id", length = 32)
    private String currentMerchantId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "quote_amount", precision = 10, scale = 2)
    private BigDecimal quoteAmount;

    @Column(name = "quote_payment_method", length = 20)
    private String quotePaymentMethod;

    @Column(name = "service_order_id", length = 32)
    private String serviceOrderId;

    @Column(name = "closed_reason", length = 200)
    private String closedReason;
}
