package com.wuye.points.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "定向发放优惠券请求")
public class GrantCouponRequest {

    @NotBlank(message = "券模板ID不能为空")
    @Schema(description = "券模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "cpt_1a2b3c")
    private String templateId;

    @NotEmpty(message = "发放对象不能为空")
    @Schema(description = "发放对象（居民/业主ID列表）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> residentIds;

    @Min(value = 1, message = "每人发放张数至少为1")
    @Schema(description = "每人发放张数，默认1", example = "1")
    private Integer quantity;
}