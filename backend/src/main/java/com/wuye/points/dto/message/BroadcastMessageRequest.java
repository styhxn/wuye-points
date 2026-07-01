package com.wuye.points.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BroadcastMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 2000, message = "消息内容长度须在1-2000字符之间")
    private String content;

    private String toRole;
}
