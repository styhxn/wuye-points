package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "板块负责人创建响应")
public class SectorLeaderCreatedResponse {

    @Schema(description = "板块负责人ID", example = "sl_xxxx")
    private String id;

    @Schema(description = "业主ID", example = "res_xxxx")
    private String residentId;

    @Schema(description = "统筹ID", example = "coo_xxxx")
    private String coordinatorId;

    @Schema(description = "物业公司ID", example = "pc_xxxx")
    private String propertyCompanyId;

    @Schema(description = "板块编码", example = "cleaning")
    private String sector;

    @Schema(description = "抽佣比例", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "状态", example = "active")
    private String status;
}
