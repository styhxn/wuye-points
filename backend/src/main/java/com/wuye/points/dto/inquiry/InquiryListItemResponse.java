package com.wuye.points.dto.inquiry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "询价列表项响应")
public class InquiryListItemResponse {

    @Schema(description = "询价ID", example = "inq_xxxx")
    private String id;

    @Schema(description = "物业公司ID", example = "pc_xxxx")
    private String propertyCompanyId;

    @Schema(description = "询价人ID", example = "res_xxxx")
    private String inquirerId;

    @Schema(description = "商家ID", example = "mch_xxxx")
    private String merchantId;

    @Schema(description = "商品ID", example = "p_xxxx")
    private String productId;

    @Schema(description = "商品名称", example = "示例商品")
    private String productName;

    @Schema(description = "询价内容", example = "请问批发价多少？")
    private String content;

    @Schema(description = "报价金额", example = "120.00")
    private BigDecimal quotedPrice;

    @Schema(description = "报价备注", example = "含税价")
    private String quotedRemark;

    @Schema(description = "状态: pending/quoted/accepted/rejected/closed", example = "pending")
    private String status;

    @Schema(description = "报价时间", example = "2024-01-01 10:00:00")
    private String quotedAt;

    @Schema(description = "关闭时间", example = "2024-01-01 11:00:00")
    private String closedAt;

    @Schema(description = "创建时间", example = "2024-01-01 09:00:00")
    private String createdAt;
}
