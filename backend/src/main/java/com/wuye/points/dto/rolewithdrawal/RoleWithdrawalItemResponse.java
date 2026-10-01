package com.wuye.points.dto.rolewithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "多角色提现记录响应")
public class RoleWithdrawalItemResponse {

    @Schema(description = "提现记录ID", example = "wr_demo001")
    private String id;

    @Schema(description = "提现类型", example = "individual_leader", allowableValues = {"individual_leader", "courier"})
    private String withdrawalType;

    @Schema(description = "申请人主体ID", example = "il_demo001")
    private String applicantId;

    @Schema(description = "关联住户ID", example = "res_demo001")
    private String residentId;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "提现金额", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "手续费比例", example = "0.0060")
    private BigDecimal feeRate;

    @Schema(description = "手续费金额", example = "6.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额", example = "994.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "pending", allowableValues = {"pending", "approved", "rejected", "completed"})
    private String status;

    @Schema(description = "审核时间", example = "2026-01-02T00:00:00")
    private String auditedAt;

    @Schema(description = "完成时间", example = "2026-01-03T00:00:00")
    private String completedAt;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00")
    private String createdAt;
}
