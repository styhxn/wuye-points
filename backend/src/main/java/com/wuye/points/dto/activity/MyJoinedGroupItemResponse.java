package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "我加入的活动组列表项响应")
public class MyJoinedGroupItemResponse {

    @Schema(description = "成员ID", example = "agm_001")
    private String memberId;

    @Schema(description = "小组ID", example = "grp_001")
    private String groupId;

    @Schema(description = "小组名称", example = "周末跑步团")
    private String groupName;

    @Schema(description = "小组封面", example = "https://example.com/cover.jpg")
    private String groupCover;

    @Schema(description = "角色", example = "member")
    private String role;

    @Schema(description = "成员数量", example = "25")
    private Integer memberCount;

    @Schema(description = "月费金额", example = "10.00")
    private BigDecimal monthlyFee;

    @Schema(description = "年费金额", example = "100.00")
    private BigDecimal yearlyFee;

    @Schema(description = "组长ID", example = "user_001")
    private String leaderId;

    @Schema(description = "组长姓名", example = "张三")
    private String leaderName;

    @Schema(description = "小组状态", example = "active")
    private String status;

    @Schema(description = "加入时间", example = "2024-01-01T00:00:00")
    private String joinedAt;
}