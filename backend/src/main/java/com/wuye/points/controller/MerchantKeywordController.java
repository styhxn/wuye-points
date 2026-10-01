package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.merchantkeyword.AddKeywordRequest;
import com.wuye.points.dto.merchantkeyword.BatchAddKeywordsRequest;
import com.wuye.points.dto.merchantkeyword.KeywordItemResponse;
import com.wuye.points.dto.merchantkeyword.KeywordListResponse;
import com.wuye.points.dto.merchantkeyword.MatchByKeywordsRequest;
import com.wuye.points.dto.merchantkeyword.MatchedMerchantResponse;
import com.wuye.points.dto.merchantkeyword.UpdateKeywordRequest;
import com.wuye.points.service.MerchantKeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商家关键词模块
 */
@Tag(name = "商家关键词模块", description = "商家关键词增删改查、按关键词匹配商家")
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantKeywordController {

    private final MerchantKeywordService merchantKeywordService;

    /**
     * 我的商家关键词列表
     * Auth: Required | Roles: merchant
     */
    @Operation(summary = "我的商家关键词列表", description = "获取当前商家关键词列表，需登录，角色：merchant")
    @GetMapping("/my/keywords")
    public ApiResponse<KeywordListResponse> listMyKeywords() {
        String merchantId = merchantKeywordService.requireCurrentMerchantId();
        return ApiResponse.success(merchantKeywordService.listKeywords(merchantId));
    }

    /**
     * 增加单个关键词
     * Auth: Required | Roles: merchant
     */
    @Operation(summary = "增加单个关键词", description = "为当前商家增加单个关键词（去重），需登录，角色：merchant")
    @PostMapping("/my/keywords")
    public ApiResponse<KeywordItemResponse> addMyKeyword(@Valid @RequestBody AddKeywordRequest request) {
        String merchantId = merchantKeywordService.requireCurrentMerchantId();
        return ApiResponse.success(merchantKeywordService.addKeyword(merchantId, request));
    }

    /**
     * 批量增加关键词
     * Auth: Required | Roles: merchant
     */
    @Operation(summary = "批量增加关键词", description = "为当前商家批量增加关键词（自动去重），需登录，角色：merchant")
    @PostMapping("/my/keywords/batch")
    public ApiResponse<List<KeywordItemResponse>> batchAddMyKeywords(
            @Valid @RequestBody BatchAddKeywordsRequest request) {
        String merchantId = merchantKeywordService.requireCurrentMerchantId();
        return ApiResponse.success(merchantKeywordService.batchAddKeywords(merchantId, request));
    }

    /**
     * 更新关键词权重
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @Operation(summary = "更新关键词权重", description = "更新关键词权重，需登录，角色：merchant/property_admin/platform_admin")
    @PutMapping("/keywords/{id}")
    public ApiResponse<KeywordItemResponse> updateKeyword(
            @Parameter(description = "关键词ID", example = "mk_xxx") @PathVariable String id,
            @Valid @RequestBody UpdateKeywordRequest request) {
        return ApiResponse.success(merchantKeywordService.updateKeyword(id, request));
    }

    /**
     * 删除关键词
     * Auth: Required | Roles: merchant, property_admin, platform_admin
     */
    @Operation(summary = "删除关键词", description = "删除关键词，需登录，角色：merchant/property_admin/platform_admin")
    @DeleteMapping("/keywords/{id}")
    public ApiResponse<Void> deleteKeyword(
            @Parameter(description = "关键词ID", example = "mk_xxx") @PathVariable String id) {
        merchantKeywordService.deleteKeyword(id);
        return ApiResponse.success(null);
    }

    /**
     * 按关键词匹配商家
     * Auth: Optional | Roles: any
     */
    @Operation(summary = "按关键词匹配商家", description = "按关键词匹配商家，返回命中的商家列表（按总权重降序），公开/居民调用")
    @PostMapping("/match-by-keywords")
    public ApiResponse<List<MatchedMerchantResponse>> matchByKeywords(
            @Valid @RequestBody MatchByKeywordsRequest request) {
        return ApiResponse.success(merchantKeywordService.matchMerchantsByKeywords(
                request.getKeywords(), request.getPropertyCompanyId()));
    }
}
