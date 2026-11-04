package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "楼长单元通知列表项")
public class UnitNoticeItemResponse {

    @Schema(description = "公告ID", example = "ann_xxxxxxxx")
    private String id;

    @Schema(description = "通知标题", example = "本周六本单元楼道清洁")
    private String title;

    @Schema(description = "通知正文（列表返回全文，长度不截断）")
    private String content;

    @Schema(description = "内容类型：notification=单元通知，product=商品分享", example = "notification")
    private String contentType;

    @Schema(description = "定向单元列表（楼长发布固定为本单元）", example = "[\"5单元\"]")
    private List<String> targetUnits;

    @Schema(description = "投放渠道：announcement_board/chat/both", example = "announcement_board")
    private String deliveryChannel;

    @Schema(description = "关联商品ID（仅 contentType=product 时有值）", example = "prd_xxxxxxxx")
    private String productId;

    @Schema(description = "状态", example = "published")
    private String status;

    @Schema(description = "发布时间（UTC ISO8601）", example = "2026-11-03T10:00:00Z")
    private String publishedAt;

    @Schema(description = "创建时间（UTC ISO8601）", example = "2026-11-03T10:00:00Z")
    private String createdAt;
}
