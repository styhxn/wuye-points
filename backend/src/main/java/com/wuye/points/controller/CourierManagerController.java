package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.couriermanager.AddCourierRequest;
import com.wuye.points.dto.couriermanager.CourierListResponse;
import com.wuye.points.dto.couriermanager.CourierManagerDetailResponse;
import com.wuye.points.dto.couriermanager.CourierManagerListItemResponse;
import com.wuye.points.dto.couriermanager.CourierOperationResponse;
import com.wuye.points.dto.couriermanager.CreateCourierManagerRequest;
import com.wuye.points.dto.couriermanager.MyCourierManagerResponse;
import com.wuye.points.dto.couriermanager.UpdateCourierManagerRequest;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.service.CourierManagerService;
import com.wuye.points.service.RoleWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 快递负责人模块（含快递员管理、负责人工作台）
 */
@Tag(name = "快递负责人管理", description = "快递负责人CRUD、快递员管理、负责人工作台、提现")
@RestController
@RequestMapping("/courier-managers")
@RequiredArgsConstructor
public class CourierManagerController {

    private final CourierManagerService courierManagerService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "快递负责人列表", description = "分页查询快递负责人列表。角色: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<PageResponse<CourierManagerListItemResponse>> listCourierManagers(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键字（姓名/手机号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司ID（platform_admin 可指定）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(courierManagerService.listCourierManagers(
                page, pageSize, communityId, status, keyword, propertyCompanyId));
    }

    @Operation(summary = "快递负责人详情", description = "获取快递负责人详情。角色: property_admin, platform_admin")
    @GetMapping("/{id}")
    public ApiResponse<CourierManagerDetailResponse> getCourierManager(
            @Parameter(description = "负责人ID") @PathVariable String id) {
        return ApiResponse.success(courierManagerService.getCourierManager(id));
    }

    @Operation(summary = "创建快递负责人", description = "创建快递负责人。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<CourierManagerDetailResponse> createCourierManager(
            @Valid @RequestBody CreateCourierManagerRequest request) {
        return ApiResponse.success(courierManagerService.createCourierManager(request));
    }

    @Operation(summary = "更新快递负责人", description = "更新快递负责人信息。角色: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<CourierManagerDetailResponse> updateCourierManager(
            @Parameter(description = "负责人ID") @PathVariable String id,
            @Valid @RequestBody UpdateCourierManagerRequest request) {
        return ApiResponse.success(courierManagerService.updateCourierManager(id, request));
    }

    @Operation(summary = "删除快递负责人", description = "逻辑删除（设为inactive），并解绑名下快递员。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<CourierManagerDetailResponse> deleteCourierManager(
            @Parameter(description = "负责人ID") @PathVariable String id) {
        return ApiResponse.success(courierManagerService.deleteCourierManager(id));
    }

    @Operation(summary = "我的快递负责人工作台", description = "获取当前 courier 角色对应的快递负责人信息。角色: courier")
    @GetMapping("/my")
    public ApiResponse<MyCourierManagerResponse> getMyCourierManager() {
        return ApiResponse.success(courierManagerService.getMyCourierManager());
    }

    @Operation(summary = "快递员提现申请", description = "快递员从配送收入中提现。角色: courier")
    @PostMapping("/my/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForCourier(request));
    }

    @Operation(summary = "快递员提现记录列表", description = "查询当前快递员提现记录。角色: courier")
    @GetMapping("/my/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForCourier(
                page, pageSize, auditStatus, startDate, endDate));
    }

    @Operation(summary = "获取快递员列表", description = "获取指定负责人名下快递员列表。角色: property_admin, platform_admin")
    @GetMapping("/{id}/couriers")
    public ApiResponse<CourierListResponse> listCouriers(
            @Parameter(description = "负责人ID") @PathVariable String id) {
        return ApiResponse.success(courierManagerService.listCouriers(id));
    }

    @Operation(summary = "增加快递员", description = "将住户增加为指定负责人的快递员。角色: property_admin, platform_admin")
    @PostMapping("/{id}/couriers")
    public ApiResponse<CourierOperationResponse> addCourier(
            @Parameter(description = "负责人ID") @PathVariable String id,
            @Valid @RequestBody AddCourierRequest request) {
        return ApiResponse.success(courierManagerService.addCourier(id, request));
    }

    @Operation(summary = "移除快递员", description = "从负责人名下移除快递员。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}/couriers/{courierId}")
    public ApiResponse<CourierOperationResponse> removeCourier(
            @Parameter(description = "负责人ID") @PathVariable String id,
            @Parameter(description = "快递员业主ID") @PathVariable String courierId) {
        return ApiResponse.success(courierManagerService.removeCourier(id, courierId));
    }
}
