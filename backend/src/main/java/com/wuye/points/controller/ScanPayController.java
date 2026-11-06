package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.scanpay.ScanPayCreateRequest;
import com.wuye.points.dto.scanpay.ScanPayCreateResponse;
import com.wuye.points.dto.scanpay.ScanPayStatusResponse;
import com.wuye.points.service.ScanPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家扫码付款（v8.40）：顾客扫商家小程序码进入收银台后调这两个接口。
 * 角色 resident；创建接口一次返回 payParams，前端原样调 wx.requestPayment。
 */
@Tag(name = "扫码付款", description = "商家专属收款码落地收银台：输金额建单、返回微信支付参数、轮询到账")
@RestController
@RequestMapping("/scan-pay")
@RequiredArgsConstructor
public class ScanPayController {

    private final ScanPayService scanPayService;

    @Operation(summary = "创建扫码付款单并预下单",
            description = "输入商家ID与金额，返回 scanPayId + payParams；前端把 payParams 原样传给 wx.requestPayment")
    @PostMapping("/orders")
    public ApiResponse<ScanPayCreateResponse> create(@Valid @RequestBody ScanPayCreateRequest request) {
        return ApiResponse.success(scanPayService.create(request));
    }

    @Operation(summary = "查询扫码付款状态",
            description = "拉起支付后轮询；仅 status=paid 且 paidAt 有值视为到账，pointEarned 为本次发放积分")
    @GetMapping("/orders/{id}")
    public ApiResponse<ScanPayStatusResponse> status(@PathVariable String id) {
        return ApiResponse.success(scanPayService.getStatus(id));
    }
}
