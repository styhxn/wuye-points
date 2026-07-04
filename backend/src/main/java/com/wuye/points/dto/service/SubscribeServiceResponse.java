package com.wuye.points.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "订阅服务响应")
public class SubscribeServiceResponse {

    @Schema(description = "ID", example = "ss_m8k2n4")
    private String id;

    @Schema(description = "服务ID", example = "svc_m8k2n4")
    private String serviceId;

    @Schema(description = "服务名称", example = "家政保洁")
    private String serviceName;

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "订阅类型", example = "月度")
    private String subType;

    @Schema(description = "订阅金额", example = "200.00")
    private BigDecimal amount;

    @Schema(description = "开始日期", example = "2026-01-01T00:00:00.000Z")
    private String startDate;

    @Schema(description = "结束日期", example = "2026-02-01T00:00:00.000Z")
    private String endDate;

    @Schema(description = "是否自动续订", example = "true")
    private Boolean autoRenew;

    @Schema(description = "状态", example = "生效中")
    private String status;

    @Schema(description = "创建时间", example = "2026-01-01T00:00:00.000Z")
    private String createdAt;
}
