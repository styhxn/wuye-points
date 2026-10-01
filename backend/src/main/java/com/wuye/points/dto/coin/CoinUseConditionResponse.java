package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "物业币使用条件响应")
public class CoinUseConditionResponse {

    @Schema(description = "使用条件：none/point_threshold/property_fee_paid/manual_issue",
            example = "point_threshold")
    private String condition;

    @Schema(description = "积分门槛（condition=point_threshold 时生效）", example = "100")
    private Integer pointThreshold;

    @Schema(description = "当前用户是否满足使用条件", example = "true")
    private Boolean satisfied;

    @Schema(description = "是否具备物业币使用资格（satisfied 且物业币未冻结）", example = "true")
    private Boolean eligible;
}
