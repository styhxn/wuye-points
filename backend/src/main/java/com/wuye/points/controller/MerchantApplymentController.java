package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.MediaUploadResponse;
import com.wuye.points.service.EcommerceApplymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商家端 — 微信进件（电商收付通阶段1）。
 *
 * <p>角色：merchant / activity_leader / technician（本人店铺）。资料敏感字段由后端
 * 以微信平台证书加密落库，前端仅经媒体上传接口获取 media_id 后随资料一并提交。</p>
 */
@Tag(name = "商家端 — 微信进件", description = "商家提交微信进件资料、查看状态/法人验证/签约链接")
@RestController
@RequestMapping("/merchants/my/applyment")
@RequiredArgsConstructor
public class MerchantApplymentController {

    private final EcommerceApplymentService applymentService;

    @Operation(summary = "查询我的进件状态", description = "返回进件状态机、法人验证/签约链接、驳回原因等")
    @GetMapping
    public ApiResponse<ApplymentDetailResponse> getMyApplyment() {
        return ApiResponse.success(applymentService.getApplyment(applymentService.resolveCurrentMerchantId()));
    }

    @Operation(summary = "保存/更新进件资料", description = "审核中不可修改；驳回后可改资料重提")
    @PostMapping
    public ApiResponse<ApplymentDetailResponse> saveApplyment(
            @Valid @RequestBody ApplymentSubmitRequest request) {
        return ApiResponse.success(
                applymentService.saveApplyment(applymentService.resolveCurrentMerchantId(), request));
    }

    @Operation(summary = "提交进件到微信", description = "资料就绪后提交，进入微信审核流程")
    @PostMapping("/submit")
    public ApiResponse<ApplymentDetailResponse> submitApplyment() {
        return ApiResponse.success(applymentService.submitApplyment(applymentService.resolveCurrentMerchantId()));
    }

    @Operation(summary = "上传进件资料图片", description = "返回 media_id（有效期约3天），用于提交进件")
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(applymentService.uploadMedia(file));
    }
}
