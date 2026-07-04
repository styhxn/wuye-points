package com.wuye.points.controller;

import com.wuye.points.dto.admin.coinfreeze.AdminFreezeCoinRequest;
import com.wuye.points.dto.admin.coinfreeze.AdminUnfreezeCoinRequest;
import com.wuye.points.dto.admin.coinfreeze.CoinFreezeRecordItemResponse;
import com.wuye.points.dto.coin.FreezeCoinRequest;
import com.wuye.points.dto.coin.FreezeCoinResponse;
import com.wuye.points.dto.coin.UnfreezeCoinRequest;
import com.wuye.points.dto.coin.UnfreezeCoinResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.PropertyCoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端 — 业主物业币管控", description = "冻结记录、冻结物业币、解冻物业币")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCoinManagementController {

    private final PropertyCoinService propertyCoinService;

    @Operation(summary = "冻结记录列表", description = "获取物业币冻结记录列表。角色: property_admin, platform_admin")
    @GetMapping("/coin-freeze-records")
    public ApiResponse<PageResponse<CoinFreezeRecordItemResponse>> listFreezeRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "业主ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String action,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyCoinService.listFreezeRecords(
                page, pageSize, residentId, action, propertyCompanyId, sort));
    }

    @Operation(summary = "冻结业主物业币", description = "冻结业主物业币。角色: property_admin, platform_admin")
    @PostMapping("/residents/{id}/coin/freeze")
    public ApiResponse<FreezeCoinResponse> freeze(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AdminFreezeCoinRequest request) {
        FreezeCoinRequest coinRequest = new FreezeCoinRequest();
        coinRequest.setResidentId(id);
        coinRequest.setAmount(request.getAmount());
        coinRequest.setReason(request.getReason());
        return ApiResponse.success(propertyCoinService.freeze(coinRequest));
    }

    @Operation(summary = "解冻业主物业币", description = "解冻业主物业币。角色: property_admin, platform_admin")
    @PostMapping("/residents/{id}/coin/unfreeze")
    public ApiResponse<UnfreezeCoinResponse> unfreeze(
            @Parameter(description = "资源ID") @PathVariable String id,
            @Valid @RequestBody AdminUnfreezeCoinRequest request) {
        UnfreezeCoinRequest coinRequest = new UnfreezeCoinRequest();
        coinRequest.setResidentId(id);
        coinRequest.setFrozenRecordId(request.getFrozenRecordId());
        coinRequest.setReason(request.getReason());
        return ApiResponse.success(propertyCoinService.unfreeze(coinRequest));
    }
}
