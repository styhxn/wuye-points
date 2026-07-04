package com.wuye.points.dto.propertycompany;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "小区信息项响应")
public class CommunityItemResponse {

    @Schema(description = "小区ID", example = "com_xk2m9z")
    private String id;

    @Schema(description = "小区名称", example = "万科城市花园")
    private String name;

    @Schema(description = "小区地址", example = "北京市朝阳区建国路88号")
    private String address;

    @Schema(description = "楼栋总数", example = "10")
    private Integer totalBuildings;

    @Schema(description = "单元总数", example = "300")
    private Integer totalUnits;

    @Schema(description = "小区状态", allowableValues = {"active", "inactive"}, example = "active")
    private String status;
}
