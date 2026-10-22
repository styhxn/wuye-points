package com.wuye.points.dto.activityleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "申请成为活动组组长请求")
public class ApplyActivityLeaderRequest {

    @Size(max = 50, message = "擅长领域长度不能超过50")
    @Schema(description = "擅长领域/活动类型（可选，如 文艺/体育/亲子/户外）")
    private String activityField;

    @Size(max = 200, message = "自我介绍长度不能超过200")
    @Schema(description = "自我介绍/备注（可选）")
    private String remark;
}
