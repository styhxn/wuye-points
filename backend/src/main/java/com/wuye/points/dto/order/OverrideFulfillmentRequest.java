package com.wuye.points.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员强制改履约方式请求（需求《商家履约方式选择与自配分账》）。
 *
 * <p>mode 取值：{@code merchant_self} / {@code courier_hall}；remark 记录改派原因（留痕）。
 */
@Data
public class OverrideFulfillmentRequest {

    @NotBlank(message = "履约方式不能为空")
    private String mode;

    @Size(max = 200, message = "改派说明不超过200字")
    private String remark;
}
