package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "我的投票记录列表项")
public class MyVoteListItemResponse {

    @Schema(description = "意见箱ID", example = "oc_001")
    private String opinionBoxId;

    @Schema(description = "意见箱标题", example = "小区绿化改造建议")
    private String title;

    @Schema(description = "我的投票选项", example = "support")
    private String voteOption;

    @Schema(description = "投票时间", example = "2025-01-15 10:30:00")
    private String votedAt;

    @Schema(description = "意见箱状态", example = "ongoing")
    private String opinionBoxStatus;

    @Schema(description = "结果是否已发布", example = "false")
    private Boolean resultPublished;
}
