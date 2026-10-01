package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.priceapproval.AuditPriceApprovalRequest;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.dto.sectorleader.PriceApprovalResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderAuditMerchantRequest;
import com.wuye.points.dto.sectorleader.SectorLeaderEarningsResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMerchantItemResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMerchantRevenueItemResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMyResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "板块负责人工作台", description = "板块负责人个人信息、板块商家、收入概览、审批跟进、提现")
@RestController
@RequestMapping("/sector-leaders")
@RequiredArgsConstructor
public class SectorLeaderWorkbenchController {

    private final SectorLeaderWorkbenchService sectorLeaderWorkbenchService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "获取当前板块负责人信息")
    @GetMapping("/my")
    public ApiResponse<SectorLeaderMyResponse> getMyInfo() {
        return ApiResponse.success(sectorLeaderWorkbenchService.getMyInfo());
    }

    @Operation(summary = "本板块商家列表",
            description = "板块负责人查看与自己产生过分成关系的商家。角色: sector_leader")
    @GetMapping("/{id}/merchants")
    public ApiResponse<List<SectorLeaderMerchantItemResponse>> listMyMerchants(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id) {
        return ApiResponse.success(sectorLeaderWorkbenchService.listMyMerchants());
    }

    @Operation(summary = "收入概览",
            description = "板块负责人分成收入汇总与明细。角色: sector_leader")
    @GetMapping("/{id}/earnings")
    public ApiResponse<SectorLeaderEarningsResponse> getMyEarnings(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id) {
        return ApiResponse.success(sectorLeaderWorkbenchService.getMyEarnings());
    }

    @Operation(summary = "待关注审批列表",
            description = "本板块个体负责人发起、待审批的商家分成审批。角色: sector_leader")
    @GetMapping("/{id}/approvals")
    public ApiResponse<List<PriceApprovalResponse>> listMyApprovals(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id) {
        return ApiResponse.success(sectorLeaderWorkbenchService.listMyApprovals());
    }

    @Operation(summary = "审批商家的加入",
            description = "审批本板块个体负责人新增的商家，同意后商家可登录商家平台。角色: sector_leader")
    @PostMapping("/{id}/merchant-audits")
    public ApiResponse<SectorLeaderMerchantItemResponse> auditMerchantJoin(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id,
            @Valid @RequestBody SectorLeaderAuditMerchantRequest request) {
        return ApiResponse.success(sectorLeaderWorkbenchService.auditMerchantJoinByLeader(
                request.getMerchantId(), Boolean.TRUE.equals(request.getApproved()), request.getRemark()));
    }

    @Operation(summary = "审批商家的分成比例",
            description = "审批本板块个体负责人提交的商家分成申请。角色: sector_leader")
    @PostMapping("/{id}/approvals/{approvalId}/audit")
    public ApiResponse<Object> auditMerchantDistribution(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id,
            @Parameter(description = "审批记录ID") @PathVariable String approvalId,
            @Valid @RequestBody AuditPriceApprovalRequest request) {
        return ApiResponse.success(sectorLeaderWorkbenchService.auditMerchantDistribution(
                approvalId, Boolean.TRUE.equals(request.getApproved()), request.getRemark()));
    }

    @Operation(summary = "所负责商家营收情况",
            description = "按商家聚合的营收总额、订单笔数与我的分成。角色: sector_leader")
    @GetMapping("/{id}/merchant-revenues")
    public ApiResponse<List<SectorLeaderMerchantRevenueItemResponse>> listMyMerchantRevenues(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id) {
        return ApiResponse.success(sectorLeaderWorkbenchService.listMyMerchantRevenues());
    }

    @Operation(summary = "板块负责人提现申请",
            description = "从分成收入中提现。角色: sector_leader，仅能操作本人（sl_ID）")
    @PostMapping("/{id}/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id,
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForSectorLeader(id, request));
    }

    @Operation(summary = "板块负责人提现记录列表",
            description = "查询本人提现记录。角色: sector_leader")
    @GetMapping("/{id}/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "板块负责人ID（sl_）") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForSectorLeader(
                id, page, pageSize, auditStatus, startDate, endDate));
    }
}
