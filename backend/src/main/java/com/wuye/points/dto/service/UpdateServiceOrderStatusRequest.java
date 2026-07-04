package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新服务订单状态请求")
public class UpdateServiceOrderStatusRequest {

    @Schema(description = "订单状态", example = "已完成", required = true)
    @NotBlank(message = "orderStatus不能为空")
    private String orderStatus;
}
