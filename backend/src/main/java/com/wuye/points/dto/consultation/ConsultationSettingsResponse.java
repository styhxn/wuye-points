package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "咨询平台配置")
public class ConsultationSettingsResponse {

    private BigDecimal commissionRate;
}
