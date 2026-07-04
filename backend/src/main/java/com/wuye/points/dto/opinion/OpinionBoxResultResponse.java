package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "意见箱投票结果响应")
public class OpinionBoxResultResponse {

    @Schema(description = "意见箱ID", example = "oc_001")
    private String opinionBoxId;

    @Schema(description = "意见箱标题", example = "小区绿化改造建议")
    private String title;

    @Schema(description = "意见箱状态", example = "completed")
    private String status;

    @Schema(description = "总投票数", example = "120")
    private Integer totalVotes;

    @Schema(description = "赞成票数", example = "80")
    private Integer supportCount;

    @Schema(description = "反对票数", example = "30")
    private Integer opposeCount;

    @Schema(description = "弃权票数", example = "10")
    private Integer abstainCount;

    @Schema(description = "赞成率", example = "66.67")
    private BigDecimal supportRate;

    @Schema(description = "反对率", example = "25.00")
    private BigDecimal opposeRate;

    @Schema(description = "弃权率", example = "8.33")
    private BigDecimal abstainRate;

    @Schema(description = "结果是否已发布", example = "true")
    private Boolean resultPublished;

    @Schema(description = "结束时间", example = "2025-01-31 23:59:59")
    private String endTime;

    @Schema(description = "发布时间", example = "2025-02-01 10:00:00")
    private String publishedAt;
}
