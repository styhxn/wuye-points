package com.wuye.points.dto.regionallistleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "区域负责人列表项响应")
public class RegionalLeaderListItemResponse {

    @Schema(description = "区域负责人ID", example = "rl_xxxxxxxx")
    private String id;

    @Schema(description = "业主ID", example = "res_xxxxxxxx")
    private String residentId;

    @Schema(description = "所属板块负责人ID", example = "sl_xxxxxxxx")
    private String sectorLeaderId;

    @Schema(description = "所属统筹ID", example = "coo_xxxxxxxx")
    private String coordinatorId;

    @Schema(description = "物业公司ID", example = "pc_xxxxxxxx")
    private String propertyCompanyId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "负责区域名称", example = "华东区")
    private String regionName;

    @Schema(description = "分成比例", example = "0.2000")
    private BigDecimal shareRate;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "下级项目负责人数量", example = "5")
    private Long projectLeaderCount;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private String createdAt;
}
