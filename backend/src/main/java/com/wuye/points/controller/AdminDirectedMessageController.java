package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.directedmessage.AgeBracketListResponse;
import com.wuye.points.dto.directedmessage.CreateDirectedMessageRequest;
import com.wuye.points.dto.directedmessage.CreateDirectedMessageResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageDetailResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageListItemResponse;
import com.wuye.points.dto.directedmessage.DirectedMessageRecipientItemResponse;
import com.wuye.points.dto.directedmessage.UpdateAgeBracketsRequest;
import com.wuye.points.service.DirectedMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端 — 定向消息", description = "定向消息推送与年龄段配置")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDirectedMessageController {

    private final DirectedMessageService directedMessageService;

    @Operation(summary = "创建并发送定向消息", description = "角色: property_admin, platform_admin, coordinator")
    @PostMapping("/directed-messages")
    public ApiResponse<CreateDirectedMessageResponse> createDirectedMessage(
            @Valid @RequestBody CreateDirectedMessageRequest request) {
        return ApiResponse.success(directedMessageService.createAndSend(request));
    }

    @Operation(summary = "定向推送任务列表", description = "角色: property_admin, platform_admin, coordinator")
    @GetMapping("/directed-messages")
    public ApiResponse<PageResponse<DirectedMessageListItemResponse>> listDirectedMessages(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "官方发送方类型") @RequestParam(required = false) String officialSenderType,
            @Parameter(description = "物业公司ID") @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(directedMessageService.listTasks(
                page, pageSize, startDate, endDate, officialSenderType, propertyCompanyId));
    }

    @Operation(summary = "定向推送任务详情", description = "角色: property_admin, platform_admin, coordinator")
    @GetMapping("/directed-messages/{id}")
    public ApiResponse<DirectedMessageDetailResponse> getDirectedMessage(
            @Parameter(description = "任务ID") @PathVariable String id) {
        return ApiResponse.success(directedMessageService.getTask(id));
    }

    @Operation(summary = "定向推送收件人明细", description = "角色: property_admin, platform_admin, coordinator")
    @GetMapping("/directed-messages/{id}/recipients")
    public ApiResponse<PageResponse<DirectedMessageRecipientItemResponse>> listRecipients(
            @Parameter(description = "任务ID") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "已读状态") @RequestParam(required = false) String readStatus,
            @Parameter(description = "楼栋号") @RequestParam(required = false) String buildingNo,
            @Parameter(description = "性别") @RequestParam(required = false) String gender,
            @Parameter(description = "年龄段ID") @RequestParam(required = false) String ageBracketId) {
        return ApiResponse.success(directedMessageService.listRecipients(
                id, page, pageSize, readStatus, buildingNo, gender, ageBracketId));
    }

    @Operation(summary = "年龄段配置列表", description = "角色: property_admin, platform_admin, coordinator（只读）")
    @GetMapping("/age-brackets")
    public ApiResponse<AgeBracketListResponse> listAgeBrackets() {
        return ApiResponse.success(directedMessageService.listAgeBrackets());
    }

    @Operation(summary = "批量更新年龄段配置", description = "角色: property_admin, platform_admin")
    @PutMapping("/age-brackets")
    public ApiResponse<AgeBracketListResponse> updateAgeBrackets(
            @Valid @RequestBody UpdateAgeBracketsRequest request) {
        return ApiResponse.success(directedMessageService.updateAgeBrackets(request));
    }
}
