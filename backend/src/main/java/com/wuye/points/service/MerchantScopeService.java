package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.MessageServiceConfigResponse;
import com.wuye.points.dto.merchant.ServiceScopeCommunityItem;
import com.wuye.points.dto.merchant.ServiceScopeResponse;
import com.wuye.points.dto.merchant.UpdateMessageServiceRequest;
import com.wuye.points.dto.merchant.UpdateServiceScopeRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantMessageServiceConfig;
import com.wuye.points.entity.MerchantServiceCommunity;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ServiceCategoryDict;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.MerchantMessageServiceConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantServiceCommunityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.ServiceCategoryDictRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantScopeService {

    private static final Set<String> ALLOWED_RADIUS = Set.of("1km", "3km", "5km", "district", "city");

    private final RoleGuard roleGuard;
    private final ResidentRepository residentRepository;
    private final MerchantRepository merchantRepository;
    private final CommunityRepository communityRepository;
    private final MerchantServiceCommunityRepository merchantServiceCommunityRepository;
    private final MerchantMessageServiceConfigRepository merchantMessageServiceConfigRepository;
    private final ServiceCategoryDictRepository serviceCategoryDictRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ServiceScopeResponse getServiceScope() {
        Merchant merchant = requireCurrentMerchant();
        List<Community> communities = communityRepository.findByPropertyCompanyId(merchant.getPropertyCompanyId());
        Set<String> selectedIds = merchantServiceCommunityRepository.findByMerchantId(merchant.getId()).stream()
                .map(MerchantServiceCommunity::getCommunityId)
                .collect(Collectors.toSet());
        boolean serveAll = Boolean.TRUE.equals(merchant.getServeAllCommunities());

        List<ServiceScopeCommunityItem> items = communities.stream()
                .map(c -> ServiceScopeCommunityItem.builder()
                        .communityId(c.getId())
                        .communityName(c.getName())
                        .selected(serveAll || selectedIds.contains(c.getId()))
                        .build())
                .toList();

        return ServiceScopeResponse.builder()
                .serveAllCommunities(serveAll)
                .communities(items)
                .build();
    }

    @Transactional
    public ServiceScopeResponse updateServiceScope(UpdateServiceScopeRequest request) {
        Merchant merchant = requireCurrentMerchant();
        boolean serveAll = Boolean.TRUE.equals(request.getServeAllCommunities());

        if (!serveAll) {
            if (CollectionUtils.isEmpty(request.getCommunityIds())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "未全选时须指定至少一个小区");
            }
            List<Community> allowed = communityRepository.findByPropertyCompanyId(merchant.getPropertyCompanyId());
            Set<String> allowedIds = allowed.stream().map(Community::getId).collect(Collectors.toSet());
            for (String communityId : request.getCommunityIds()) {
                if (!allowedIds.contains(communityId)) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "小区不在本物业公司范围内: " + communityId);
                }
            }
        }

        merchant.setServeAllCommunities(serveAll);
        merchantRepository.save(merchant);

        merchantServiceCommunityRepository.deleteByMerchantId(merchant.getId());
        if (!serveAll && !CollectionUtils.isEmpty(request.getCommunityIds())) {
            Set<String> unique = new HashSet<>(request.getCommunityIds());
            List<MerchantServiceCommunity> rows = new ArrayList<>();
            for (String communityId : unique) {
                MerchantServiceCommunity row = new MerchantServiceCommunity();
                row.setId(idGenerator.generateId("msc"));
                row.setMerchantId(merchant.getId());
                row.setCommunityId(communityId);
                rows.add(row);
            }
            merchantServiceCommunityRepository.saveAll(rows);
        }

        return getServiceScope();
    }

    @Transactional
    public MessageServiceConfigResponse updateMessageService(UpdateMessageServiceRequest request) {
        Merchant merchant = requireCurrentMerchant();
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());

        if (enabled) {
            if (!StringUtils.hasText(request.getServiceRadius()) || !ALLOWED_RADIUS.contains(request.getServiceRadius())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "serviceRadius 须为 1km/3km/5km/district/city");
            }
            List<String> categoryIds = request.getCategoryIds();
            if (CollectionUtils.isEmpty(categoryIds)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "开通消息服务时至少选择一个服务分类");
            }
            Set<String> uniqueIds = new HashSet<>(categoryIds);
            Set<String> validIds = serviceCategoryDictRepository.findAllById(uniqueIds).stream()
                    .filter(c -> "active".equals(c.getStatus()))
                    .map(ServiceCategoryDict::getId)
                    .collect(Collectors.toSet());
            uniqueIds.removeAll(validIds);
            if (!uniqueIds.isEmpty()) {
                throw new BizException(ErrorCode.PARAM_INVALID, "存在非法的服务分类ID: " + String.join(",", uniqueIds));
            }
        }

        MerchantMessageServiceConfig config = merchantMessageServiceConfigRepository
                .findByMerchantId(merchant.getId())
                .orElseGet(() -> {
                    MerchantMessageServiceConfig created = new MerchantMessageServiceConfig();
                    created.setId(idGenerator.generateId("mms"));
                    created.setMerchantId(merchant.getId());
                    return created;
                });

        config.setEnabled(enabled);
        config.setServiceRadius(request.getServiceRadius());
        try {
            config.setCategoryIds(objectMapper.writeValueAsString(
                    request.getCategoryIds() == null ? List.of() : request.getCategoryIds()));
        } catch (Exception e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "分类列表序列化失败");
        }
        merchantMessageServiceConfigRepository.save(config);

        return toMessageServiceResponse(config);
    }

    @Transactional(readOnly = true)
    public MessageServiceConfigResponse getMessageService() {
        Merchant merchant = requireCurrentMerchant();
        return merchantMessageServiceConfigRepository.findByMerchantId(merchant.getId())
                .map(this::toMessageServiceResponse)
                .orElse(MessageServiceConfigResponse.builder()
                        .enabled(false)
                        .serviceRadius(null)
                        .categoryIds(List.of())
                        .build());
    }

    private MessageServiceConfigResponse toMessageServiceResponse(MerchantMessageServiceConfig config) {
        List<String> categoryIds = List.of();
        if (StringUtils.hasText(config.getCategoryIds())) {
            try {
                categoryIds = objectMapper.readValue(config.getCategoryIds(), new TypeReference<>() {});
            } catch (Exception e) {
                // 配置数据损坏时降级为"无类目"避免整体 500，但必须留痕，否则脏配置永远无法被发现
                log.error("[商家消息配置] 类目解析失败 merchantId={} raw={}",
                        config.getMerchantId(), config.getCategoryIds(), e);
                categoryIds = List.of();
            }
        }
        return MessageServiceConfigResponse.builder()
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .serviceRadius(config.getServiceRadius())
                .categoryIds(categoryIds)
                .build();
    }

    private Merchant requireCurrentMerchant() {
        roleGuard.requireAnyRole("merchant");
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
}
