package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectedMessageRecipientItemResponse {

    private String id;
    private String recipientId;
    private String recipientName;
    private String building;
    private Integer gender;
    private Integer age;
    private String readStatus;
    private String readStatusCode;
    private String readAt;
    private String createdAt;
}
