package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 幂等请求记录。
 *
 * <p>以 (user_id, request_key) 唯一键保证多实例下同一请求只执行一次；
 * 首次请求落 processing，完成后写 response_body，重复请求直接回放。</p>
 */
@Data
@Entity
@Table(name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_idem_user_key",
                columnNames = {"user_id", "request_key"}),
        indexes = @Index(name = "idx_idem_expires", columnList = "expires_at"))
@EqualsAndHashCode(callSuper = true)
public class IdempotencyRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "user_id", length = 32, nullable = false)
    private String userId;

    /** SHA-256(method + ":" + path + ":" + X-Request-Id) */
    @Column(name = "request_key", length = 64, nullable = false)
    private String requestKey;

    @Column(name = "request_id", length = 128, nullable = false)
    private String requestId;

    @Column(name = "method", length = 10, nullable = false)
    private String method;

    @Column(name = "path", length = 255, nullable = false)
    private String path;

    /** processing / completed */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Lob
    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
