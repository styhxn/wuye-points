package com.wuye.points.dto.admin.offlinepoint;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfflinePointRecordItemResponse {

    private String id;
    private String residentId;
    private String residentName;
    private Integer pointAmount;
    private Integer balanceAfter;
    private String source;
    private String description;
    private String operatorId;
    private String operatorName;
    private String createdAt;
}
