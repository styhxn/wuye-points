package com.wuye.points.dto.admin.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "个人组长详情响应")
public class IndividualLeaderDetailResponse {
    @Schema(description = "个人组长ID", example = "IL001")
    private String id;
    @Schema(description = "关联住户ID", example = "RES001")
    private String residentId;
    @Schema(description = "住户姓名", example = "李四")
    private String residentName;
    @Schema(description = "联系电话", example = "13900139000")
    private String phone;
    @Schema(description = "组长姓名", example = "李四")
    private String name;
    @Schema(description = "物业公司ID", example = "PC001")
    private String propertyCompanyId;
    @Schema(description = "片区组长ID", example = "SL001")
    private String sectorLeaderId;
    @Schema(description = "所属片区", example = "A片区")
    private String sector;
    @Schema(description = "片区名称", example = "阳光A片区")
    private String sectorName;
    @Schema(description = "佣金比例", example = "0.03")
    private BigDecimal commissionRate;
    @Schema(description = "累计佣金收入", example = "6800.00")
    private BigDecimal totalEarnings;
    @Schema(description = "状态", example = "active")
    private String status;
    @Schema(description = "任命时间", example = "2025-01-20T10:00:00")
    private String appointedAt;
    @Schema(description = "创建时间", example = "2025-01-20T10:00:00")
    private String createdAt;
    @Schema(description = "更新时间", example = "2025-01-20T10:00:00")
    private String updatedAt;
}
