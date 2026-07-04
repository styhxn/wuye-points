package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "踢出商家响应")
@Data
@Builder
public class KickMerchantResponse {

    @Schema(description = "商家ID", example = "merchant_001")
    private String id;

    @Schema(description = "当前状态", example = "kicked")
    private String status;

    @Schema(description = "踢出原因", example = "违规经营")
    private String reason;

    @Schema(description = "踢出时间", example = "2026-07-01 14:30:00")
    private String kickedAt;
}
