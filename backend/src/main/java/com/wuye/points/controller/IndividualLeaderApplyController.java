package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.individualleader.ApplyIndividualLeaderRequest;
import com.wuye.points.dto.individualleader.ApplyIndividualLeaderResponse;
import com.wuye.points.service.IndividualLeaderApplyService;
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
 * 住户端 — 一级代理申请
 * 住户自主申请成为个体负责人，由管理端审核
 */
@Tag(name = "一级代理申请（住户端）", description = "住户自主申请成为个体负责人（一级代理），管理端审核")
@RestController
@RequestMapping("/individual-leaders")
@RequiredArgsConstructor
public class IndividualLeaderApplyController {

    private final IndividualLeaderApplyService individualLeaderApplyService;

    @Operation(summary = "申请成为个体负责人",
            description = "住户自主申请成为个体负责人（一级代理），需登录，角色：resident。"
                    + "每位住户在同一物业公司下仅可有一个待审核申请。")
    @PostMapping("/apply")
    public ApiResponse<ApplyIndividualLeaderResponse> apply(
            @Valid @RequestBody ApplyIndividualLeaderRequest request) {
        return ApiResponse.success(individualLeaderApplyService.apply(request));
    }

    @Operation(summary = "查询我的最新申请状态",
            description = "住户查询自己最新的个体负责人申请状态。需登录，角色：resident。")
    @GetMapping("/my-application")
    public ApiResponse<ApplyIndividualLeaderResponse> getMyApplication() {
        return ApiResponse.success(individualLeaderApplyService.getMyLatestApplication());
    }
}
