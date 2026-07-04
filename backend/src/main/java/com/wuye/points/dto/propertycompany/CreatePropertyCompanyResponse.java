package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "创建物业公司响应")
public class CreatePropertyCompanyResponse {

    @Schema(description = "物业公司ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "物业公司名称", example = "万科物业")
    private String name;

    @Schema(description = "物业公司状态", allowableValues = {"active", "inactive", "pending"}, example = "active")
    private String status;

    @Schema(description = "小区列表")
    private List<CommunityItemResponse> communities;

    @Schema(description = "管理员账户信息")
    private AdminAccountResponse adminAccount;

    @Schema(description = "默认配置", example = "{}")
    private String defaultConfigs;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
