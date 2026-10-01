package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新咨询平台配置")
public class UpdateConsultationSettingsRequest {

    @Schema(description = "平台抽成比例 0-1")
    private BigDecimal commissionRate;
}
