package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "更新订单状态请求")
public class UpdateOrderStatusRequest {

    @NotBlank(message = "订单状态不能为空")
    @Pattern(regexp = "pending|paid|delivering|completed|cancelled|refunding|refunded", message = "订单状态值无效")
    @Schema(description = "订单状态", required = true, example = "pending", allowableValues = {"pending", "paid", "delivering", "completed", "cancelled", "refunding", "refunded"})
    private String orderStatus;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "快递员ID", example = "courier_001")
    private String courierId;
}
