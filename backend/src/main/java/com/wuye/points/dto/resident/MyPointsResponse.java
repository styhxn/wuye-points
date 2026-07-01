package com.wuye.points.dto.resident;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyPointsResponse {

    private String residentId;
    private Integer pointBalance;
    private String familyId;
    private Integer familyPointBalance;
    private Integer earnedThisMonth;
    private Integer spentThisMonth;
    private Integer expiringSoon;
    private String expiryDate;
}
