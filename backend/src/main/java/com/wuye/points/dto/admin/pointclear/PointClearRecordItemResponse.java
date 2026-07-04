package com.wuye.points.dto.admin.pointclear;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "积分清零记录项响应")
public class PointClearRecordItemResponse {

    @Schema(description = "记录ID", example = "CLR20240101001")
    private String id;

    @Schema(description = "住户ID", example = "RES001")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "清零积分数量", example = "200")
    private Integer clearedPoints;

    @Schema(description = "等值金额", example = "20.00")
    private BigDecimal equivalentAmount;

    @Schema(description = "转入资金池金额", example = "18.00")
    private BigDecimal poolInAmount;

    @Schema(description = "操作员ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
