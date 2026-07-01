package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionOverrideItemResponse {
    private String permissionCode;
    private Boolean granted;
    private String source;
    private String operatorId;
    private String updatedAt;
}
