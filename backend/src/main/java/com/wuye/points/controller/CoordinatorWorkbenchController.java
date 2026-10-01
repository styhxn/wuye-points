package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.coordinator.AddCoordinatorMerchantRequest;
import com.wuye.points.dto.coordinator.AddIndividualLeaderRequest;
import com.wuye.points.dto.coordinator.AddSectorLeaderRequest;
import com.wuye.points.dto.coordinator.CoordinatorMerchantCreatedResponse;
import com.wuye.points.dto.coordinator.CoordinatorMyResponse;
import com.wuye.points.dto.coordinator.FreezeMerchantRequest;
import com.wuye.points.dto.coordinator.IndividualLeaderCreatedResponse;
import com.wuye.points.dto.coordinator.MerchantFreezeResponse;
import com.wuye.points.dto.coordinator.SectorLeaderCreatedResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.dto.sectorleader.AuditMerchantJoinRequest;
import com.wuye.points.dto.sectorleader.MerchantUploadResponse;
import com.wuye.points.service.CoordinatorWorkbenchService;
import com.wuye.points.service.RoleWithdrawalService;
import com.wuye.points.service.SectorLeaderWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "统筹工作台", description = "统筹个人信息、板块管理、商家管理、特惠管理")
@RestController
@RequestMapping("/coordinators")
@RequiredArgsConstructor
public class CoordinatorWorkbenchController {

    private final CoordinatorWorkbenchService coordinatorWorkbenchService;
    private final SectorLeaderWorkbenchService sectorLeaderWorkbenchService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "获取当前统筹信息")
    @GetMapping("/my")
    public ApiResponse<CoordinatorMyResponse> getMyInfo() {
        return ApiResponse.success(coordinatorWorkbenchService.getMyInfo());
    }

    @Operation(summary = "统筹增加板块负责人",
            description = "统筹/物业管理员新增板块负责人。角色: coordinator, property_admin")
    @PostMapping("/{id}/sector-leaders")
    public ApiResponse<SectorLeaderCreatedResponse> addSectorLeader(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Valid @RequestBody AddSectorLeaderRequest request) {
        return ApiResponse.success(coordinatorWorkbenchService.addSectorLeader(id, request));
    }

    @Operation(summary = "统筹增加个体负责人",
            description = "统筹/物业管理员新增个体负责人。角色: coordinator, property_admin")
    @PostMapping("/{id}/individual-leaders")
    public ApiResponse<IndividualLeaderCreatedResponse> addIndividualLeader(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Valid @RequestBody AddIndividualLeaderRequest request) {
        return ApiResponse.success(coordinatorWorkbenchService.addIndividualLeader(id, request));
    }

    @Operation(summary = "统筹增加商户",
            description = "统筹/物业管理员挂接平台商家到本物业。角色: coordinator, property_admin")
    @PostMapping("/{id}/merchants")
    public ApiResponse<CoordinatorMerchantCreatedResponse> addMerchant(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Valid @RequestBody AddCoordinatorMerchantRequest request) {
        return ApiResponse.success(coordinatorWorkbenchService.addMerchant(id, request));
    }

    @Operation(summary = "商户冻结/解冻",
            description = "统筹/物业管理员冻结或解冻商户。角色: coordinator, property_admin")
    @PutMapping("/{id}/merchants/{merchantId}/freeze")
    public ApiResponse<MerchantFreezeResponse> freezeMerchant(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Parameter(description = "商家ID") @PathVariable String merchantId,
            @Valid @RequestBody FreezeMerchantRequest request) {
        return ApiResponse.success(coordinatorWorkbenchService.freezeMerchant(id, merchantId, request));
    }

    @Operation(summary = "统筹审核商家加入",
            description = "统筹审核板块提交的商家加入申请（仅统筹可审核）。角色: coordinator")
    @PutMapping("/{id}/merchant-audit")
    public ApiResponse<MerchantUploadResponse> auditMerchantJoin(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Valid @RequestBody AuditMerchantJoinRequest request) {
        return ApiResponse.success(sectorLeaderWorkbenchService.auditMerchantJoin(id, request));
    }

    @Operation(summary = "统筹申请提现",
            description = "统筹按分成累计收益申请提现。角色: coordinator，金额不超过可提现余额（累计分成-已占用提现）")
    @PostMapping("/{id}/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForCoordinator(id, request));
    }

    @Operation(summary = "统筹提现记录", description = "统筹提现记录列表。角色: coordinator")
    @GetMapping("/{id}/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "统筹ID") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForCoordinator(
                id, page, pageSize, auditStatus, startDate, endDate));
    }
}
