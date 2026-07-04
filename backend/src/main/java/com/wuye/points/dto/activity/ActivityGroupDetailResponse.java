package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "活动小组详情响应")
public class ActivityGroupDetailResponse {

    @Schema(description = "小组ID", example = "grp_001")
    private String id;

    @Schema(description = "小组名称", example = "周末跑步团")
    private String name;

    @Schema(description = "小组描述", example = "一起跑步，一起锻炼")
    private String description;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "组长ID", example = "user_001")
    private String leaderId;

    @Schema(description = "组长姓名", example = "张三")
    private String leaderName;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "community_001")
    private String communityId;

    @Schema(description = "小区名称", example = "幸福家园")
    private String communityName;

    @Schema(description = "成员数量", example = "25")
    private Integer memberCount;

    @Schema(description = "订阅人数", example = "15")
    private Integer subscriberCount;

    @Schema(description = "月费金额", example = "10.00")
    private BigDecimal monthlyFee;

    @Schema(description = "年费金额", example = "100.00")
    private BigDecimal yearlyFee;

    @Schema(description = "小组状态", example = "active")
    private String status;

    @Schema(description = "当前用户是否已加入", example = "true")
    private Boolean hasJoined;

    @Schema(description = "当前用户的角色", example = "member")
    private String myRole;

    @Schema(description = "创建时间", example = "2024-01-01T00:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2024-01-01T00:00:00")
    private String updatedAt;
}
