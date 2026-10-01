package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "可选配送员")
public class CourierOptionResponse {

    private String id;
    private String courierId;
    private String courierName;
    private String status;
    private String createdAt;
}
