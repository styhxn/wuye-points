package com.wuye.points.controller;

import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ReviewActivityGroupRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.ActivityGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 活动组审核", description = "活动组注册申请审核（物业/平台管理员）")
@RestController
@RequestMapping("/admin/activity-groups")
@RequiredArgsConstructor
public class AdminActivityGroupController {

    private final ActivityGroupService activityGroupService;

    @Operation(summary = "活动组审核列表", description = "查询活动组注册审核申请（可按 reviewStatus 过滤）。角色: property_admin, platform_admin")
    @GetMapping("/reviews")
    public ApiResponse<PageResponse<ActivityGroupListItemResponse>> listReviews(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态：pending/approved/rejected", example = "pending") @RequestParam(required = false) String status) {
        return ApiResponse.success(activityGroupService.listReviewRequests(page, pageSize, status));
    }

    @Operation(summary = "审核活动组注册", description = "通过或驳回活动组注册申请。角色: property_admin, platform_admin")
    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(
            @Parameter(description = "活动组ID(act_/ag_)") @PathVariable String id,
            @Valid @RequestBody ReviewActivityGroupRequest request) {
        activityGroupService.reviewActivityGroup(id, request.getAction(), request.getRemark());
        return ApiResponse.success(null);
    }
}
