package com.wuye.points.controller;

import com.wuye.points.dto.activity.ActivityGroupDetailResponse;
import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ActivityGroupMemberListResponse;
import com.wuye.points.dto.activity.ActivityGroupSubscriptionItemResponse;
import com.wuye.points.dto.activity.CreateActivityGroupRequest;
import com.wuye.points.dto.activity.JoinActivityGroupResponse;
import com.wuye.points.dto.activity.LeaveActivityGroupResponse;
import com.wuye.points.dto.activity.RemoveActivityGroupMemberResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupRequest;
import com.wuye.points.dto.activity.SubscribeActivityGroupResponse;
import com.wuye.points.dto.activity.UpdateActivityGroupRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.ActivityGroupService;
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

/**
 * 活动组模块
 */
@RestController
@RequestMapping("/activity-groups")
@RequiredArgsConstructor
public class ActivityGroupController {

    private final ActivityGroupService activityGroupService;

    @GetMapping("/my-subscriptions")
    public ApiResponse<PageResponse<ActivityGroupSubscriptionItemResponse>> listMySubscriptions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(activityGroupService.listMySubscriptions(page, pageSize, status));
    }

    @GetMapping
    public ApiResponse<PageResponse<ActivityGroupListItemResponse>> listActivityGroups(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String communityId,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(activityGroupService.listActivityGroups(
                page, pageSize, communityId, mine, sort));
    }

    @PostMapping
    public ApiResponse<ActivityGroupDetailResponse> createActivityGroup(
            @Valid @RequestBody CreateActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.createActivityGroup(request));
    }

    @GetMapping("/{id}/members")
    public ApiResponse<ActivityGroupMemberListResponse> listMembers(@PathVariable String id) {
        return ApiResponse.success(activityGroupService.listMembers(id));
    }

    @PostMapping("/{id}/subscribe")
    public ApiResponse<SubscribeActivityGroupResponse> subscribe(
            @PathVariable String id,
            @Valid @RequestBody SubscribeActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.subscribe(id, request));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<JoinActivityGroupResponse> joinActivityGroup(@PathVariable String id) {
        return ApiResponse.success(activityGroupService.joinActivityGroup(id));
    }

    @PostMapping("/{id}/leave")
    public ApiResponse<LeaveActivityGroupResponse> leaveActivityGroup(@PathVariable String id) {
        return ApiResponse.success(activityGroupService.leaveActivityGroup(id));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ApiResponse<RemoveActivityGroupMemberResponse> removeMember(
            @PathVariable String id,
            @PathVariable String memberId) {
        return ApiResponse.success(activityGroupService.removeMember(id, memberId));
    }

    @PutMapping("/{id}")
    public ApiResponse<ActivityGroupDetailResponse> updateActivityGroup(
            @PathVariable String id,
            @Valid @RequestBody UpdateActivityGroupRequest request) {
        return ApiResponse.success(activityGroupService.updateActivityGroup(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityGroupDetailResponse> getActivityGroup(@PathVariable String id) {
        return ApiResponse.success(activityGroupService.getActivityGroup(id));
    }
}
