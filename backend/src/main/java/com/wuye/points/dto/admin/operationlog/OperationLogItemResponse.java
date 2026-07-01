package com.wuye.points.dto.admin.operationlog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationLogItemResponse {

    private String id;
    private String operatorId;
    private String operatorName;
    private String operatorRole;
    private String propertyCompanyId;
    private String propertyCompanyName;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String beforeData;
    private String afterData;
    private String ipAddress;
    private String userAgent;
    private String createdAt;
}
