package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新服务订单状态请求")
public class UpdateServiceOrderStatusRequest {

    @Schema(description = "订单状态", example = "in_progress", required = true)
    @NotBlank(message = "orderStatus不能为空")
    private String orderStatus;

    @Schema(description = "技工ID（指派时必填）", example = "res_tech001")
    private String providerId;

    @Schema(description = "备注", example = "请准时到达")
    private String remark;
}
