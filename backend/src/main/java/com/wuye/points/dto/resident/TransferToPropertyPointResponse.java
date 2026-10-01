package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "住户转给物业 - 积分响应")
@Data
@Builder
public class TransferToPropertyPointResponse {

    @Schema(description = "流水ID", example = "pt_xxx")
    private String recordId;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "转出积分数量", example = "100")
    private Integer pointAmount;

    @Schema(description = "扣减后个人积分余额", example = "400")
    private Integer balanceAfter;

    @Schema(description = "转出时间", example = "2026-08-17T10:00:00Z")
    private String transferredAt;
}
