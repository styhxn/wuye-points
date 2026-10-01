package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.propertybankcard.PropertyBankCardItemResponse;
import com.wuye.points.entity.PropertyBankCard;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyBankCardRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SensitiveMasker;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 物业公司收款银行卡维护。
 */
@Service
@RequiredArgsConstructor
public class AdminPropertyBankCardService {

    private final PropertyBankCardRepository propertyBankCardRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<PropertyBankCardItemResponse> list() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return propertyBankCardRepository
                .findByPropertyCompanyIdOrderByIsDefaultDescCreatedAtDesc(UserContext.currentPropertyCompanyId())
                .stream().map(this::toItem).toList();
    }

    @Transactional
    public PropertyBankCardItemResponse create(PropertyBankCard request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validate(request);
        PropertyBankCard card = new PropertyBankCard();
        card.setId(idGenerator.generateId("pbc"));
        card.setPropertyCompanyId(UserContext.currentPropertyCompanyId());
        apply(card, request);
        if (Boolean.TRUE.equals(card.getIsDefault())) {
            clearDefault(card.getPropertyCompanyId(), null);
        }
        return toItem(propertyBankCardRepository.save(card));
    }

    @Transactional
    public PropertyBankCardItemResponse update(String id, PropertyBankCard request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyBankCard card = propertyBankCardRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "银行卡不存在"));
        if (request.getBankName() != null) {
            card.setBankName(request.getBankName());
        }
        if (request.getBranchName() != null) {
            card.setBranchName(request.getBranchName());
        }
        if (request.getAccountName() != null) {
            card.setAccountName(request.getAccountName());
        }
        if (StringUtils.hasText(request.getAccountNo())) {
            card.setAccountNo(request.getAccountNo());
        }
        if (request.getStatus() != null) {
            card.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            card.setRemark(request.getRemark());
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(card.getPropertyCompanyId(), card.getId());
            card.setIsDefault(true);
        } else if (request.getIsDefault() != null) {
            card.setIsDefault(false);
        }
        return toItem(propertyBankCardRepository.save(card));
    }

    @Transactional
    public void delete(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        propertyBankCardRepository.deleteById(id);
    }

    private void apply(PropertyBankCard card, PropertyBankCard request) {
        card.setBankName(request.getBankName());
        card.setBranchName(request.getBranchName());
        card.setAccountName(request.getAccountName());
        card.setAccountNo(request.getAccountNo());
        card.setPurpose(StringUtils.hasText(request.getPurpose()) ? request.getPurpose() : "points");
        card.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        card.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        card.setRemark(request.getRemark());
    }

    private void clearDefault(String propertyCompanyId, String exceptId) {
        propertyBankCardRepository
                .findFirstByPropertyCompanyIdAndIsDefaultTrueAndStatus(propertyCompanyId, "active")
                .ifPresent(card -> {
                    if (!card.getId().equals(exceptId)) {
                        card.setIsDefault(false);
                        propertyBankCardRepository.save(card);
                    }
                });
    }

    private void validate(PropertyBankCard request) {
        if (!StringUtils.hasText(request.getBankName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "开户行不能为空");
        }
        if (!StringUtils.hasText(request.getAccountName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "户名不能为空");
        }
        if (!StringUtils.hasText(request.getAccountNo())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "账号不能为空");
        }
        if (StringUtils.hasText(request.getPurpose())
                && !Set.of("points", "reward").contains(request.getPurpose())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "purpose 必须为 points(积分钱) 或 reward(奖励金)");
        }
    }

    private PropertyBankCardItemResponse toItem(PropertyBankCard card) {
        return PropertyBankCardItemResponse.builder()
                .id(card.getId())
                .propertyCompanyId(card.getPropertyCompanyId())
                .bankName(card.getBankName())
                .branchName(card.getBranchName())
                .accountName(card.getAccountName())
                .accountNoMasked(SensitiveMasker.maskAccountNo(card.getAccountNo()))
                .purpose(card.getPurpose())
                .isDefault(card.getIsDefault())
                .status(card.getStatus())
                .remark(card.getRemark())
                .createdAt(card.getCreatedAt())
                .build();
    }
}
