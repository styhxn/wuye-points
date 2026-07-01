package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EarnPointResponse {

    private String recordId;
    private String residentId;
    private Integer pointAmount;
    private Integer balanceAfter;
    private Integer familyBalanceAfter;
    private String source;
    private String operatorId;
    private String createdAt;
}
