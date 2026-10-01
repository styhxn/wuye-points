package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物业公司收款银行卡。
 */
@Data
@Entity
@Table(name = "property_bank_cards")
@EqualsAndHashCode(callSuper = true)
public class PropertyBankCard extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    /** 开户行名称 */
    @Column(name = "bank_name", length = 100, nullable = false)
    private String bankName;

    /** 支行名称 */
    @Column(name = "branch_name", length = 100)
    private String branchName;

    /** 户名 */
    @Column(name = "account_name", length = 50, nullable = false)
    private String accountName;

    /** 对公账号（敏感，展示时脱敏） */
    @Column(name = "account_no", length = 50, nullable = false)
    private String accountNo;

    /** 用途 points(积分钱)/reward(奖励金) */
    @Column(name = "purpose", length = 20, nullable = false)
    private String purpose;

    /** 是否默认收款卡 */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /** 状态 active / disabled */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "remark", length = 200)
    private String remark;
}
