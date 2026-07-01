package com.wuye.points.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMessageRequest {

    private String toId;

    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 2000, message = "消息内容长度须在1-2000字符之间")
    private String content;

    @NotBlank(message = "messageType不能为空")
    private String messageType;

    @NotBlank(message = "chatType不能为空")
    private String chatType;

    private String toRole;

    private String groupId;
}
