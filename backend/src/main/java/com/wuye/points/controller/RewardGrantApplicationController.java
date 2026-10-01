package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.rewardgrant.AuditRewardGrantRequest;
import com.wuye.points.dto.rewardgrant.RewardGrantItemResponse;
import com.wuye.points.dto.rewardgrant.SubmitRewardGrantRequest;
import com.wuye.points.service.RewardGrantApplicationService;
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
 * 奖励金/积分发放审批
 */
@Tag(name = "奖励金/积分审批模块", description = "奖励金/积分发放申请、物业审批")
@RestController
@RequestMapping("/reward-grants")
@RequiredArgsConstructor
public class RewardGrantApplicationController {

    private final RewardGrantApplicationService rewardGrantApplicationService;

    @Operation(summary = "提交发放申请", description = "住户/商家提交奖励金/积分发放申请，需物业审批。Roles: resident, merchant")
    @PostMapping
    public ApiResponse<RewardGrantItemResponse> submit(
            @Valid @RequestBody SubmitRewardGrantRequest request) {
        return ApiResponse.success(rewardGrantApplicationService.submit(request));
    }

    @Operation(summary = "审批发放申请", description = "物业审批奖励金/积分发放申请，通过后发放积分。Roles: property_admin")
    @PostMapping("/{id}/audit")
    public ApiResponse<RewardGrantItemResponse> audit(
            @Parameter(description = "申请ID") @PathVariable String id,
            @Valid @RequestBody AuditRewardGrantRequest request) {
        return ApiResponse.success(rewardGrantApplicationService.audit(id, request));
    }

    @Operation(summary = "我的申请", description = "申请人查看自己的发放申请。Roles: resident, merchant")
    @GetMapping("/my")
    public ApiResponse<PageResponse<RewardGrantItemResponse>> listMy(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(rewardGrantApplicationService.listMy(page, pageSize));
    }

    @Operation(summary = "审批列表", description = "物业/平台查看发放申请列表（可按状态/目标住户过滤）。平台管理员仅查看，审批操作仍限物业。Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<RewardGrantItemResponse>> listAdmin(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态 pending_audit/approved/rejected") @RequestParam(required = false) String status,
            @Parameter(description = "目标住户ID") @RequestParam(required = false) String targetResidentId) {
        return ApiResponse.success(rewardGrantApplicationService.listAdmin(page, pageSize, status, targetResidentId));
    }
}
