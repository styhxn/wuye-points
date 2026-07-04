package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.specialoffer.CreateSpecialOfferRequest;
import com.wuye.points.dto.specialoffer.SpecialOfferDetailResponse;
import com.wuye.points.dto.specialoffer.SpecialOfferListItemResponse;
import com.wuye.points.service.SpecialOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "特惠推送模块", description = "特惠推送创建、列表、详情、更新、删除")
@RestController
@RequestMapping("/special-offers")
@RequiredArgsConstructor
public class SpecialOfferController {

    private final SpecialOfferService specialOfferService;

    @Operation(summary = "创建特惠推送", description = "创建一条特惠推送")
    @PostMapping
    public ApiResponse<SpecialOfferDetailResponse> create(
            @Valid @RequestBody CreateSpecialOfferRequest request) {
        return ApiResponse.success(specialOfferService.create(request));
    }

    @Operation(summary = "特惠推送列表", description = "获取特惠推送列表（分页/筛选）")
    @GetMapping
    public ApiResponse<PageResponse<SpecialOfferListItemResponse>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "目标类型") @RequestParam(required = false) String targetType,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(specialOfferService.list(page, pageSize, targetType, status));
    }

    @Operation(summary = "特惠推送详情", description = "获取特惠推送详细信息")
    @GetMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> getById(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.getById(id));
    }

    @Operation(summary = "更新特惠推送", description = "更新指定特惠推送的信息")
    @PutMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> update(
            @Parameter(description = "特惠推送ID") @PathVariable String id,
            @Valid @RequestBody CreateSpecialOfferRequest request) {
        return ApiResponse.success(specialOfferService.update(id, request));
    }

    @Operation(summary = "删除特惠推送", description = "删除指定特惠推送")
    @DeleteMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> delete(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.delete(id));
    }
}
