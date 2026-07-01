package com.wuye.points.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageListItemResponse {

    private String id;
    private String fromId;
    private String fromName;
    private String fromAvatarUrl;
    private String toId;
    private String toRole;
    private String messageType;
    private String content;
    private String chatType;
    private String groupId;
    private String readStatus;
    private String propertyCompanyId;
    private String createdAt;
}
