package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchantpost.CreateMerchantPostRequest;
import com.wuye.points.dto.merchantpost.MerchantPostItemResponse;
import com.wuye.points.dto.merchantpost.UpdateMerchantPostRequest;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPost;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPostRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantPostService {

    private final MerchantPostRepository merchantPostRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public MerchantPostItemResponse createPost(CreateMerchantPostRequest request) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin");
        Merchant merchant = resolveCurrentMerchant();

        MerchantPost post = new MerchantPost();
        post.setId(idGenerator.generateId("mp"));
        post.setMerchantId(merchant.getId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrls(request.getImageUrls());
        post.setVideoUrl(request.getVideoUrl());
        post.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "published");
        post.setViewCount(0);
        merchantPostRepository.save(post);
        return toItem(post, merchant.getName());
    }

    @Transactional
    public MerchantPostItemResponse updatePost(String id, UpdateMerchantPostRequest request) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin");
        Merchant merchant = resolveCurrentMerchant();
        MerchantPost post = merchantPostRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "动态不存在"));
        if (!post.getMerchantId().equals(merchant.getId()) && !roleGuard.isPlatformAdmin()) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (StringUtils.hasText(request.getTitle())) post.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getContent())) post.setContent(request.getContent());
        if (request.getImageUrls() != null) post.setImageUrls(request.getImageUrls());
        if (request.getVideoUrl() != null) post.setVideoUrl(request.getVideoUrl());
        if (StringUtils.hasText(request.getStatus())) post.setStatus(request.getStatus());
        merchantPostRepository.save(post);
        return toItem(post, merchant.getName());
    }

    @Transactional
    public void deletePost(String id) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin");
        Merchant merchant = resolveCurrentMerchant();
        MerchantPost post = merchantPostRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "动态不存在"));
        if (!post.getMerchantId().equals(merchant.getId()) && !roleGuard.isPlatformAdmin()) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        merchantPostRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PageResponse<MerchantPostItemResponse> listMyPosts(int page, int pageSize) {
        roleGuard.requireAnyRole("merchant", "property_admin", "platform_admin");
        Merchant merchant = resolveCurrentMerchant();
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<MerchantPost> postPage = merchantPostRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchant.getId(), pageable);
        List<MerchantPostItemResponse> list = postPage.getContent().stream()
                .map(p -> toItem(p, merchant.getName()))
                .toList();
        return PageResponse.of(list, page, pageSize, postPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<MerchantPostItemResponse> listPostsByMerchant(
            String merchantId, int page, int pageSize) {
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<MerchantPost> postPage = merchantPostRepository
                .findByMerchantIdAndStatusOrderByCreatedAtDesc(merchantId, "published", pageable);
        String merchantName = merchantRepository.findById(merchantId)
                .map(Merchant::getName).orElse("");
        List<MerchantPostItemResponse> list = postPage.getContent().stream()
                .map(p -> toItem(p, merchantName))
                .toList();
        return PageResponse.of(list, page, pageSize, postPage.getTotalElements());
    }

    @Transactional
    public MerchantPostItemResponse getPostDetail(String id) {
        MerchantPost post = merchantPostRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "动态不存在"));
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        merchantPostRepository.save(post);
        String merchantName = merchantRepository.findById(post.getMerchantId())
                .map(Merchant::getName).orElse("");
        return toItem(post, merchantName);
    }

    private Merchant resolveCurrentMerchant() {
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private MerchantPostItemResponse toItem(MerchantPost post, String merchantName) {
        return MerchantPostItemResponse.builder()
                .id(post.getId())
                .merchantId(post.getMerchantId())
                .merchantName(merchantName)
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .videoUrl(post.getVideoUrl())
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
