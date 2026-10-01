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
 * 分账回退待追回台账（split_recovery_records）。
 *
 * <p>电商收付通分账后退款：平台（MERCHANT_ID）部分可调微信回退；个人接收方
 * （快递员 PERSONAL_OPENID）不可回退，由平台先行垫付，并记入本台账待线下追回
 * （对账后催收 / 后续配送费抵扣）。status：pending(待追回) / recovered(已追回) / written_off(已核销)。</p>
 */
@Data
@Entity
@Table(name = "split_recovery_records")
@EqualsAndHashCode(callSuper = true)
public class SplitRecoveryRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "split_record_id", length = 32, nullable = false)
    private String splitRecordId;

    @Column(name = "order_id", length = 32, nullable = false)
    private String orderId;

    /** 角色：COURIER 等 */
    @Column(name = "owner_type", length = 32, nullable = false)
    private String ownerType;

    @Column(name = "owner_id", length = 32)
    private String ownerId;

    /** 待追回金额（元） */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** pending(待追回)/recovered(已追回)/written_off(已核销) */
    @Column(name = "status", length = 24, nullable = false)
    private String status;

    /** 备注（追回方式/线下处理记录） */
    @Column(name = "remark", length = 500)
    private String remark;
}
