package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundRequestDTO {

    @NotBlank(message = "退货原因不能为空")
    @Size(max = 200, message = "退货原因不超过200字")
    @Schema(description = "退货原因", example = "商品与描述不符", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Size(max = 500, message = "补充说明不超过500字")
    @Schema(description = "补充说明（选填）", example = "请提供退货单号")
    private String remark;
}
