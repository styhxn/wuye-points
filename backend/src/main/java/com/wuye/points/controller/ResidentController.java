package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.BuildingChangeResponse;
import com.wuye.points.dto.resident.CreateBuildingChangeRequest;
import com.wuye.points.dto.resident.CreateResidentRequest;
import com.wuye.points.dto.resident.DeleteAccountResponse;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.dto.resident.RequestAccountDeletionRequest;
import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ResidentStatusResponse;
import com.wuye.points.dto.resident.UpdateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentStatusRequest;
import com.wuye.points.dto.resident.BindCommunityRequest;
import com.wuye.points.dto.resident.BookingContactInfoResponse;
import com.wuye.points.dto.resident.TransferToPropertyCoinRequest;
import com.wuye.points.dto.resident.TransferToPropertyCoinResponse;
import com.wuye.points.dto.resident.TransferToPropertyPointRequest;
import com.wuye.points.dto.resident.TransferToPropertyPointResponse;
import com.wuye.points.service.PointService;
import com.wuye.points.service.PropertyCoinService;
import com.wuye.points.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 业主/住户模块
 */
@Tag(name = "业主/住户模块", description = "业主列表、创建、详情、状态管理、积分/物业币余额")
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;

    /**
     * 管理员创建住户
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "管理员创建住户", description = "管理员创建住户，需登录，角色：property_admin, platform_admin")
    @PostMapping
    public ApiResponse<ResidentDetailResponse> createResident(
            @Valid @RequestBody CreateResidentRequest request) {
        return ApiResponse.success(residentService.createResident(request));
    }

    /**
     * 业主列表（管理员）
     * Auth: Required | Roles: property_admin, platform_admin, coordinator（coordinator 仅本物业只读）
     */
    @Operation(summary = "业主列表", description = "分页查询业主列表（管理员），需登录，角色：property_admin, platform_admin, coordinator（coordinator 仅本物业只读）")
    @GetMapping
    public ApiResponse<PageResponse<ResidentListItemResponse>> listResidents(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(residentService.listResidents(
                page, pageSize, keyword, communityId, building, status, role, propertyCompanyId, sort));
    }

    /**
     * 我的积分余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "我的积分余额", description = "获取我的积分余额，需登录，角色：resident")
    @GetMapping("/me/points")
    public ApiResponse<MyPointsResponse> getMyPoints() {
        return ApiResponse.success(residentService.getMyPoints());
    }

    /**
     * 我的物业币余额
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "我的物业币余额", description = "获取我的物业币余额，需登录，角色：resident")
    @GetMapping("/me/coins")
    public ApiResponse<MyCoinsResponse> getMyCoins() {
        return ApiResponse.success(residentService.getMyCoins());
    }

    /**
     * 获取当前用户信息
     * Auth: Required
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息，需登录")
    @GetMapping("/me")
    public ApiResponse<ResidentDetailResponse> getCurrentResident() {
        return ApiResponse.success(residentService.getCurrentResident());
    }

    /**
     * 预约预填信息（v7.8）：完整手机号 + 默认服务地址（楼栋/单元/房号），供预约服务自动带出。需登录。
     * Auth: Required
     */
    @Operation(summary = "预约预填信息", description = "预约服务时自动带出的联系电话与默认地址，需登录")
    @GetMapping("/me/booking-info")
    public ApiResponse<BookingContactInfoResponse> getBookingInfo() {
        return ApiResponse.success(residentService.getBookingInfo());
    }

    /**
     * 更新当前用户信息
     * Auth: Required
     */
    @Operation(summary = "更新当前用户信息", description = "更新当前用户信息，需登录")
    @PutMapping("/me")
    public ApiResponse<ResidentDetailResponse> updateCurrentResident(
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateCurrentResident(request));
    }

    /**
     * 业主详情
     * Auth: Required | Roles: property_admin, platform_admin, resident（仅本人）
     */
    @Operation(summary = "业主详情", description = "获取业主详情，需登录，角色：property_admin, platform_admin, resident（仅本人）")
    @GetMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> getResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(residentService.getResident(id));
    }

    /**
     * 更新业主信息
     * Auth: Required | Roles: resident（仅本人）, property_admin, platform_admin
     */
    @Operation(summary = "更新业主信息", description = "更新业主信息，需登录，角色：resident（仅本人）, property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<ResidentDetailResponse> updateResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateResidentRequest request) {
        return ApiResponse.success(residentService.updateResident(id, request));
    }

    /**
     * 冻结/解冻/禁用
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "冻结/解冻/禁用", description = "冻结/解冻/禁用业主，需登录，角色：property_admin, platform_admin")
    @PatchMapping("/{id}/status")
    public ApiResponse<ResidentStatusResponse> updateStatus(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id,
            @Valid @RequestBody UpdateResidentStatusRequest request) {
        return ApiResponse.success(residentService.updateStatus(id, request));
    }

    /**
     * 删除住户（软删除）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @Operation(summary = "删除住户", description = "删除住户（软删除），需登录，角色：property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResident(
            @Parameter(description = "业主ID", example = "1") @PathVariable String id) {
        residentService.deleteResident(id);
        return ApiResponse.success(null);
    }

    /**
     * 申请注销账号
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "申请注销账号", description = "申请注销账号，需登录，角色：resident")
    @PostMapping("/me/delete-request")
    public ApiResponse<DeleteAccountResponse> requestAccountDeletion(
            @Valid @RequestBody RequestAccountDeletionRequest request) {
        return ApiResponse.success(residentService.requestAccountDeletion(request));
    }

    @Operation(summary = "提交楼栋变更申请",
            description = "住户提交楼栋/单元/房号变更申请，物业审批通过后写入住户地址。同一住户同时仅允许一条待审批申请。需登录，角色：resident")
    @PostMapping("/me/building-changes")
    public ApiResponse<BuildingChangeResponse> createBuildingChangeRequest(
            @Valid @RequestBody CreateBuildingChangeRequest request) {
        return ApiResponse.success(residentService.createBuildingChangeRequest(request));
    }

    @Operation(summary = "我的楼栋变更申请", description = "按申请时间倒序返回当前住户的楼栋变更申请。需登录，角色：resident")
    @GetMapping("/me/building-changes")
    public ApiResponse<List<BuildingChangeResponse>> listMyBuildingChanges() {
        return ApiResponse.success(residentService.listMyBuildingChanges());
    }

    /**
     * 提交楼栋变更申请（单数路径别名，与 §2.6.2.1 文档路径一致）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "提交楼栋变更申请（单数别名）",
            description = "与 POST /residents/me/building-changes 等价，兼容文档路径 §2.6.2.1。需登录，角色：resident")
    @PostMapping("/me/building-change")
    public ApiResponse<BuildingChangeResponse> createBuildingChange(
            @Valid @RequestBody CreateBuildingChangeRequest request) {
        return ApiResponse.success(residentService.createBuildingChangeRequest(request));
    }

    /**
     * 最近一次楼栋变更申请（§2.6.2.2）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "查询最近一次楼栋变更申请",
            description = "返回当前住户最近一次楼栋变更申请（含 status/rejectReason），无申请时 data 为 null。需登录，角色：resident")
    @GetMapping("/me/building-change")
    public ApiResponse<BuildingChangeResponse> getMyLatestBuildingChange() {
        return ApiResponse.success(residentService.getMyLatestBuildingChange());
    }

    /**
     * 完善/绑定小区（§2.6.1）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "完善/绑定小区",
            description = "住户自主绑定所属小区；小区须存在且属于当前住户所属物业公司，否则返回 10002。需登录，角色：resident")
    @PutMapping("/me/community")
    public ApiResponse<ResidentDetailResponse> bindMyCommunity(
            @Valid @RequestBody BindCommunityRequest request) {
        return ApiResponse.success(residentService.bindMyCommunity(request));
    }

    /**
     * 积分转给物业（§2.6.4.1）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "积分转给物业",
            description = "住户主动将个人积分转给物业公司：立即扣减个人积分并生成 source=transfer_to_property 流水，"
                    + "物业端可在「转给物业」对账列表查看，无需审批。家庭池不参与。需登录，角色：resident")
    @PostMapping("/me/points/transfer-to-property")
    public ApiResponse<TransferToPropertyPointResponse> transferPointsToProperty(
            @Valid @RequestBody TransferToPropertyPointRequest request) {
        return ApiResponse.success(pointService.transferToProperty(request));
    }

    /**
     * 物业币转给物业（§2.6.4.2）
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "物业币转给物业",
            description = "住户主动将家庭物业币转给物业公司：仅扣减<b>家庭物业币</b>并生成 source=transfer_to_property 流水，"
                    + "物业端可在「转给物业」对账列表查看，无需审批。个人物业币不可转赠；无家庭或家庭余额不足时拒绝。需登录，角色：resident")
    @PostMapping("/me/coins/transfer-to-property")
    public ApiResponse<TransferToPropertyCoinResponse> transferCoinsToProperty(
            @Valid @RequestBody TransferToPropertyCoinRequest request) {
        return ApiResponse.success(propertyCoinService.transferToProperty(request));
    }
}
