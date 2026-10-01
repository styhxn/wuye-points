package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "活动组注册审核请求")
public class ReviewActivityGroupRequest {

    @NotBlank(message = "action不能为空")
    @Schema(description = "审核动作：approve=通过；reject=驳回", example = "approve", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "审核备注（驳回时填写）", example = "信息不完整")
    private String remark;
}
