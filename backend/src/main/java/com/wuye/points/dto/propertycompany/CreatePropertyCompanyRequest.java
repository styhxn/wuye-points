package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建物业公司请求")
public class CreatePropertyCompanyRequest {

    @NotBlank(message = "物业公司名称不能为空")
    @Schema(description = "物业公司名称", required = true, example = "万科物业")
    private String name;

    @Schema(description = "物业公司Logo地址", example = "https://example.com/logo.png")
    private String logoUrl;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", required = true, example = "13800138000")
    private String contactPhone;

    @Schema(description = "公司地址", example = "北京市朝阳区建国路88号")
    private String address;

    @NotEmpty(message = "至少需要创建一个小区")
    @Valid
    @Schema(description = "小区列表", required = true)
    private List<CreateCommunityRequest> communities;

    @NotNull(message = "adminAccount不能为空")
    @Valid
    @Schema(description = "管理员账户信息", required = true)
    private CreateAdminAccountRequest adminAccount;

    @Data
    @Schema(description = "创建小区请求")
    public static class CreateCommunityRequest {

        @NotBlank(message = "小区名称不能为空")
        @Schema(description = "小区名称", required = true, example = "万科城市花园")
        private String name;

        @Schema(description = "小区地址", example = "北京市朝阳区建国路88号")
        private String address;

        @Schema(description = "楼栋总数", example = "10")
        private Integer totalBuildings;

        @Schema(description = "单元总数", example = "300")
        private Integer totalUnits;
    }

    @Data
    @Schema(description = "创建管理员账户请求")
    public static class CreateAdminAccountRequest {

        @NotBlank(message = "管理员姓名不能为空")
        @Schema(description = "管理员姓名", required = true, example = "张三")
        private String name;

        @NotBlank(message = "管理员手机号不能为空")
        @Schema(description = "管理员手机号", required = true, example = "13800138000")
        private String phone;

        @Schema(description = "管理员邮箱", example = "admin@wuye.com")
        private String email;
    }
}
