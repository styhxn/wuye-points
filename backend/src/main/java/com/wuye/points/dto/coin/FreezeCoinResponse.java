package com.wuye.points.dto.coin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "冻结物业币响应")
public class FreezeCoinResponse {

    @Schema(description = "记录ID", example = "fc_abc123")
    private String id;

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "用户姓名", example = "张三")
    private String residentName;

    @Schema(description = "操作类型", example = "freeze", allowableValues = {"freeze", "unfreeze"})
    private String action;

    @Schema(description = "冻结金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "冻结原因", example = "违规操作冻结")
    private String reason;

    @Schema(description = "操作人ID", example = "admin_001")
    private String operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "冻结时间", example = "2026-01-01T00:00:00.000Z")
    private String frozenAt;
}
