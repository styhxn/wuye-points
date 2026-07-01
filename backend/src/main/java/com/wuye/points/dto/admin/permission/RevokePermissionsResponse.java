package com.wuye.points.dto.admin.permission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RevokePermissionsResponse {
    private String accountId;
    private Integer revokedCount;
    private List<String> revokedCodes;
    private String operatorId;
    private String createdAt;
}
