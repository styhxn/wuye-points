package com.wuye.points.dto.opinion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "意见箱详情响应")
public class OpinionBoxDetailResponse {

    @Schema(description = "意见箱ID", example = "oc_001")
    private String id;

    @Schema(description = "意见箱标题", example = "小区绿化改造建议")
    private String title;

    @Schema(description = "意见箱内容", example = "建议对小区中心花园进行绿化升级改造")
    private String content;

    @Schema(description = "发布者ID", example = "u_001")
    private String publisherId;

    @Schema(description = "发布者姓名", example = "张三")
    private String publisherName;

    @Schema(description = "家庭ID", example = "f_001")
    private String familyId;

    @Schema(description = "物业公司ID", example = "pc_001")
    private String propertyCompanyId;

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2025-01-31 23:59:59")
    private String endTime;

    @Schema(description = "总投票数", example = "120")
    private Integer totalVotes;

    @Schema(description = "赞成票数", example = "80")
    private Integer supportCount;

    @Schema(description = "反对票数", example = "30")
    private Integer opposeCount;

    @Schema(description = "弃权票数", example = "10")
    private Integer abstainCount;

    @Schema(description = "意见箱状态", example = "ongoing")
    private String status;

    @Schema(description = "当前用户是否已投票", example = "true")
    private Boolean hasVoted;

    @Schema(description = "当前用户的投票选项", example = "support")
    private String myVoteOption;

    @Schema(description = "结果是否已发布", example = "false")
    private Boolean resultPublished;

    @Schema(description = "创建时间", example = "2025-01-01 10:00:00")
    private String createdAt;

    @Schema(description = "更新时间", example = "2025-01-15 10:30:00")
    private String updatedAt;
}
