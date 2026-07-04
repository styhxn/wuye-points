package com.wuye.points.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "积分转移响应")
public class TransferPointResponse {

    @Schema(description = "记录ID", example = "pr_abc123")
    private String recordId;

    @Schema(description = "转出成员ID", example = "res_b7m9x1")
    private String fromMemberId;

    @Schema(description = "接收成员ID", example = "res_c8n0y2")
    private String toMemberId;

    @Schema(description = "转移数量", example = "100")
    private Integer amount;

    @Schema(description = "转出后余额", example = "400")
    private Integer fromBalance;

    @Schema(description = "接收后余额", example = "600")
    private Integer toBalance;

    @Schema(description = "转移时间", example = "2026-01-01T00:00:00.000Z")
    private String transferredAt;
}
