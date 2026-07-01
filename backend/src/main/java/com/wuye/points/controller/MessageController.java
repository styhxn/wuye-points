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
import com.wuye.points.service.MessageService;
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
 * 消息模块
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/neighbor-limit")
    public ApiResponse<NeighborLimitResponse> getNeighborLimit() {
        return ApiResponse.success(messageService.getNeighborLimit());
    }

    @PostMapping("/neighbor-chat")
    public ApiResponse<NeighborChatResponse> neighborChat(
            @Valid @RequestBody NeighborChatRequest request) {
        return ApiResponse.success(messageService.neighborChat(request));
    }

    @PostMapping("/broadcast")
    public ApiResponse<MessageDetailResponse> broadcastMessage(
            @Valid @RequestBody BroadcastMessageRequest request) {
        return ApiResponse.success(messageService.broadcastMessage(request));
    }

    @PostMapping
    public ApiResponse<MessageDetailResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request) {
        return ApiResponse.success(messageService.sendMessage(request));
    }

    @GetMapping
    public ApiResponse<PageResponse<MessageListItemResponse>> listMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String chatType,
            @RequestParam(required = false) String readStatus,
            @RequestParam(required = false) String fromId,
            @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(messageService.listMessages(
                page, pageSize, messageType, chatType, readStatus, fromId, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<MessageDetailResponse> getMessage(@PathVariable String id) {
        return ApiResponse.success(messageService.getMessage(id));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<MarkMessageReadResponse> markRead(@PathVariable String id) {
        return ApiResponse.success(messageService.markRead(id));
    }
}
