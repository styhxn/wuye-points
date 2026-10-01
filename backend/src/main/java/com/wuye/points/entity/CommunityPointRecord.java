package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小区建设积分流水表 cpr_xxxxxxxx
 * 注：表仅含 created_at，无 updated_at，故不继承 BaseEntity，
 *     仅声明 created_at 字段以适配 ddl-auto: validate。
 */
@Data
@Entity
@Table(name = "community_point_records")
@EntityListeners(AuditingEntityListener.class)
public class CommunityPointRecord {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "community_id", length = 32, nullable = false)
    private String communityId;

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "source", length = 30, nullable = false)
    private String source;

    @Column(name = "remark", length = 200)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
