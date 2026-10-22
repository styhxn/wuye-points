package com.wuye.points.controller;

import com.wuye.points.dto.activityleader.ApplyActivityLeaderRequest;
import com.wuye.points.dto.activityleader.ApplyActivityLeaderResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.ActivityLeaderApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 住户端 — 活动组组长申请。
 * 住户自主申请成为活动组组长，由管理端审核；通过后身份以标签形式叠加在住户账号上
 * （role 仍为 resident），并自动开通组长小店。
 */
@Tag(name = "活动组组长申请（住户端）", description = "住户自主申请成为活动组组长，管理端审核")
@RestController
@RequestMapping("/activity-leader-applications")
@RequiredArgsConstructor
public class ActivityLeaderApplyController {

    private final ActivityLeaderApplyService activityLeaderApplyService;

    @Operation(summary = "申请成为活动组组长",
            description = "住户自主申请成为活动组组长，需登录，角色：resident。"
                    + "每位住户在同一物业公司下仅可有一个待审核申请；已是组长则拒绝。")
    @PostMapping("/apply")
    public ApiResponse<ApplyActivityLeaderResponse> apply(
            @Valid @RequestBody ApplyActivityLeaderRequest request) {
        return ApiResponse.success(activityLeaderApplyService.apply(request));
    }

    @Operation(summary = "查询我的最新申请状态",
            description = "住户查询自己最新的组长申请状态（none/pending/approved/rejected）。需登录，角色：resident")
    @GetMapping("/my-application")
    public ApiResponse<ApplyActivityLeaderResponse> getMyApplication() {
        return ApiResponse.success(activityLeaderApplyService.getMyLatestApplication());
    }
}
