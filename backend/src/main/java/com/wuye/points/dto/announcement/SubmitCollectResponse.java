package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "提交收集信息响应")
@Data
@Builder
public class SubmitCollectResponse {

    @Schema(description = "ID", example = "coll_xxx")
    private String id;

    @Schema(description = "公告ID", example = "ann_xxx")
    private String announcementId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "提交时间", example = "2026-01-01T00:00:00.000Z")
    private String submittedAt;
}
