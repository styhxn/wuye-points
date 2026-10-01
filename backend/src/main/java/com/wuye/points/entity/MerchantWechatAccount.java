package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户微信二级商户账号表（merchant_wechat_accounts）。
 *
 * <p>电商收付通（平台收付通）模式下，每个商家需在微信服务商平台进件为「二级商户」，
 * 系统通过本表维护 {@code merchant_id → sub_mchid} 映射。订单下单 / 查询 / 退款 / 分账均按
 * 订单所属商家的二级商户号路由，实现「用户付款直进商家账户 → 平台调分账 API 主动分账」，避免二清。</p>
 */
@Data
@Entity
@Table(name = "merchant_wechat_accounts")
@EqualsAndHashCode(callSuper = true)
public class MerchantWechatAccount extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "merchant_id", length = 32, nullable = false)
    private String merchantId;

    /** 微信二级商户号（电商收付通进件后分配） */
    @Column(name = "sub_mchid", length = 32, nullable = false)
    private String subMchid;

    /** 二级商户主体名称（营业执照 / 身份证姓名） */
    @Column(name = "merchant_name", length = 128)
    private String merchantName;

    /** unbound / auditing / active / frozen */
    @Column(name = "status", length = 20)
    private String status;
}
