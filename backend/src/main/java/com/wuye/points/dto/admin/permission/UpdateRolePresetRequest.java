package com.wuye.points.dto.admin.permission;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePresetRequest {
    private String name;
    private String code;
    private String description;
    private List<String> permissionCodes;
}
