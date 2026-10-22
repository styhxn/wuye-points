package com.wuye.points.dto.distributor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 一级代理申请请求（C端）
 */
@Data
public class ApplyDistributorRequest {

    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名不超过50字")
    @Schema(description = "联系人姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "联系电话格式不正确（11位手机号）")
    @Schema(description = "联系电话（11位手机号）", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactPhone;

    @NotBlank(message = "经营品类不能为空")
    @Pattern(regexp = "fresh|daily|grain|other", message = "经营品类仅支持 fresh/daily/grain/other")
    @Schema(description = "经营品类：fresh=生鲜 / daily=日用 / grain=粮油 / other=其它", example = "daily", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessScope;

    @NotBlank(message = "服务范围不能为空")
    @Pattern(regexp = "community|city", message = "服务范围仅支持 community/city")
    @Schema(description = "服务范围：community=本小区 / city=本市", example = "community", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceScope;

    @Size(max = 200, message = "备注不超过200字")
    @Schema(description = "备注（选填）", example = "小区门口便利店，可辐射周边3个小区")
    private String remark;
}
