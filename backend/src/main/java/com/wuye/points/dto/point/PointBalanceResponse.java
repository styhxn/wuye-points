package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointBalanceResponse {

    private Integer pointBalance;
    private Integer familyPointBalance;
    private String familyId;
    private Integer todayEarned;
    private Integer todaySpent;
    private Integer expiringPoints;
    private String expiringDate;
}
