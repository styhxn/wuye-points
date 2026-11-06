package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.BankBranchListResponse;
import com.wuye.points.dto.merchant.BankListResponse;
import com.wuye.points.dto.merchant.MediaUploadResponse;
import com.wuye.points.service.MerchantApplymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "保存/更新进件资料（草稿）",
            description = "允许分步填写：只校验已填字段的取值与交叉一致性，不要求表单完整；"
                    + "必填项在 /submit 时统一校验。审核中不可修改；驳回后可改资料重提")
    @PostMapping
    public ApiResponse<ApplymentDetailResponse> saveApplyment(
            @Valid @RequestBody ApplymentSubmitRequest request) {
        return ApiResponse.success(
                applymentService.saveApplyment(applymentService.resolveCurrentMerchantId(), request));
    }

    @Operation(summary = "提交进件到微信",
            description = "先按微信《提交申请单》必填清单校验库中资料，缺项时一次性返回「资料不完整，请补齐：…」；"
                    + "校验通过后进入微信审核流程")
    @PostMapping("/submit")
    public ApiResponse<ApplymentDetailResponse> submitApplyment() {
        return ApiResponse.success(applymentService.submitApplyment(applymentService.resolveCurrentMerchantId()));
    }

    @Operation(summary = "上传进件资料图片", description = "返回 media_id（有效期约3天），用于提交进件")
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaUploadResponse> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(applymentService.uploadMedia(file));
    }

    @Operation(summary = "查询开户银行列表",
            description = "进件表单「开户银行」下拉数据源。后端已拉微信全量（约 4400+ 条）并缓存 24 小时，"
                    + "支持 keyword 关键字检索与 offset/limit 分页；提交进件用 accountBank + accountBankCode，"
                    + "bankAliasCode 仅用于查支行")
    @GetMapping("/banks")
    public ApiResponse<BankListResponse> listBanks(
            @Parameter(description = "账户类型") @RequestParam(defaultValue = "BANK_ACCOUNT_TYPE_CORPORATE") String bankAccountType,
            @Parameter(description = "关键字（银行名称/别名/编码）") @RequestParam(required = false) String keyword,
            @Parameter(description = "偏移量") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "本次返回条数（默认50，上限500）") @RequestParam(defaultValue = "50") Integer limit) {
        return ApiResponse.success(applymentService.listBanks(bankAccountType, keyword, offset, limit));
    }

    @Operation(summary = "查询支行列表",
            description = "银行要求填写支行时（needBankBranch=true），按银行别名编码+城市编码查询联行号。"
                    + "cityCode 必填；limit 超微信上限 500 时夹取，生效值回显在 limitEffective")
    @GetMapping("/banks/{bankAliasCode}/branches")
    public ApiResponse<BankBranchListResponse> listBranches(@PathVariable String bankAliasCode,
                                                           @RequestParam String cityCode,
                                                           @RequestParam(defaultValue = "0") Integer offset,
                                                           @RequestParam(defaultValue = "100") Integer limit) {
        return ApiResponse.success(applymentService.listBranches(bankAliasCode, cityCode, offset, limit));
    }

    @Operation(summary = "查询可选行业与结算规则", description = "进件表单「所属行业」下拉数据源；结算规则由后端按主体类型自动匹配")
    @GetMapping("/industries")
    public ApiResponse<java.util.Map<String, Object>> industries() {
        return ApiResponse.success(applymentService.industryOptions());
    }
}
