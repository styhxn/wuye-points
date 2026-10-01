package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.message.ConversationItemResponse;
import com.wuye.points.dto.message.MessageListItemResponse;
import com.wuye.points.dto.message.MessageDetailResponse;
import com.wuye.points.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 消息会话", description = "物业管理员与住户的一对一聊天")
@RestController
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
@Validated
public class AdminMessageController {

    private final MessageService messageService;

    @Operation(summary = "物业侧会话列表", description = "物业管理员与住户的会话列表。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ConversationItemResponse>> conversations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "搜索关键词（住户姓名/ID）") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(messageService.listAdminConversations(page, pageSize, keyword));
    }

    @Operation(summary = "物业侧会话详情", description = "查看与指定住户的聊天记录。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/conversations/{residentId}")
    public ApiResponse<PageResponse<MessageListItemResponse>> conversationMessages(
            @Parameter(description = "住户ID") @PathVariable String residentId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(messageService.getAdminConversationMessages(residentId, page, pageSize));
    }

    @Operation(summary = "发送消息给住户", description = "物业管理员以物业官方身份向住户发送消息。Auth: Required | Roles: property_admin, platform_admin")
    @PostMapping("/conversations/{residentId}")
    public ApiResponse<MessageDetailResponse> sendMessage(
            @Parameter(description = "住户ID") @PathVariable String residentId,
            @RequestBody SendMessageRequest request) {
        return ApiResponse.success(messageService.sendAdminMessage(residentId, request.getContent()));
    }

    @lombok.Data
    public static class SendMessageRequest {
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 2000, message = "消息内容不能超过2000字")
        private String content;
    }
}
