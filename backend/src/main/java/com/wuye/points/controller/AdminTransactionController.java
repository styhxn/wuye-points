package com.wuye.points.controller;

import com.wuye.points.dto.admin.transaction.TransactionItemResponse;
import com.wuye.points.dto.admin.transaction.TransactionOverviewResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 管理端 — 平台交易明细（仅 platform_admin）。
 */
@Tag(name = "管理端 — 交易明细",
        description = "平台交易总览、明细与 CSV 导出。仅 platform_admin 可访问，不对物业管理员开放。")
@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class AdminTransactionController {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final AdminTransactionService adminTransactionService;

    @Operation(summary = "交易明细列表",
            description = "分页查询平台交易明细。角色: platform_admin。"
                    + "group: PAYMENT/WALLET/SETTLEMENT/VALUE；"
                    + "category: payment/refund/wallet/distribution/split/withdrawal/transfer/settlement/point/coin/canteen；"
                    + "direction: income/expense/neutral。不传时间默认最近 30 天。")
    @GetMapping
    public ApiResponse<PageResponse<TransactionItemResponse>> list(
            @Parameter(description = "物业公司ID（为空=全部）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / ISO8601")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / ISO8601")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "分组: PAYMENT/WALLET/SETTLEMENT/VALUE")
            @RequestParam(required = false) String group,
            @Parameter(description = "交易分类")
            @RequestParam(required = false) String category,
            @Parameter(description = "收支方向: income/expense/neutral")
            @RequestParam(required = false) String direction,
            @Parameter(description = "业务状态，原样匹配")
            @RequestParam(required = false) String status,
            @Parameter(description = "关键词：交易ID/业务单号/订单号/渠道流水号/主体名称/说明")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数，最大100", example = "20")
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(adminTransactionService.list(propertyCompanyId, startDate, endDate,
                group, category, direction, status, keyword, page, pageSize));
    }

    @Operation(summary = "交易总览",
            description = "按分组/单位、交易分类、状态、日期汇总平台交易。角色: platform_admin。"
                    + "不同 unit（元/积分/物业币/定向余额）不可直接相加。")
    @GetMapping("/overview")
    public ApiResponse<TransactionOverviewResponse> overview(
            @Parameter(description = "物业公司ID（为空=全部）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "分组: PAYMENT/WALLET/SETTLEMENT/VALUE")
            @RequestParam(required = false) String group,
            @Parameter(description = "交易分类")
            @RequestParam(required = false) String category,
            @Parameter(description = "收支方向: income/expense/neutral")
            @RequestParam(required = false) String direction,
            @Parameter(description = "业务状态，原样匹配")
            @RequestParam(required = false) String status,
            @Parameter(description = "关键词")
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminTransactionService.overview(propertyCompanyId, startDate, endDate,
                group, category, direction, status, keyword));
    }

    @Operation(summary = "交易明细 CSV 导出",
            description = "按当前筛选条件导出 CSV（UTF-8 BOM，Excel 可直接打开），最多 50000 行。"
                    + "角色: platform_admin。响应头 X-Export-Truncated=true 表示超过上限已截断。")
    @GetMapping("/export")
    public void export(
            @Parameter(description = "物业公司ID（为空=全部）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "分组: PAYMENT/WALLET/SETTLEMENT/VALUE")
            @RequestParam(required = false) String group,
            @Parameter(description = "交易分类")
            @RequestParam(required = false) String category,
            @Parameter(description = "收支方向: income/expense/neutral")
            @RequestParam(required = false) String direction,
            @Parameter(description = "业务状态，原样匹配")
            @RequestParam(required = false) String status,
            @Parameter(description = "关键词")
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) throws IOException {
        AdminTransactionService.CsvExport export = adminTransactionService.exportCsv(
                propertyCompanyId, startDate, endDate, group, category, direction, status, keyword);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"transactions_" + LocalDateTime.now().format(FILE_TIME) + ".csv\"");
        response.setHeader("X-Export-Rows", String.valueOf(export.rowCount()));
        response.setHeader("X-Export-Truncated", String.valueOf(export.truncated()));
        response.getWriter().write(export.content());
    }
}
