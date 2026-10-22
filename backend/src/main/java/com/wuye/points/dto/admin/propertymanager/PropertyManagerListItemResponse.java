package com.wuye.points.dto.admin.propertymanager;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "物业经理列表项")
public class PropertyManagerListItemResponse {

    @Schema(description = "经理ID pm_")
    private String id;

    @Schema(description = "住户ID")
    private String residentId;

    @Schema(description = "住户姓名")
    private String residentName;

    @Schema(description = "住户手机号")
    private String residentPhone;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyCompanyName;

    @Schema(description = "经理抽成比例（占单元楼长分成）")
    private BigDecimal commissionRate;

    @Schema(description = "状态 active/inactive")
    private String status;

    @Schema(description = "创建时间")
    private String createdAt;
}
