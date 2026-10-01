package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 社区食堂-商家给用户充定向余额请求
 * 手机号与 residentId 二选一
 */
@Data
@Schema(description = "商家给用户充定向余额请求")
public class DirectedRechargeRequest {

    @Schema(description = "用户手机号（与residentId二选一）", example = "13800138000")
    private String phone;

    @Schema(description = "用户ID（与phone二选一）", example = "res_xxx")
    private String residentId;

    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", required = true, example = "50.00")
    private BigDecimal amount;

    @Schema(description = "备注", example = "食堂补贴充值")
    private String remark;
}
