package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.platformshare.PlatformEarningRecordItemResponse;
import com.wuye.points.dto.admin.platformshare.PlatformEarningsBalanceResponse;
import com.wuye.points.dto.admin.platformshare.PlatformEarningsStatsResponse;
import com.wuye.points.dto.admin.platformshare.PlatformShareRatesResponse;
import com.wuye.points.dto.admin.platformshare.UpdatePlatformShareRatesRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantWithdrawalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.repository.spec.DistributionSpecifications;
import com.wuye.points.repository.spec.MerchantWithdrawalSpecifications;
import com.wuye.points.repository.spec.WithdrawalRecordSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.config.DistributionProperties;
import com.wuye.points.util.RoleGuard;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 平台管理员分成比例与收益统计服务
 */
@Service
@RequiredArgsConstructor
public class PlatformShareService {

    private static final Set<String> EARNING_TYPES = Set.of("distribution", "delivery", "withdrawal");
    private static final int MAX_PAGE_SIZE = 100;
    /** 分成比例求和容差（2026-09-08 错误码 97006） */
    private static final BigDecimal RATE_TOLERANCE = new BigDecimal("0.0001");

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final DeliveryRepository deliveryRepository;
    private final MerchantWithdrawalRepository merchantWithdrawalRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final RoleGuard roleGuard;
    private final DistributionProperties distributionProperties;

    @Transactional(readOnly = true)
    public PlatformShareRatesResponse getShareRates(String propertyCompanyId) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        if (!StringUtils.hasText(scopedPropertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }
        PropertyCompany company = propertyCompanyRepository.findById(scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        return toRatesResponse(company);
    }

    @Transactional
    public PlatformShareRatesResponse updateShareRates(UpdatePlatformShareRatesRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        String propertyCompanyId = request.getPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId) || !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        if (request.getPlatformShareRate() != null) {
            company.setPlatformShareRate(request.getPlatformShareRate());
        }
        if (request.getPlatformDeliveryShareRate() != null) {
            company.setPlatformDeliveryShareRate(request.getPlatformDeliveryShareRate());
        }
        if (request.getPlatformWithdrawalFeeShareRate() != null) {
            company.setPlatformWithdrawalFeeShareRate(request.getPlatformWithdrawalFeeShareRate());
        }
        if (request.getPropertyShareRate() != null) {
            company.setPropertyShareRate(request.getPropertyShareRate());
        }
        if (request.getCoordinatorShareRate() != null) {
            company.setCoordinatorShareRate(request.getCoordinatorShareRate());
        }
        if (request.getSectorLeaderRate() != null) {
            company.setSectorLeaderRate(request.getSectorLeaderRate());
        }
        if (request.getIndividualLeaderRate() != null) {
            company.setIndividualLeaderRate(request.getIndividualLeaderRate());
        }
        if (request.getCourierShareRate() != null) {
            company.setCourierShareRate(request.getCourierShareRate());
        }
        validateShareRates(company);
        propertyCompanyRepository.save(company);
        return toRatesResponse(company);
    }

