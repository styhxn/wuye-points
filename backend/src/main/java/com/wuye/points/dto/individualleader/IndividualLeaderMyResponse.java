package com.wuye.points.dto.individualleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "个体负责人工作台-我的信息")
public class IndividualLeaderMyResponse {

    @Schema(description = "个体负责人记录ID（il_ 前缀，后续工作台接口路径请用此 id）")
    private String id;

    @Schema(description = "业主ID")
    private String residentId;

    @Schema(description = "业主姓名")
    private String residentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个体负责人姓名")
    private String name;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyCompanyName;

    @Schema(description = "板块负责人ID")
    private String sectorLeaderId;

    @Schema(description = "所属板块")
    private String sector;

    @Schema(description = "分成比例")
    private BigDecimal commissionRate;

    @Schema(description = "累计收益")
    private BigDecimal totalEarnings;

    @Schema(description = "状态")
    private String status;
}
