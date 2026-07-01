package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.delivery.CreateDeliveryRuleRequest;
import com.wuye.points.dto.delivery.DeliveryRuleResponse;
import com.wuye.points.dto.delivery.UpdateDeliveryRuleRequest;
import com.wuye.points.entity.DeliveryRule;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryRuleRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryRuleService {

    private static final BigDecimal DEFAULT_COURIER_SHARE_RATE = new BigDecimal("1.0000");
    private static final BigDecimal DEFAULT_COURIER_PER_ORDER = new BigDecimal("0.35");
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    private final DeliveryRuleRepository deliveryRuleRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<DeliveryRuleResponse> listRules(
            int page,
            int pageSize,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }

        String scopedPropertyCompanyId = resolvePropertyCompanyId(propertyCompanyId);
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DeliveryRule> rulePage;
        if (StringUtils.hasText(scopedPropertyCompanyId)) {
            rulePage = deliveryRuleRepository.findByPropertyCompanyId(scopedPropertyCompanyId, pageable);
        } else {
            rulePage = deliveryRuleRepository.findAll(pageable);
        }

        List<DeliveryRuleResponse> list = rulePage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(list, page, pageSize, rulePage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public DeliveryRuleResponse getRule(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateRuleId(id);
        DeliveryRule rule = getRuleInScope(id);
        return toResponse(rule);
    }

    @Transactional
    public DeliveryRuleResponse createRule(CreateDeliveryRuleRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        String propertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        if (deliveryRuleRepository.findByPropertyCompanyId(propertyCompanyId).isPresent()) {
            throw new BizException(ErrorCode.DELIVERY_RULE_DUPLICATE);
        }

        DeliveryRule rule = new DeliveryRule();
        rule.setId(idGenerator.generateId("drl"));
        rule.setName(request.getName());
        rule.setBaseFee(request.getBaseFee());
        rule.setPerKgFee(request.getPerKgFee());
        rule.setFreeThreshold(request.getFreeThreshold());
        rule.setMaxWeight(request.getMaxWeight());
        rule.setMaxDistance(request.getMaxDistance());
        rule.setCourierShareRate(request.getCourierShareRate() != null
                ? request.getCourierShareRate() : DEFAULT_COURIER_SHARE_RATE);
        rule.setCourierPerOrder(request.getCourierPerOrder() != null
                ? request.getCourierPerOrder() : DEFAULT_COURIER_PER_ORDER);
        rule.setTimeoutMinutes(request.getTimeoutMinutes() != null
                ? request.getTimeoutMinutes() : DEFAULT_TIMEOUT_MINUTES);
        rule.setPropertyCompanyId(propertyCompanyId);
        rule.setStatus("active");
        deliveryRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public DeliveryRuleResponse updateRule(String id, UpdateDeliveryRuleRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateRuleId(id);

        DeliveryRule rule = getRuleInScope(id);
        if (StringUtils.hasText(request.getName())) {
            rule.setName(request.getName());
        }
        if (request.getBaseFee() != null) {
            rule.setBaseFee(request.getBaseFee());
        }
        if (request.getPerKgFee() != null) {
            rule.setPerKgFee(request.getPerKgFee());
        }
        if (request.getFreeThreshold() != null) {
            rule.setFreeThreshold(request.getFreeThreshold());
        }
        if (request.getMaxWeight() != null) {
            rule.setMaxWeight(request.getMaxWeight());
        }
        if (request.getMaxDistance() != null) {
            rule.setMaxDistance(request.getMaxDistance());
        }
        if (request.getCourierShareRate() != null) {
            rule.setCourierShareRate(request.getCourierShareRate());
        }
        if (request.getCourierPerOrder() != null) {
            rule.setCourierPerOrder(request.getCourierPerOrder());
        }
        if (request.getTimeoutMinutes() != null) {
            rule.setTimeoutMinutes(request.getTimeoutMinutes());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!RoleGuard.DELIVERY_RULE_STATUS.contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
            }
            rule.setStatus(request.getStatus());
        }

        deliveryRuleRepository.save(rule);
        return toResponse(rule);
    }

    private DeliveryRule getRuleInScope(String id) {
        DeliveryRule rule = deliveryRuleRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.DELIVERY_RULE_NOT_FOUND));
        if (roleGuard.isPlatformAdmin()) {
            return rule;
        }
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (currentPropertyCompanyId == null
                || !currentPropertyCompanyId.equals(rule.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.DELIVERY_RULE_NOT_FOUND);
        }
        return rule;
    }

    private String resolvePropertyCompanyId(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(propertyCompanyId) && !propertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return propertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private void validateRuleId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("drl_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配送规则ID格式不正确");
        }
    }

    private DeliveryRuleResponse toResponse(DeliveryRule rule) {
        return DeliveryRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .baseFee(rule.getBaseFee())
                .perKgFee(rule.getPerKgFee())
                .freeThreshold(rule.getFreeThreshold())
                .maxWeight(rule.getMaxWeight())
                .maxDistance(rule.getMaxDistance())
                .courierShareRate(rule.getCourierShareRate())
                .courierPerOrder(rule.getCourierPerOrder())
                .timeoutMinutes(rule.getTimeoutMinutes())
                .propertyCompanyId(rule.getPropertyCompanyId())
                .status(rule.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(rule.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(rule.getUpdatedAt()))
                .build();
    }
}
