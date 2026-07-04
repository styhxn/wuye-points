package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "取消订单请求")
public class CancelOrderRequest {

    @NotBlank(message = "取消原因不能为空")
    @Size(min = 1, max = 200, message = "取消原因长度需在1-200之间")
    @Schema(description = "取消原因", required = true, example = "商品不需要了")
    private String reason;
}
