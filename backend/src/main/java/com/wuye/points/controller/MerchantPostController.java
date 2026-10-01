package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchantpost.CreateMerchantPostRequest;
import com.wuye.points.dto.merchantpost.MerchantPostItemResponse;
import com.wuye.points.dto.merchantpost.UpdateMerchantPostRequest;
import com.wuye.points.service.MerchantPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant-posts")
@RequiredArgsConstructor
public class MerchantPostController {

    private final MerchantPostService merchantPostService;

    /** 商家创建动态 */
    @PostMapping
    public ApiResponse<MerchantPostItemResponse> createPost(@Valid @RequestBody CreateMerchantPostRequest request) {
        return ApiResponse.success(merchantPostService.createPost(request));
    }

    /** 商家更新动态 */
    @PutMapping("/{id}")
    public ApiResponse<MerchantPostItemResponse> updatePost(
            @PathVariable String id, @RequestBody UpdateMerchantPostRequest request) {
        return ApiResponse.success(merchantPostService.updatePost(id, request));
    }

    /** 商家删除动态 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable String id) {
        merchantPostService.deletePost(id);
        return ApiResponse.success(null);
    }

    /** 商家查询自己的动态列表 */
    @GetMapping("/my")
    public ApiResponse<PageResponse<MerchantPostItemResponse>> listMyPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(merchantPostService.listMyPosts(page, pageSize));
    }

    /** 公开查询指定商家的已发布动态 */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<PageResponse<MerchantPostItemResponse>> listPostsByMerchant(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(merchantPostService.listPostsByMerchant(merchantId, page, pageSize));
    }

    /** 动态详情（自动 +1 浏览量） */
    @GetMapping("/{id}")
    public ApiResponse<MerchantPostItemResponse> getPostDetail(@PathVariable String id) {
        return ApiResponse.success(merchantPostService.getPostDetail(id));
    }
}
