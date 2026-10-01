package com.wuye.points.controller;

import com.wuye.points.dto.cart.CartDeliveryFeeRequest;
import com.wuye.points.dto.cart.CartDeliveryFeeResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车模块
 */
@Tag(name = "购物车模块", description = "购物车运费预估")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final OrderService orderService;

    /**
     * 购物车运费预估
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "购物车运费预估",
            description = "根据购物车商品列表预估整单运费，全单仅计算一次（不会因商品份数重复累加）。"
                    + "需登录，角色：resident。前端购物车应调用本接口获取运费，禁止按商品行累加。")
    @PostMapping("/delivery-fee")
    public ApiResponse<CartDeliveryFeeResponse> previewDeliveryFee(
            @Valid @RequestBody CartDeliveryFeeRequest request) {
        return ApiResponse.success(orderService.previewCartDeliveryFee(request));
    }
}
