package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertycompany.CommunityListResponse;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyRequest;
import com.wuye.points.dto.propertycompany.CreatePropertyCompanyResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyCurrentResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyDetailResponse;
import com.wuye.points.dto.propertycompany.PropertyCompanyListItemResponse;
import com.wuye.points.dto.propertycompany.UpdatePropertyCompanyRequest;
import com.wuye.points.service.PropertyCompanyService;
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

/**
 * 物业公司模块
 */
@RestController
@RequestMapping("/property-companies")
@RequiredArgsConstructor
public class PropertyCompanyController {

    private final PropertyCompanyService propertyCompanyService;

    @GetMapping("/current")
    public ApiResponse<PropertyCompanyCurrentResponse> getCurrentConfig() {
        return ApiResponse.success(propertyCompanyService.getCurrentConfig());
    }

    @GetMapping
    public ApiResponse<PageResponse<PropertyCompanyListItemResponse>> listPropertyCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(propertyCompanyService.listPropertyCompanies(
                page, pageSize, keyword, status, sort));
    }

    @PostMapping
    public ApiResponse<CreatePropertyCompanyResponse> createPropertyCompany(
            @Valid @RequestBody CreatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.createPropertyCompany(request));
    }

    @GetMapping("/{id}/communities")
    public ApiResponse<CommunityListResponse> listCommunities(@PathVariable String id) {
        return ApiResponse.success(propertyCompanyService.listCommunities(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> getPropertyCompany(@PathVariable String id) {
        return ApiResponse.success(propertyCompanyService.getPropertyCompany(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PropertyCompanyDetailResponse> updatePropertyCompany(
            @PathVariable String id,
            @Valid @RequestBody UpdatePropertyCompanyRequest request) {
        return ApiResponse.success(propertyCompanyService.updatePropertyCompany(id, request));
    }
}
