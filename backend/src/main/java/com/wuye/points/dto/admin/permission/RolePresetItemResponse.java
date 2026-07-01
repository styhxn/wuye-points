package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RolePresetItemResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private String propertyCompanyId;
    private Boolean isBuiltin;
    private List<String> permissionCodes;
    private Integer permissionCount;
    private String createdAt;
}
