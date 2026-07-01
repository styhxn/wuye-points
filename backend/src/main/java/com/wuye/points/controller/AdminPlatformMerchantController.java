package com.wuye.points.controller;

import com.wuye.points.dto.admin.platformmerchant.CreatePlatformMerchantRequest;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantDetailResponse;
import com.wuye.points.dto.admin.platformmerchant.PlatformMerchantListItemResponse;
import com.wuye.points.dto.admin.platformmerchant.UpdatePlatformMerchantRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminPlatformMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/platform-merchants")
@RequiredArgsConstructor
public class AdminPlatformMerchantController {

    private final AdminPlatformMerchantService adminPlatformMerchantService;

    @GetMapping
    public ApiResponse<PageResponse<PlatformMerchantListItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String linkedPropertyCompanyId) {
        return ApiResponse.success(adminPlatformMerchantService.list(
                page, pageSize, keyword, category, status, linkedPropertyCompanyId));
    }

    @PostMapping
    public ApiResponse<PlatformMerchantDetailResponse> create(
            @Valid @RequestBody CreatePlatformMerchantRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlatformMerchantDetailResponse> getById(@PathVariable String id) {
        return ApiResponse.success(adminPlatformMerchantService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PlatformMerchantDetailResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdatePlatformMerchantRequest request) {
        return ApiResponse.success(adminPlatformMerchantService.update(id, request));
    }
}
