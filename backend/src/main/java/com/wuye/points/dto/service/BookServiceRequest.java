package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "预约服务请求")
public class BookServiceRequest {

    @Schema(description = "服务时间", example = "2026-01-01T10:00:00.000Z")
    private String serviceTime;

    @Schema(description = "服务地址", example = "阳光花园1栋1单元101", required = true)
    @NotBlank(message = "地址不能为空")
    private String address;

    @Schema(description = "联系电话", example = "13800138000", required = true)
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "备注", example = "请提前联系")
    private String remark;

    @Schema(description = "支付方式", example = "微信支付", required = true)
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
