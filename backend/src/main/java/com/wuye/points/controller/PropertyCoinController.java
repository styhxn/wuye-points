package com.wuye.points.controller;

import com.wuye.points.dto.coin.CoinBalanceResponse;
import com.wuye.points.dto.coin.CoinRecordItemResponse;
import com.wuye.points.dto.coin.EarnCoinRequest;
import com.wuye.points.dto.coin.EarnCoinResponse;
import com.wuye.points.dto.coin.ExpiringCoinResponse;
import com.wuye.points.dto.coin.FamilyCoinBalanceResponse;
import com.wuye.points.dto.coin.FreezeCoinRequest;
import com.wuye.points.dto.coin.FreezeCoinResponse;
import com.wuye.points.dto.coin.HideCoinRequest;
import com.wuye.points.dto.coin.HideCoinResponse;
import com.wuye.points.dto.coin.RechargeCoinRequest;
import com.wuye.points.dto.coin.RechargeCoinResponse;
import com.wuye.points.dto.coin.SpendCoinRequest;
import com.wuye.points.dto.coin.SpendCoinResponse;
import com.wuye.points.dto.coin.UnfreezeCoinRequest;
import com.wuye.points.dto.coin.UnfreezeCoinResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.PropertyCoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物业币模块
 */
@RestController
@RequestMapping("/property-coins")
@RequiredArgsConstructor
public class PropertyCoinController {

    private final PropertyCoinService propertyCoinService;

    /**
     * 获取物业币余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/balance")
    public ApiResponse<CoinBalanceResponse> getBalance() {
        return ApiResponse.success(propertyCoinService.getBalance());
    }

    /**
     * 获取家庭物业币余额
     * Auth: Required | Roles: resident
     */
    @GetMapping("/family-balance")
    public ApiResponse<FamilyCoinBalanceResponse> getFamilyBalance() {
        return ApiResponse.success(propertyCoinService.getFamilyBalance());
    }

    /**
     * 物业币记录列表
     * Auth: Required | Roles: resident
     */
    @GetMapping("/records")
    public ApiResponse<PageResponse<CoinRecordItemResponse>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String coinType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String familyId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyCoinService.listRecords(
                page, pageSize, coinType, source, startDate, endDate, familyId, sort));
    }

    /**
     * 即将过期物业币
     * Auth: Required | Roles: resident
     */
    @GetMapping("/expiring")
    public ApiResponse<ExpiringCoinResponse> getExpiringCoins() {
        return ApiResponse.success(propertyCoinService.getExpiringCoins());
    }

    /**
     * 充值物业币
     * Auth: Required | Roles: resident
     */
    @PostMapping("/recharge")
    public ApiResponse<RechargeCoinResponse> recharge(@Valid @RequestBody RechargeCoinRequest request) {
        return ApiResponse.success(propertyCoinService.recharge(request));
    }

    /**
     * 隐藏/显示物业币卡片
     * Auth: Required | Roles: resident
     */
    @PostMapping("/hide")
    public ApiResponse<HideCoinResponse> hide(@Valid @RequestBody HideCoinRequest request) {
        return ApiResponse.success(propertyCoinService.hide(request));
    }

    /**
     * 获取物业币（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/earn")
    public ApiResponse<EarnCoinResponse> earn(@Valid @RequestBody EarnCoinRequest request) {
        return ApiResponse.success(propertyCoinService.earn(request));
    }

    /**
     * 消费物业币（管理员/内部）
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/spend")
    public ApiResponse<SpendCoinResponse> spend(@Valid @RequestBody SpendCoinRequest request) {
        return ApiResponse.success(propertyCoinService.spend(request));
    }

    /**
     * 冻结物业币
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/freeze")
    public ApiResponse<FreezeCoinResponse> freeze(@Valid @RequestBody FreezeCoinRequest request) {
        return ApiResponse.success(propertyCoinService.freeze(request));
    }

    /**
     * 解冻物业币
     * Auth: Required | Roles: property_admin, platform_admin
     */
    @PostMapping("/unfreeze")
    public ApiResponse<UnfreezeCoinResponse> unfreeze(@Valid @RequestBody UnfreezeCoinRequest request) {
        return ApiResponse.success(propertyCoinService.unfreeze(request));
    }
}
