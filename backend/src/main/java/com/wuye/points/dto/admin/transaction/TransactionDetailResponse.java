package com.wuye.points.dto.admin.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "平台交易流水详情（打印数据包）")
public class TransactionDetailResponse {

    @Schema(description = "流水行本体，字段与 GET /admin/transactions 列表行完全一致")
    private TransactionItemResponse transaction;

    @Schema(description = "来源表名，供打印页标注数据来源", example = "payment_records")
    private String sourceTable;

    @Schema(description = "可用于 GET /admin/transactions?keyword= 的关联单号（优先订单号，其次业务单号）；"
            + "用它可把同一笔单据的收款/分账/入账/提现流水一次捞全，拼成一张对账单")
    private String relatedKeyword;

    @Schema(description = "可直接调用的关联接口（相对 Base URL，需另带 Authorization 头）")
    private List<Link> links;

    @Data
    @Builder
    @Schema(description = "关联接口指引")
    public static class Link {

        @Schema(description = "用途", example = "订单详情")
        private String label;

        @Schema(description = "HTTP 方法", example = "GET")
        private String method;

        @Schema(description = "可直接请求的路径（含查询参数）", example = "/orders/ord_xxx")
        private String path;
    }
}
