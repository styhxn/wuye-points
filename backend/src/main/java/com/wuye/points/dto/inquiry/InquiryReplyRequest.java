package com.wuye.points.dto.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "询价回复/确认请求")
public class InquiryReplyRequest {

    @NotBlank(message = "action不能为空")
    @Schema(description = "操作类型: quote(商家报价)/accept(业主接受)/reject(业主拒绝)",
            example = "quote", requiredMode = Schema.RequiredMode.REQUIRED)
    private String action;

    @Schema(description = "报价金额（action=quote时必填）", example = "120.00")
    private BigDecimal quotedPrice;

    @Schema(description = "报价备注", example = "含税价")
    private String quotedRemark;
}
