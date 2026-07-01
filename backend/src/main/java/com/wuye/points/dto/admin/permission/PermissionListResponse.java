package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PermissionListResponse {
    private List<PermissionItemResponse> permissions;
    private List<String> modules;
}
