package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionChangeLogItemResponse {
    private String id;
    private String residentId;
    private String residentName;
    private String operatorId;
    private String operatorName;
    private String action;
    private String permissionCode;
    private String presetCode;
    private String beforeValue;
    private String afterValue;
    private String reason;
    private String createdAt;
}
