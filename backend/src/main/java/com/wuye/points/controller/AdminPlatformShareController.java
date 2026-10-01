package com.wuye.points.controller;

import com.wuye.points.dto.admin.platformshare.PlatformEarningRecordItemResponse;
import com.wuye.points.dto.admin.platformshare.PlatformEarningsBalanceResponse;
import com.wuye.points.dto.admin.platformshare.PlatformEarningsStatsResponse;
import com.wuye.points.dto.admin.platformshare.PlatformShareRatesResponse;
import com.wuye.points.dto.admin.platformshare.UpdatePlatformShareRatesRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.PlatformShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 平台分成比例与收益统计
 */
@Tag(name = "管理端 — 平台分成与收益", description = "平台管理员分成比例配置、平台收益统计与明细查询")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPlatformShareController {

    private final PlatformShareService platformShareService;

    @Operation(summary = "查询平台分成比例配置",
            description = "查询物业公司平台分成比例。角色: platform_admin, property_admin。"
                    + "platform_admin 可通过 propertyCompanyId 指定物业公司，property_admin 仅能查询本物业")
    @GetMapping("/platform-share-rates")
    public ApiResponse<PlatformShareRatesResponse> getShareRates(
            @Parameter(description = "物业公司ID（仅 platform_admin 可指定）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(platformShareService.getShareRates(propertyCompanyId));
    }

    @Operation(summary = "更新平台分成比例",
            description = "更新物业公司的平台分成比例。角色: platform_admin")
    @PutMapping("/platform-share-rates")
    public ApiResponse<PlatformShareRatesResponse> updateShareRates(
            @Valid @RequestBody UpdatePlatformShareRatesRequest request) {
        return ApiResponse.success(platformShareService.updateShareRates(request));
    }

    @Operation(summary = "平台收益统计",
            description = "按时间区间统计平台收益（订单分成、配送费分成、提现手续费分成），支持按物业公司分组。"
                    + "角色: platform_admin")
    @GetMapping("/platform-earnings/stats")
    public ApiResponse<PlatformEarningsStatsResponse> getEarningsStats(
            @Parameter(description = "物业公司ID（可选，不传则统计全部物业）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "开始日期，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(platformShareService.getEarningsStats(
                propertyCompanyId, startDate, endDate));
    }

    @Operation(summary = "平台收益明细列表",
            description = "分页查询平台收益明细记录。角色: platform_admin。"
                    + "type 可选: distribution(订单分成)/delivery(配送费分成)/withdrawal(提现手续费分成)，不传则查全部")
    @GetMapping("/platform-earnings/records")
    public ApiResponse<PageResponse<PlatformEarningRecordItemResponse>> listEarningRecords(
            @Parameter(description = "物业公司ID（可选）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "收益类型: distribution/delivery/withdrawal")
            @RequestParam(required = false) String type,
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "开始日期，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(platformShareService.listEarningRecords(
                propertyCompanyId, type, page, pageSize, startDate, endDate));
    }

    @Operation(summary = "平台收益余额/对账快照",
            description = "只读展示平台账户已入账余额及构成，用于看账与对照流水。"
                    + "微信 真分账模式下平台抽成由微信直接入账，withdrawAvailable=false（不提供提现申请），"
                    + "前端可据 settleMode / withdrawAvailable 隐藏「申请提现」按钮。角色: platform_admin")
    @GetMapping("/platform-earnings/balance")
    public ApiResponse<PlatformEarningsBalanceResponse> getEarningsBalance(
            @Parameter(description = "物业公司ID（可选，不传则统计全部物业）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(platformShareService.getEarningsBalance(propertyCompanyId));
    }
}
