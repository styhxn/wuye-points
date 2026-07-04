package com.wuye.points.dto.specialoffer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建优惠活动请求")
public class CreateSpecialOfferRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新业主专属优惠")
    private String title;

    @NotBlank(message = "content不能为空")
    @Schema(description = "活动内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "新业主首次消费享8折优惠")
    private String content;

    @NotBlank(message = "targetType不能为空")
    @Schema(description = "目标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "全体业主", allowableValues = {"全体业主", "指定小区", "指定商户"})
    private String targetType;

    @Schema(description = "目标标签，逗号分隔", example = "新业主,首套房")
    private String targetTags;

    @Schema(description = "小区ID", example = "com_u7v8w")
    private String communityId;

    @Schema(description = "商户ID", example = "mer_p5q6r")
    private String merchantId;

    @Schema(description = "封面图URL", example = "https://cdn.example.com/covers/offer.jpg")
    private String coverUrl;

    @Schema(description = "优惠信息", example = "满100减20")
    private String discountInfo;

    @Schema(description = "开始时间", example = "2026-01-01T00:00:00.000Z")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-01-31T23:59:59.000Z")
    private String endTime;

    @Schema(description = "活动状态", example = "待发布", allowableValues = {"草稿", "待发布", "已发布", "已结束"})
    private String status;
}
