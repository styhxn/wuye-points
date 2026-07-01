package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointRecordItemResponse {

    private String id;
    private String residentId;
    private String familyId;
    private String pointType;
    private Integer amount;
    private Integer balance;
    private String source;
    private String sourceId;
    private String description;
    private String operatorId;
    private String createdAt;
}
