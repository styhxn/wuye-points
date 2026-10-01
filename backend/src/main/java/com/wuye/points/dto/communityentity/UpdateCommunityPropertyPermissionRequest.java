package com.wuye.points.dto.communityentity;

import lombok.Data;

import java.util.List;

@Data
public class UpdateCommunityPropertyPermissionRequest {

    private String permissionLevel;

    private List<String> allowedModules;
}
