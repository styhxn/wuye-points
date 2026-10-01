package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.inquiry.CreateInquiryRequest;
import com.wuye.points.dto.inquiry.InquiryDetailResponse;
import com.wuye.points.dto.inquiry.InquiryListItemResponse;
import com.wuye.points.dto.inquiry.InquiryReplyRequest;
import com.wuye.points.service.InquiryService;
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

/**
 * 询价模块
 */
@Tag(name = "询价", description = "业主发起询价、商家报价、业主接受/拒绝")
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "发起询价", description = "业主向商家发起询价。角色: resident")
    @PostMapping
    public ApiResponse<InquiryDetailResponse> createInquiry(
            @Valid @RequestBody CreateInquiryRequest request) {
        return ApiResponse.success(inquiryService.createInquiry(request));
    }

    @Operation(summary = "询价列表（业主视角）", description = "当前业主发起的询价列表。角色: resident")
    @GetMapping
    public ApiResponse<PageResponse<InquiryListItemResponse>> listMyInquiries(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(inquiryService.listMyInquiries(page, pageSize));
    }

    @Operation(summary = "询价列表（商家视角）", description = "当前商家收到的询价列表。角色: merchant")
    @GetMapping("/merchant")
    public ApiResponse<PageResponse<InquiryListItemResponse>> listMerchantInquiries(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(inquiryService.listMerchantInquiries(page, pageSize));
    }

    @Operation(summary = "询价回复/确认",
            description = "商家报价(action=quote)或业主接受/拒绝(action=accept/reject)。角色: merchant 或 resident")
    @PutMapping("/{id}")
    public ApiResponse<InquiryDetailResponse> replyInquiry(
            @Parameter(description = "询价ID") @PathVariable String id,
            @Valid @RequestBody InquiryReplyRequest request) {
        return ApiResponse.success(inquiryService.replyInquiry(id, request));
    }
}
