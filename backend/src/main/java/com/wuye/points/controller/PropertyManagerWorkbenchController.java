package com.wuye.points.controller;

import com.wuye.points.dto.admin.buildingleader.AppointBuildingLeaderRequest;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderDetailResponse;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderListItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerEarningItemResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerMyResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerWalletResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.service.PropertyManagerWorkbenchService;
import com.wuye.points.service.RoleWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物业经理工作台：本人信息、楼长管理、抽成收益、钱包与提现。
 */
@Tag(name = "物业经理工作台", description = "物业经理个人信息、楼长管理、抽成收益流水、钱包与提现")
@RestController
@RequestMapping("/property-managers")
@RequiredArgsConstructor
public class PropertyManagerWorkbenchController {

    private final PropertyManagerWorkbenchService propertyManagerWorkbenchService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "获取当前物业经理信息", description = "经理身份与抽成比例。角色: resident（须为在职物业经理）")
    @GetMapping("/my")
    public ApiResponse<PropertyManagerMyResponse> getMyInfo() {
        return ApiResponse.success(propertyManagerWorkbenchService.getMyInfo());
    }

    @Operation(summary = "名下楼长列表", description = "本物业公司全部单元楼长。角色: resident（须为在职物业经理）")
    @GetMapping("/my/building-leaders")
    public ApiResponse<PageResponse<BuildingLeaderListItemResponse>> listMyBuildingLeaders(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词（住户姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态 active/inactive") @RequestParam(required = false) String status) {
        return ApiResponse.success(propertyManagerWorkbenchService
                .listMyBuildingLeaders(page, pageSize, keyword, status));
    }

    @Operation(summary = "指定单元楼长",
            description = "复用管理端楼长指定逻辑，限定本物业公司。角色: resident（须为在职物业经理）")
    @PostMapping("/my/building-leaders")
    public ApiResponse<BuildingLeaderDetailResponse> appointBuildingLeader(
            @Valid @RequestBody AppointBuildingLeaderRequest request) {
        return ApiResponse.success(propertyManagerWorkbenchService.appointMyBuildingLeader(request));
    }

    @Operation(summary = "我的抽成收益流水", description = "经理抽成流水（含退款冲正负向流水）。角色: resident（须为在职物业经理）")
    @GetMapping("/my/earnings")
    public ApiResponse<PageResponse<PropertyManagerEarningItemResponse>> listMyEarnings(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(propertyManagerWorkbenchService.listMyEarnings(page, pageSize));
    }

    @Operation(summary = "我的钱包", description = "累计抽成、已提现、在途、可提现余额。角色: resident（须为在职物业经理）")
    @GetMapping("/my/wallet")
    public ApiResponse<PropertyManagerWalletResponse> getMyWallet() {
        return ApiResponse.success(propertyManagerWorkbenchService.getMyWallet());
    }

    @Operation(summary = "经理申请提现",
            description = "按累计抽成申请提现，金额不超过可提现余额（累计抽成 − 已提现 − 在途）。角色: resident（须为在职物业经理）")
    @PostMapping("/my/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForPropertyManager(request));
    }

    @Operation(summary = "经理提现记录", description = "经理提现记录列表。角色: resident（须为在职物业经理）")
    @GetMapping("/my/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForPropertyManager(
                page, pageSize, auditStatus, startDate, endDate));
    }
}
