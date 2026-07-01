package com.wuye.points.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @NotBlank(message = "取消原因不能为空")
    @Size(min = 1, max = 200, message = "取消原因长度需在1-200之间")
    private String reason;
}
