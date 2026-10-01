package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.priceapproval.AuditPriceApprovalRequest;
import com.wuye.points.dto.priceapproval.CreatePriceApprovalRequest;
import com.wuye.points.dto.priceapproval.PriceApprovalDetailResponse;
import com.wuye.points.dto.priceapproval.PriceApprovalListItemResponse;
import com.wuye.points.service.PriceApprovalService;
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

/**
 * 价格审批模块（管理端）
 */
@Tag(name = "管理端 — 价格审批", description = "价格修改审批的提交、审批、列表查询")
@RestController
@RequestMapping("/admin/price-approvals")
@RequiredArgsConstructor
public class AdminPriceApprovalController {

    private final PriceApprovalService priceApprovalService;

    @Operation(summary = "提交价格修改审批",
            description = "角色: platform_admin 或 物业领导(property_admin + propertySubRole=property_leader)。"
                    + "property_operator 403。platform_admin 提交时 body 必须带 propertyCompanyId。")
    @PostMapping
    public ApiResponse<PriceApprovalDetailResponse> createPriceApproval(
            @Valid @RequestBody CreatePriceApprovalRequest request) {
        return ApiResponse.success(priceApprovalService.createPriceApproval(request));
    }

    @Operation(summary = "审批价格修改",
            description = "角色: 仅物业领导。platform_admin 可看可提但不可批（403）。property_operator 403。"
                    + "approved=true 时：resident_share_rate 落库物业公司分成；product_price 落库商品价格(price/memberPrice/pointPrice)")
    @PutMapping("/{id}")
    public ApiResponse<PriceApprovalDetailResponse> auditPriceApproval(
            @Parameter(description = "审批记录ID") @PathVariable String id,
            @Valid @RequestBody AuditPriceApprovalRequest request) {
        return ApiResponse.success(priceApprovalService.auditPriceApproval(id, request));
    }

    @Operation(summary = "审批列表",
            description = "角色: platform_admin 或 物业领导。property_operator 403。"
                    + "物业领导自动限定本公司；platform_admin 可传 propertyCompanyId 跨物业筛选（不传则全部）")
    @GetMapping
    public ApiResponse<PageResponse<PriceApprovalListItemResponse>> listPriceApprovals(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审批状态: pending/approved/rejected") @RequestParam(required = false) String status,
            @Parameter(description = "物业公司ID（仅 platform_admin 可指定）") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(priceApprovalService.listPriceApprovals(page, pageSize, status, propertyCompanyId));
    }
}
