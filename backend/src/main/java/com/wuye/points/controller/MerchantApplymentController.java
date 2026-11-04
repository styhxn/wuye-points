package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.MediaUploadResponse;
import com.wuye.points.service.MerchantApplymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商家端 — 微信进件（普通服务商「特约商户进件」）。
 *
 * <p>角色：merchant / activity_leader / technician（本人店铺）。资料敏感字段由后端
 * 以微信支付公钥 RSA-OAEP 加密落库，前端仅经媒体上传接口获取 media_id 后随资料一并提交。
 * 审核通过进入待签约后，响应中的 {@code signUrl} 转二维码给超级管理员扫码完成签约。</p>
 */
@Tag(name = "商家端 — 微信进件", description = "商家提交特约商户进件资料、查看状态/签约链接、查询银行列表")
@RestController
@RequestMapping("/merchants/my/applyment")
@RequiredArgsConstructor
public class MerchantApplymentController {

    private final MerchantApplymentService applymentService;

    @Operation(summary = "查询我的进件状态", description = "返回进件状态机、签约链接、驳回原因等")
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

    @Operation(summary = "查询开户银行列表", description = "进件表单「开户银行」下拉数据源；类型：对公/对私")
    @GetMapping("/banks")
    public ApiResponse<Object> listBanks(
            @RequestParam(defaultValue = "BANK_ACCOUNT_TYPE_CORPORATE") String bankAccountType) {
        return ApiResponse.success(applymentService.listBanks(bankAccountType));
    }

    @Operation(summary = "查询支行列表",
            description = "银行要求填写支行时，按银行编码+城市编码查询联行号（城市编码必填，支持 offset/limit 分页）")
    @GetMapping("/banks/{bankAliasCode}/branches")
    public ApiResponse<Object> listBranches(@PathVariable String bankAliasCode,
                                           @RequestParam String cityCode,
                                           @RequestParam(defaultValue = "0") int offset,
                                           @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(applymentService.listBranches(bankAliasCode, cityCode, offset, limit));
    }

    @Operation(summary = "查询可选行业与结算规则", description = "进件表单「所属行业」下拉数据源；结算规则由后端按主体类型自动匹配")
    @GetMapping("/industries")
    public ApiResponse<java.util.Map<String, Object>> industries() {
        return ApiResponse.success(applymentService.industryOptions());
    }
}
