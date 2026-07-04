package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "更新公告请求")
public class UpdateAnnouncementRequest {

    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "公告标题", example = "小区停水通知")
    private String title;

    @Schema(description = "公告内容", example = "因管道维修，明天上午停水")
    private String content;

    @Schema(description = "公告类型", example = "notice")
    private String announcementType;

    @Schema(description = "小区ID", example = "community_001")
    private String communityId;

    @Schema(description = "商家ID", example = "merchant_001")
    private String merchantId;

    @Schema(description = "封面图片URL列表")
    private List<String> coverUrls;

    @Schema(description = "目标角色列表", example = "{\"owner\", \"tenant\"}")
    private List<String> targetRoles;

    @Schema(description = "目标楼栋列表", example = "{\"1栋\", \"2栋\"}")
    private List<String> targetBuildings;

    @Schema(description = "是否开启收集", example = "true")
    private Boolean collectEnabled;

    @Schema(description = "收集字段配置列表")
    private List<CollectFieldRequest> collectFields;

    @Schema(description = "公告状态", example = "draft")
    private String status;
}
