package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容举报")
public class ContentReportResponse {

    @Schema(description = "举报ID")
    private String id;

    @Schema(description = "举报人ID")
    private String reporterId;

    @Schema(description = "举报人昵称")
    private String reporterName;

    @Schema(description = "举报对象类型 post/comment")
    private String targetType;

    @Schema(description = "举报对象ID")
    private String targetId;

    @Schema(description = "举报原因类型")
    private String reasonType;

    @Schema(description = "举报说明")
    private String reasonDetail;

    @Schema(description = "状态 pending/accepted/rejected")
    private String status;

    @Schema(description = "所属小区ID")
    private String communityId;

    @Schema(description = "所属物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "处理人ID")
    private String handlerId;

    @Schema(description = "处理人昵称")
    private String handlerName;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "处理时间(ISO8601)")
    private String handledAt;

    @Schema(description = "创建时间(ISO8601)")
    private String createdAt;

    @Schema(description = "被举报内容摘要")
    private String targetContent;

    @Schema(description = "被举报内容作者昵称")
    private String targetAuthorName;
}
