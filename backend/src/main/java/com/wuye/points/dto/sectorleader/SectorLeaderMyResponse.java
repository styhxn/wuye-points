package com.wuye.points.dto.sectorleader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "板块负责人工作台-我的信息")
public class SectorLeaderMyResponse {

    @Schema(description = "负责人ID")
    private String id;

    @Schema(description = "业主姓名")
    private String residentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyCompanyName;

    @Schema(description = "板块编码")
    private String sector;

    @Schema(description = "板块名称")
    private String sectorName;

    @Schema(description = "统筹负责人ID")
    private String coordinatorId;

    @Schema(description = "统筹负责人姓名")
    private String coordinatorName;

    @Schema(description = "个体负责人数量")
    private Long individualLeaderCount;

    @Schema(description = "板块商家数量")
    private Long merchantCount;

    @Schema(description = "进行中特惠数量")
    private Long activeSpecialOfferCount;

    @Schema(description = "累计收益")
    private BigDecimal totalEarnings;

    @Schema(description = "状态")
    private String status;
}
