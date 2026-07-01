package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物业币过期清零记录表 cer_xxxxxxxx
 */
@Data
@Entity
@Table(name = "coin_expiry_records")
@EqualsAndHashCode(callSuper = true)
public class CoinExpiryRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "expired_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal expiredAmount;

    @Column(name = "pool_in_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal poolInAmount;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}
