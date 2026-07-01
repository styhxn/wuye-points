package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionItemResponse {
    private String id;
    private String code;
    private String name;
    private String module;
    private String description;
    private Boolean isPlatformOnly;
    private String status;
}
