package com.wuye.points.controller;

import com.wuye.points.dto.admin.transfer.TransferToPropertyListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminTransferToPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 住户「转给物业」流水（对账查看）
 */
@Tag(name = "管理端 — 转给物业流水", description = "住户主动转给物业的积分/物业币流水（对账，无需审批）")
@RestController
@RequestMapping("/admin/transfer-to-property")
@RequiredArgsConstructor
public class AdminTransferToPropertyController {

    private final AdminTransferToPropertyService adminTransferToPropertyService;

    @Operation(summary = "转给物业-积分流水",
            description = "分页查询住户转给物业的积分流水。角色: property_admin, platform_admin, coordinator, sector_leader, individual_leader")
    @GetMapping("/points")
    public ApiResponse<PageResponse<TransferToPropertyListItemResponse>> listPointTransfers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID（platform_admin可指定，否则取当前）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "住户ID筛选") @RequestParam(required = false) String residentId) {
        return ApiResponse.success(adminTransferToPropertyService.listPointTransfers(
                page, pageSize, propertyCompanyId, residentId));
    }

    @Operation(summary = "转给物业-物业币流水",
            description = "分页查询住户转给物业的物业币流水。角色: property_admin, platform_admin, coordinator, sector_leader, individual_leader")
    @GetMapping("/coins")
    public ApiResponse<PageResponse<TransferToPropertyListItemResponse>> listCoinTransfers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID（platform_admin可指定，否则取当前）")
            @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "住户ID筛选") @RequestParam(required = false) String residentId) {
        return ApiResponse.success(adminTransferToPropertyService.listCoinTransfers(
                page, pageSize, propertyCompanyId, residentId));
    }
}
