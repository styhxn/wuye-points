package com.wuye.points.dto.admin.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新物业经理请求")
public class UpdatePropertyManagerRequest {

    @Schema(description = "经理抽成比例（占单元楼长分成的比例，0~1不含1）", example = "0.2000")
    private BigDecimal commissionRate;

    @Schema(description = "状态 active/inactive", example = "active")
    private String status;
}
