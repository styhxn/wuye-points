package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建快递负责人请求")
public class CreateCourierManagerRequest {

    @NotBlank(message = "residentId不能为空")
    @Schema(description = "业主ID", example = "res_xxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String residentId;

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Size(max = 50, message = "name长度不能超过50")
    @Schema(description = "负责人姓名", example = "张三")
    private String name;

    @Size(max = 20, message = "phone长度不能超过20")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Size(max = 500, message = "responsibleArea长度不能超过500")
    @Schema(description = "负责区域", example = "1-3号楼")
    private String responsibleArea;

    @Schema(description = "配送时间配置JSON", example = "{\"start\":\"08:00\",\"end\":\"20:00\"}")
    private String deliveryTimeConfig;
}
