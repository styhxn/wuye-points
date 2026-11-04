package com.wuye.points.dto.admin.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 微信分账收款账户新增/修改请求（管理端手填通道）。
 *
 * <p>只接受<b>微信商户号</b>（8~20 位纯数字，对应接收方类型 MERCHANT_ID）。
 * 个人 openid 已停止手填：抄错一位会静默打给另一个人，且唯一索引被错误值占用后无法再绑回
 * 正确账号；个人接收方请改用 {@code POST /admin/wechat-split/receivers/auto}，
 * openid 由后端按主体反查。</p>
 */
@Schema(description = "微信分账收款账户请求（仅商户号；个人 openid 请走 /receivers/auto）")
@Data
public class WechatSplitReceiverRequest {

    @Schema(description = "主体类型（PLATFORM 为平台特例，无需登记）",
            example = "PROPERTY", required = true)
    @NotBlank(message = "主体类型不能为空")
    private String ownerType;

    @Schema(description = "主体 ID（物业公司ID/快递员ID/协调员ID/负责人ID/住户ID 等）",
            example = "pc_123", required = true)
    @NotBlank(message = "主体ID不能为空")
    @Size(max = 32, message = "主体ID长度不能超过32")
    private String ownerId;

    @Schema(description = "微信商户号（8~20 位纯数字）。个人 openid 不接受手填，请改走 /receivers/auto",
            example = "1900000109", required = true)
    @NotBlank(message = "收款账户号不能为空")
    @Size(max = 64, message = "收款账户号长度不能超过64")
    private String accountNo;

    @Schema(description = "账户名称：商户全称（商户号必填）；个人真实姓名（openid 选填，填则微信校验实名一致性）")
    @Size(max = 128, message = "账户名称长度不能超过128")
    private String accountName;

    @Schema(description = "是否已完成校验（默认 true；资料待核实时可置 false，分账将跳过该主体）")
    private Boolean verified;
}
