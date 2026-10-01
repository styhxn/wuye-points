package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小区建设积分池表 cpp_xxxxxxxx
 * 注：表仅含 updated_at，无 created_at，故不继承 BaseEntity，
 *     仅声明 updated_at 字段以适配 ddl-auto: validate。
 */
@Data
@Entity
@Table(name = "community_point_pools")
@EntityListeners(AuditingEntityListener.class)
public class CommunityPointPool {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "total_in", precision = 10, scale = 2)
    private BigDecimal totalIn;

    @Column(name = "total_out", precision = 10, scale = 2)
    private BigDecimal totalOut;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
