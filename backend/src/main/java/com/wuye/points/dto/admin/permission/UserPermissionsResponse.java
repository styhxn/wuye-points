package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserPermissionsResponse {
    private String accountId;
    private String accountName;
    private AppliedPresetResponse appliedPreset;
    private List<PermissionOverrideItemResponse> overrides;
    private List<String> effectivePermissions;
    private Integer effectivePermissionCount;
    private String updatedAt;
}
