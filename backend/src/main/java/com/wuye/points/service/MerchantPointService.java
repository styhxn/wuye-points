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
import com.wuye.points.support.MoneySupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.MerchantSupport;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantPointService {

    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");
    private static final Set<String> PURCHASE_AUDIT_STATUS = Set.of("pending", "pending_audit", "approved", "rejected");

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
        if ("pending_audit".equals(auditStatus)) {
            auditStatus = "pending";
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

    /**
     * 商家积分消费扣减（如用积分支付广告位）。
     *
     * <p>口径：入参为「应付金额（元）」，按商家挂接配置的积分兑换比折算为积分
     * （{@code 1元 = X积分}，默认 100），取整沿用全站统一口径
     * {@link MoneySupport#toPointsForConsume}（消费方向向上取整，绝不会少扣）。
     * 随后按购买记录时间顺序（FIFO）扣减已审核通过记录的 {@code remainingPoints}；
     * 余额不足抛出 {@link ErrorCode#MERCHANT_POINT_INSUFFICIENT}，由事务整体回滚。</p>
     * <p>并发安全：查询带行级写锁（{@code PESSIMISTIC_WRITE}），保证「校验 + 扣减」原子，防止并发透支。</p>
     *
     * @param merchantId        商家ID
     * @param propertyCompanyId 物业公司ID（决定积分兑换比）
     * @param payAmountYuan     应付金额（元）
     * @param reason            消费事由（仅记录日志，便于对账追溯）
     */
    @Transactional
    public void spendPoints(String merchantId, String propertyCompanyId, BigDecimal payAmountYuan, String reason) {
        if (payAmountYuan == null || payAmountYuan.compareTo(BigDecimal.ZERO) <= 0) {
            return; // 0 元（如免费额度）无需扣减
        }
        BigDecimal rate = resolveExchangeRate(merchantId, propertyCompanyId);
        int pointsNeeded = MoneySupport.toPointsForConsume(payAmountYuan, rate);
        if (pointsNeeded <= 0) {
            return;
        }

        int remaining = pointsNeeded;
        for (MerchantPointPurchase purchase : merchantPointPurchaseRepository
                .findApprovedWithRemainingForUpdate(merchantId)) {
            if (remaining <= 0) {
                break;
            }
            int available = purchase.getRemainingPoints() != null ? purchase.getRemainingPoints() : 0;
            if (available <= 0) {
                continue;
            }
            int deduct = Math.min(available, remaining);
            purchase.setRemainingPoints(available - deduct);
            merchantPointPurchaseRepository.save(purchase);
            remaining -= deduct;
        }

        if (remaining > 0) {
            // 余额不足：抛异常回滚本次事务（含已扣减的记录），避免部分扣减的脏数据
            throw new BizException(ErrorCode.MERCHANT_POINT_INSUFFICIENT);
        }
        log.info("[商家积分] 消费扣减 merchantId={} propertyCompanyId={} payAmount={}元 points={} rate={} reason={}",
                merchantId, propertyCompanyId, payAmountYuan, pointsNeeded, rate, reason);
    }
}
