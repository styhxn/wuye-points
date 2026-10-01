package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "更新商家多档配送费请求")
@Data
public class UpdateDeliveryFeesRequest {

    @Schema(description = "多档配送费配置")
    @Valid
    private List<DeliveryFeeTier> deliveryFeeTiers;

    @Schema(description = "是否启用满额减免", example = "true")
    private Boolean freeDeliveryEnabled;

    @Schema(description = "免配送费门槛金额", example = "30.00")
    @DecimalMin(value = "0", message = "免配送费门槛不能为负数")
    private BigDecimal freeDeliveryThreshold;
}
