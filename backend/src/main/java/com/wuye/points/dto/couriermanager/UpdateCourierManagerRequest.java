package com.wuye.points.dto.couriermanager;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新快递负责人请求")
public class UpdateCourierManagerRequest {

    @Size(max = 50, message = "name长度不能超过50")
    @Schema(description = "负责人姓名", example = "张三")
    private String name;

    @Size(max = 20, message = "phone长度不能超过20")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @Schema(description = "小区ID", example = "com_xxx")
    private String communityId;

    @Size(max = 500, message = "responsibleArea长度不能超过500")
    @Schema(description = "负责区域", example = "1-3号楼")
    private String responsibleArea;

    @Schema(description = "配送时间配置JSON", example = "{\"start\":\"08:00\",\"end\":\"20:00\"}")
    private String deliveryTimeConfig;

    @Schema(description = "状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;

    @Schema(description = "快递负责人分成比例（0~1）", example = "0.2000")
    private BigDecimal shareRate;
}
