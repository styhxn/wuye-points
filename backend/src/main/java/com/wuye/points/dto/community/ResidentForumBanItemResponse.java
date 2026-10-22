package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "论坛禁言记录")
public class ResidentForumBanItemResponse {

    @Schema(description = "禁言记录ID")
    private String id;

    @Schema(description = "被禁住户ID")
    private String residentId;

    @Schema(description = "被禁住户姓名")
    private String residentName;

    @Schema(description = "手机号（脱敏）")
    private String residentPhone;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "禁言原因")
    private String reason;

    @Schema(description = "操作管理员住户ID")
    private String bannedBy;

    @Schema(description = "操作管理员姓名")
    private String bannedByName;

    @Schema(description = "禁言开始时间(ISO8601)")
    private String bannedAt;

    @Schema(description = "到期时间(ISO8601)；null=永久禁言")
    private String expiresAt;

    @Schema(description = "状态 active/lifted/expired")
    private String status;
}
