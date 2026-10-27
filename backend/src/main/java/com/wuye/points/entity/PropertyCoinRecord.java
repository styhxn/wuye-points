package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物业币记录表 coin_xxxxxxxx
 */
@Data
@Entity
@Table(name = "property_coin_records")
@EqualsAndHashCode(callSuper = true)
public class PropertyCoinRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "family_id", length = 32)
    private String familyId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "coin_type", length = 30, nullable = false)
    private String coinType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    /** 本次变动中来自/进入家庭池的物业币；正数=进入家庭池，负数=从家庭池扣除，0=仅个人池。 */
    @Column(name = "family_amount", precision = 10, scale = 2)
    private BigDecimal familyAmount;

    /** 变动后的家庭物业币池余额快照；无家庭时为 null。 */
    @Column(name = "family_balance", precision = 10, scale = 2)
    private BigDecimal familyBalance;

    /** 该笔 earn/recharge 记录已过期清零的金额；用于防止定时任务重复扣减同一笔。 */
    @Column(name = "expired_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal expiredAmount;

    @Column(name = "source", length = 30)
    private String source;

    @Column(name = "source_id", length = 32)
    private String sourceId;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "operator_id", length = 32)
    private String operatorId;
}
