package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "服务订单列表项响应")
public class ServiceOrderListItemResponse {

    @Schema(description = "ID", example = "so_r8k2m4")
    private String id;

    @Schema(description = "服务ID", example = "svc_m8k2n4")
    private String serviceId;

    @Schema(description = "服务名称", example = "家政保洁")
    private String serviceName;

    @Schema(description = "服务分类", example = "cleaning_repair")
    private String serviceCategory;

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "服务商ID", example = "prov_x9k2m1")
    private String providerId;

    @Schema(description = "服务商名称", example = "阳光家政")
    private String providerName;

    @Schema(description = "订单状态", example = "pending")
    private String orderStatus;

    @Schema(description = "订单金额", example = "100.00")
    private BigDecimal price;

    @Schema(description = "会员价格", example = "80.00")
    private BigDecimal memberPrice;

    @Schema(description = "实付金额", example = "80.00")
    private BigDecimal paidAmount;

    @Schema(description = "支付方式", example = "wechat")
    private String paymentMethod;

    @Schema(description = "服务时间", example = "2026-01-01T10:00:00.000Z")
    private String serviceTime;

    @Schema(description = "服务地址", example = "阳光花园1栋1单元101")
    private String address;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
