package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertyfee.CreatePropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeRequest;
import com.wuye.points.dto.propertyfee.PayPropertyFeeResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeDetailResponse;
import com.wuye.points.dto.propertyfee.PropertyFeeListItemResponse;
import com.wuye.points.service.PropertyFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物业费模块
 */
@Tag(name = "物业费模块", description = "物业费列表、缴纳、详情、创建")
@RestController
@RequestMapping("/property-fees")
@RequiredArgsConstructor
public class PropertyFeeController {

    private final PropertyFeeService propertyFeeService;

    @Operation(summary = "我的物业费", description = "我的物业费。Auth: Required | Roles: resident")
    @GetMapping("/my")
    public ApiResponse<PageResponse<PropertyFeeListItemResponse>> listMyPropertyFees(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "费用类型") @RequestParam(required = false) String feeType,
            @Parameter(description = "缴费状态") @RequestParam(required = false) String paymentStatus,
            @Parameter(description = "账期") @RequestParam(required = false) String period,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-dueDate") @RequestParam(defaultValue = "-dueDate") String sort) {
        return ApiResponse.success(propertyFeeService.listMyPropertyFees(
                page, pageSize, feeType, paymentStatus, period, sort));
    }

    @Operation(summary = "缴纳物业费", description = "缴纳物业费。Auth: Required | Roles: resident")
    @PostMapping("/{id}/pay")
    public ApiResponse<PayPropertyFeeResponse> payPropertyFee(
            @Parameter(description = "物业费ID") @PathVariable String id,
            @Valid @RequestBody PayPropertyFeeRequest request) {
        return ApiResponse.success(propertyFeeService.payPropertyFee(id, request));
    }

    @Operation(summary = "物业费详情", description = "物业费详情。Auth: Required | Roles: resident（本人/家庭）, property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<PropertyFeeDetailResponse> getPropertyFee(
            @Parameter(description = "物业费ID") @PathVariable String id) {
        return ApiResponse.success(propertyFeeService.getPropertyFee(id));
    }

    @Operation(summary = "物业费列表（管理员）", description = "物业费列表（管理员）。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<PropertyFeeListItemResponse>> listPropertyFees(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "费用类型") @RequestParam(required = false) String feeType,
            @Parameter(description = "缴费状态") @RequestParam(required = false) String paymentStatus,
            @Parameter(description = "账期") @RequestParam(required = false) String period,
            @Parameter(description = "社区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "住户ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyFeeService.listPropertyFees(
                page, pageSize, feeType, paymentStatus, period, communityId,
                residentId, propertyCompanyId, keyword, sort));
    }

    @Operation(summary = "创建物业费", description = "创建物业费账单。Auth: Required | Roles: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<PropertyFeeDetailResponse> createPropertyFee(
            @Valid @RequestBody CreatePropertyFeeRequest request) {
        return ApiResponse.success(propertyFeeService.createPropertyFee(request));
    }
}
