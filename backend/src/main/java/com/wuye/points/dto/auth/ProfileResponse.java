package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "用户信息响应")
public class ProfileResponse {

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String id;

    @Schema(description = "微信OpenID", example = "o_ABC123DEF456")
    private String openid;

    @Schema(description = "微信UnionID", example = "u_XYZ789ABC012")
    private String unionid;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "年龄", example = "30")
    private Integer age;

    @Schema(description = "婚姻状态", example = "married")
    private String maritalStatus;

    @Schema(description = "是否有子女", example = "true")
    private Boolean hasChildren;

    @Schema(description = "用户类型", example = "owner")
    private String userType;

    @Schema(description = "角色", example = "resident")
    private String role;

    @Schema(description = "物业公司ID", example = "pc_abc123")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称", example = "阳光物业")
    private String propertyName;

    @Schema(description = "小区ID", example = "comm_xyz789")
    private String communityId;

    @Schema(description = "小区名称", example = "阳光花园")
    private String communityName;

    @Schema(description = "楼栋", example = "1号楼")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房间号", example = "101")
    private String room;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;

    @Schema(description = "积分余额", example = "500")
    private Integer pointBalance;

    @Schema(description = "物业币余额", example = "120.50")
    private BigDecimal coinBalance;

    @Schema(description = "物业币是否冻结", example = "false")
    private Boolean coinFrozen;

    @Schema(description = "物业币是否隐藏", example = "false")
    private Boolean coinHidden;

    @Schema(description = "是否绑定微信", example = "true")
    private Boolean wechatBound;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
