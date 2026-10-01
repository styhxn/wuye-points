package com.wuye.points.dto.servicerequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发布服务需求请求")
public class CreateServiceRequestRequest {

    @NotBlank
    @Size(max = 2000)
    @Schema(description = "需求描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "小区ID，默认当前用户小区")
    private String communityId;

    @Size(max = 20)
    @Schema(description = "联系电话（不传时自动带出用户注册手机号）")
    private String contactPhone;

    @Size(max = 200)
    @Schema(description = "服务地址（不传时自动带出用户登记的楼栋+单元+房号）", example = "3栋5单元502")
    private String address;

    @Schema(description = "期望服务时间，支持 yyyy-MM-dd HH:mm:ss")
    private String preferredTime;
}
