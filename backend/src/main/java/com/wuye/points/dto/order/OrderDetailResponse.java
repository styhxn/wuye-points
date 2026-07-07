package com.wuye.points.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单详情响应")
public class OrderDetailResponse extends OrderListItemResponse {

    @Schema(description = "物业公司ID", example = "pc_abc123")
    private String propertyCompanyId;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "备注", example = "请尽快配送")
    private String remark;

    @Schema(description = "快递员姓名", example = "李师傅")
    private String courierName;

    @Schema(description = "配送单ID", example = "dlv_abc123")
    private String deliveryId;

    @Schema(description = "取消时间", example = "2026-01-01T00:00:00.000Z")
    private String cancelledAt;

    @Schema(description = "版本号", example = "1")
    private Integer version;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
