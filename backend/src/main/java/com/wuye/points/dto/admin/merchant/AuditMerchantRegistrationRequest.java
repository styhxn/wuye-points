package com.wuye.points.dto.admin.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "审核商家入驻申请请求")
@Data
public class AuditMerchantRegistrationRequest {

    @Schema(description = "审核结果", example = "approved", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    @Schema(description = "拒绝原因", example = "资料不完整")
    private String rejectReason;

    @Schema(description = "备注", example = "已核实营业执照")
    private String remark;

    @Schema(description = "商家等级", example = "gold")
    private String merchantLevel;

    @Schema(description = "经营类别", example = "餐饮")
    private String category;

    @Schema(description = "营业时间", example = "09:00-21:00")
    private String businessHours;

    @Schema(description = "配送费（元）", example = "5.00")
    private String deliveryFee;

    @Schema(description = "免配送费门槛（元）", example = "50.00")
    private String freeDeliveryThreshold;
}
