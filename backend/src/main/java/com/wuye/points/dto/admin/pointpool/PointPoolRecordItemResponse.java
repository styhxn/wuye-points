package com.wuye.points.dto.admin.pointpool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PointPoolRecordItemResponse {

    private String id;
    private String propertyCompanyId;
    private String poolType;
    private String source;
    private BigDecimal amount;
    private BigDecimal balance;
    private String description;
    private String sourceId;
    private String operatorId;
    private String operatorName;
    private String createdAt;
}
