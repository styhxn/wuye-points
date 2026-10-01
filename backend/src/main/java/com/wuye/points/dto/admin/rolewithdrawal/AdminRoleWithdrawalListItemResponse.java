package com.wuye.points.dto.admin.rolewithdrawal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "配送员/多角色提现列表项响应")
@Data
@Builder
public class AdminRoleWithdrawalListItemResponse {

    @Schema(description = "提现记录ID", example = "wr_xxx")
    private String id;

    @Schema(description = "提现类型", example = "courier", allowableValues = {
            "individual_leader", "courier", "coordinator", "sector_leader"})
    private String withdrawalType;

    @Schema(description = "申请人主体ID（cm_/il_/coo_/sec_/res_）", example = "cm_demo001")
    private String applicantId;

    @Schema(description = "申请人姓名", example = "吴十")
    private String applicantName;

    @Schema(description = "关联住户ID", example = "res_courier01")
    private String residentId;

    @Schema(description = "关联住户姓名", example = "吴十")
    private String residentName;

    @Schema(description = "关联住户手机号", example = "13900000016")
    private String residentPhone;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "提现金额（元）", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "手续费比例", example = "0.0200")
    private BigDecimal feeRate;

    @Schema(description = "手续费（元）", example = "2.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额（元）", example = "98.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核状态", example = "pending", allowableValues = {
            "pending", "approved", "rejected", "completed"})
    private String auditStatus;

    @Schema(description = "审核人ID", example = "res_admin01")
    private String auditorId;

    @Schema(description = "审核时间", example = "2026-07-01 14:30:00")
    private String auditedAt;

    @Schema(description = "完成时间", example = "2026-07-01 15:00:00")
    private String completedAt;

    @Schema(description = "打款渠道", example = "manual", allowableValues = {"manual", "auto_wechat"})
    private String disburseChannel;

    @Schema(description = "打款状态", example = "pending", allowableValues = {"pending", "success", "failed"})
    private String disburseStatus;

    @Schema(description = "创建时间", example = "2026-07-01 10:00:00")
    private String createdAt;
}
