package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.deliverypricerange.CreateDeliveryPriceRangeRequest;
import com.wuye.points.dto.admin.deliverypricerange.DeliveryPriceRangeResponse;
import com.wuye.points.dto.admin.deliverypricerange.UpdateDeliveryPriceRangeRequest;
import com.wuye.points.entity.DeliveryPriceRange;
import com.wuye.points.enums.DeliveryScope;
import com.wuye.points.enums.DistanceType;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryPriceRangeRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.DisplayLabelUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 物业公司配送价格区间管理服务。
 */
@Service
@RequiredArgsConstructor
public class AdminDeliveryPriceRangeService {

    private static final Set<String> RANGE_STATUS = Set.of("active", "inactive");
    private static final String DEFAULT_STATUS = "active";
    private static final String DEFAULT_DISTANCE_TYPE = DistanceType.ANY.getCode();
    private static final String DEFAULT_DELIVERY_SCOPE = DeliveryScope.BOTH.getCode();

    private final DeliveryPriceRangeRepository deliveryPriceRangeRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<DeliveryPriceRangeResponse> listRanges(String propertyCompanyId, String status,
                                                       String distanceType, String deliveryScope) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateStatus(status);
        validateDistanceType(distanceType);
        validateDeliveryScope(deliveryScope);

