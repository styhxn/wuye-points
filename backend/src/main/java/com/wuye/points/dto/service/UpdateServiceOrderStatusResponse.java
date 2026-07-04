package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "更新服务订单状态响应")
public class UpdateServiceOrderStatusResponse {

    @Schema(description = "ID", example = "so_r8k2m4")
    private String id;

    @Schema(description = "订单状态", example = "已完成")
    private String orderStatus;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
