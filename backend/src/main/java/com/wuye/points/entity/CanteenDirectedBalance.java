package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区食堂-用户定向余额账户（id 前缀 cdb_，用户 × 主商家体系）
 * 定向余额挂在主商家体系下，只能在该主商家及其绑定的子商家消费。
 * version 乐观锁，防止并发充值/消费扣减覆盖。
 */
@Data
@Entity
@Table(name = "canteen_directed_balances")
@EqualsAndHashCode(callSuper = true)
public class CanteenDirectedBalance extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 用户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 所属主商家体系（餐饮公司） */
    @Column(name = "main_merchant_id", length = 32, nullable = false)
    private String mainMerchantId;

    /** 所属物业公司 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 可用定向余额 */
    @Column(name = "balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;

    /** 乐观锁版本号 */
    @Version
    @Column(name = "version")
    private Integer version;
}
