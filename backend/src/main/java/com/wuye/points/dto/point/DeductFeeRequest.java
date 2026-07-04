package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "积分抵扣物业费请求")
public class DeductFeeRequest {

    @NotBlank(message = "物业费账单ID不能为空")
    @Schema(description = "物业费账单ID", required = true, example = "pf_abc123")
    private String propertyFeeId;

    @NotNull(message = "抵扣积分数量不能为空")
    @Min(value = 1, message = "抵扣积分数量至少为1")
    @Schema(description = "抵扣积分数量", required = true, example = "100")
    private Integer points;

    @Schema(description = "家庭ID", example = "fam_abc123")
    private String familyId;
}
