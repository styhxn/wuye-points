package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "快递员信息响应")
public class CourierItemResponse {

    @Schema(description = "业主ID", example = "res_xxx")
    private String residentId;

    @Schema(description = "姓名", example = "李四")
    private String name;

    @Schema(description = "手机号", example = "13800138001")
    private String phone;

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Schema(description = "楼栋", example = "1栋")
    private String building;

    @Schema(description = "单元", example = "1单元")
    private String unit;

    @Schema(description = "房号", example = "101")
    private String room;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "绑定时间", example = "2024-01-01 10:00:00")
    private String boundAt;
}
