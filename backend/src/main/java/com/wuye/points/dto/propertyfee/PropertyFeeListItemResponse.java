package com.wuye.points.dto.propertyfee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业费列表项响应")
public class PropertyFeeListItemResponse {

    @Schema(description = "ID", example = "pf_r8k2m4")
    private String id;

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "家庭ID", example = "fam_k3j8n2")
    private String familyId;

    @Schema(description = "小区ID", example = "com_x9k2m")
    private String communityId;

    @Schema(description = "小区名称", example = "阳光花园")
    private String communityName;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "房间号", example = "101")
    private String room;

    @Schema(description = "费用类型", example = "物业费")
    private String feeType;

    @Schema(description = "费用类型名称", example = "物业管理费")
    private String feeTypeName;

    @Schema(description = "费用周期", example = "2026-01")
    private String period;

    @Schema(description = "费用金额", example = "1500.00")
    private BigDecimal amount;

    @Schema(description = "已付金额", example = "1500.00")
    private BigDecimal paidAmount;

    @Schema(description = "剩余金额", example = "0.00")
    private BigDecimal remainingAmount;

    @Schema(description = "积分抵扣", example = "100")
    private Integer pointDeducted;

    @Schema(description = "物业币抵扣", example = "50.00")
    private BigDecimal coinDeducted;

    @Schema(description = "支付状态", example = "已支付")
    private String paymentStatus;

    @Schema(description = "截止日期", example = "2026-01-01T00:00:00.000Z")
    private String dueDate;

    @Schema(description = "支付时间", example = "2026-01-01T00:00:00.000Z")
    private String paidAt;

    @Schema(description = "是否自动扣款", example = "false")
    private Boolean autoDeduct;

    @Schema(description = "逾期天数", example = "0")
    private Integer overdueDays;

    @Schema(description = "滞纳金", example = "0.00")
    private BigDecimal lateFee;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
