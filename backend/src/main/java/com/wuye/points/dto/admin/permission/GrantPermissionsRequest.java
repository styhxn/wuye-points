package com.wuye.points.dto.admin.permission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GrantPermissionsRequest {

    @NotEmpty(message = "permissionCodes不能为空")
    private List<String> permissionCodes;

    private String reason;
}
