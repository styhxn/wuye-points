package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区食堂-定向充值流水（id 前缀 cdr_）
 * 主商家从自己额度中扣减，给用户充入定向余额。
 */
@Data
@Entity
@Table(name = "canteen_directed_recharges")
@EqualsAndHashCode(callSuper = true)
public class CanteenDirectedRecharge extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    /** 充值主体（主商家） */
    @Column(name = "main_merchant_id", length = 32, nullable = false)
    private String mainMerchantId;

    /** 被充值用户ID */
    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    /** 所属物业公司 */
    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 充值金额 */
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 充值后该体系定向余额快照 */
    @Column(name = "balance_after", precision = 12, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    /** 操作人住户ID（主商家店主） */
    @Column(name = "operator_id", length = 32)
    private String operatorId;

    /** 备注 */
    @Column(name = "remark", length = 200)
    private String remark;
}
