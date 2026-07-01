package com.wuye.points.controller;

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
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * 获取积分余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/balance")
    public ApiResponse<PointBalanceResponse> getBalance() {
        return ApiResponse.success(pointService.getBalance());
    }

    /**
     * 获取家庭积分余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/family-balance")
    public ApiResponse<FamilyPointBalanceResponse> getFamilyBalance() {
        return ApiResponse.success(pointService.getFamilyBalance());
    }

    /**
     * 积分记录列表
     * Auth: Required | Roles: resident
     */
    @GetMapping("/records")
    public ApiResponse<PageResponse<PointRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String pointType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String familyId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(pointService.listRecords(
                page, pageSize, pointType, source, startDate, endDate, familyId, sort));
    }

    /**
     * 即将过期积分
     * Auth: Required | Roles: resident
     */
    @GetMapping("/expiring")
    public ApiResponse<ExpiringPointsResponse> getExpiringPoints() {
        return ApiResponse.success(pointService.getExpiringPoints());
    }

    /**
     * 家庭内积分转移
     * Auth: Required | Roles: resident
     */
    @PostMapping("/transfer")
    public ApiResponse<TransferPointResponse> transfer(@Valid @RequestBody TransferPointRequest request) {
        return ApiResponse.success(pointService.transfer(request));
    }

    /**
     * 积分抵扣物业费
     * Auth: Required | Roles: resident
     */
    @PostMapping("/deduct-fee")
    public ApiResponse<DeductFeeResponse> deductFee(@Valid @RequestBody DeductFeeRequest request) {
        return ApiResponse.success(pointService.deductFee(request));
    }

    /**
     * 积分兑换（100积分=1元）
     * Auth: Required | Roles: resident
     */
    @PostMapping("/redeem")
    public ApiResponse<RedeemPointResponse> redeem(@Valid @RequestBody RedeemPointRequest request) {
        return ApiResponse.success(pointService.redeem(request));
    }

    /**
     * 获取积分（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/earn")
    public ApiResponse<EarnPointResponse> earn(@Valid @RequestBody EarnPointRequest request) {
        return ApiResponse.success(pointService.earn(request));
    }

    /**
     * 消费积分（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/spend")
    public ApiResponse<SpendPointResponse> spend(@Valid @RequestBody SpendPointRequest request) {
        return ApiResponse.success(pointService.spend(request));
    }
}
