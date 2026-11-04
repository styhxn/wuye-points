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
 * 物业币现金资金账户（按物业公司）。
 *
 * <p>物业币充值现金全部进入平台直连商户号，平台据此对每个物业公司承担等额代管责任；
 * 商家物业币应收结算、平台向物业转账均从本账户出账。</p>
 */
@Data
@Entity
@Table(name = "property_coin_fund_account")
@EqualsAndHashCode(callSuper = true)
public class PropertyCoinFundAccount extends BaseEntity {

    @Id
    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;

    @Column(name = "balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "total_in", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalIn;

    @Column(name = "total_out", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalOut;
}
