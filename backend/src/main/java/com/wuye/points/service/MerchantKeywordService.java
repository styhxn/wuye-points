package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchantkeyword.AddKeywordRequest;
import com.wuye.points.dto.merchantkeyword.BatchAddKeywordsRequest;
import com.wuye.points.dto.merchantkeyword.KeywordItemResponse;
import com.wuye.points.dto.merchantkeyword.KeywordListResponse;
import com.wuye.points.dto.merchantkeyword.MatchedMerchantResponse;
import com.wuye.points.dto.merchantkeyword.UpdateKeywordRequest;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantKeyword;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantKeywordRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantKeywordService {

    private static final String STATUS_ACTIVE = "active";

    private final MerchantKeywordRepository merchantKeywordRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public KeywordListResponse listKeywords(String merchantId) {
        if (!StringUtils.hasText(merchantId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "merchantId不能为空");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        List<MerchantKeyword> keywords = merchantKeywordRepository.findByMerchantId(merchantId);
        return KeywordListResponse.builder()
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .keywords(keywords.stream().map(this::toItem).toList())
                .build();
    }

    @Transactional
    public KeywordItemResponse addKeyword(String merchantId, AddKeywordRequest request) {
        if (!StringUtils.hasText(merchantId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "merchantId不能为空");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        String keyword = normalizeKeyword(request.getKeyword());
        assertKeywordNotExists(merchantId, keyword);

        MerchantKeyword entity = new MerchantKeyword();
        entity.setId(idGenerator.generateId("mk"));
        entity.setMerchantId(merchantId);
        entity.setKeyword(keyword);
        entity.setWeight(request.getWeight() != null ? request.getWeight() : 0);
        entity.setStatus(STATUS_ACTIVE);

        try {
            merchantKeywordRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关键词已存在");
        }
        return toItem(entity);
    }

    @Transactional
    public List<KeywordItemResponse> batchAddKeywords(String merchantId, BatchAddKeywordsRequest request) {
        if (!StringUtils.hasText(merchantId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "merchantId不能为空");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        // 去重 + 规范化
        Set<String> seen = new HashSet<>();
        List<String> normalized = new ArrayList<>();
        for (String raw : request.getKeywords()) {
            String k = normalizeKeyword(raw);
            if (seen.add(k)) {
                normalized.add(k);
            }
        }

        // 排除已存在的
        List<MerchantKeyword> existing = merchantKeywordRepository.findByMerchantId(merchantId);
        Set<String> existingKeywords = existing.stream()
                .map(MerchantKeyword::getKeyword)
                .collect(Collectors.toSet());

        List<MerchantKeyword> toSave = new ArrayList<>();
        for (String k : normalized) {
            if (existingKeywords.contains(k)) {
                continue;
            }
            MerchantKeyword entity = new MerchantKeyword();
            entity.setId(idGenerator.generateId("mk"));
            entity.setMerchantId(merchantId);
            entity.setKeyword(k);
            entity.setWeight(0);
            entity.setStatus(STATUS_ACTIVE);
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            try {
                merchantKeywordRepository.saveAll(toSave);
            } catch (DataIntegrityViolationException e) {
                throw new BizException(ErrorCode.PARAM_INVALID, "部分关键词已存在");
            }
        }
        return toSave.stream().map(this::toItem).toList();
    }

    @Transactional
    public KeywordItemResponse updateKeyword(String id, UpdateKeywordRequest request) {
        if (!id.startsWith("mk_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关键词ID格式不正确");
        }
        MerchantKeyword keyword = merchantKeywordRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "关键词不存在"));
        assertCanManageKeyword(keyword);

        keyword.setWeight(request.getWeight());
        merchantKeywordRepository.save(keyword);
        return toItem(keyword);
    }

    @Transactional
    public void deleteKeyword(String id) {
        if (!id.startsWith("mk_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关键词ID格式不正确");
        }
        MerchantKeyword keyword = merchantKeywordRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "关键词不存在"));
        assertCanManageKeyword(keyword);
        merchantKeywordRepository.delete(keyword);
    }

    @Transactional(readOnly = true)
    public List<MatchedMerchantResponse> matchMerchantsByKeywords(List<String> keywords, String propertyCompanyId) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }
        // 规范化 + 去重
        Set<String> normalized = keywords.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<MerchantKeyword> matched = merchantKeywordRepository
                .findByKeywordInAndStatus(new ArrayList<>(normalized), STATUS_ACTIVE);
        if (matched.isEmpty()) {
            return List.of();
        }

        // 按 merchantId 分组
        Map<String, List<MerchantKeyword>> byMerchant = matched.stream()
                .collect(Collectors.groupingBy(MerchantKeyword::getMerchantId));

        // 加载商家并按物业公司范围/状态过滤
        List<Merchant> merchants = merchantRepository.findAllById(byMerchant.keySet());
        String scopedPropertyCompanyId = resolveMatchScope(propertyCompanyId);

        List<MatchedMerchantResponse> result = new ArrayList<>();
        for (Merchant merchant : merchants) {
            if (!"approved".equals(merchant.getAuditStatus()) || !"active".equals(merchant.getStatus())) {
                continue;
            }
            if (StringUtils.hasText(scopedPropertyCompanyId)
                    && !scopedPropertyCompanyId.equals(merchant.getPropertyCompanyId())) {
                continue;
            }
            List<MerchantKeyword> kws = byMerchant.get(merchant.getId());
            if (kws == null || kws.isEmpty()) {
                continue;
            }
            List<String> matchedNames = kws.stream()
                    .map(MerchantKeyword::getKeyword)
                    .sorted()
                    .toList();
            int totalWeight = kws.stream()
                    .mapToInt(k -> k.getWeight() == null ? 0 : k.getWeight())
                    .sum();
            result.add(MatchedMerchantResponse.builder()
                    .merchantId(merchant.getId())
                    .name(merchant.getName())
                    .matchedKeywords(matchedNames)
                    .totalWeight(totalWeight)
                    .build());
        }

        // 按总权重降序排序
        result.sort(Comparator.comparingInt(MatchedMerchantResponse::getTotalWeight).reversed());
        return result;
    }

    /**
     * 获取当前登录商家ID（角色：merchant）
     */
    public String requireCurrentMerchantId() {
        Merchant merchant = requireCurrentMerchant();
        return merchant.getId();
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

    private void assertCanManageKeyword(MerchantKeyword keyword) {
        // 商家管理自己的关键词；管理员可管理本物业范围内的关键词
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            Merchant merchant = merchantRepository.findById(keyword.getMerchantId())
                    .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
            roleGuard.assertSamePropertyOrPlatform(merchant.getPropertyCompanyId());
            return;
        }
        if (roleGuard.isMerchant()) {
            String currentMerchantId = requireCurrentMerchantId();
            if (!currentMerchantId.equals(keyword.getMerchantId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertKeywordNotExists(String merchantId, String keyword) {
        if (merchantKeywordRepository.existsByMerchantIdAndKeyword(merchantId, keyword)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关键词已存在");
        }
    }

    private String normalizeKeyword(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "keyword不能为空");
        }
        return raw.trim();
    }

    private String resolveMatchScope(String requestedPropertyCompanyId) {
        // 已登录用户使用其物业范围；未登录则使用请求参数
        String contextPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(contextPropertyCompanyId)) {
            if (roleGuard.isPlatformAdmin() && StringUtils.hasText(requestedPropertyCompanyId)) {
                return requestedPropertyCompanyId;
            }
            return contextPropertyCompanyId;
        }
        return requestedPropertyCompanyId;
    }

    private KeywordItemResponse toItem(MerchantKeyword keyword) {
        return KeywordItemResponse.builder()
                .id(keyword.getId())
                .merchantId(keyword.getMerchantId())
                .keyword(keyword.getKeyword())
                .weight(keyword.getWeight())
                .status(keyword.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(keyword.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(keyword.getUpdatedAt()))
                .build();
    }
}
