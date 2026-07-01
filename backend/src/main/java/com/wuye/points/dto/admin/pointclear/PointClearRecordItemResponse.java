package com.wuye.points.dto.admin.pointclear;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PointClearRecordItemResponse {

    private String id;
    private String residentId;
    private String residentName;
    private Integer clearedPoints;
    private BigDecimal equivalentAmount;
    private BigDecimal poolInAmount;
    private String operatorId;
    private String createdAt;
}
