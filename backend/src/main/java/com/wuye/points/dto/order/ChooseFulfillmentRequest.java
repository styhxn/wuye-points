package com.wuye.points.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商家选择履约方式请求（需求《商家履约方式选择与自配分账》）。
 *
 * <p>mode 取值：
 * <ul>
 *   <li>{@code merchant_self}：商家自配（配送费分成归商家，平台抽成另计）；</li>
 *   <li>{@code courier_hall}：发布到抢单大厅（平台配送员承运）。</li>
 * </ul>
 */
@Data
public class ChooseFulfillmentRequest {

    @NotBlank(message = "履约方式不能为空")
    private String mode;
}
