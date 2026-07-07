package com.wuye.points.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

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

    @Schema(description = "用户角色", example = "resident", allowableValues = {"resident", "property_admin", "platform_admin", "merchant", "courier", "coordinator", "sector_leader", "individual_leader", "activity_leader", "technician"})
    private String role;

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
