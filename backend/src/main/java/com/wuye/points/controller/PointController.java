package com.wuye.points.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.point.DeductFeeRequest;
import com.wuye.points.dto.point.DeductFeeResponse;
import com.wuye.points.dto.point.EarnPointRequest;
import com.wuye.points.dto.point.EarnPointResponse;
import com.wuye.points.dto.point.ExpiringPointsResponse;
import com.wuye.points.dto.point.FamilyPointBalanceResponse;
import com.wuye.points.dto.point.PointBalanceResponse;
import com.wuye.points.dto.point.PointRecordItemResponse;
import com.wuye.points.dto.point.RedeemPointRequest;
import com.wuye.points.dto.point.RedeemPointResponse;
import com.wuye.points.dto.point.SpendPointRequest;
import com.wuye.points.dto.point.SpendPointResponse;
import com.wuye.points.dto.point.TransferPointRequest;
import com.wuye.points.dto.point.TransferPointResponse;
import com.wuye.points.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分模块
 */
@Tag(name = "积分模块", description = "积分余额、记录、转移、抵扣物业费、兑换")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 获取积分余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "获取积分余额", description = "返回当前用户积分余额及家庭积分汇总")
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getBalance() {
        return ApiResponse.success(pointService.getBalance());
    }

    /**
     * 获取家庭积分余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "获取家庭积分余额", description = "返回家庭所有成员的积分余额明细")
    @GetMapping("/family-balance")
    public ApiResponse<FamilyPointBalanceResponse> getFamilyBalance() {
        return ApiResponse.success(pointService.getFamilyBalance());
    }

    /**
     * 积分记录列表
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "积分记录列表", description = "分页查询积分流水记录")
    @GetMapping("/records")
    public ApiResponse<PageResponse<PointRecordItemResponse>> listRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "积分类型") @RequestParam(required = false) String pointType,
            @Parameter(description = "来源") @RequestParam(required = false) String source,
            @Parameter(description = "开始日期", example = "2026-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2026-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "家庭ID") @RequestParam(required = false) String familyId,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(pointService.listRecords(
                page, pageSize, pointType, source, startDate, endDate, familyId, sort));
    }

    /**
     * 即将过期积分
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "即将过期积分", description = "获取即将过期的积分，需登录，角色：resident")
    @GetMapping("/expiring")
    public ApiResponse<ExpiringPointsResponse> getExpiringPoints() {
        return ApiResponse.success(pointService.getExpiringPoints());
    }

    /**
     * 家庭内积分转移
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "家庭内积分转移", description = "家庭内积分转移，需登录，角色：resident")
    @PostMapping("/transfer")
    public ApiResponse<TransferPointResponse> transfer(@Valid @RequestBody TransferPointRequest request) {
        return ApiResponse.success(pointService.transfer(request));
    }

    /**
     * 积分抵扣物业费
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "积分抵扣物业费", description = "积分抵扣物业费，需登录，角色：resident")
    @PostMapping("/deduct-fee")
    public ApiResponse<DeductFeeResponse> deductFee(@Valid @RequestBody DeductFeeRequest request) {
        return ApiResponse.success(pointService.deductFee(request));
    }

    /**
     * 积分兑换（100积分=1元）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "积分兑换", description = "积分兑换（100积分=1元），需登录，角色：resident")
    @PostMapping("/redeem")
    public ApiResponse<RedeemPointResponse> redeem(@Valid @RequestBody RedeemPointRequest request) {
        return ApiResponse.success(pointService.redeem(request));
    }

    /**
     * 获取积分（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "获取积分", description = "获取积分（管理员/内部），需登录，角色：property_admin, platform_admin")
    @PostMapping("/earn")
    public ApiResponse<EarnPointResponse> earn(@Valid @RequestBody EarnPointRequest request) {
        return ApiResponse.success(pointService.earn(request));
    }

    /**
     * 消费积分（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "消费积分", description = "消费积分（管理员/内部），需登录，角色：property_admin, platform_admin")
    @PostMapping("/spend")
    public ApiResponse<SpendPointResponse> spend(@Valid @RequestBody SpendPointRequest request) {
        return ApiResponse.success(pointService.spend(request));
    }
}
