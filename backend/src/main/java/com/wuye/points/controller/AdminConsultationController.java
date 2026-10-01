package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.consultation.AuditConsultantRequest;
import com.wuye.points.dto.consultation.ConsultantDetailResponse;
import com.wuye.points.dto.consultation.ConsultantListItemResponse;
import com.wuye.points.dto.consultation.ConsultationSettingsResponse;
import com.wuye.points.dto.consultation.CreateConsultantRequest;
import com.wuye.points.dto.consultation.UpdateConsultantRequest;
import com.wuye.points.dto.consultation.UpdateConsultationSettingsRequest;
import com.wuye.points.service.AdminConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "管理端 — 咨询师", description = "咨询师创建、更新、审核")
@RestController
@RequiredArgsConstructor
public class AdminConsultationController {

    private final AdminConsultationService adminConsultationService;

    @Operation(summary = "咨询师列表", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/consultants")
    public ApiResponse<PageResponse<ConsultantListItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminConsultationService.listConsultants(
                page, pageSize, category, keyword, status, propertyCompanyId));
    }

    @Operation(summary = "创建咨询师", description = "角色: property_admin, platform_admin")
    @PostMapping("/admin/consultants")
    public ApiResponse<ConsultantDetailResponse> create(
            @Valid @RequestBody CreateConsultantRequest request,
            @Parameter(description = "物业公司ID（平台超管必填，PC 端经 Query 自动附加）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminConsultationService.createConsultant(request, propertyCompanyId));
    }

    @Operation(summary = "更新咨询师", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/consultants/{id}")
    public ApiResponse<ConsultantDetailResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateConsultantRequest request) {
        return ApiResponse.success(adminConsultationService.updateConsultant(id, request));
    }

    @Operation(summary = "审核咨询师", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/consultants/{id}/audit")
    public ApiResponse<ConsultantDetailResponse> audit(
            @PathVariable String id,
            @Valid @RequestBody AuditConsultantRequest request) {
        return ApiResponse.success(adminConsultationService.auditConsultant(id, request));
    }

    @Operation(summary = "咨询平台配置", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/consultation-settings")
    public ApiResponse<ConsultationSettingsResponse> getSettings() {
        return ApiResponse.success(adminConsultationService.getSettings());
    }

    @Operation(summary = "更新咨询平台配置", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/consultation-settings")
    public ApiResponse<ConsultationSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateConsultationSettingsRequest request) {
        return ApiResponse.success(adminConsultationService.updateSettings(request));
    }
}
