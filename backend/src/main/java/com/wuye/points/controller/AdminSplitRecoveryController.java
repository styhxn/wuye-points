package com.wuye.points.controller;

import com.wuye.points.dto.admin.recovery.RecoveryRecordResponse;
import com.wuye.points.dto.admin.recovery.UpdateRecoveryRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.SplitRecoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 分账回退待追回台账（阶段2）。
 *
 * <p>电商收付通分账后退款：个人接收方（快递员）不可微信回退，平台垫付后登记本台账，
 * 运营查询、线下追回并核销。权限：property_admin / platform_admin。</p>
 */
@Tag(name = "管理端 — 分账回退待追回台账", description = "个人接收方不可回退的垫付资金台账查询与核销")
@RestController
@RequestMapping("/admin/split-recovery")
@RequiredArgsConstructor
public class AdminSplitRecoveryController {

    private final SplitRecoveryService recoveryService;

    @Operation(summary = "台账分页查询",
            description = "按状态/角色筛选待追回记录。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<RecoveryRecordResponse>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "排序方式", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort,
            @Parameter(description = "状态：pending/recovered/written_off") @RequestParam(required = false) String status,
            @Parameter(description = "角色：COURIER 等") @RequestParam(required = false) String ownerType) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setPageSize(pageSize);
        pageRequest.setSort(sort);
        return ApiResponse.success(recoveryService.list(pageRequest, status, ownerType));
    }

    @Operation(summary = "标记已追回",
            description = "线下追回成功后置 recovered（仅 pending 可操作）。角色: property_admin, platform_admin")
    @PostMapping("/{id}/recovered")
    public ApiResponse<RecoveryRecordResponse> markRecovered(
            @Parameter(description = "台账ID") @PathVariable String id,
            @Valid @RequestBody(required = false) UpdateRecoveryRequest request) {
        return ApiResponse.success(recoveryService.markRecovered(id, request));
    }

    @Operation(summary = "核销",
            description = "确认无法追回做坏账处理，置 written_off（仅 pending 可操作）。角色: property_admin, platform_admin")
    @PostMapping("/{id}/write-off")
    public ApiResponse<RecoveryRecordResponse> writeOff(
            @Parameter(description = "台账ID") @PathVariable String id,
            @Valid @RequestBody(required = false) UpdateRecoveryRequest request) {
        return ApiResponse.success(recoveryService.writeOff(id, request));
    }
}
