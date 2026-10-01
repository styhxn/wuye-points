package com.wuye.points.dto.propertycontact;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "配置/更新物业联系方式请求")
public class UpsertPropertyContactRequest {

    @Schema(description = "物业名称", example = "阳光花园物业服务中心")
    @NotBlank(message = "物业名称不能为空")
    private String propertyName;

    @Schema(description = "联系电话（座机/服务热线）", example = "0571-88886666")
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Schema(description = "备用手机/微信电话", example = "13800000000")
    private String contactMobile;

    @Schema(description = "物业地址", example = "浙江省杭州市西湖区阳光路1号")
    private String address;

    @Schema(description = "服务时间", example = "周一至周日 8:00-20:00")
    private String serviceHours;

    @Schema(description = "联系邮箱", example = "service@sunshine-property.com")
    private String email;

    @Schema(description = "备注（仅管理端可见，不会下发前端）", example = "前台分机 8001")
    private String remark;

    @Schema(description = "是否启用，启用后前端在用户未绑定楼栋时展示", example = "true")
    private Boolean enabled;
}
