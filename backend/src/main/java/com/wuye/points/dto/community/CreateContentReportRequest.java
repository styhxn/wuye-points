package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "举报请求")
public class CreateContentReportRequest {

    @Schema(description = "举报对象类型 post/comment", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报对象类型不能为空")
    private String targetType;

    @Schema(description = "举报对象ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报对象ID不能为空")
    private String targetId;

    @Schema(description = "举报原因类型 spam/abuse/politics/porn/other")
    private String reasonType;

    @Schema(description = "举报说明")
    @Size(max = 500, message = "举报说明不能超过500字")
    private String reasonDetail;
}
