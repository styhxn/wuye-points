package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "广播消息请求")
public class BroadcastMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 2000, message = "消息内容长度须在1-2000字符之间")
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "系统通知：明日社区活动")
    private String content;

    @Schema(description = "目标角色", example = "RESIDENT")
    private String toRole;
}
