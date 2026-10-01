package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.message.BroadcastMessageRequest;
import com.wuye.points.dto.message.ConversationItemResponse;
import com.wuye.points.dto.message.CreateMessageRequest;
import com.wuye.points.dto.message.MarkMessageReadResponse;
import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.dto.message.NeighborBlockRequest;
import com.wuye.points.dto.message.NeighborBlockResponse;
import com.wuye.points.dto.message.NeighborChatRequest;
import com.wuye.points.dto.message.NeighborChatResponse;
import com.wuye.points.dto.message.NeighborLimitResponse;
import com.wuye.points.dto.message.NeighborSearchResponse;
import com.wuye.points.dto.message.UnreadCountResponse;
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

/**
 * 消息模块
 */
@Tag(name = "消息模块", description = "邻居搜索、私聊、广播消息、消息列表、已读标记")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "搜索邻居", description = "根据关键词搜索同小区邻居，支持分页")
    @GetMapping("/neighbors")
    public ApiResponse<PageResponse<NeighborSearchResponse>> searchNeighbors(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词（姓名/房号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "小区ID") @RequestParam(required = false) String communityId,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "排序字段", example = "-createdAt") @RequestParam(defaultValue = "-createdAt") String sort) {
        return ApiResponse.success(messageService.searchNeighbors(page, pageSize, keyword, communityId, building, sort));
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

    @Operation(summary = "屏蔽邻居", description = "屏蔽指定邻居，不再接收其私聊消息")
    @PostMapping("/neighbors/block")
    public ApiResponse<NeighborBlockResponse> blockNeighbor(
            @Valid @RequestBody NeighborBlockRequest request) {
        return ApiResponse.success(messageService.blockNeighbor(request));
    }

    @Operation(summary = "取消屏蔽邻居", description = "取消屏蔽指定邻居")
    @PostMapping("/neighbors/unblock")
    public ApiResponse<NeighborBlockResponse> unblockNeighbor(
            @Valid @RequestBody NeighborBlockRequest request) {
        return ApiResponse.success(messageService.unblockNeighbor(request));
    }

    @Operation(summary = "会话列表",
            description = "当前用户聊天会话列表，按对方聚合（邻居/官方/服务需求/商家广告），显示最后一条消息与未读数。"
                    + "支持 resident/activity_leader/merchant 等角色")
    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ConversationItemResponse>> listConversations(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "会话类型：neighbor/official/service_request/merchant_ad")
            @RequestParam(required = false) String conversationType) {
        return ApiResponse.success(messageService.listMyConversations(page, pageSize, conversationType));
    }

    @Operation(summary = "会话聊天记录",
            description = "与指定对方的完整会话记录（分页，按时间倒序）。"
                    + "peerId 支持住户 res_ 或官方会话键 official_；打开会话自动把对方发来的未读标记为已读")
    @GetMapping("/conversations/{peerId}")
    public ApiResponse<PageResponse<MessageListItemResponse>> getConversationMessages(
            @Parameter(description = "对方会话ID（res_ 或 official_）") @PathVariable String peerId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(messageService.getConversationMessages(peerId, page, pageSize));
    }

    @Operation(summary = "获取未读消息数", description = "获取当前用户未读消息总数及分类统计")
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount() {
        return ApiResponse.success(messageService.getUnreadCount());
    }
}
