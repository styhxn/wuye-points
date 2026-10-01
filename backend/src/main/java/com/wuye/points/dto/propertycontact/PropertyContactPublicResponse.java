package com.wuye.points.dto.propertycontact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物业联系方式（前端展示）")
public class PropertyContactPublicResponse {

    @Schema(description = "物业名称", example = "阳光花园物业服务中心")
    private String propertyName;

    @Schema(description = "联系电话", example = "0571-88886666")
    private String contactPhone;

    @Schema(description = "备用手机/微信电话", example = "13800000000")
    private String contactMobile;

    @Schema(description = "物业地址", example = "浙江省杭州市西湖区阳光路1号")
    private String address;

    @Schema(description = "服务时间", example = "周一至周日 8:00-20:00")
    private String serviceHours;

    @Schema(description = "联系邮箱", example = "service@sunshine-property.com")
    private String email;

    @Schema(description = "是否启用，前端仅当 enabled=true 时展示", example = "true")
    private Boolean enabled;
}
