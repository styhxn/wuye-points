package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.consultation.ConsultantDetailResponse;
import com.wuye.points.dto.consultation.ConsultantListItemResponse;
import com.wuye.points.dto.consultation.ConsultationAppointmentItemResponse;
import com.wuye.points.dto.consultation.ConsultationChatOrderResponse;
import com.wuye.points.dto.consultation.CreateConsultationAppointmentRequest;
import com.wuye.points.dto.consultation.CreateConsultationChatOrderRequest;
import com.wuye.points.service.ConsultationService;
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
import java.util.Map;

@Tag(name = "咨询板块", description = "医疗/教育/养老咨询师")
@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @Operation(summary = "咨询领域列表")
    @GetMapping("/categories")
    public ApiResponse<Map<String, List<Map<String, String>>>> categories() {
        return ApiResponse.success(Map.of("list", List.of(
                Map.of("code", "medical", "name", "医疗"),
                Map.of("code", "education", "name", "教育"),
                Map.of("code", "elderly", "name", "养老"))));
    }

    @Operation(summary = "咨询师列表", description = "按领域分页查询已审核上架的咨询师")
    @GetMapping("/consultants")
    public ApiResponse<PageResponse<ConsultantListItemResponse>> listConsultants(
            @Parameter(description = "领域 medical/education/elderly") @RequestParam(required = false) String category,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(consultationService.listConsultants(category, page, pageSize, keyword));
    }

    @Operation(summary = "咨询师详情")
    @GetMapping("/consultants/{id}")
    public ApiResponse<ConsultantDetailResponse> getConsultant(
            @Parameter(description = "咨询师ID") @PathVariable String id) {
        return ApiResponse.success(consultationService.getConsultant(id));
    }

    @Operation(summary = "发起图文咨询", description = "一步扣款并返回会话对象ID")
    @PostMapping("/consultants/{id}/chat-orders")
    public ApiResponse<ConsultationChatOrderResponse> createChatOrder(
            @Parameter(description = "咨询师ID") @PathVariable String id,
            @Valid @RequestBody CreateConsultationChatOrderRequest request) {
        return ApiResponse.success(consultationService.createChatOrder(id, request));
    }

    @Operation(summary = "提交预约面诊")
    @PostMapping("/appointments")
    public ApiResponse<ConsultationAppointmentItemResponse> createAppointment(
            @Valid @RequestBody CreateConsultationAppointmentRequest request) {
        return ApiResponse.success(consultationService.createAppointment(request));
    }

    @Operation(summary = "我的预约")
    @GetMapping("/appointments/my")
    public ApiResponse<PageResponse<ConsultationAppointmentItemResponse>> listMyAppointments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(consultationService.listMyAppointments(page, pageSize));
    }
}
