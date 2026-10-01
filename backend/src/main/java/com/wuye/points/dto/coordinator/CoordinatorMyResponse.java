package com.wuye.points.dto.coordinator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "统筹工作台-我的信息")
public class CoordinatorMyResponse {

    @Schema(description = "统筹ID")
    private String id;

    @Schema(description = "业主姓名")
    private String residentName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "物业公司ID")
    private String propertyCompanyId;

    @Schema(description = "物业公司名称")
    private String propertyCompanyName;

    @Schema(description = "分成比例")
    private BigDecimal commissionRate;

    @Schema(description = "下属板块负责人数量")
    private Long sectorLeaderCount;

    @Schema(description = "下属个体负责人数量")
    private Long individualLeaderCount;

    @Schema(description = "管辖商家数量")
    private Long merchantCount;

    @Schema(description = "进行中特惠数量")
    private Long activeSpecialOfferCount;

    @Schema(description = "累计收益")
    private BigDecimal totalEarnings;

    @Schema(description = "可提现金额（累计分成-已占用提现，占用含 pending/approved/completed）", example = "800.00")
    private BigDecimal withdrawableAmount;

    @Schema(description = "状态")
    private String status;
}
