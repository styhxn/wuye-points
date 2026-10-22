package com.wuye.points.dto.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "当前物业经理信息")
public class PropertyManagerMyResponse {

    @Schema(description = "经理ID pm_")
    private String propertyManagerId;

    @Schema(description = "住户ID")
    private String residentId;

    @Schema(description = "姓名")
    private String residentName;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyCompanyName;

    @Schema(description = "经理抽成比例（占单元楼长分成）")
    private BigDecimal commissionRate;

    @Schema(description = "状态 active/inactive")
    private String status;
}
