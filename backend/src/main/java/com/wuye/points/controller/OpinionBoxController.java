package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.opinion.CreateOpinionBoxRequest;
import com.wuye.points.dto.opinion.OpinionBoxDetailResponse;
import com.wuye.points.dto.opinion.OpinionBoxListItemResponse;
import com.wuye.points.dto.opinion.MyVoteListItemResponse;
import com.wuye.points.dto.opinion.OpinionBoxResultResponse;
import com.wuye.points.dto.opinion.VoteOpinionBoxRequest;
import com.wuye.points.dto.opinion.VoteOpinionBoxResponse;
import com.wuye.points.service.OpinionBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意见箱模块
 */
@Tag(name = "意见箱模块", description = "意见箱列表、创建、投票、结果查看")
@RestController
@RequestMapping("/opinion-boxes")
@RequiredArgsConstructor
public class OpinionBoxController {

    private final OpinionBoxService opinionBoxService;

    @Operation(summary = "意见箱列表", description = "意见箱列表。Auth: Required | Roles: resident")
    @GetMapping
    public ApiResponse<PageResponse<OpinionBoxListItemResponse>> listOpinionBoxes(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "意见箱状态") @RequestParam(required = false) String status,
            @Parameter(description = "是否只看自己的") @RequestParam(required = false) Boolean mine,
            @Parameter(description = "是否已投票") @RequestParam(required = false) Boolean voted,
            @Parameter(description = "排序字段（-降序，+升序）", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(opinionBoxService.listOpinionBoxes(
                page, pageSize, status, mine, voted, sort));
    }

    @Operation(summary = "创建意见箱", description = "创建意见箱。Auth: Required | Roles: resident")
    @PostMapping
    public ApiResponse<OpinionBoxDetailResponse> createOpinionBox(
            @Valid @RequestBody CreateOpinionBoxRequest request) {
        return ApiResponse.success(opinionBoxService.createOpinionBox(request));
    }

    @Operation(summary = "投票结果", description = "投票结果。Auth: Required | Roles: resident")
    @GetMapping("/{id}/result")
    public ApiResponse<OpinionBoxResultResponse> getOpinionBoxResult(
            @Parameter(description = "意见箱ID") @PathVariable String id) {
        return ApiResponse.success(opinionBoxService.getOpinionBoxResult(id));
    }

    @Operation(summary = "投票", description = "投票。Auth: Required | Roles: resident")
    @PostMapping("/{id}/vote")
    public ApiResponse<VoteOpinionBoxResponse> vote(
            @Parameter(description = "意见箱ID") @PathVariable String id,
            @Valid @RequestBody VoteOpinionBoxRequest request) {
        return ApiResponse.success(opinionBoxService.vote(id, request));
    }

    @Operation(summary = "我的投票记录", description = "我参与的投票列表。Auth: Required | Roles: resident")
    @GetMapping("/my-votes")
    public ApiResponse<PageResponse<MyVoteListItemResponse>> listMyVotes(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(opinionBoxService.listMyVotes(page, pageSize));
    }

    @Operation(summary = "意见箱详情", description = "意见箱详情。Auth: Required | Roles: resident")
    @GetMapping("/{id}")
    public ApiResponse<OpinionBoxDetailResponse> getOpinionBox(
            @Parameter(description = "意见箱ID") @PathVariable String id) {
        return ApiResponse.success(opinionBoxService.getOpinionBox(id));
    }
}
