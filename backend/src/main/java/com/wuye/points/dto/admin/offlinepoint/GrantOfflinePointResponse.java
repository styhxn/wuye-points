package com.wuye.points.dto.admin.offlinepoint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrantOfflinePointResponse {

    private String recordId;
    private String residentId;
    private Integer pointAmount;
    private Integer balanceAfter;
    private Integer familyBalanceAfter;
    private String source;
    private String operatorId;
    private String createdAt;
}
