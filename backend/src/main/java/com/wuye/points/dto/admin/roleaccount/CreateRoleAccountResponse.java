package com.wuye.points.dto.admin.roleaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 直接创建业务角色账号响应
 */
@Data
@Builder
@Schema(description = "直接创建业务角色账号响应")
public class CreateRoleAccountResponse {

    @Schema(description = "账号ID（residents.id）", example = "res_mch08")
    private String residentId;

    @Schema(description = "姓名", example = "张老板")
    private String name;

    @Schema(description = "手机号", example = "13900000008")
    private String phone;

    @Schema(description = "角色", example = "merchant")
    private String role;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "商家ID（role=merchant 时返回）", example = "mch_xxx001")
    private String merchantId;

    @Schema(description = "状态", example = "active")
    private String status;
}
