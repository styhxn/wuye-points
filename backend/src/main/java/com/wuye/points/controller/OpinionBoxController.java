package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.opinion.CreateOpinionBoxRequest;
import com.wuye.points.dto.opinion.OpinionBoxDetailResponse;
import com.wuye.points.dto.opinion.OpinionBoxListItemResponse;
import com.wuye.points.dto.opinion.OpinionBoxResultResponse;
import com.wuye.points.dto.opinion.VoteOpinionBoxRequest;
import com.wuye.points.dto.opinion.VoteOpinionBoxResponse;
import com.wuye.points.service.OpinionBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意见箱模块
 */
@RestController
@RequestMapping("/opinion-boxes")
@RequiredArgsConstructor
public class OpinionBoxController {

    private final OpinionBoxService opinionBoxService;

    /**
     * 意见箱列表
     * Auth: Required | Roles: resident
     */
    @GetMapping
    public ApiResponse<PageResponse<OpinionBoxListItemResponse>> listOpinionBoxes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean mine,
            @RequestParam(required = false) Boolean voted,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(opinionBoxService.listOpinionBoxes(
                page, pageSize, status, mine, voted, sort));
    }

    /**
     * 创建意见箱
     * Auth: Required | Roles: resident
     */
    @PostMapping
    public ApiResponse<OpinionBoxDetailResponse> createOpinionBox(
            @Valid @RequestBody CreateOpinionBoxRequest request) {
        return ApiResponse.success(opinionBoxService.createOpinionBox(request));
    }

    /**
     * 投票结果
     * Auth: Required | Roles: resident
     */
    @GetMapping("/{id}/result")
    public ApiResponse<OpinionBoxResultResponse> getOpinionBoxResult(@PathVariable String id) {
        return ApiResponse.success(opinionBoxService.getOpinionBoxResult(id));
    }

    /**
     * 投票
     * Auth: Required | Roles: resident
     */
    @PostMapping("/{id}/vote")
    public ApiResponse<VoteOpinionBoxResponse> vote(
            @PathVariable String id,
            @Valid @RequestBody VoteOpinionBoxRequest request) {
        return ApiResponse.success(opinionBoxService.vote(id, request));
    }

    /**
     * 意见箱详情
     * Auth: Required | Roles: resident
     */
    @GetMapping("/{id}")
    public ApiResponse<OpinionBoxDetailResponse> getOpinionBox(@PathVariable String id) {
        return ApiResponse.success(opinionBoxService.getOpinionBox(id));
    }
}
