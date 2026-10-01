package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectedMessageDetailResponse {

    private String id;
    private String title;
    private String content;
    private String filterSummary;
    private Integer recipientCount;
    private Integer readCount;
    private Integer unreadCount;
    private Double readRate;
    private String status;
    private String statusCode;
    private String createdAt;
}
