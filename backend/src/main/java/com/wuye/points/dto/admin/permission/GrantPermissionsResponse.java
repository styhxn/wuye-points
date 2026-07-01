package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GrantPermissionsResponse {
    private String accountId;
    private Integer grantedCount;
    private List<String> grantedCodes;
    private String operatorId;
    private String createdAt;
}
