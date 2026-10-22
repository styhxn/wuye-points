package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "住户详情响应")
public class ResidentDetailResponse {

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

    @Schema(description = "物业公司名称", example = "万科物业")
    private String propertyName;

    @Schema(description = "小区ID", example = "com_def456")
    private String communityId;

    @Schema(description = "小区名称", example = "万科城市花园")
    private String communityName;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "楼层", example = "5")
    private String floor;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "家庭ID", example = "fam_ghi789")
    private String familyId;

    @Schema(description = "家庭名称", example = "张家")
    private String familyName;

    @Schema(description = "积分余额", example = "1000")
    private Integer pointBalance;

    @Schema(description = "金币余额", example = "500.00")
    private BigDecimal coinBalance;

    @Schema(description = "金币是否冻结", example = "false")
    private Boolean coinFrozen;

    @Schema(description = "累计消费金额", example = "1200.50")
    private BigDecimal totalConsumption;

    @Schema(description = "累计订单数", example = "25")
    private Long totalOrders;

    @Schema(description = "是否绑定微信", example = "true")
    private Boolean wechatBound;

    @Schema(description = "住户状态", example = "active", allowableValues = {"active", "frozen", "disabled"})
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;

    @Schema(description = "是否准时缴费用户", example = "true")
    private Boolean onTimePaymentBadge;

    @Schema(description = "是否已开通一级代理（名下有 is_distributor=1 且审核通过的商家载体）", example = "false")
    private Boolean isDistributor;

    @Schema(description = "是否为统筹（负责人标签：住户子身份，role 仍为 resident；身份由 coordinators 表 active 记录判定）", example = "false")
    private Boolean isCoordinator;

    @Schema(description = "统筹ID（非统筹时为 null）", example = "coo_abc123")
    private String coordinatorId;

    @Schema(description = "是否为板块/区域负责人（负责人标签：住户子身份，身份由 sector_leaders 表 active 记录判定，可跨板块多个）", example = "false")
    private Boolean isSectorLeader;

    @Schema(description = "板块负责人记录ID列表（非板块负责人时为空数组；同一住户可负责多个板块）", example = "[\"sl_abc123\"]")
    private List<String> sectorLeaderIds;

    @Schema(description = "是否为个体负责人（负责人标签：住户子身份，身份由 individual_leaders 表 active 记录判定）", example = "false")
    private Boolean isIndividualLeader;

    @Schema(description = "个体负责人ID（非个体负责人时为 null）", example = "il_abc123")
    private String individualLeaderId;

    @Schema(description = "是否为活动组组长（住户子身份标签，role 仍为 resident；身份由 activity_leader_identities 表 active 记录判定）", example = "false")
    private Boolean isActivityLeader;

    @Schema(description = "活动组组长身份记录ID（非组长时为 null）", example = "ali_abc123")
    private String activityLeaderId;

    @Schema(description = "是否为业主小店店主（营业中；身份由 resident_merchant_applications active 记录判定）", example = "false")
    private Boolean isResidentMerchant;

    @Schema(description = "业主小店申请记录ID（非营业中时为 null）", example = "rma_abc123")
    private String residentMerchantId;
}
