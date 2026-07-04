package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "住户列表项响应")
public class ResidentListItemResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Integer gender;

    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Schema(description = "婚姻状态", example = "已婚")
    private String maritalStatus;

    @Schema(description = "是否有子女", example = "true")
    private Boolean hasChildren;

    @Schema(description = "角色", example = "user")
    private String role;

    @Schema(description = "用户类型", example = "owner", allowableValues = {"owner", "tenant"})
    private String userType;

    @Schema(description = "物业公司ID", example = "pc_abc123")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "com_def456")
    private String communityId;

    @Schema(description = "小区名称", example = "万科城市花园")
    private String communityName;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "家庭ID", example = "fam_ghi789")
    private String familyId;

    @Schema(description = "积分余额", example = "1000")
    private Integer pointBalance;

    @Schema(description = "金币余额", example = "500.00")
    private BigDecimal coinBalance;

    @Schema(description = "金币是否冻结", example = "false")
    private Boolean coinFrozen;

    @Schema(description = "住户状态", example = "active", allowableValues = {"active", "frozen", "disabled"})
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
