package com.wuye.points.dto.resident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 预约信息预填响应：预约服务（book）时前端展示自动带出的联系电话与默认地址。
 * 说明：联系电话为完整手机号（仅返回给本人），地址为楼栋+单元+房号拼接的默认地址。
 */
@Data
@Builder
@Schema(description = "预约信息预填响应")
public class BookingContactInfoResponse {

    @Schema(description = "住户ID", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "联系电话（完整手机号，预约自动带出）", example = "13800138000")
    private String contactPhone;

    @Schema(description = "默认服务地址（楼栋+单元+房号，未登记时为 null）", example = "3栋5单元502")
    private String address;

    @Schema(description = "楼栋", example = "3栋")
    private String building;

    @Schema(description = "单元", example = "5单元")
    private String unit;

    @Schema(description = "房号", example = "502")
    private String room;

    @Schema(description = "小区ID", example = "com_demo001")
    private String communityId;

    @Schema(description = "小区名称", example = "演示小区")
    private String communityName;

    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "是否已登记联系电话（false 时前端应引导用户完善资料）", example = "true")
    private Boolean hasPhone;

    @Schema(description = "是否已登记地址（楼栋/单元/房号，false 时前端应引导用户完善资料）", example = "true")
    private Boolean hasAddress;
}
