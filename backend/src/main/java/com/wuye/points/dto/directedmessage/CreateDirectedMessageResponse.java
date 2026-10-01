package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDirectedMessageResponse {

    private String taskId;
    private Integer recipientCount;
    private String status;
    private String statusCode;
    private String createdAt;
}
