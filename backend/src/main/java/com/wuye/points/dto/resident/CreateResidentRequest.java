package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建住户请求")
public class CreateResidentRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Size(max = 500, message = "头像URL长度不能超过500")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Size(max = 20, message = "婚姻状态无效")
    @Schema(description = "婚姻状态", example = "已婚")
    private String maritalStatus;

    @Schema(description = "是否有子女", example = "true")
    private Boolean hasChildren;

    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "owner|tenant", message = "用户类型只能是owner或tenant")
    @Schema(description = "用户类型", example = "owner", allowableValues = {"owner", "tenant"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String userType;

    @NotBlank(message = "角色不能为空")
    @Schema(description = "角色", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @NotBlank(message = "物业公司ID不能为空")
    @Schema(description = "物业公司ID", example = "pc_abc123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String propertyCompanyId;

    @NotBlank(message = "小区ID不能为空")
    @Schema(description = "小区ID", example = "com_def456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String communityId;

    @Size(max = 20, message = "楼栋长度不能超过20")
    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Size(max = 20, message = "单元长度不能超过20")
    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Size(max = 20, message = "房号长度不能超过20")
    @Schema(description = "房号", example = "101")
    private String room;
}
