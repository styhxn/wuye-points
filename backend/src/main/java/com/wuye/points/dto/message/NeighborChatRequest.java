package com.wuye.points.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NeighborChatRequest {

    @NotBlank(message = "接收者ID不能为空")
    private String toId;

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 500, message = "消息内容长度须在1-500字符之间")
    private String content;
}
