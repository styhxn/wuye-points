package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.specialoffer.CreateSpecialOfferRequest;
import com.wuye.points.dto.specialoffer.SpecialOfferConverter;
import com.wuye.points.dto.specialoffer.SpecialOfferDetailResponse;
import com.wuye.points.dto.specialoffer.SpecialOfferListItemResponse;
import com.wuye.points.entity.SpecialOffer;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.SpecialOfferRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpecialOfferService {

    private static final Set<String> VALID_STATUS = Set.of("draft", "published", "ended", "archived");
    private static final Set<String> VALID_TARGET_TYPES = Set.of("all", "tag", "building", "role");

    private final SpecialOfferRepository specialOfferRepository;
    private final SpecialOfferConverter specialOfferConverter;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public SpecialOfferDetailResponse create(CreateSpecialOfferRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        validateTargetType(request.getTargetType());
        String propertyCompanyId = resolvePropertyCompanyId(null);
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft";
        validateStatus(status);

        SpecialOffer offer = new SpecialOffer();
        offer.setId(idGenerator.generateId("so"));
        offer.setTitle(request.getTitle());
        offer.setContent(request.getContent());
        offer.setTargetType(request.getTargetType());
        offer.setTargetTags(specialOfferConverter.toJson(
                StringUtils.hasText(request.getTargetTags()) ? List.of(request.getTargetTags().split(",")) : null));
        offer.setPublisherId(UserContext.currentUserId());
        offer.setPublisherRole(UserContext.currentRole());
        offer.setPropertyCompanyId(propertyCompanyId);
        offer.setCommunityId(request.getCommunityId());
        offer.setMerchantId(request.getMerchantId());
        offer.setCoverUrl(request.getCoverUrl());
        offer.setDiscountInfo(request.getDiscountInfo());
        offer.setStartTime(adminSupport.parseIsoDateTime(request.getStartTime()));
        offer.setEndTime(adminSupport.parseIsoDateTime(request.getEndTime()));
        offer.setStatus(status);

        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional(readOnly = true)
    public PageResponse<SpecialOfferListItemResponse> list(
            int page, int pageSize, String targetType, String status) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(targetType)) {
            validateTargetType(targetType);
        }
        if (StringUtils.hasText(status)) {
            validateStatus(status);
        }

        String propertyCompanyId = resolvePropertyCompanyId(null);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");

        Page<SpecialOffer> offerPage;
        if (StringUtils.hasText(targetType) && StringUtils.hasText(status)) {
            offerPage = specialOfferRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("propertyCompanyId"), propertyCompanyId),
                            cb.equal(root.get("targetType"), targetType),
                            cb.equal(root.get("status"), status)),
                    pageable);
        } else if (StringUtils.hasText(targetType)) {
            offerPage = specialOfferRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("propertyCompanyId"), propertyCompanyId),
                            cb.equal(root.get("targetType"), targetType)),
                    pageable);
        } else if (StringUtils.hasText(status)) {
            offerPage = specialOfferRepository.findAll(
                    (root, query, cb) -> cb.and(
                            cb.equal(root.get("propertyCompanyId"), propertyCompanyId),
                            cb.equal(root.get("status"), status)),
                    pageable);
        } else {
            offerPage = specialOfferRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("propertyCompanyId"), propertyCompanyId),
                    pageable);
        }

        List<SpecialOfferListItemResponse> list = offerPage.getContent().stream()
                .map(specialOfferConverter::toListItem)
                .toList();

        return PageResponse.of(list, page, pageSize, offerPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SpecialOfferDetailResponse getById(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional
    public SpecialOfferDetailResponse update(String id, CreateSpecialOfferRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);

        if (StringUtils.hasText(request.getTitle())) {
            offer.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            offer.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getTargetType())) {
            validateTargetType(request.getTargetType());
            offer.setTargetType(request.getTargetType());
        }
        if (request.getTargetTags() != null) {
            offer.setTargetTags(specialOfferConverter.toJson(
                    StringUtils.hasText(request.getTargetTags()) ? List.of(request.getTargetTags().split(",")) : null));
        }
        if (request.getCommunityId() != null) {
            offer.setCommunityId(request.getCommunityId());
        }
        if (request.getMerchantId() != null) {
            offer.setMerchantId(request.getMerchantId());
        }
        if (request.getCoverUrl() != null) {
            offer.setCoverUrl(request.getCoverUrl());
        }
        if (request.getDiscountInfo() != null) {
            offer.setDiscountInfo(request.getDiscountInfo());
        }
        if (StringUtils.hasText(request.getStartTime())) {
            offer.setStartTime(adminSupport.parseIsoDateTime(request.getStartTime()));
        }
        if (StringUtils.hasText(request.getEndTime())) {
            offer.setEndTime(adminSupport.parseIsoDateTime(request.getEndTime()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateStatus(request.getStatus());
            offer.setStatus(request.getStatus());
        }

        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional
    public SpecialOfferDetailResponse delete(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);
        offer.setStatus("archived");
        specialOfferRepository.save(offer);

        return specialOfferConverter.toDetail(offer);
    }

    private SpecialOffer getManageableOffer(String id) {
        SpecialOffer offer = specialOfferRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在"));

        if (roleGuard.isPlatformAdmin()) {
            return offer;
        }

        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (propertyCompanyId == null
                || offer.getPropertyCompanyId() == null
                || !propertyCompanyId.equals(offer.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        return offer;
    }

    private String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)) {
                if (!requestedPropertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
                return requestedPropertyCompanyId;
            }
            return null;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private void validateId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("so_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }

    private void validateTargetType(String targetType) {
        if (!VALID_TARGET_TYPES.contains(targetType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "targetType不合法");
        }
    }

    private void validateStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }
}
