package com.wuye.points.dto.merchantad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "商家广告额度包")
public class MerchantAdPackageResponse {

    private String id;
    private String name;
    private Integer weeklyQuota;
    private BigDecimal price;
}
