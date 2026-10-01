package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "consultants")
@EqualsAndHashCode(callSuper = true)
public class Consultant extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** medical/education/elderly */
    @Column(name = "category", length = 20, nullable = false)
    private String category;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "organization", length = 100)
    private String organization;

    @Column(name = "specialty", length = 100)
    private String specialty;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "chat_price", precision = 10, scale = 2)
    private BigDecimal chatPrice;

    @Column(name = "appointment_price", precision = 10, scale = 2)
    private BigDecimal appointmentPrice;

    @Column(name = "appointment_enabled")
    private Boolean appointmentEnabled;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "status", length = 20)
    private String status;
}
