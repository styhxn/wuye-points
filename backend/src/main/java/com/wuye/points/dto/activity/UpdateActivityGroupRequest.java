package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新活动小组请求")
public class UpdateActivityGroupRequest {

    @Schema(description = "小组名称", example = "周末跑步团")
    private String name;

    @Schema(description = "小组描述", example = "一起跑步，一起锻炼")
    private String description;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @Schema(description = "小区ID", example = "community_001")
    private String communityId;

    @Schema(description = "月费金额", example = "10.00")
    private BigDecimal monthlyFee;

    @Schema(description = "年费金额", example = "100.00")
    private BigDecimal yearlyFee;

    @Schema(description = "小组状态", example = "active")
    private String status;
}
