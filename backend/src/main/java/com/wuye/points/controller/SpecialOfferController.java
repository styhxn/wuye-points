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

@Tag(name = "特惠推送模块", description = "特惠推送创建、列表、详情、更新、发布/下架、删除（住户端按可见规则读取）")
@RestController
@RequestMapping("/special-offers")
@RequiredArgsConstructor
public class SpecialOfferController {

    private final SpecialOfferService specialOfferService;

    @Operation(summary = "创建特惠推送",
            description = "创建一条特惠推送。角色: property_admin, platform_admin, sector_leader, coordinator（需 promotion.push）。"
                    + "板块负责人按角色放行（无需 promotion.push），数据范围收敛到本人物业。"
                    + "status 默认 draft（仅管理端可见）；要住户端可见需传 published 或调用发布接口")
    @PostMapping
    public ApiResponse<SpecialOfferDetailResponse> create(
            @Valid @RequestBody CreateSpecialOfferRequest request) {
        return ApiResponse.success(specialOfferService.create(request));
    }

    @Operation(summary = "特惠推送列表",
            description = "特惠推送列表（分页/筛选）。管理端角色: property_admin, platform_admin, sector_leader, coordinator；"
                    + "住户 resident 走 §25.6 C 端可见规则（本物业 + published + 时间窗内 + 目标人群匹配）")
    @GetMapping
    public ApiResponse<PageResponse<SpecialOfferListItemResponse>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "目标类型：all/building/role/tag（管理端有效，住户端忽略）") @RequestParam(required = false) String targetType,
            @Parameter(description = "状态：draft/published/ended/archived（管理端有效，住户端忽略）") @RequestParam(required = false) String status) {
        return ApiResponse.success(specialOfferService.list(page, pageSize, targetType, status));
    }

    @Operation(summary = "我的特惠（住户端）",
            description = "住户端专属入口，等价于住户 Token 调用 GET /special-offers，按 §25.6 可见规则过滤。角色: resident")
    @GetMapping("/my")
    public ApiResponse<PageResponse<SpecialOfferListItemResponse>> listMy(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(specialOfferService.listForResident(page, pageSize));
    }

    @Operation(summary = "特惠推送详情", description = "获取特惠推送详细信息；住户仅可查看本物业已发布且在时间窗内的记录")
    @GetMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> getById(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.getById(id));
    }

    @Operation(summary = "更新特惠推送",
            description = "更新指定特惠推送的信息。角色: property_admin, platform_admin, sector_leader, coordinator（需 promotion.push）")
    @PutMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> update(
            @Parameter(description = "特惠推送ID") @PathVariable String id,
            @Valid @RequestBody CreateSpecialOfferRequest request) {
        return ApiResponse.success(specialOfferService.update(id, request));
    }

    @Operation(summary = "发布特惠推送",
            description = "将草稿/已结束的特惠置为 published（住户端立即可见）。角色: property_admin, platform_admin, sector_leader, coordinator")
    @PostMapping("/{id}/publish")
    public ApiResponse<SpecialOfferDetailResponse> publish(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.publish(id));
    }

    @Operation(summary = "下架特惠推送",
            description = "将特惠置为 draft（住户端不再可见，管理端仍可编辑）。角色: property_admin, platform_admin, sector_leader, coordinator")
    @PostMapping("/{id}/unpublish")
    public ApiResponse<SpecialOfferDetailResponse> unpublish(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.unpublish(id));
    }

    @Operation(summary = "删除特惠推送",
            description = "删除指定特惠推送（置为 archived）。角色: property_admin, platform_admin, sector_leader, coordinator")
    @DeleteMapping("/{id}")
    public ApiResponse<SpecialOfferDetailResponse> delete(
            @Parameter(description = "特惠推送ID") @PathVariable String id) {
        return ApiResponse.success(specialOfferService.delete(id));
    }
}
