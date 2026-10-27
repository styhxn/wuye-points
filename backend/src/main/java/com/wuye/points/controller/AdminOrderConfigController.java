package com.wuye.points.controller;

import com.wuye.points.dto.admin.orderconfig.OrderConfigResponse;
import com.wuye.points.dto.admin.orderconfig.UpdateOrderConfigRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.PlatformSettingService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 订单配置（商家核实/冻结期/退款窗口）
 */
@Tag(name = "管理端 - 订单配置", description = "商家核实、冻结期、退款窗口等参数配置")
@RestController
@RequestMapping("/admin/order-config")
@RequiredArgsConstructor
public class AdminOrderConfigController {

    private final PlatformSettingService platformSettingService;
    private final RoleGuard roleGuard;

    @Operation(summary = "获取订单配置", description = "获取商家核实/冻结期/退款窗口等配置参数。角色: platform_admin / property_admin（只读）")
    @GetMapping
    public ApiResponse<OrderConfigResponse> getConfig() {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        return ApiResponse.success(OrderConfigResponse.builder()
                .merchantVerificationEnabled(platformSettingService.isMerchantVerificationEnabled())
                .merchantVerificationHours(platformSettingService.getMerchantVerificationHours())
                .freezeDays(platformSettingService.getFreezeDays())
                .refundWindowDays(platformSettingService.getRefundWindowDays())
                .build());
    }

    @Operation(summary = "更新订单配置", description = "更新商家核实/冻结期/退款窗口等配置参数。角色: platform_admin")
    @PutMapping
    public ApiResponse<OrderConfigResponse> updateConfig(@Valid @RequestBody UpdateOrderConfigRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        platformSettingService.updateOrderConfig(
                request.getMerchantVerificationEnabled(),
                request.getMerchantVerificationHours(),
                request.getFreezeDays(),
                request.getRefundWindowDays());
        return getConfig();
    }
}
