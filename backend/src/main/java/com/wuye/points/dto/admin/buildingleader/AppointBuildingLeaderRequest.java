package com.wuye.points.dto.admin.buildingleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "指定单元楼长请求")
public class AppointBuildingLeaderRequest {

    @Schema(description = "住户（楼长）ID；与 phone 二选一，两者都传时以 residentId 为准",
            example = "res_xxx", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String residentId;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确（11位手机号）")
    @Schema(description = "住户手机号；物业经理工作台用手机号选人（条目21），按当前物业公司定位住户",
            example = "13800000000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;

    @Schema(description = "小区ID；不传则取住户所属小区")
    private String communityId;

    @Schema(description = "单元；不传则取住户所属单元")
    private String unit;

    @Schema(description = "楼长分成比例（占物业公司分成的比例，0~1）；不传默认 0.1000", example = "0.1000")
    private BigDecimal commissionRate;

    @Schema(description = "备注/说明", example = "1单元楼长")
    private String description;

    @Schema(description = "状态 active/inactive；不传默认 active", example = "active")
    private String status;
}