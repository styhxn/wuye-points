package com.wuye.points.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NeighborChatResponse {

    private String id;
    private String fromId;
    private String toId;
    private String messageType;
    private String content;
    private String chatType;
    private String readStatus;
    private String createdAt;
    private Integer dailyLimitRemaining;
    private Integer usedToday;
    private Integer remaining;
}
