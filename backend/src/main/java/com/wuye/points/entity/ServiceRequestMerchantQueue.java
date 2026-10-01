package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_request_merchant_queue")
@EqualsAndHashCode(callSuper = true)
public class ServiceRequestMerchantQueue extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "request_id", length = 32, nullable = false)
    private String requestId;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    @Column(name = "queue_order", nullable = false)
    private Integer queueOrder;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;
}
