package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.individualleader.IndividualLeaderMerchantRevenueItemResponse;
import com.wuye.points.dto.individualleader.IndividualLeaderMyResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.dto.sectorleader.MerchantDeliveryFeeResponse;
import com.wuye.points.dto.sectorleader.PriceApprovalResponse;
import com.wuye.points.dto.sectorleader.SetFreeDeliveryThresholdRequest;
import com.wuye.points.dto.sectorleader.SetMerchantDistributionRequest;
import com.wuye.points.service.IndividualLeaderWorkbenchService;
import com.wuye.points.service.RoleWithdrawalService;
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

@Tag(name = "个体负责人工作台", description = "个体负责人设置商家分成、满额配送费、提现")
@RestController
@RequestMapping("/individual-leaders")
@RequiredArgsConstructor
public class IndividualLeaderWorkbenchController {

    private final IndividualLeaderWorkbenchService individualLeaderWorkbenchService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "获取当前个体负责人信息",
            description = "返回个体负责人记录ID（il_）。路径参数也可用 me 或住户ID（res_）代替 il_ID")
    @GetMapping("/my")
    public ApiResponse<IndividualLeaderMyResponse> getMyInfo() {
        return ApiResponse.success(individualLeaderWorkbenchService.getMyInfo());
    }

    @Operation(summary = "个体负责人设置与商家分成",
            description = "角色: individual_leader，需提交模块负责人同意，创建价格审批记录。"
                    + "路径 id 可为 il_ID / 住户ID / me")
    @PutMapping("/{id}/merchant-distribution")
    public ApiResponse<PriceApprovalResponse> setMerchantDistribution(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id,
            @Valid @RequestBody SetMerchantDistributionRequest request) {
        return ApiResponse.success(individualLeaderWorkbenchService.setMerchantDistribution(id, request));
    }

    @Operation(summary = "个体负责人设置满额配送费",
            description = "角色: individual_leader，更新商家的满额免配送费阈值。"
                    + "路径 id 可为 il_ID / 住户ID / me")
    @PutMapping("/{id}/delivery-fee")
    public ApiResponse<MerchantDeliveryFeeResponse> setDeliveryFee(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id,
            @Valid @RequestBody SetFreeDeliveryThresholdRequest request) {
        return ApiResponse.success(individualLeaderWorkbenchService.setDeliveryFee(id, request));
    }

    @Operation(summary = "个体负责人提现申请",
            description = "个体负责人从分成收入中提现。角色: individual_leader。路径 id 可为 il_ID / 住户ID / me")
    @PostMapping("/{id}/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id,
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForIndividualLeader(id, request));
    }

    @Operation(summary = "个体负责人提现记录列表",
            description = "查询当前个体负责人提现记录。角色: individual_leader。路径 id 可为 il_ID / 住户ID / me")
    @GetMapping("/{id}/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForIndividualLeader(
                id, page, pageSize, auditStatus, startDate, endDate));
    }

    @Operation(summary = "所负责商家及营收情况",
            description = "我新增/归属的商家：审核状态、营收总额、我的分成。角色: individual_leader。路径 id 可为 il_ID / 住户ID / me")
    @GetMapping("/{id}/merchant-revenues")
    public ApiResponse<java.util.List<IndividualLeaderMerchantRevenueItemResponse>> listMyMerchantRevenues(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id) {
        return ApiResponse.success(individualLeaderWorkbenchService.listMyMerchantRevenues());
    }

    @Operation(summary = "商家审批情况",
            description = "我提交的商家分成审批记录（待审/通过/驳回）。角色: individual_leader。路径 id 可为 il_ID / 住户ID / me")
    @GetMapping("/{id}/approvals")
    public ApiResponse<java.util.List<PriceApprovalResponse>> listMyApprovals(
            @Parameter(description = "个体负责人ID（il_）、住户ID（res_）或 me") @PathVariable String id) {
        return ApiResponse.success(individualLeaderWorkbenchService.listMyApprovals());
    }
}
