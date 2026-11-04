package com.wuye.points.dto.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "楼长分享商品给本单元请求")
public class ShareUnitProductRequest {

    @NotBlank(message = "productId不能为空")
    @Schema(description = "商品ID（须在本小区服务范围内，由 GET /building-leaders/my/shareable-products 取得）",
            example = "prd_xxxxxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @Size(max = 200, message = "remark长度不能超过200")
    @Schema(description = "楼长推荐语（可选，拼在卡片消息正文里）", example = "这家师傅很靠谱，团购更划算")
    private String remark;
}
