package com.wuye.points.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "创建活动小组请求")
@Data
public class CreateActivityGroupRequest {

    @Schema(description = "活动小组名称", example = "跑步俱乐部", required = true)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Schema(description = "月费", example = "30.00")
    @DecimalMin(value = "0.00", message = "monthlyFee不能为负数")
    private BigDecimal monthlyFee;

    @Schema(description = "年费", example = "300.00")
    @DecimalMin(value = "0.00", message = "yearlyFee不能为负数")
    private BigDecimal yearlyFee;
}