    /**
     * 校验平台盘分成比例。
     *
     * <p>平台盘（订单抽佣部分）需在「平台服务费 / 物业公司 / 管理人员 / 快递员」四档之间分完，
     * 四档之和必须恰好为 100%，否则会出现平台盘多分或少分、分账总额与实收对不平。
     * 老板定稿口径为 10% / 50% / 25% / 15%（即 100 元订单抽佣 20 元时对应 2 / 10 / 5 / 3 元）。
     *
     * <p>管理人员档内部再由「区块负责人 / 个体负责人」二次分配，剩余归统筹负责人，
     * 因此二者之和不得超过 100%。
     */
    private void validateShareRates(PropertyCompany company) {
        BigDecimal platform = requireRatio(company.getPlatformShareRate(), "平台服务费分成比例");
        BigDecimal property = requireRatio(company.getPropertyShareRate(), "物业公司分成比例");
        BigDecimal coordinator = requireRatio(company.getCoordinatorShareRate(), "管理人员分成比例");

        if (distributionProperties.isCourierShareFromDeliveryFee()) {
            // B 方案：配送员收入改由配送费核算，平台盘仅剩「平台服务费/物业/管理人员」三档，之和须为100%
            BigDecimal sum = platform.add(property).add(coordinator);
            // 2026-09-08：与物业配置接口统一为错误码 97006（商品平台盘一级三档合计必须为 100%）
            String sumHint = "商品平台盘一级分成比例合计必须为100%（我们公司/物业公司/管理盘，配送员改由配送费核算）";
            if (sum.subtract(BigDecimal.ONE).abs().compareTo(RATE_TOLERANCE) > 0) {
                throw new BizException(ErrorCode.SHARE_RATE_PRIMARY_TOTAL_INVALID,
                        sumHint + "，当前为"
                                + sum.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%");
            }
        } else {
            // A 方案（历史）：快递员分成回到平台盘，四档（平台/物业/管理/快递）之和须为100%
            BigDecimal courier = requireRatio(company.getCourierShareRate(), "快递员分成比例");
            BigDecimal sum = platform.add(property).add(coordinator).add(courier);
            // 2026-09-08：与物业配置接口统一为错误码 97006
            String sumHint = "商品平台盘一级分成比例合计必须为100%（我们公司/物业公司/管理盘/快递员）";
            if (sum.subtract(BigDecimal.ONE).abs().compareTo(RATE_TOLERANCE) > 0) {
                throw new BizException(ErrorCode.SHARE_RATE_PRIMARY_TOTAL_INVALID,
                        sumHint + "，当前为"
                                + sum.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + "%");
            }
        }

        // B 方案：sectorLeaderRate 与 individualLeaderRate 为不同层级级联值（占管理盘 / 占板块毛额），不可相加校验；
        // 仅各自需在 [0,1] 区间。管理盘内 4:3:3 由前端级联换算保证（板块+个体=管理盘60%，个体占板块50%）。
        requireRatio(company.getSectorLeaderRate(), "区块负责人分成比例");
        requireRatio(company.getIndividualLeaderRate(), "个体负责人分成比例");

        // 平台在配送费、提现手续费中的分成同样只能落在 0~100%
        requireRatio(company.getPlatformDeliveryShareRate(), "平台配送费分成比例");
        requireRatio(company.getPlatformWithdrawalFeeShareRate(), "平台提现手续费分成比例");
    }

    private BigDecimal requireRatio(BigDecimal value, String label) {
        if (value == null) {
            throw new BizException(ErrorCode.PARAM_MISSING, label + "不能为空");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, label + "必须在0~100%之间");
        }
        return value;
    }

