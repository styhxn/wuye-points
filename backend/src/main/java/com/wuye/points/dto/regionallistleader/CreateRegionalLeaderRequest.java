package com.wuye.points.dto.regionallistleader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建区域负责人请求")
public class CreateRegionalLeaderRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "res_xxxxxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @Schema(description = "所属板块负责人ID", example = "sl_xxxxxxxx")
    private String sectorLeaderId;

    @Schema(description = "所属统筹ID（也可由统筹直接任命）", example = "coo_xxxxxxxx")
    private String coordinatorId;

    @NotBlank(message = "name不能为空")
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "负责区域名称", example = "华东区")
    private String regionName;

    @Schema(description = "分成比例（从板块负责人中抽取）", example = "0.2000")
    private BigDecimal shareRate;

    @Schema(description = "状态：active/inactive", example = "active")
    private String status;
}
