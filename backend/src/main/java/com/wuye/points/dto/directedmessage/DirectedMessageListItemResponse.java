package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectedMessageListItemResponse {

    private String id;
    private String title;
    private String officialSenderType;
    private Integer recipientCount;
    private Integer readCount;
    private Double readRate;
    private String status;
    private String statusCode;
    private String createdAt;
}
