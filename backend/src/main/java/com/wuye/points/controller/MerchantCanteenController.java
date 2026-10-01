package com.wuye.points.controller;

import com.wuye.points.dto.canteen.DirectedRechargeRequest;
import com.wuye.points.dto.canteen.DirectedRechargeResponse;
import com.wuye.points.dto.canteen.MerchantQuotaBalanceResponse;
import com.wuye.points.dto.canteen.QuotaPurchaseRequest;
import com.wuye.points.dto.canteen.QuotaPurchaseResponse;
import com.wuye.points.dto.canteen.QuotaQuoteRequest;
import com.wuye.points.dto.canteen.QuotaQuoteResponse;
import com.wuye.points.dto.canteen.QuotaRecordResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.CanteenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 社区食堂-商家端接口（仅主商家可操作）
 */
@Tag(name = "商家端 — 社区食堂", description = "购买充值额度、额度余额/流水、给用户充定向余额")
@RestController
@RequestMapping("/merchant/canteen")
@RequiredArgsConstructor
public class MerchantCanteenController {

    private final CanteenService canteenService;

    @Operation(summary = "购买额度试算", description = "返回手续费与到账额度（内扣口径）。角色: merchant（主商家）")
    @PostMapping("/quota/quote")
    public ApiResponse<QuotaQuoteResponse> quoteQuota(@Valid @RequestBody QuotaQuoteRequest request) {
        return ApiResponse.success(canteenService.quoteQuota(request));
    }

    @Operation(summary = "发起购买额度", description = "扫码微信小程序支付，回调后自动到账。角色: merchant（主商家）")
    @PostMapping("/quota/purchase")
    public ApiResponse<QuotaPurchaseResponse> purchaseQuota(@Valid @RequestBody QuotaPurchaseRequest request) {
        return ApiResponse.success(canteenService.purchaseQuota(request));
    }

    @Operation(summary = "额度余额", description = "角色: merchant（主商家）")
    @GetMapping("/quota/balance")
    public ApiResponse<MerchantQuotaBalanceResponse> getQuotaBalance() {
        return ApiResponse.success(canteenService.getQuotaBalance());
    }

    @Operation(summary = "额度流水（购买+充值扣减）", description = "角色: merchant（主商家）")
    @GetMapping("/quota/records")
    public ApiResponse<PageResponse<QuotaRecordResponse>> listQuotaRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(canteenService.listQuotaRecords(page, pageSize));
    }

    @Operation(summary = "给用户充定向余额", description = "手机号/residentId 二选一定位用户，从自己额度扣减。角色: merchant（主商家）")
    @PostMapping("/recharge")
    public ApiResponse<DirectedRechargeResponse> recharge(@Valid @RequestBody DirectedRechargeRequest request) {
        return ApiResponse.success(canteenService.recharge(request));
    }

    @Operation(summary = "定向充值记录", description = "角色: merchant（主商家）")
    @GetMapping("/recharge-records")
    public ApiResponse<PageResponse<DirectedRechargeResponse>> listRechargeRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(canteenService.listRechargeRecords(page, pageSize));
    }
}
