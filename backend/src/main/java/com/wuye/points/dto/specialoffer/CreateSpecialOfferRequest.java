package com.wuye.points.dto.specialoffer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建/更新特惠推送请求")
public class CreateSpecialOfferRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新业主专属优惠")
    private String title;

    @NotBlank(message = "content不能为空")
    @Schema(description = "活动内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "新业主首次消费享8折优惠")
    private String content;

    @NotBlank(message = "targetType不能为空")
    @Schema(description = "推送对象类型：all（全体业主）/ building（指定小区）/ role（指定商户）/ tag（指定标签）；"
            + "亦支持中文「全体业主」「指定小区」「指定商户」「指定标签」",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "all")
    private String targetType;

    @Schema(description = "目标标签，逗号分隔", example = "新业主,首套房")
    private String targetTags;

    @Schema(description = "小区ID（targetType=building 时必填）", example = "com_u7v8w")
    private String communityId;

    @Schema(description = "商户ID（targetType=role 时必填）", example = "mer_p5q6r")
    private String merchantId;

    @Schema(description = "封面图URL", example = "https://cdn.example.com/covers/offer.jpg")
    private String coverUrl;

    @Schema(description = "优惠信息", example = "满100减20")
    private String discountInfo;

    @Schema(description = "最低消费门槛（不传则无门槛）", example = "50.00")
    @DecimalMin(value = "0.00", message = "minConsumption不能为负数")
    private BigDecimal minConsumption;

    @Schema(description = "总配额（不传则不限）", example = "100")
    @Min(value = 1, message = "totalQuota最小为1")
    private Integer totalQuota;

    @Schema(description = "每人限领数量（不传则不限）", example = "1")
    @Min(value = 1, message = "perUserQuota最小为1")
    private Integer perUserQuota;

    @Schema(description = "开始时间，支持 ISO8601（2026-09-08T00:00:00.000Z）、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd",
            example = "2026-09-08T00:00:00.000Z")
    private String startTime;

    @Schema(description = "结束时间，支持 ISO8601（2026-09-15T23:59:59.000Z）、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd"
            + "（纯日期按当天 23:59:59）", example = "2026-09-15T23:59:59.000Z")
    private String endTime;

    @Schema(description = "活动状态：draft（草稿，仅管理端可见）/ published（已发布，住户端可见）/ ended（已结束）/ archived（已归档）；"
            + "亦支持中文「草稿」「待发布」「已发布」「已结束」「已归档」与历史别名 active。默认 draft",
            example = "published")
    private String status;
}
