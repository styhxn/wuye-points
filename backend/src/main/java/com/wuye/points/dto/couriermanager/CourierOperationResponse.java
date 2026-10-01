package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "快递负责人操作结果响应")
public class CourierOperationResponse {

    @Schema(description = "负责人ID", example = "cm_xxx")
    private String courierManagerId;

    @Schema(description = "快递员业主ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "操作", example = "added")
    private String operation;
}