    @Transactional(readOnly = true)
    public PlatformEarningsStatsResponse getEarningsStats(String propertyCompanyId,
                                                          String startDate,
                                                          String endDate) {
        roleGuard.requireAnyRole("platform_admin");
        validateOptionalPropertyCompanyId(propertyCompanyId);
        LocalDateTime start = parseDate(startDate);
        LocalDateTime end = parseDate(endDate);

        BigDecimal totalPlatformShare = defaultDecimal(
                distributionRecordRepository.sumPlatformShare(propertyCompanyId, start, end));
        BigDecimal totalDeliveryEarning = defaultDecimal(
                deliveryRepository.sumPlatformEarning(propertyCompanyId, start, end));
        BigDecimal merchantWithdrawalShare = defaultDecimal(
                merchantWithdrawalRepository.sumPlatformFeeShare(propertyCompanyId, start, end));
        BigDecimal withdrawalRecordShare = defaultDecimal(
                withdrawalRecordRepository.sumPlatformFeeShare(propertyCompanyId, start, end));
        BigDecimal totalWithdrawalFeeShare = merchantWithdrawalShare.add(withdrawalRecordShare);
        BigDecimal totalEarning = totalPlatformShare
                .add(totalDeliveryEarning)
                .add(totalWithdrawalFeeShare);

        List<PlatformEarningsStatsResponse.ByPropertyItem> byProperty =
                buildByProperty(propertyCompanyId, start, end);

        return PlatformEarningsStatsResponse.builder()
                .totalPlatformShare(totalPlatformShare)
                .totalDeliveryEarning(totalDeliveryEarning)
                .totalWithdrawalFeeShare(totalWithdrawalFeeShare)
                .totalEarning(totalEarning)
                .byProperty(byProperty)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PlatformEarningRecordItemResponse> listEarningRecords(
            String propertyCompanyId,
            String type,
            int page,
            int pageSize,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("platform_admin");
        if (StringUtils.hasText(type) && !EARNING_TYPES.contains(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "type值无效，可选: distribution/delivery/withdrawal");
        }
        validateOptionalPropertyCompanyId(propertyCompanyId);
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        LocalDateTime start = parseDate(startDate);
        LocalDateTime end = parseDate(endDate);

        List<PlatformEarningRecordItemResponse> merged = new ArrayList<>();

        if (!StringUtils.hasText(type) || "distribution".equals(type)) {
            List<DistributionRecord> records = distributionRecordRepository.findAll(
                    DistributionSpecifications.withFilters(
                            propertyCompanyId, null, null, null, null, null, start, end));
            for (DistributionRecord record : records) {
                merged.add(PlatformEarningRecordItemResponse.builder()
                        .id(record.getId())
                        .type("distribution")
                        .propertyCompanyId(record.getPropertyCompanyId())
                        .amount(defaultDecimal(record.getPlatformShare()))
                        .orderId(record.getOrderId())
                        .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                        .build());
            }
        }

        if (!StringUtils.hasText(type) || "delivery".equals(type)) {
            List<Delivery> records = deliveryRepository.findAll(
                    buildDeliverySpec(propertyCompanyId, start, end));
            for (Delivery record : records) {
                merged.add(PlatformEarningRecordItemResponse.builder()
                        .id(record.getId())
                        .type("delivery")
                        .propertyCompanyId(record.getPropertyCompanyId())
                        .amount(defaultDecimal(record.getPlatformEarning()))
                        .orderId(record.getOrderId())
                        .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                        .build());
            }
        }

        if (!StringUtils.hasText(type) || "withdrawal".equals(type)) {
            List<MerchantWithdrawal> merchantRecords = merchantWithdrawalRepository.findAll(
                    MerchantWithdrawalSpecifications.withFilters(
                            propertyCompanyId, null, null, start, end));
            for (MerchantWithdrawal record : merchantRecords) {
                merged.add(PlatformEarningRecordItemResponse.builder()
                        .id(record.getId())
                        .type("withdrawal")
                        .propertyCompanyId(record.getPropertyCompanyId())
                        .amount(defaultDecimal(record.getPlatformFeeShare()))
                        .withdrawalId(record.getId())
                        .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                        .build());
            }
            List<WithdrawalRecord> withdrawalRecords = withdrawalRecordRepository.findAll(
                    WithdrawalRecordSpecifications.withFilters(
                            null, null, propertyCompanyId, null, start, end));
            for (WithdrawalRecord record : withdrawalRecords) {
                merged.add(PlatformEarningRecordItemResponse.builder()
                        .id(record.getId())
                        .type("withdrawal")
                        .propertyCompanyId(record.getPropertyCompanyId())
                        .amount(defaultDecimal(record.getPlatformFeeShare()))
                        .withdrawalId(record.getId())
                        .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                        .build());
            }
        }

        fillPropertyName(merged);

        merged.sort(Comparator.comparing(
                PlatformEarningRecordItemResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        int total = merged.size();
        int fromIndex = Math.min((safePage - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        List<PlatformEarningRecordItemResponse> pageList = merged.subList(fromIndex, toIndex);

        return PageResponse.of(pageList, safePage, safePageSize, total);
    }

    /**
     * 平台收益余额 / 对账快照（只读）。
     *
     * <p>微信 真分账模式下，平台抽成由微信直接划入平台账户、实时到账，无内部待提钱包。
     * 本方法聚合「订单分成 + 配送费分成 + 提现手续费分成」全量合计，仅用于前端看账与对照流水，
     * 不落库、不触发提现。
     */
    @Transactional(readOnly = true)
    public PlatformEarningsBalanceResponse getEarningsBalance(String propertyCompanyId) {
        roleGuard.requireAnyRole("platform_admin");
        validateOptionalPropertyCompanyId(propertyCompanyId);

        BigDecimal totalPlatformShare = defaultDecimal(
                distributionRecordRepository.sumPlatformShare(propertyCompanyId, null, null));
        BigDecimal totalDeliveryEarning = defaultDecimal(
                deliveryRepository.sumPlatformEarning(propertyCompanyId, null, null));
        BigDecimal merchantWithdrawalShare = defaultDecimal(
                merchantWithdrawalRepository.sumPlatformFeeShare(propertyCompanyId, null, null));
        BigDecimal withdrawalRecordShare = defaultDecimal(
                withdrawalRecordRepository.sumPlatformFeeShare(propertyCompanyId, null, null));
        BigDecimal totalWithdrawalFeeShare = merchantWithdrawalShare.add(withdrawalRecordShare);
        BigDecimal totalEarned = totalPlatformShare
                .add(totalDeliveryEarning)
                .add(totalWithdrawalFeeShare);

        // 微信 真分账：平台抽成由微信直接划入平台账户，无内部待提钱包，withdrawAvailable=false
        return PlatformEarningsBalanceResponse.builder()
                .totalEarned(totalEarned)
                .settledBalance(totalEarned)
                .pendingAmount(BigDecimal.ZERO)
                .settleMode("wechat_profit_sharing")
                .settlementStatus("微信 自动结算·已实时入账平台账户")
                .withdrawAvailable(false)
                .distributionShare(totalPlatformShare)
                .deliveryShare(totalDeliveryEarning)
                .withdrawalFeeShare(totalWithdrawalFeeShare)
                .build();
    }

    // ============== 私有工具 ==============

    private List<PlatformEarningsStatsResponse.ByPropertyItem> buildByProperty(
            String propertyCompanyId, LocalDateTime start, LocalDateTime end) {
        List<PropertyCompany> companies;
        if (StringUtils.hasText(propertyCompanyId)) {
            companies = propertyCompanyRepository.findById(propertyCompanyId)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            companies = propertyCompanyRepository.findAll();
        }

        List<PlatformEarningsStatsResponse.ByPropertyItem> result = new ArrayList<>();
        for (PropertyCompany company : companies) {
            String pcId = company.getId();
            BigDecimal platformShare = defaultDecimal(
                    distributionRecordRepository.sumPlatformShare(pcId, start, end));
            BigDecimal deliveryEarning = defaultDecimal(
                    deliveryRepository.sumPlatformEarning(pcId, start, end));
            BigDecimal merchantShare = defaultDecimal(
                    merchantWithdrawalRepository.sumPlatformFeeShare(pcId, start, end));
            BigDecimal withdrawalShare = defaultDecimal(
                    withdrawalRecordRepository.sumPlatformFeeShare(pcId, start, end));
            BigDecimal withdrawalFeeShare = merchantShare.add(withdrawalShare);

            if (platformShare.compareTo(BigDecimal.ZERO) == 0
                    && deliveryEarning.compareTo(BigDecimal.ZERO) == 0
                    && withdrawalFeeShare.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            result.add(PlatformEarningsStatsResponse.ByPropertyItem.builder()
                    .propertyCompanyId(pcId)
                    .propertyName(company.getName())
                    .platformShare(platformShare)
                    .deliveryEarning(deliveryEarning)
                    .withdrawalFeeShare(withdrawalFeeShare)
                    .build());
        }
        return result;
    }

    private void fillPropertyName(List<PlatformEarningRecordItemResponse> items) {
        Set<String> propertyCompanyIds = items.stream()
                .map(PlatformEarningRecordItemResponse::getPropertyCompanyId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (propertyCompanyIds.isEmpty()) {
            return;
        }
        Map<String, PropertyCompany> companyMap = propertyCompanyRepository
                .findAllById(propertyCompanyIds).stream()
                .collect(Collectors.toMap(PropertyCompany::getId, Function.identity()));
        for (PlatformEarningRecordItemResponse item : items) {
            PropertyCompany company = companyMap.get(item.getPropertyCompanyId());
            if (company != null) {
                item.setPropertyName(company.getName());
            }
        }
    }

    private Specification<Delivery> buildDeliverySpec(String propertyCompanyId,
                                                      LocalDateTime start,
                                                      LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PlatformShareRatesResponse toRatesResponse(PropertyCompany company) {
        return PlatformShareRatesResponse.builder()
                .propertyCompanyId(company.getId())
                .platformShareRate(company.getPlatformShareRate())
                .platformDeliveryShareRate(company.getPlatformDeliveryShareRate())
                .platformWithdrawalFeeShareRate(company.getPlatformWithdrawalFeeShareRate())
                .propertyShareRate(company.getPropertyShareRate())
                .coordinatorShareRate(company.getCoordinatorShareRate())
                .sectorLeaderRate(company.getSectorLeaderRate())
                .individualLeaderRate(company.getIndividualLeaderRate())
                .courierShareRate(company.getCourierShareRate())
                .build();
    }

    private String resolveScopedPropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)
                    && !requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
    }

    private void validateOptionalPropertyCompanyId(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId) && !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
    }

    private LocalDateTime parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String[] dateTimePatterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss"
        };
        for (String pattern : dateTimePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }
        String[] datePatterns = {"yyyy-MM-dd", "yyyy/MM/dd"};
        for (String pattern : datePatterns) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(
                        value, DateTimeFormatter.ofPattern(pattern));
                return date.atStartOfDay();
            } catch (Exception ignored) {
            }
        }
        throw new BizException(ErrorCode.PARAM_INVALID,
                "日期格式不正确，支持：yyyy-MM-dd、yyyy/MM/dd、yyyy-MM-dd HH:mm:ss 或 ISO8601格式");
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
