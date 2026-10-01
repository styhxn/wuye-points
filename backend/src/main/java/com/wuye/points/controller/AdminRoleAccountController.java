package com.wuye.points.controller;

import com.wuye.points.dto.admin.roleaccount.CreateRoleAccountRequest;
import com.wuye.points.dto.admin.roleaccount.CreateRoleAccountResponse;
import com.wuye.points.dto.admin.roleaccount.DeleteRoleAccountResponse;
import com.wuye.points.dto.admin.roleaccount.DisableRoleAccountRequest;
import com.wuye.points.dto.admin.roleaccount.DisableRoleAccountResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AdminRoleAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 业务角色账号直建/直删。
 * <p>用于数据被误删后的快速恢复：商家 / 活动组长 / 技工 / 配送员。
 * <p>权限：物业管理员（property_admin，含物业领导/物业操作员）、平台管理员。
 */
@Tag(name = "管理端 — 角色账号直建/直删", description = "直接创建/删除业务角色账号（商家、活动组长、技工、配送员），用于数据恢复")
@RestController
@RequestMapping("/admin/role-accounts")
@RequiredArgsConstructor
public class AdminRoleAccountController {

    private final AdminRoleAccountService adminRoleAccountService;

    @Operation(summary = "直接创建业务角色账号",
            description = "创建商家/活动组长/技工/配送员账号（含密码）。商家自动建店铺并置为已审核，可直接登录。角色: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<CreateRoleAccountResponse> createAccount(
            @Valid @RequestBody CreateRoleAccountRequest request) {
        return ApiResponse.success(adminRoleAccountService.createAccount(request));
    }

    @Operation(summary = "直接删除业务角色账号",
            description = "硬删除（物理删除）商家/活动组长/技工/配送员账号，关联店铺链一并彻底删除（无孤儿数据），手机号随之释放。账号存在业务数据（订单/积分/提现等）时返回 90120 拒绝。角色: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteRoleAccountResponse> deleteAccount(
            @Parameter(description = "账号ID（residents.id）") @PathVariable String id) {
        return ApiResponse.success(adminRoleAccountService.deleteAccount(id));
    }

    @Operation(summary = "禁用/停用业务角色账号",
            description = "软性停用商家/活动组长/技工/配送员账号（status=disabled），并连带停用其名下仍在营业的店铺（status=stopped）。账号不可登录/接单，历史业务数据完整保留。适用于存在关联业务数据、无法硬删除的账号。角色: property_admin, platform_admin")
    @PostMapping("/{id}/disable")
    public ApiResponse<DisableRoleAccountResponse> disableAccount(
            @Parameter(description = "账号ID（residents.id）") @PathVariable String id,
            @Valid @RequestBody DisableRoleAccountRequest request) {
        return ApiResponse.success(adminRoleAccountService.disableAccount(id, request.getReason()));
    }

    @Operation(summary = "软删除业务角色账号",
            description = "软删除（标记删除）商家/活动组长/技工/配送员账号（status=deleted + deletedAt），并连带停用其名下仍在营业的店铺。历史业务数据完整保留，列表自动隐藏。适用于存在关联业务数据、无法硬删除的账号。角色: property_admin, platform_admin")
    @PostMapping("/{id}/soft-delete")
    public ApiResponse<DeleteRoleAccountResponse> softDeleteAccount(
            @Parameter(description = "账号ID（residents.id）") @PathVariable String id) {
        return ApiResponse.success(adminRoleAccountService.softDeleteAccount(id));
    }
}
