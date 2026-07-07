package com.wuye.points.controller;

import com.wuye.points.dto.activity.ActivityGroupDetailResponse;
import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ActivityGroupMemberListResponse;
import com.wuye.points.dto.activity.ActivityGroupSubscriptionItemResponse;
import com.wuye.points.dto.activity.CreateActivityGroupRequest;
import com.wuye.points.dto.activity.JoinActivityGroupResponse;
import com.wuye.points.dto.activity.LeaveActivityGroupResponse;
import com.wuye.points.dto.activity.MyJoinedGroupItemResponse;
import com.wuye.points.dto.activity.RemoveActivityGroupMemberResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupRequest;
import com.wuye.points.dto.activity.SubscribeActivityGroupResponse;
import com.wuye.points.dto.activity.SubscriptionStatusResponse;
import com.wuye.points.dto.activity.UpdateActivityGroupRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.ActivityGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 活动组模块
 */
@Tag(name = "活动组模块", description = "活动组列表、创建、订阅、加入、退出、成员管理")
@RestController
@RequestMapping("/activity-groups")
@RequiredArgsConstructor
public class ActivityGroupController {

    private final ActivityGroupService activityGroupService;

    @Operation(summary = "我订阅的活动组", description = "获取当前用户订阅的活动组列表")
    @GetMapping("/my-subscriptions")
    public ApiResponse<PageResponse<ActivityGroupSubscriptionItemResponse>> listMySubscriptions(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "订阅状态") @RequestParam(required = false) String status) {
        return ApiResponse.success(activityGroupService.listMySubscriptions(page, pageSize, status));
    }

    @Operation(summary = "我加入的活动组", description = "获取当前用户加入的活动组列表（作为成员的组）")
    @GetMapping("/my-joined")
    public ApiResponse<PageResponse<MyJoinedGroupItemResponse>> listMyJoinedGroups(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(activityGroupService.listMyJoinedGroups(page, pageSize));
    }

    @Operation(summary = "活动组列表", description = "获取活动组列表（分页/筛选）")
    @GetMapping
    public ApiResponse<PageResponse<ActivityGroupListItemResponse>> listActivityGroups(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "社区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "是否只看自己创建的") @RequestParam(required = false) Boolean mine,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(activityGroupService.listActivityGroups(
                page, pageSize, communityId, mine, sort));
    }

    @Operation(summary = "创建活动组", description = "创建活动组")
    @PostMapping
    public ApiResponse<ActivityGroupDetailResponse> createActivityGroup(
            @Valid @RequestBody CreateActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.createActivityGroup(request));
    }

    @Operation(summary = "活动组成员列表", description = "获取活动组的成员列表")
    @GetMapping("/{id}/members")
    public ApiResponse<ActivityGroupMemberListResponse> listMembers(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupService.listMembers(id));
    }

    @Operation(summary = "订阅活动组", description = "订阅指定活动组，接收活动更新通知")
    @PostMapping("/{id}/subscribe")
    public ApiResponse<SubscribeActivityGroupResponse> subscribe(
            @Parameter(description = "活动组ID") @PathVariable String id,
            @Valid @RequestBody SubscribeActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.subscribe(id, request));
    }

    @Operation(summary = "加入活动组", description = "加入指定活动组，成为活动组成员")
    @PostMapping("/{id}/join")
    public ApiResponse<JoinActivityGroupResponse> joinActivityGroup(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupService.joinActivityGroup(id));
    }

    @Operation(summary = "退出活动组", description = "退出指定活动组")
    @PostMapping("/{id}/leave")
    public ApiResponse<LeaveActivityGroupResponse> leaveActivityGroup(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupService.leaveActivityGroup(id));
    }

    @Operation(summary = "移除活动组成员", description = "移除指定活动组中的成员")
    @DeleteMapping("/{id}/members/{memberId}")
    public ApiResponse<RemoveActivityGroupMemberResponse> removeMember(
            @Parameter(description = "活动组ID") @PathVariable String id,
            @Parameter(description = "成员ID") @PathVariable String memberId) {
        return ApiResponse.success(activityGroupService.removeMember(id, memberId));
    }

    @Operation(summary = "更新活动组", description = "更新活动组信息")
    @PutMapping("/{id}")
    public ApiResponse<ActivityGroupDetailResponse> updateActivityGroup(
            @Parameter(description = "活动组ID") @PathVariable String id,
            @Valid @RequestBody UpdateActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.updateActivityGroup(id, request));
    }

    @Operation(summary = "活动组详情", description = "获取活动组详细信息")
    @GetMapping("/{id}")
    public ApiResponse<ActivityGroupDetailResponse> getActivityGroup(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupService.getActivityGroup(id));
    }

    @Operation(summary = "订阅状态", description = "获取当前用户对该活动组的订阅状态")
    @GetMapping("/{id}/subscription-status")
    public ApiResponse<SubscriptionStatusResponse> getSubscriptionStatus(
            @Parameter(description = "活动组ID") @PathVariable String id) {
        return ApiResponse.success(activityGroupService.getSubscriptionStatus(id));
    }
}
