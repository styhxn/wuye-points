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

    @Schema(description = "可提现金额（累计分成 − 已占用提现 − 微信分账已实时到账；"
            + "与 POST /sector-leaders/{id}/withdrawals 校验同源）", example = "800.00")
    private BigDecimal withdrawableAmount;

    @Schema(description = "在途提现金额（待审核 + 已通过待打款）", example = "200.00")
    private BigDecimal pendingWithdrawalAmount;

    @Schema(description = "提现手续费率（物业公司覆盖值优先，未配置取平台默认，手续费开关关闭时为 0）",
            example = "0.0060")
    private BigDecimal feeRate;

    @Schema(description = "手续费承担方：payee=从提现金额内扣（实到 = amount − amount × feeRate）",
            example = "payee")
    private String withdrawFeeBearer;

    @Schema(description = "状态")
    private String status;
}
