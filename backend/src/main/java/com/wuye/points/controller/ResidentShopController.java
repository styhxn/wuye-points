package com.wuye.points.controller;

import com.wuye.points.dto.admin.residentmerchant.ResidentMerchantSettlementItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.residentmerchant.ApplyResidentMerchantRequest;
import com.wuye.points.dto.residentmerchant.ApplyResidentMerchantResponse;
import com.wuye.points.dto.residentmerchant.CreateResidentMerchantListingRequest;
import com.wuye.points.dto.residentmerchant.MyResidentMerchantResponse;
import com.wuye.points.dto.residentmerchant.PayResidentMerchantDepositRequest;
import com.wuye.points.dto.residentmerchant.PayResidentMerchantDepositResponse;
import com.wuye.points.dto.residentmerchant.PublicResidentMerchantListingFlatItemResponse;
import com.wuye.points.dto.residentmerchant.PublicResidentMerchantShopResponse;
import com.wuye.points.dto.residentmerchant.ResidentMerchantListingItemResponse;
import com.wuye.points.dto.residentmerchant.ShareListingResponse;
import com.wuye.points.dto.residentmerchant.UpdateResidentMerchantListingRequest;
import com.wuye.points.dto.residentmerchant.UpdateResidentMerchantVisibilityRequest;
import com.wuye.points.service.ResidentMerchantSettlementService;
import com.wuye.points.service.ResidentShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "业主商户", description = "业主申请成为二级商户")
@RestController
@RequestMapping("/resident-merchants")
@RequiredArgsConstructor
public class ResidentShopController {

    private final ResidentShopService residentShopService;
    private final ResidentMerchantSettlementService residentMerchantSettlementService;

    @Operation(summary = "申请成为业主商户", description = "需登录，角色：resident（业主）")
    @PostMapping("/apply")
    public ApiResponse<ApplyResidentMerchantResponse> apply(
            @Valid @RequestBody ApplyResidentMerchantRequest request) {
        return ApiResponse.success(residentShopService.apply(request));
    }

    @Operation(summary = "缴纳保证金", description = "模拟微信支付成功，需登录，角色：resident")
    @PostMapping("/deposit/pay")
    public ApiResponse<PayResidentMerchantDepositResponse> payDeposit(
            @Valid @RequestBody PayResidentMerchantDepositRequest request) {
        return ApiResponse.success(residentShopService.payDeposit(request));
    }

    @Operation(summary = "我的业主商户状态", description = "需登录，角色：resident")
    @GetMapping("/my")
    public ApiResponse<MyResidentMerchantResponse> my() {
        return ApiResponse.success(residentShopService.myStatus());
    }

    @Operation(summary = "设置对外/不对外展示", description = "需登录，角色：resident（营业中业主商户）；public=对外(任何人可查看)/private=不对外")
    @PutMapping("/visibility")
    public ApiResponse<MyResidentMerchantResponse> updateVisibility(
            @Valid @RequestBody UpdateResidentMerchantVisibilityRequest request) {
        return ApiResponse.success(residentShopService.updateVisibility(request));
    }

    @Operation(summary = "公开橱窗列表", description = "公开接口（无需登录）：返回对外(public)且营业中(active)的业主商户")
    @GetMapping("/public")
    public ApiResponse<PageResponse<PublicResidentMerchantShopResponse>> listPublicShops(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "店主姓名/手机号关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(
                residentShopService.listPublicShops(keyword, propertyCompanyId, page, pageSize));
    }

    @Operation(summary = "公开橱窗详情", description = "公开接口（无需登录）：返回店铺+启用中商品；未对外展示返回 90111")
    @GetMapping("/public/{residentId}")
    public ApiResponse<PublicResidentMerchantShopResponse> getPublicShop(
            @Parameter(description = "业主商户(住户)ID") @PathVariable String residentId) {
        return ApiResponse.success(residentShopService.getPublicShop(residentId));
    }

    @Operation(summary = "业主商城·扁平公开货源列表", description = "公开接口（无需登录）：单接口按商品分页，返回对外且营业中店铺的启用中上架商品；propertyCompanyId 必填（按物业隔离）；keyword 匹配商品名，category 精确分类目")
    @GetMapping("/public-listings")
    public ApiResponse<PageResponse<PublicResidentMerchantListingFlatItemResponse>> listPublicListings(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "物业公司ID（必填）") @RequestParam String propertyCompanyId,
            @Parameter(description = "商品名关键词（模糊匹配）") @RequestParam(required = false) String keyword,
            @Parameter(description = "货源类目（精确匹配 distributor_products.category）") @RequestParam(required = false) String category) {
        return ApiResponse.success(
                residentShopService.listPublicListings(keyword, propertyCompanyId, category, page, pageSize));
    }

    @Operation(summary = "申请退出", description = "需登录，角色：resident")
    @PostMapping("/quit")
    public ApiResponse<MyResidentMerchantResponse> quit() {
        return ApiResponse.success(residentShopService.quit());
    }

    @Operation(summary = "从一级选品上架", description = "需登录，角色：resident（营业中业主商户）")
    @PostMapping("/listings")
    public ApiResponse<ResidentMerchantListingItemResponse> createListing(
            @Valid @RequestBody CreateResidentMerchantListingRequest request) {
        return ApiResponse.success(residentShopService.createListing(request));
    }

    @Operation(summary = "我的上架商品", description = "需登录，角色：resident")
    @GetMapping("/listings")
    public ApiResponse<Map<String, List<ResidentMerchantListingItemResponse>>> listListings() {
        return ApiResponse.success(Map.of("list", residentShopService.listMyListings()));
    }

    @Operation(summary = "改价/上下架", description = "需登录，角色：resident")
    @PutMapping("/listings/{id}")
    public ApiResponse<ResidentMerchantListingItemResponse> updateListing(
            @Parameter(description = "上架ID") @PathVariable String id,
            @Valid @RequestBody UpdateResidentMerchantListingRequest request) {
        return ApiResponse.success(residentShopService.updateListing(id, request));
    }

    @Operation(summary = "生成分享链接", description = "需登录，角色：resident")
    @PostMapping("/listings/{id}/share")
    public ApiResponse<ShareListingResponse> shareListing(
            @Parameter(description = "上架ID") @PathVariable String id) {
        return ApiResponse.success(residentShopService.shareListing(id));
    }

    @Operation(summary = "取消拿货", description = "删除自己从一级经销商拿货的上架记录。需登录，角色：resident（营业中业主商户）")
    @DeleteMapping("/listings/{id}")
    public ApiResponse<Void> cancelListing(
            @Parameter(description = "上架ID") @PathVariable String id) {
        residentShopService.cancelListing(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "卖货明细", description = "业主商户卖货结算明细（订单完成自动生成，商品自动下架）。需登录，角色：resident")
    @GetMapping("/settlements")
    public ApiResponse<PageResponse<ResidentMerchantSettlementItemResponse>> listMySales(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(residentMerchantSettlementService.listMySales(page, pageSize));
    }
}
