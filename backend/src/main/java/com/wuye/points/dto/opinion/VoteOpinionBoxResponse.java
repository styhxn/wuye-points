package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "投票结果响应")
public class VoteOpinionBoxResponse {

    @Schema(description = "投票记录ID", example = "vr_001")
    private String voteRecordId;

    @Schema(description = "意见箱ID", example = "oc_001")
    private String opinionBoxId;

    @Schema(description = "家庭ID", example = "f_001")
    private String familyId;

    @Schema(description = "投票选项", example = "support")
    private String voteOption;

    @Schema(description = "总投票数", example = "121")
    private Integer totalVotes;

    @Schema(description = "赞成票数", example = "81")
    private Integer supportCount;

    @Schema(description = "反对票数", example = "30")
    private Integer opposeCount;

    @Schema(description = "弃权票数", example = "10")
    private Integer abstainCount;

    @Schema(description = "投票时间", example = "2025-01-15 10:30:00")
    private String votedAt;
}
