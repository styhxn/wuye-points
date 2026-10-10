package com.wuye.points.dto.admin.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新单元楼长请求")
public class UpdateBuildingLeaderRequest {

    @Schema(description = "楼长分成比例（占物业公司分成的比例，0~1）", example = "0.1200")
    private BigDecimal commissionRate;

    @Schema(description = "备注/说明")
    private String description;

    @Schema(description = "状态 active/inactive", example = "active")
    private String status;
}