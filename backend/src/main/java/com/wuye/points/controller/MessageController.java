package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.message.BroadcastMessageRequest;
import com.wuye.points.dto.message.CreateMessageRequest;
import com.wuye.points.dto.message.MarkMessageReadResponse;
import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.dto.message.NeighborChatRequest;
import com.wuye.points.dto.message.NeighborChatResponse;
import com.wuye.points.dto.message.NeighborLimitResponse;
import com.wuye.points.dto.message.NeighborSearchResponse;
import com.wuye.points.service.MessageService;
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

import java.util.List;

/**
 * 消息模块
 */
@Tag(name = "消息模块", description = "邻居搜索、私聊、广播消息、消息列表、已读标记")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "搜索邻居", description = "根据楼栋和单元搜索邻居")
    @GetMapping("/neighbors")
    public ApiResponse<List<NeighborSearchResponse>> searchNeighbors(
            @Parameter(description = "楼栋", example = "1") @RequestParam String building,
            @Parameter(description = "单元", example = "1") @RequestParam String unit) {
        return ApiResponse.success(messageService.searchNeighbors(building, unit));
    }

    @Operation(summary = "获取邻居搜索限制", description = "获取邻居搜索限制")
    @GetMapping("/neighbor-limit")
    public ApiResponse<NeighborLimitResponse> getNeighborLimit() {
        return ApiResponse.success(messageService.getNeighborLimit());
    }

    @Operation(summary = "邻居私聊", description = "邻居私聊")
    @PostMapping("/neighbor-chat")
    public ApiResponse<NeighborChatResponse> neighborChat(
            @Valid @RequestBody NeighborChatRequest request) {
        return ApiResponse.success(messageService.neighborChat(request));
    }

    @Operation(summary = "群发消息给技工", description = "群发消息给技工")
    @PostMapping("/broadcast")
    public ApiResponse<MessageDetailResponse> broadcastMessage(
            @Valid @RequestBody BroadcastMessageRequest request) {
        return ApiResponse.success(messageService.broadcastMessage(request));
    }

    @Operation(summary = "发送消息", description = "发送消息")
    @PostMapping
    public ApiResponse<MessageDetailResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request) {
        return ApiResponse.success(messageService.sendMessage(request));
    }

    @Operation(summary = "消息列表", description = "分页查询消息列表")
    @GetMapping
    public ApiResponse<PageResponse<MessageListItemResponse>> listMessages(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "消息类型") @RequestParam(required = false) String messageType,
            @Parameter(description = "聊天类型") @RequestParam(required = false) String chatType,
            @Parameter(description = "已读状态") @RequestParam(required = false) String readStatus,
            @Parameter(description = "发送者ID") @RequestParam(required = false) String fromId,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(messageService.listMessages(
                page, pageSize, messageType, chatType, readStatus, fromId, sort));
    }

    @Operation(summary = "消息详情", description = "获取消息详情")
    @GetMapping("/{id}")
    public ApiResponse<MessageDetailResponse> getMessage(
            @Parameter(description = "消息ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(messageService.getMessage(id));
    }

    @Operation(summary = "标记已读", description = "标记消息已读")
    @PostMapping("/{id}/read")
    public ApiResponse<MarkMessageReadResponse> markRead(
            @Parameter(description = "消息ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(messageService.markRead(id));
    }
}
