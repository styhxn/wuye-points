package com.wuye.points.dto.specialoffer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "优惠活动详情响应")
public class SpecialOfferDetailResponse {

    @Schema(description = "活动ID", example = "so_x1y2z")
    private String id;

    @Schema(description = "活动标题", example = "新业主专属优惠")
    private String title;

    @Schema(description = "活动内容", example = "新业主首次消费享8折优惠")
    private String content;

    @Schema(description = "目标类型", example = "全体业主")
    private String targetType;

    @Schema(description = "目标标签列表", example = "[\"新业主\",\"首套房\"]")
    private List<String> targetTags;

    @Schema(description = "发布者ID", example = "admin_a3b4c")
    private String publisherId;

    @Schema(description = "发布者姓名", example = "张经理")
    private String publisherName;

    @Schema(description = "发布者角色", example = "物业管理员")
    private String publisherRole;

    @Schema(description = "物业公司ID", example = "pco_r4t5y")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "com_u7v8w")
    private String communityId;

    @Schema(description = "商户ID", example = "mer_p5q6r")
    private String merchantId;

    @Schema(description = "商户名称", example = "家政服务中心")
    private String merchantName;

    @Schema(description = "封面图URL", example = "https://cdn.example.com/covers/offer.jpg")
    private String coverUrl;

    @Schema(description = "优惠信息", example = "满100减20")
    private String discountInfo;

    @Schema(description = "开始时间", example = "2026-01-01T00:00:00.000Z")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-01-31T23:59:59.000Z")
    private String endTime;

    @Schema(description = "活动状态", example = "已发布")
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;

    @Schema(description = "更新时间", example = "2026-01-15T10:30:00.000Z")
    private String updatedAt;
}
