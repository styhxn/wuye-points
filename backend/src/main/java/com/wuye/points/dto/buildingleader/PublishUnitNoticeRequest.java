package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "楼长发布单元通知请求")
public class PublishUnitNoticeRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    @Schema(description = "通知标题", example = "本周六本单元楼道清洁", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "content不能为空")
    @Schema(description = "通知正文", example = "请各位住户把杂物收进屋内，谢谢配合",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Size(max = 9, message = "coverUrls最多9张")
    @Schema(description = "封面图片URL列表（可选）")
    private List<String> coverUrls;
}
