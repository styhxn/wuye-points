package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "公告详情响应")
public class AnnouncementDetailResponse {

    @Schema(description = "公告ID", example = "ann_001")
    private String id;

    @Schema(description = "公告标题", example = "小区停水通知")
    private String title;

    @Schema(description = "公告内容", example = "因管道维修，明天上午停水")
    private String content;

    @Schema(description = "公告类型", example = "notice")
    private String announcementType;

    @Schema(description = "发布者ID", example = "user_001")
    private String publisherId;

    @Schema(description = "发布者姓名", example = "物业管理员")
    private String publisherName;

    @Schema(description = "发布者角色", example = "property_admin")
    private String publisherRole;

    @Schema(description = "封面图片URL列表")
    private List<String> coverUrls;

    @Schema(description = "是否开启收集", example = "true")
    private Boolean collectEnabled;

    @Schema(description = "收集截止时间", example = "2024-01-31T23:59:59")
    private String collectDeadline;

    @Schema(description = "当前用户是否已收集", example = "false")
    private Boolean hasCollected;

    @Schema(description = "阅读状态", example = "read")
    private String readStatus;

    @Schema(description = "发布时间", example = "2024-01-01T10:00:00")
    private String publishedAt;

    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private String createdAt;

    @Schema(description = "物业公司ID", example = "property_001")
    private String propertyCompanyId;

    @Schema(description = "小区ID", example = "community_001")
    private String communityId;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "关联活动组ID（活动公告专用）", example = "act_001")
    private String activityGroupId;

    @Schema(description = "目标角色列表", example = "{\"owner\", \"tenant\"}")
    private List<String> targetRoles;

    @Schema(description = "目标楼栋列表", example = "{\"1栋\", \"2栋\"}")
    private List<String> targetBuildings;

    @Schema(description = "目标单元列表（楼长单元通知为本单元；空=不限单元）", example = "{\"5单元\"}")
    private List<String> targetUnits;

    @Schema(description = "内容类型：notification/product/activity", example = "notification")
    private String contentType;

    @Schema(description = "关联商品ID（仅 contentType=product 时有值）", example = "prd_001")
    private String productId;

    @Schema(description = "投放渠道：announcement_board/chat/both", example = "announcement_board")
    private String deliveryChannel;

    @Schema(description = "收集字段配置列表")
    private List<CollectFieldResponse> collectFields;

    @Schema(description = "公告状态", example = "published")
    private String status;

    @Schema(description = "更新时间", example = "2024-01-01T10:00:00")
    private String updatedAt;
}
