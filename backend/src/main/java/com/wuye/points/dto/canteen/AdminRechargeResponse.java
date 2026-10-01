package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-管理端定向充值流水项
 */
@Data
@Builder
@Schema(description = "管理端定向充值流水项")
public class AdminRechargeResponse {

    @Schema(description = "充值流水ID", example = "cdr_xxx")
    private String id;

    @Schema(description = "主商家体系ID", example = "mch_xxx")
    private String mainMerchantId;

    @Schema(description = "主商家名称", example = "幸福餐饮公司")
    private String mainMerchantName;

    @Schema(description = "被充值用户ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "被充值用户姓名", example = "张三")
    private String residentName;

    @Schema(description = "被充值用户手机号", example = "13800138000")
    private String residentPhone;

    @Schema(description = "充值金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "充值后余额", example = "150.00")
    private BigDecimal balanceAfter;

    @Schema(description = "操作人ID", example = "res_xxx")
    private String operatorId;

    @Schema(description = "备注", example = "食堂补贴充值")
    private String remark;

    @Schema(description = "创建时间（ISO-8601）", example = "2026-08-27T10:00:00.000Z")
    private String createdAt;
}
