package com.wuye.points.dto.regionallistleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新区域负责人请求")
public class UpdateRegionalLeaderRequest {

    @Schema(description = "所属板块负责人ID", example = "sl_xxxxxxxx")
    private String sectorLeaderId;

    @Schema(description = "所属统筹ID", example = "coo_xxxxxxxx")
    private String coordinatorId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "负责区域名称", example = "华东区")
    private String regionName;

    @Schema(description = "分成比例", example = "0.2000")
    private BigDecimal shareRate;

    @Schema(description = "状态：active/inactive", example = "active")
    private String status;
}
