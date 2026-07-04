package com.wuye.points.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "邻居聊天请求")
public class NeighborChatRequest {

    @NotBlank(message = "接收者ID不能为空")
    @Schema(description = "接收者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "res_k3p5n2")
    private String toId;

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 500, message = "消息内容长度须在1-500字符之间")
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，方便聊一下吗？")
    private String content;
}
