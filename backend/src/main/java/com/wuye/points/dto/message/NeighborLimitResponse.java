package com.wuye.points.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NeighborLimitResponse {

    private String residentId;
    private Integer dailyLimit;
    private Integer usedToday;
    private Integer remaining;
    private String resetAt;
}
