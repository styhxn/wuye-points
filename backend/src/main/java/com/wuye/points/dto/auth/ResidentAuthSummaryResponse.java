package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "业主认证摘要响应")
public class ResidentAuthSummaryResponse {

    @Schema(description = "用户ID", example = "u_001")
    private String id;

    @Schema(description = "用户姓名", example = "张三")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "手机号", example = "138****8888")
    private String phone;

    @Schema(description = "用户角色", example = "resident", allowableValues = {"resident", "property_admin", "platform_admin", "community_admin", "merchant", "courier", "coordinator", "sector_leader", "individual_leader", "activity_leader", "technician"})
    private String role;

    @Schema(description = "是否具备管理端工作台身份（平台/物业/社区管理员，或持组长/负责人标签的住户）", example = "true")
    private Boolean adminWorkspace;

    @Schema(description = "是否为单元楼长（住户子身份，role 仍为 resident）", example = "false")
    private Boolean isBuildingLeader;

    @Schema(description = "是否为统筹（负责人标签：住户子身份，身份由 coordinators 表 active 记录判定）", example = "false")
    private Boolean isCoordinator;

    @Schema(description = "统筹ID（非统筹时为 null）", example = "coo_abc123")
    private String coordinatorId;

    @Schema(description = "是否为板块/区域负责人（负责人标签：住户子身份，身份由 sector_leaders 表 active 记录判定，可跨板块多个）", example = "false")
    private Boolean isSectorLeader;

    @Schema(description = "板块负责人记录ID列表（非板块负责人时为空数组）", example = "[\"sl_abc123\"]")
    private List<String> sectorLeaderIds;

    @Schema(description = "是否为个体负责人（负责人标签：住户子身份，身份由 individual_leaders 表 active 记录判定）", example = "false")
    private Boolean isIndividualLeader;

    @Schema(description = "个体负责人ID（非个体负责人时为 null）", example = "il_abc123")
    private String individualLeaderId;

    @Schema(description = "是否为活动组组长（住户子身份标签，role 仍为 resident；身份由 activity_leader_identities 表 active 记录判定）", example = "false")
    private Boolean isActivityLeader;

    @Schema(description = "活动组组长身份记录ID（非组长时为 null）", example = "ali_abc123")
    private String activityLeaderId;

    @Schema(description = "是否为业主小店店主（营业中）", example = "false")
    private Boolean isResidentMerchant;

    @Schema(description = "业主小店申请记录ID（非营业中时为 null）", example = "rma_abc123")
    private String residentMerchantId;

    @Schema(description = "是否已开通一级代理", example = "false")
    private Boolean isDistributor;

    @Schema(description = "一级代理商家载体ID（未开通时为 null）", example = "mch_abc123")
    private String distributorId;

    @Schema(description = "物业公司ID", example = "pc_001")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "comm_001")
    private String communityId;

    @Schema(description = "家庭ID", example = "f_001")
    private String familyId;

    @Schema(description = "是否已绑定微信", example = "true")
    private Boolean wechatBound;

    @Schema(description = "是否新用户", example = "false")
    private Boolean newUser;

    @Schema(description = "用户状态", example = "active", allowableValues = {"active", "frozen", "disabled"})
    private String status;
}