        String scopedPropertyCompanyId = resolvePropertyCompanyId(propertyCompanyId);
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            return List.of();
        }

        Specification<DeliveryPriceRange> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("propertyCompanyId"), scopedPropertyCompanyId));
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(distanceType)) {
                predicates.add(cb.equal(root.get("distanceType"), distanceType));
            }
            if (StringUtils.hasText(deliveryScope)) {
                predicates.add(cb.equal(root.get("deliveryScope"), deliveryScope));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DeliveryPriceRange> ranges = deliveryPriceRangeRepository.findAll(spec);
        return ranges.stream()
                .sorted(Comparator.comparing(DeliveryPriceRange::getMinPrice))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryPriceRangeResponse createRange(CreateDeliveryPriceRangeRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        validatePriceRange(request.getMinPrice(), request.getMaxPrice());

        String distanceType = StringUtils.hasText(request.getDistanceType())
                ? request.getDistanceType() : DEFAULT_DISTANCE_TYPE;
        validateDistanceType(distanceType);

        String deliveryScope = StringUtils.hasText(request.getDeliveryScope())
                ? request.getDeliveryScope() : DEFAULT_DELIVERY_SCOPE;
        validateDeliveryScope(deliveryScope);

        String propertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        // 校验与同维度现有 active 区间不重叠
        assertNoOverlap(propertyCompanyId, distanceType, deliveryScope,
                request.getMinPrice(), request.getMaxPrice(), null);

        DeliveryPriceRange range = new DeliveryPriceRange();
        range.setId(idGenerator.generateId("dpr"));
        range.setPropertyCompanyId(propertyCompanyId);
        range.setMinPrice(request.getMinPrice());
        range.setMaxPrice(request.getMaxPrice());
        range.setDistanceType(distanceType);
        range.setDeliveryScope(deliveryScope);
        range.setStatus(DEFAULT_STATUS);
        deliveryPriceRangeRepository.save(range);

        return toResponse(range);
    }

    @Transactional
    public DeliveryPriceRangeResponse updateRange(String id, UpdateDeliveryPriceRangeRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateId(id);
        DeliveryPriceRange range = getAccessibleRange(id);

        BigDecimal newMin = request.getMinPrice() != null ? request.getMinPrice() : range.getMinPrice();
        BigDecimal newMax = request.getMaxPrice() != null ? request.getMaxPrice() : range.getMaxPrice();
        validatePriceRange(newMin, newMax);

        if (StringUtils.hasText(request.getDistanceType())) {
            validateDistanceType(request.getDistanceType());
        }
        if (StringUtils.hasText(request.getDeliveryScope())) {
            validateDeliveryScope(request.getDeliveryScope());
        }

        String newDistanceType = StringUtils.hasText(request.getDistanceType())
                ? request.getDistanceType() : range.getDistanceType();
        String newDeliveryScope = StringUtils.hasText(request.getDeliveryScope())
                ? request.getDeliveryScope() : range.getDeliveryScope();

        if (request.getMinPrice() != null || request.getMaxPrice() != null
                || StringUtils.hasText(request.getDistanceType())
                || StringUtils.hasText(request.getDeliveryScope())) {
            assertNoOverlap(range.getPropertyCompanyId(), newDistanceType, newDeliveryScope,
                    newMin, newMax, range.getId());
            range.setMinPrice(newMin);
            range.setMaxPrice(newMax);
            range.setDistanceType(newDistanceType);
            range.setDeliveryScope(newDeliveryScope);
        }
        if (StringUtils.hasText(request.getStatus())) {
            validateStatus(request.getStatus());
            range.setStatus(request.getStatus());
        }

        deliveryPriceRangeRepository.save(range);
        return toResponse(range);
    }

    @Transactional
    public DeliveryPriceRangeResponse deleteRange(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateId(id);
        DeliveryPriceRange range = getAccessibleRange(id);
        range.setStatus("inactive");
        deliveryPriceRangeRepository.save(range);
        return toResponse(range);
    }

    // ============== 私有工具 ==============

    private DeliveryPriceRange getAccessibleRange(String id) {
        DeliveryPriceRange range = deliveryPriceRangeRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.DELIVERY_PRICE_RANGE_NOT_FOUND));
        if (roleGuard.isPlatformAdmin()) {
            return range;
        }
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (currentPropertyCompanyId == null
                || !currentPropertyCompanyId.equals(range.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.DELIVERY_PRICE_RANGE_NOT_FOUND);
        }
        return range;
    }

    private String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)
                    && !requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private void validateId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("dpr_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配送价格区间ID格式不正确");
        }
    }

    private void validateStatus(String status) {
        if (StringUtils.hasText(status) && !RANGE_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, "minPrice/maxPrice不能为空");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.DELIVERY_PRICE_RANGE_INVALID, "价格不能为负");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BizException(ErrorCode.DELIVERY_PRICE_RANGE_INVALID, "minPrice不能大于maxPrice");
        }
    }

    /**
     * 校验新区间与物业公司同维度（distanceType + deliveryScope）下现有 active 区间不重叠。
     * 区间为闭区间 [min, max]，重叠判定：existing.min <= newMax && newMin <= existing.max
     */
    private void assertNoOverlap(String propertyCompanyId, String distanceType, String deliveryScope,
                                 BigDecimal newMin, BigDecimal newMax, String excludeId) {
        List<DeliveryPriceRange> existing = deliveryPriceRangeRepository
                .findByPropertyCompanyIdAndStatusAndDistanceTypeAndDeliveryScope(
                        propertyCompanyId, DEFAULT_STATUS, distanceType, deliveryScope);
        for (DeliveryPriceRange r : existing) {
            if (excludeId != null && excludeId.equals(r.getId())) {
                continue;
            }
            boolean overlaps = r.getMinPrice().compareTo(newMax) <= 0
                    && newMin.compareTo(r.getMaxPrice()) <= 0;
            if (overlaps) {
                throw new BizException(ErrorCode.DELIVERY_PRICE_RANGE_OVERLAP);
            }
        }
    }

    private DeliveryPriceRangeResponse toResponse(DeliveryPriceRange range) {
        String statusCode = range.getStatus();
        String statusLabel = DisplayLabelUtil.leaderStatusLabel(statusCode);
        String distanceTypeCode = range.getDistanceType();
        String deliveryScopeCode = range.getDeliveryScope();
        return DeliveryPriceRangeResponse.builder()
                .id(range.getId())
                .propertyCompanyId(range.getPropertyCompanyId())
                .minPrice(range.getMinPrice())
                .maxPrice(range.getMaxPrice())
                .distanceType(distanceTypeCode)
                .distanceTypeLabel(distanceTypeLabel(distanceTypeCode))
                .deliveryScope(deliveryScopeCode)
                .deliveryScopeLabel(deliveryScopeLabel(deliveryScopeCode))
                .statusCode(statusCode)
                .statusLabel(statusLabel)
                .status(DisplayLabelUtil.displayOrCode(statusCode, statusLabel))
                .createdAt(DateTimeUtil.toIsoUtc(range.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(range.getUpdatedAt()))
                .build();
    }

    private String distanceTypeLabel(String code) {
        if (code == null) {
            return null;
        }
        for (DistanceType t : DistanceType.values()) {
            if (t.getCode().equals(code)) {
                return t.getDescription();
            }
        }
        return code;
    }

    private String deliveryScopeLabel(String code) {
        if (code == null) {
            return null;
        }
        for (DeliveryScope s : DeliveryScope.values()) {
            if (s.getCode().equals(code)) {
                return s.getDescription();
            }
        }
        return code;
    }

    private void validateDistanceType(String code) {
        if (StringUtils.hasText(code) && !DistanceType.isValid(code)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "distanceType不合法");
        }
    }

    private void validateDeliveryScope(String code) {
        if (StringUtils.hasText(code) && !DeliveryScope.isValid(code)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "deliveryScope不合法");
        }
    }
}
