package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.point.GrantPointRequest;
import com.wuye.points.dto.merchant.point.GrantPointResponse;
import com.wuye.points.dto.merchant.point.PurchasePointRequest;
import com.wuye.points.dto.merchant.point.PurchasePointResponse;
import com.wuye.points.dto.merchant.point.PurchaseRecordItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPointGrant;
import com.wuye.points.entity.MerchantPointPurchase;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantPointGrantRepository;
import com.wuye.points.repository.MerchantPointPurchaseRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.MerchantPointPurchaseSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MerchantPointService {

    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");
    private static final Set<String> PURCHASE_AUDIT_STATUS = Set.of("pending", "approved", "rejected");

    private final MerchantPointPurchaseRepository merchantPointPurchaseRepository;
    private final MerchantPointGrantRepository merchantPointGrantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final ResidentRepository residentRepository;
    private final PointService pointService;
    private final MerchantSupport merchantSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public PurchasePointResponse purchase(PurchasePointRequest request) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();

        MerchantPointPurchase purchase = new MerchantPointPurchase();
        purchase.setId(idGenerator.generateId("mpp"));
        purchase.setMerchantId(merchant.getId());
        purchase.setPropertyCompanyId(merchant.getPropertyCompanyId());
        purchase.setPointAmount(request.getPointAmount());
        purchase.setPayAmount(request.getPayAmount());
        purchase.setAuditStatus("pending");
        purchase.setRemainingPoints(0);
        merchantPointPurchaseRepository.save(purchase);

        return PurchasePointResponse.builder()
                .id(purchase.getId())
                .merchantId(purchase.getMerchantId())
                .pointAmount(purchase.getPointAmount())
                .payAmount(purchase.getPayAmount())
                .status(purchase.getAuditStatus())
                .createdAt(DateTimeUtil.toIsoUtc(purchase.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseRecordItemResponse> listPurchases(
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(auditStatus) && !PURCHASE_AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }

        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<MerchantPointPurchase> purchasePage = merchantPointPurchaseRepository.findAll(
                MerchantPointPurchaseSpecifications.withFilters(
                        merchant.getPropertyCompanyId(), merchant.getId(), auditStatus, start, end),
                pageable);

        return PageResponse.of(
                purchasePage.getContent().stream().map(this::toPurchaseRecordItem).toList(),
                page,
                pageSize,
                purchasePage.getTotalElements());
    }

    @Transactional
    public GrantPointResponse grant(GrantPointRequest request) {
        Merchant merchant = merchantSupport.requireCurrentMerchant();
        if (!request.getPurchaseId().startsWith("mpp_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "purchaseId格式不正确");
        }
        if (!request.getResidentId().startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }

        MerchantPointPurchase purchase = merchantPointPurchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "购买记录不存在"));
        if (!merchant.getId().equals(purchase.getMerchantId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (!"approved".equals(purchase.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_POINT_PURCHASE_AUDITING);
        }

        int remaining = purchase.getRemainingPoints() != null ? purchase.getRemainingPoints() : 0;
        if (remaining < request.getPointAmount()) {
            throw new BizException(ErrorCode.MERCHANT_POINT_INSUFFICIENT);
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!merchant.getPropertyCompanyId().equals(resident.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "住户与商家不属于同一物业公司");
        }

        BigDecimal exchangeRate = resolveExchangeRate(merchant.getId(), merchant.getPropertyCompanyId());
        String description = StringUtils.hasText(request.getDescription())
                ? request.getDescription()
                : "商家线下消费返积分";

        purchase.setRemainingPoints(remaining - request.getPointAmount());
        merchantPointPurchaseRepository.save(purchase);

        PointRecord pointRecord = pointService.earnPoints(
                resident,
                request.getPointAmount(),
                "merchant_grant",
                purchase.getId(),
                description,
                UserContext.currentUserId());

        MerchantPointGrant grant = new MerchantPointGrant();
        grant.setId(idGenerator.generateId("mpg"));
        grant.setMerchantId(merchant.getId());
        grant.setPurchaseId(purchase.getId());
        grant.setResidentId(resident.getId());
        grant.setFamilyId(resident.getFamilyId());
        grant.setPropertyCompanyId(merchant.getPropertyCompanyId());
        grant.setPointAmount(request.getPointAmount());
        grant.setExchangeRate(exchangeRate);
        grant.setDescription(description);
        merchantPointGrantRepository.save(grant);

        return GrantPointResponse.builder()
                .id(grant.getId())
                .merchantId(merchant.getId())
                .purchaseId(purchase.getId())
                .residentId(resident.getId())
                .pointAmount(request.getPointAmount())
                .exchangeRate(exchangeRate)
                .purchaseRemainingPoints(purchase.getRemainingPoints())
                .residentBalanceAfter(pointRecord.getBalance())
                .createdAt(DateTimeUtil.toIsoUtc(grant.getCreatedAt()))
                .build();
    }

    private PurchaseRecordItemResponse toPurchaseRecordItem(MerchantPointPurchase purchase) {
        return PurchaseRecordItemResponse.builder()
                .id(purchase.getId())
                .pointAmount(purchase.getPointAmount())
                .payAmount(purchase.getPayAmount())
                .status(purchase.getAuditStatus())
                .remainingPoints(purchase.getRemainingPoints())
                .auditedAt(DateTimeUtil.toIsoUtc(purchase.getAuditedAt()))
                .auditorId(purchase.getAuditorId())
                .createdAt(DateTimeUtil.toIsoUtc(purchase.getCreatedAt()))
                .build();
    }

    private BigDecimal resolveExchangeRate(String merchantId, String propertyCompanyId) {
        return merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchantId, propertyCompanyId)
                .map(MerchantPropertyConfig::getPointExchangeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_POINT_EXCHANGE_RATE);
    }

    private Pageable buildPageable(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = parseDate(value);
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = parseDate(value);
        return date != null ? date.plusDays(1).atStartOfDay() : null;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式应为YYYY-MM-DD");
        }
    }
}
