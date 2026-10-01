package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ReviewAccountDeletionRequest;
import com.wuye.points.service.ResidentService;
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

@Tag(name = "管理端 — 住户管理", description = "住户注销审核、注销申请列表（平台管理员）")
@RestController
@RequestMapping("/admin/residents")
@RequiredArgsConstructor
public class AdminResidentController {

    private final ResidentService residentService;

    @Operation(summary = "注销申请列表", description = "查询处于注销冷静期的住户（status=pending_deletion）。角色: platform_admin")
    @GetMapping("/deletion-requests")
    public ApiResponse<PageResponse<ResidentListItemResponse>> listDeletionRequests(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(residentService.listDeletionRequests(page, pageSize));
    }

    @Operation(summary = "审核注销账号", description = "平台审核住户注销申请：approve 通过并注销，reject 驳回恢复。角色: platform_admin")
    @PostMapping("/{id}/deletion/review")
    public ApiResponse<Void> reviewAccountDeletion(
            @Parameter(description = "住户ID(res_)") @PathVariable String id,
            @Valid @RequestBody ReviewAccountDeletionRequest request) {
        residentService.reviewAccountDeletion(id, request.getAction(), request.getRejectReason());
        return ApiResponse.success(null);
    }
}
