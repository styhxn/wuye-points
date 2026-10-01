package com.wuye.points.controller;

import com.wuye.points.dto.admin.residentmerchant.AuditResidentMerchantApplicationRequest;
import com.wuye.points.dto.admin.residentmerchant.ConfirmResidentMerchantDepositRequest;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductRequest;
import com.wuye.points.dto.admin.residentmerchant.CreateDistributorProductResponse;
import com.wuye.points.dto.admin.residentmerchant.DeductResidentMerchantDepositRequest;
import com.wuye.points.dto.admin.residentmerchant.DistributorProductListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantApplicationListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantDepositListItemResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettingsResponse;
import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettlementItemResponse;
import com.wuye.points.dto.admin.residentmerchant.UpdateBillingCycleRequest;
import com.wuye.points.dto.admin.residentmerchant.UpdateBillingCycleResponse;
import com.wuye.points.dto.admin.residentmerchant.UpdateDistributorProductRequest;
import com.wuye.points.dto.admin.residentmerchant.UpdateDistributorProductResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.wuye.points.dto.admin.residentmerchant.UpdateResidentMerchantSettingsRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.AdminResidentMerchantService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 业主商户", description = "申请审核、保证金、结算")
@RestController
@RequiredArgsConstructor
public class AdminResidentMerchantController {

    private final AdminResidentMerchantService adminResidentMerchantService;

    @Operation(summary = "申请列表", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/resident-merchant-applications")
    public ApiResponse<PageResponse<ResidentMerchantApplicationListItemResponse>> listApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminResidentMerchantService.listApplications(
                page, pageSize, status, keyword, propertyCompanyId));
    }

    @Operation(summary = "审核申请", description = "角色: property_admin, platform_admin")
    @PostMapping("/admin/resident-merchant-applications/{id}/audit")
    public ApiResponse<ResidentMerchantApplicationListItemResponse> auditApplication(
            @PathVariable String id,
            @Valid @RequestBody AuditResidentMerchantApplicationRequest request) {
        return ApiResponse.success(adminResidentMerchantService.auditApplication(id, request));
    }

    @Operation(summary = "确认保证金已缴纳（线下收款）",
            description = "角色: property_admin, platform_admin。"
                    + "线上微信支付失败/不可用导致申请长期停在 pending_deposit 时，物业线下收款后在此确认："
                    + "保证金置 frozen，申请推进到 pending_audit；directActivate=true 时直接开通(active)。")
    @PostMapping("/admin/resident-merchant-applications/{id}/confirm-deposit")
    public ApiResponse<ResidentMerchantApplicationListItemResponse> confirmDeposit(
            @Parameter(description = "申请ID") @PathVariable String id,
            @Valid @RequestBody(required = false) ConfirmResidentMerchantDepositRequest request) {
        ConfirmResidentMerchantDepositRequest body =
                request != null ? request : new ConfirmResidentMerchantDepositRequest();
        return ApiResponse.success(adminResidentMerchantService.confirmDeposit(id, body));
    }

    @Operation(summary = "保证金列表", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/resident-merchant-deposits")
    public ApiResponse<PageResponse<ResidentMerchantDepositListItemResponse>> listDeposits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminResidentMerchantService.listDeposits(page, pageSize, status, propertyCompanyId));
    }

    @Operation(summary = "违规扣除保证金", description = "角色: property_admin, platform_admin")
    @PostMapping("/admin/resident-merchant-deposits/{id}/deduct")
    public ApiResponse<ResidentMerchantDepositListItemResponse> deductDeposit(
            @PathVariable String id,
            @Valid @RequestBody DeductResidentMerchantDepositRequest request) {
        return ApiResponse.success(adminResidentMerchantService.deductDeposit(id, request));
    }

    @Operation(summary = "结算明细", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/resident-merchant-settlements")
    public ApiResponse<PageResponse<ResidentMerchantSettlementItemResponse>> listSettlements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminResidentMerchantService.listSettlements(
                page, pageSize, startDate, endDate));
    }

    @Operation(summary = "业主商户配置", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/resident-merchant-settings")
    public ApiResponse<ResidentMerchantSettingsResponse> getSettings() {
        return ApiResponse.success(adminResidentMerchantService.getSettings());
    }

    @Operation(summary = "更新业主商户配置", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/resident-merchant-settings")
    public ApiResponse<ResidentMerchantSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateResidentMerchantSettingsRequest request) {
        return ApiResponse.success(adminResidentMerchantService.updateSettings(request));
    }

    @Operation(summary = "分销商品列表", description = "角色: property_admin, platform_admin。platform_admin 需传 propertyCompanyId")
    @GetMapping("/admin/distributor-products")
    public ApiResponse<PageResponse<DistributorProductListItemResponse>> listDistributorProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(adminResidentMerchantService.listDistributorProducts(
                page, pageSize, keyword, propertyCompanyId));
    }

    @Operation(summary = "一级经销商上架商品", description = "角色: property_admin, platform_admin")
    @PostMapping("/admin/distributor-products")
    public ApiResponse<CreateDistributorProductResponse> createDistributorProduct(
            @Valid @RequestBody CreateDistributorProductRequest request) {
        return ApiResponse.success(adminResidentMerchantService.createDistributorProduct(request));
    }

    @Operation(summary = "设置分销商品收费周期", description = "角色: property_admin, platform_admin, merchant")
    @PutMapping("/admin/distributor-products/{id}/billing-cycle")
    public ApiResponse<UpdateBillingCycleResponse> updateBillingCycle(
            @Parameter(description = "分销商品ID") @PathVariable String id,
            @Valid @RequestBody UpdateBillingCycleRequest request) {
        return ApiResponse.success(adminResidentMerchantService.updateBillingCycle(id, request));
    }

    @Operation(summary = "更新分销商品", description = "改名称/封面/价格/库存/描述/服务区间/上下架；全部字段可选，仅更新传入字段。"
            + "角色: property_admin, platform_admin（2026-09-07 新增）")
    @PutMapping("/admin/distributor-products/{id}")
    public ApiResponse<UpdateDistributorProductResponse> updateDistributorProduct(
            @Parameter(description = "分销商品ID(dpr_)") @PathVariable String id,
            @Valid @RequestBody UpdateDistributorProductRequest request) {
        return ApiResponse.success(adminResidentMerchantService.updateDistributorProduct(id, request));
    }

    @Operation(summary = "删除分销商品", description = "软删除（status=inactive），保留历史拿货记录。"
            + "角色: property_admin, platform_admin（2026-09-07 新增）")
    @DeleteMapping("/admin/distributor-products/{id}")
    public ApiResponse<UpdateDistributorProductResponse> deleteDistributorProduct(
            @Parameter(description = "分销商品ID(dpr_)") @PathVariable String id) {
        return ApiResponse.success(adminResidentMerchantService.deleteDistributorProduct(id));
    }
}
