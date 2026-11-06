package com.wuye.points.dto.merchant.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "商家录入积分请求")
@Data
public class GrantPointRequest {

    /**
     * 条目18（2026-11-05）：购买记录不再由前端选择，缺省由后端按时间顺序（FIFO）
     * 从商家全部「已审核通过且有余额」的购买记录中扣减；传值时仅从该批次扣减。
     */
    @Schema(description = "购买记录ID（可选，不传则后端 FIFO 自动扣减）", example = "mpp_xxx", deprecated = true)
    private String purchaseId;

    @Schema(description = "住户手机号（与 residentId 二选一，推荐）", example = "13800000000")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "住户ID（与 phone 二选一）", example = "res_xxx")
    private String residentId;

    @Schema(description = "录入积分数量", example = "100", required = true)
    @NotNull(message = "录入积分数量不能为空")
    @Min(value = 1, message = "录入积分数量至少为1")
    private Integer pointAmount;

    @Schema(description = "描述")
    private String description;
}
