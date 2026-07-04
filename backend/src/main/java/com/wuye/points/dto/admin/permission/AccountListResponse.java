package com.wuye.points.dto.admin.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "账户列表响应")
public class AccountListResponse {
    @Schema(description = "账户列表", example = "[]")
    private List<AccountListItemResponse> accounts;

    @Schema(description = "账户总数", example = "100")
    private int total;
}
