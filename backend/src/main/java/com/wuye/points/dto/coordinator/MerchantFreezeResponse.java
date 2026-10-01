package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "商户冻结/解冻响应")
public class MerchantFreezeResponse {

    @Schema(description = "商家ID", example = "mch_xxxx")
    private String id;

    @Schema(description = "运营状态", example = "frozen")
    private String status;

    @Schema(description = "是否冻结", example = "true")
    private Boolean frozen;
}
