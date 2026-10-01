package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 当前住户（resident）在本物业的商家入驻申请状态。
 * 用于"我的商家"页面替代误用的 GET /merchants/my（后者仅对 merchant 角色开放，resident 调会 403）。
 */
@Schema(description = "我的商家入驻申请状态")
@Data
public class MyMerchantApplicationResponse {

    @Schema(description = "申请记录ID（merchant.id）；从未申请时为 null")
    private String id;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "审核状态：none(未申请)/pending_audit(待审核)/approved(已通过)/rejected(已驳回)/closed(已退出)")
    private String auditStatus;

    @Schema(description = "申请身份：merchant / technician / activity_leader")
    private String applyRole;

    @Schema(description = "商家来源：platform / technician / group_leader")
    private String merchantSource;

    @Schema(description = "记录状态（merchant.status）")
    private String status;

    @Schema(description = "是否仍在流程中（pending_audit 视为处理中）")
    private Boolean processing;
}
