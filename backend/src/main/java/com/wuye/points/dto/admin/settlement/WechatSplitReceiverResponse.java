package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 微信分账收款账户（管理端返回）。 */
@Schema(description = "微信分账收款账户")
@Data
@Builder
public class WechatSplitReceiverResponse {

    private String id;
    private String ownerType;
    private String ownerId;
    /** 微信接收方类型：MERCHANT_ID / PERSONAL_OPENID（按账户号格式推导） */
    private String receiverType;
    /** 微信商户号明文；仅 MERCHANT_ID 下发。个人 openid 一律不下发，见 accountNoMasked */
    private String accountNo;
    /** 展示用账户号：商户号为明文，个人 openid 为脱敏值（前4+****+后4） */
    private String accountNoMasked;
    private String accountName;
    private boolean verified;
    /** 个人 openid 不支持微信分账回退，退款场景需平台垫付并登记追回台账 */
    private boolean reversable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
