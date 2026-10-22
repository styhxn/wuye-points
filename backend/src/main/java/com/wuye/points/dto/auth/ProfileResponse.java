package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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

    @Schema(description = "是否为单元楼长（住户子身份，role 仍为 resident）", example = "false")
    private Boolean isBuildingLeader;

    @Schema(description = "单元楼长ID（非楼长时为 null）", example = "bl_abc123")
    private String buildingLeaderId;

    @Schema(description = "是否为统筹（负责人标签：住户子身份，role 仍为 resident；身份由 coordinators 表 active 记录判定）", example = "false")
    private Boolean isCoordinator;

    @Schema(description = "统筹ID（非统筹时为 null）", example = "coo_abc123")
    private String coordinatorId;

    @Schema(description = "是否为板块/区域负责人（负责人标签：住户子身份，role 仍为 resident；身份由 sector_leaders 表 active 记录判定，可跨板块多个）", example = "false")
    private Boolean isSectorLeader;

    @Schema(description = "板块负责人记录ID列表（非板块负责人时为空数组；同一住户可负责多个板块）", example = "[\"sl_abc123\"]")
    private List<String> sectorLeaderIds;

    @Schema(description = "是否为个体负责人（负责人标签：住户子身份，role 仍为 resident；身份由 individual_leaders 表 active 记录判定）", example = "false")
    private Boolean isIndividualLeader;

    @Schema(description = "个体负责人ID（非个体负责人时为 null）", example = "il_abc123")
    private String individualLeaderId;

    @Schema(description = "是否为活动组组长（住户子身份标签，role 仍为 resident；身份由 activity_leader_identities 表 active 记录判定；供「我的」页渲染组长入口：true=进管理页，false=进申请页）", example = "false")
    private Boolean isActivityLeader;

    @Schema(description = "活动组组长身份记录ID（非组长时为 null）", example = "ali_abc123")
    private String activityLeaderId;

    @Schema(description = "是否为业主小店店主（营业中；身份由 resident_merchant_applications active 记录判定；供「我的」页渲染小店入口：true=进管理页，false=进申请页）", example = "false")
    private Boolean isResidentMerchant;

    @Schema(description = "业主小店申请记录ID（非营业中时为 null）", example = "rma_abc123")
    private String residentMerchantId;

    @Schema(description = "是否已开通一级代理（名下有 is_distributor=1 且审核通过的商家载体；供「我的」页渲染一级代理入口：true=进管理页，false=进申请页）", example = "false")
    private Boolean isDistributor;

    @Schema(description = "一级代理商家载体ID（未开通时为 null）", example = "mch_abc123")
    private String distributorId;

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

    @Schema(description = "楼层", example = "5")
    private String floor;

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
