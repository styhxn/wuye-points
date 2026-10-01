package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.technician.TechnicianMyResponse;
import com.wuye.points.dto.technician.TechnicianTaskItemResponse;
import com.wuye.points.dto.technician.UpdateTechnicianTaskStatusRequest;
import com.wuye.points.payment.WalletCardsDTO;
import com.wuye.points.service.TechnicianWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "技工工作台", description = "技工个人信息与工单（服务订单）")
@RestController
@RequestMapping("/technicians")
@RequiredArgsConstructor
public class TechnicianWorkbenchController {

    private final TechnicianWorkbenchService technicianWorkbenchService;

    @Operation(summary = "当前技工信息", description = "角色: technician")
    @GetMapping("/my")
    public ApiResponse<TechnicianMyResponse> getMyInfo() {
        return ApiResponse.success(technicianWorkbenchService.getMyInfo());
    }

    @Operation(summary = "技工钱包两张卡", description = "角色: technician；返回钱包余额 + 微信 分账冻结")
    @GetMapping("/my/wallet/cards")
    public ApiResponse<WalletCardsDTO> getWalletCards() {
        return ApiResponse.success(technicianWorkbenchService.getWalletCards());
    }

    @Operation(summary = "我的工单列表", description = "角色: technician；status 为前端工单态 pending/assigned/in_progress/completed/cancelled")
    @GetMapping("/my/tasks")
    public ApiResponse<PageResponse<TechnicianTaskItemResponse>> listMyTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "工单状态") @RequestParam(required = false) String status,
            @Parameter(description = "排序") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(technicianWorkbenchService.listMyTasks(page, pageSize, status, sort));
    }

    @Operation(summary = "更新工单状态", description = "角色: technician；接单传 assigned，开始传 in_progress，完成传 completed")
    @PatchMapping("/my/tasks/{id}/status")
    public ApiResponse<TechnicianTaskItemResponse> updateTaskStatus(
            @Parameter(description = "工单ID（so_）") @PathVariable String id,
            @Valid @RequestBody UpdateTechnicianTaskStatusRequest request) {
        return ApiResponse.success(technicianWorkbenchService.updateTaskStatus(id, request));
    }
}
