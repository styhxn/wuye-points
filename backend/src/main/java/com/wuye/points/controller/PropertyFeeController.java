package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertyfee.CreatePropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeDetailResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeListItemResponse;
import com.wuye.points.service.PropertyFeeService;
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
 * 物业费模块
 */
@RestController
@RequestMapping("/property-fees")
@RequiredArgsConstructor
public class PropertyFeeController {

    private final PropertyFeeService propertyFeeService;

    /**
     * 我的物业费
     * Auth: Required | Roles: resident
     */
    @GetMapping("/my")
    public ApiResponse<PageResponse<PropertyFeeListItemResponse>> listMyPropertyFees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "-dueDate") String sort) {
        return ApiResponse.success(propertyFeeService.listMyPropertyFees(
                page, pageSize, feeType, paymentStatus, period, sort));
    }

    /**
     * 缴纳物业费
     * Auth: Required | Roles: resident
     */
    @PostMapping("/{id}/pay")
    public ApiResponse<PayPropertyFeeResponse> payPropertyFee(
            @PathVariable String id,
            @Valid @RequestBody PayPropertyFeeRequest request) {
        return ApiResponse.success(propertyFeeService.payPropertyFee(id, request));
    }

    /**
     * 物业费详情
     * Auth: Required | Roles: resident（本人/家庭）, property_admin, platform_admin
     */
    @GetMapping("/{id}")
    public ApiResponse<PropertyFeeDetailResponse> getPropertyFee(@PathVariable String id) {
        return ApiResponse.success(propertyFeeService.getPropertyFee(id));
    }

    /**
     * 物业费列表（管理员）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @GetMapping
    public ApiResponse<PageResponse<PropertyFeeListItemResponse>> listPropertyFees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String communityId,
            @RequestParam(required = false) String residentId,
            @RequestParam(required = false) String propertyCompanyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyFeeService.listPropertyFees(
                page, pageSize, feeType, paymentStatus, period, communityId,
                residentId, propertyCompanyId, keyword, sort));
    }

    /**
     * 创建物业费账单
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping
    public ApiResponse<PropertyFeeDetailResponse> createPropertyFee(
            @Valid @RequestBody CreatePropertyFeeRequest request) {
        return ApiResponse.success(propertyFeeService.createPropertyFee(request));
    }
}
