package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "快递员列表响应")
public class CourierListResponse {

    @Schema(description = "负责人ID", example = "cm_xxx")
    private String courierManagerId;

    @Schema(description = "快递员列表")
    private List<CourierItemResponse> couriers;

    @Schema(description = "快递员总数", example = "5")
    private Long total;
}
