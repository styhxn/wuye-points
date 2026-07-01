package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExpiringPointsResponse {

    private Integer expiringPoints;
    private String expiringDate;
}
