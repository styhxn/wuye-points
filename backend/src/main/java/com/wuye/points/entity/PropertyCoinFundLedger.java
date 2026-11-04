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
 * 物业币现金资金台账。
 *
 * <p>只记录现金归属，不记录物业币余额：充值入账（credit）、商家物业币应收结算（debit）、
 * 平台向物业公司转账（debit）、人工调整。唯一键 (bizType, bizId, entryType) 保证重复回调只记一次。</p>
 */
@Data
@Entity
@Table(name = "property_coin_fund_ledger")
@EqualsAndHashCode(callSuper = true)
public class PropertyCoinFundLedger extends BaseEntity {

    public static final String ENTRY_RECHARGE_IN = "recharge_in";
    public static final String ENTRY_MERCHANT_SETTLEMENT = "merchant_settlement";
    public static final String ENTRY_PROPERTY_TRANSFER = "property_transfer";
    public static final String ENTRY_ADJUSTMENT = "adjustment";

    public static final String DIRECTION_CREDIT = "credit";
    public static final String DIRECTION_DEBIT = "debit";

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "entry_type", length = 32, nullable = false)
    private String entryType;

    @Column(name = "direction", length = 10, nullable = false)
    private String direction;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "biz_type", length = 32)
    private String bizType;

    @Column(name = "biz_id", length = 64)
    private String bizId;

    @Column(name = "resident_id", length = 32)
    private String residentId;

    @Column(name = "merchant_id", length = 32)
    private String merchantId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "operator_id", length = 32)
    private String operatorId;

    @Column(name = "transfer_proof", length = 255)
    private String transferProof;
}
