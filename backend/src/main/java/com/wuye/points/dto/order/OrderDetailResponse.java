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

    @Schema(description = "用户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "物业公司ID", example = "pc_abc123")
    private String propertyCompanyId;

    @Schema(description = "配送地址", example = "阳光花园1号楼1单元101")
    private String address;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "备注", example = "请尽快配送")
    private String remark;

    @Schema(description = "取消时间", example = "2026-01-01T00:00:00.000Z")
    private String cancelledAt;

    @Schema(description = "版本号", example = "1")
    private Integer version;

    @Schema(description = "更新时间", example = "2026-01-01T00:00:00.000Z")
    private String updatedAt;
}
