package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "管理员账户响应")
public class AdminAccountResponse {

    @Schema(description = "管理员账户ID（residents.id）", example = "res_qw3e4r")
    private String id;

    @Schema(description = "管理员姓名", example = "张三")
    private String name;

    @Schema(description = "管理员手机号", example = "13800138000")
    private String phone;

    @Schema(description = "管理员角色", allowableValues = {"property_admin", "platform_admin"}, example = "property_admin")
    private String role;

    @Schema(description = "管理员邮箱", example = "admin@wuye.com")
    private String email;

    @Schema(description = "初始/重置密码（随机生成，仅创建物业公司或重置密码响应返回一次，详情不返回）",
            example = "A7kP2mQ9xR4tZ6")
    private String initialPassword;
}
