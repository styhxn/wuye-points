package com.wuye.points.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectedMessageStatsResponse {
    private String propertyCompanyId;
    private long taskCount;
    private long recipientCount;
    private long readCount;
    private long unreadCount;
    private double readRate;
    private long todayTaskCount;
    private long last7DaysTaskCount;
}
