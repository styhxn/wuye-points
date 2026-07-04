package com.wuye.points.dto.announcement;

import com.wuye.points.dto.common.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "公告收集列表响应")
@Data
@Builder
public class AnnouncementCollectListResponse {

    @Schema(description = "公告ID", example = "ann_xxx")
    private String announcementId;

    @Schema(description = "公告标题", example = "重要通知")
    private String announcementTitle;

    @Schema(description = "总回复数", example = "10")
    private long totalResponses;

    @Schema(description = "回复率", example = "0.85")
    private BigDecimal responseRate;

    @Schema(description = "字段定义列表")
    private List<CollectFieldResponse> fields;

    @Schema(description = "回复列表")
    private List<CollectItemResponse> list;

    @Schema(description = "分页信息")
    private PageResponse.Pagination pagination;

    @Schema(description = "收集回复项")
    @Data
    @Builder
    public static class CollectItemResponse {

        @Schema(description = "ID", example = "coll_xxx")
        private String id;

        @Schema(description = "住户ID", example = "res_xxx")
        private String residentId;

        @Schema(description = "住户姓名", example = "张三")
        private String residentName;

        @Schema(description = "手机号", example = "13800138000")
        private String phone;

        @Schema(description = "楼栋", example = "1栋")
        private String building;

        @Schema(description = "收集的数据", example = "{\"name\": \"张三\"}")
        private Map<String, Object> collectedData;

        @Schema(description = "提交时间", example = "2026-01-01T00:00:00.000Z")
        private String submittedAt;
    }
}
