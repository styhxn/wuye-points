package com.wuye.points.dto.admin.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理端-转给物业流水列表项")
@Data
@Builder
public class TransferToPropertyListItemResponse {

    @Schema(description = "流水ID", example = "pt_xxx")
    private String id;

    @Schema(description = "类型：point / coin", example = "point")
    private String type;

    @Schema(description = "住户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "住户手机号（脱敏）", example = "138****1234")
    private String residentPhone;

    @Schema(description = "数量（积分：整数；物业币：两位小数）", example = "100")
    private BigDecimal amount;

    @Schema(description = "备注", example = "主动捐给物业做活动经费")
    private String remark;

    @Schema(description = "物业公司ID", example = "pc_xxx")
    private String propertyCompanyId;

    @Schema(description = "转出时间", example = "2026-08-17T10:00:00Z")
    private String createdAt;
}
