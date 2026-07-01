package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RolePresetListResponse {
    private List<RolePresetItemResponse> presets;
}
