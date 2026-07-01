package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distribution.DistributionCalculateResponse;
import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointPoolRecordRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.spec.DistributionSpecifications;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistributionService {

    private static final BigDecimal DEFAULT_PROPERTY_SHARE = new BigDecimal("0.3000");
    private static final BigDecimal DEFAULT_COORDINATOR_SHARE = new BigDecimal("0.7000");
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_INDIVIDUAL_LEADER_RATE = new BigDecimal("0.0000");
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");
    private static final int MONEY_SCALE = 2;

    private final DistributionRecordRepository distributionRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final MerchantRepository merchantRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final PointPoolRepository pointPoolRepository;
    private final PointPoolRecordRepository pointPoolRecordRepository;
    private final DistributionConverter distributionConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public DistributionRecord calculateAndSave(Order order, Resident resident, MerchantPropertyConfig config) {
        Optional<DistributionRecord> existing = distributionRecordRepository.findFirstByOrderId(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        ShareBreakdown breakdown = buildShareBreakdown(order, resident, config);
        DistributionRecord record = toRecord(order, resident, config.getCommissionRate(), breakdown);
        distributionRecordRepository.save(record);

        if (breakdown.distributableAmount().compareTo(BigDecimal.ZERO) < 0) {
            absorbPoolDiff(resident.getPropertyCompanyId(), breakdown.distributableAmount(), order.getId());
        }
        return record;
    }

    @Transactional
    public void reverseForCancelledOrder(String orderId) {
        DistributionRecord original = distributionRecordRepository.findFirstByOrderId(orderId).orElse(null);
        if (original == null) {
            return;
        }
        if (original.getDistributableAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        DistributionRecord reversal = new DistributionRecord();
        reversal.setId(idGenerator.generateId("dr"));
        reversal.setOrderId(original.getOrderId());
        reversal.setMerchantId(original.getMerchantId());
        reversal.setPropertyCompanyId(original.getPropertyCompanyId());
        reversal.setResidentId(original.getResidentId());
        reversal.setTotalAmount(negate(original.getTotalAmount()));
        reversal.setCommissionRate(original.getCommissionRate());
        reversal.setPointCost(negate(original.getPointCost()));
        reversal.setCoinCost(negate(original.getCoinCost()));
        reversal.setDistributableAmount(negate(original.getDistributableAmount()));
        reversal.setPropertyShare(negate(original.getPropertyShare()));
        reversal.setCoordinatorShare(negate(original.getCoordinatorShare()));
        reversal.setSectorLeaderShare(negate(original.getSectorLeaderShare()));
        reversal.setIndividualLeaderShare(negate(original.getIndividualLeaderShare()));
        reversal.setCoordinatorId(original.getCoordinatorId());
        reversal.setSectorLeaderId(original.getSectorLeaderId());
        reversal.setIndividualLeaderId(original.getIndividualLeaderId());
        distributionRecordRepository.save(reversal);
    }

    @Transactional(readOnly = true)
    public PageResponse<DistributionRecordItemResponse> listRecords(
            int page,
            int pageSize,
            String orderId,
            String merchantId,
            String coordinatorId,
            String sectorLeaderId,
            String individualLeaderId,
            String propertyCompanyId,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOptionalId(orderId, "ord_");
        validateOptionalId(merchantId, "mch_");
        validateOptionalId(coordinatorId, "coo_");
        validateOptionalId(sectorLeaderId, "sl_");
        validateOptionalId(individualLeaderId, "il_");

        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        String scopedCoordinatorId = resolveScopedCoordinatorId(coordinatorId);

        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime end = parseIsoDate(endDate);

        Page<DistributionRecord> recordPage = distributionRecordRepository.findAll(
                DistributionSpecifications.withFilters(
                        scopedPropertyCompanyId,
                        orderId,
                        merchantId,
                        scopedCoordinatorId,
                        sectorLeaderId,
                        individualLeaderId,
                        start,
                        end),
                pageable);

        return buildRecordPage(recordPage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public DistributionStatsResponse getStats(
            String startDate,
            String endDate,
            String dimension,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "platform_admin");

        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime end = parseIsoDate(endDate);

        if (roleGuard.isCoordinator()) {
            Coordinator coordinator = resolveCurrentCoordinator();
            scopedPropertyCompanyId = coordinator.getPropertyCompanyId();
            return buildStats(scopedPropertyCompanyId, coordinator.getId(), start, end, dimension);
        }

        return buildStats(scopedPropertyCompanyId, null, start, end, dimension);
    }

    @Transactional(readOnly = true)
    public DistributionCalculateResponse calculatePreview(
            String merchantId,
            BigDecimal totalAmount,
            Integer pointUsed,
            BigDecimal coinUsed,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "platform_admin", "merchant", "resident");
        validateOptionalId(merchantId, "mch_");
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "totalAmount不合法");
        }

        String scopedPropertyCompanyId = resolvePreviewPropertyCompanyId(propertyCompanyId);
        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchantId, scopedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家在该物业下未完成挂接配置"));

        Resident resident = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        Order previewOrder = new Order();
        previewOrder.setMerchantId(merchantId);
        previewOrder.setTotalAmount(totalAmount);
        previewOrder.setPointUsed(pointUsed != null ? pointUsed : 0);
        previewOrder.setCoinUsed(coinUsed != null ? coinUsed : BigDecimal.ZERO);

        ShareBreakdown breakdown = buildShareBreakdown(previewOrder, resident, config);
        return DistributionCalculateResponse.builder()
                .totalAmount(totalAmount)
                .commissionRate(config.getCommissionRate())
                .pointCost(breakdown.pointCost())
                .coinCost(breakdown.coinCost())
                .distributableAmount(breakdown.distributableAmount())
                .propertyShare(breakdown.propertyShare())
                .coordinatorShare(breakdown.coordinatorShare())
                .sectorLeaderShare(breakdown.sectorLeaderShare())
                .individualLeaderShare(breakdown.individualLeaderShare())
                .coordinatorId(breakdown.coordinatorId())
                .sectorLeaderId(breakdown.sectorLeaderId())
                .individualLeaderId(breakdown.individualLeaderId())
                .build();
    }

    private ShareBreakdown buildShareBreakdown(Order order, Resident resident, MerchantPropertyConfig config) {
        String propertyCompanyId = resident.getPropertyCompanyId();
        PropertyCompany propertyCompany = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.SYSTEM_ERROR));

        BigDecimal exchangeRate = config.getPointExchangeRate() != null
                ? config.getPointExchangeRate()
                : DEFAULT_POINT_EXCHANGE_RATE;
        int pointUsed = order.getPointUsed() != null ? order.getPointUsed() : 0;
        BigDecimal coinUsed = defaultDecimal(order.getCoinUsed());

        BigDecimal pointCost = BigDecimal.valueOf(pointUsed)
                .divide(exchangeRate, MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal coinCost = coinUsed;
        BigDecimal totalAmount = defaultDecimal(order.getTotalAmount());
        BigDecimal commissionRate = config.getCommissionRate();

        BigDecimal distributableAmount = totalAmount.multiply(commissionRate)
                .subtract(pointCost)
                .subtract(coinCost)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (distributableAmount.compareTo(BigDecimal.ZERO) < 0) {
            return new ShareBreakdown(
                    pointCost, coinCost, distributableAmount,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null);
        }

        BigDecimal propertyRate = defaultRate(propertyCompany.getPropertyShareRate(), DEFAULT_PROPERTY_SHARE);
        BigDecimal coordinatorRate = defaultRate(propertyCompany.getCoordinatorShareRate(), DEFAULT_COORDINATOR_SHARE);
        BigDecimal sectorLeaderRate = defaultRate(propertyCompany.getSectorLeaderRate(), DEFAULT_SECTOR_LEADER_RATE);
        BigDecimal individualLeaderRate = defaultRate(propertyCompany.getIndividualLeaderRate(), DEFAULT_INDIVIDUAL_LEADER_RATE);

        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        String sector = resolveMerchantSector(merchant);

        Coordinator coordinator = coordinatorRepository
                .findFirstByPropertyCompanyIdAndStatusOrderByCommissionRateDesc(propertyCompanyId, "active")
                .orElse(null);

        if (coordinator == null) {
            return new ShareBreakdown(
                    pointCost, coinCost, distributableAmount,
                    distributableAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null);
        }

        BigDecimal propertyShare = money(distributableAmount.multiply(propertyRate));
        BigDecimal coordinatorRaw = money(distributableAmount.multiply(coordinatorRate));

        SectorLeader sectorLeader = sectorLeaderRepository
                .findFirstByPropertyCompanyIdAndSectorAndCoordinatorIdAndStatus(
                        propertyCompanyId, sector, coordinator.getId(), "active")
                .orElse(null);

        BigDecimal sectorLeaderGross = sectorLeader != null
                ? money(coordinatorRaw.multiply(sectorLeaderRate))
                : BigDecimal.ZERO;

        IndividualLeader individualLeader = sectorLeader != null
                ? individualLeaderRepository.findFirstBySectorLeaderIdAndSectorAndStatus(
                        sectorLeader.getId(), sector, "active").orElse(null)
                : null;

        BigDecimal individualLeaderShare = individualLeader != null
                ? money(sectorLeaderGross.multiply(individualLeaderRate))
                : BigDecimal.ZERO;
        BigDecimal sectorLeaderShare = money(sectorLeaderGross.subtract(individualLeaderShare));
        BigDecimal coordinatorShare = money(coordinatorRaw.subtract(sectorLeaderGross));

        return new ShareBreakdown(
                pointCost, coinCost, distributableAmount,
                propertyShare, coordinatorShare, sectorLeaderShare, individualLeaderShare,
                coordinator.getId(),
                sectorLeader != null ? sectorLeader.getId() : null,
                individualLeader != null ? individualLeader.getId() : null);
    }

    private DistributionRecord toRecord(Order order,
                                        Resident resident,
                                        BigDecimal commissionRate,
                                        ShareBreakdown breakdown) {
        DistributionRecord record = new DistributionRecord();
        record.setId(idGenerator.generateId("dr"));
        record.setOrderId(order.getId());
        record.setMerchantId(order.getMerchantId());
        record.setPropertyCompanyId(resident.getPropertyCompanyId());
        record.setResidentId(resident.getId());
        record.setTotalAmount(defaultDecimal(order.getTotalAmount()));
        record.setCommissionRate(commissionRate);
        record.setPointCost(breakdown.pointCost());
        record.setCoinCost(breakdown.coinCost());
        record.setDistributableAmount(breakdown.distributableAmount());
        record.setPropertyShare(breakdown.propertyShare());
        record.setCoordinatorShare(breakdown.coordinatorShare());
        record.setSectorLeaderShare(breakdown.sectorLeaderShare());
        record.setIndividualLeaderShare(breakdown.individualLeaderShare());
        record.setCoordinatorId(breakdown.coordinatorId());
        record.setSectorLeaderId(breakdown.sectorLeaderId());
        record.setIndividualLeaderId(breakdown.individualLeaderId());
        return record;
    }

    private DistributionStatsResponse buildStats(String propertyCompanyId,
                                                 String coordinatorId,
                                                 LocalDateTime start,
                                                 LocalDateTime end,
                                                 String dimension) {
        DistributionStatsResponse.Summary summary = DistributionStatsResponse.Summary.builder()
                .totalDistributableAmount(defaultDecimal(distributionRecordRepository.sumDistributableAmount(
                        propertyCompanyId, start, end)))
                .propertyAmount(defaultDecimal(distributionRecordRepository.sumPropertyShare(
                        propertyCompanyId, start, end)))
                .coordinatorAmount(defaultDecimal(distributionRecordRepository.sumCoordinatorShare(
                        propertyCompanyId, start, end)))
                .sectorLeaderAmount(defaultDecimal(distributionRecordRepository.sumSectorLeaderShare(
                        propertyCompanyId, start, end)))
                .individualLeaderAmount(defaultDecimal(distributionRecordRepository.sumIndividualLeaderShare(
                        propertyCompanyId, start, end)))
                .build();

        if (!"detail".equals(dimension)) {
            return DistributionStatsResponse.builder()
                    .summary(summary)
                    .byProperty(List.of())
                    .byCoordinator(List.of())
                    .bySector(List.of())
                    .build();
        }

        List<DistributionRecord> records = distributionRecordRepository.findAll(
                DistributionSpecifications.withFilters(
                        propertyCompanyId,
                        null,
                        null,
                        coordinatorId,
                        null,
                        null,
                        start,
                        end));

        return DistributionStatsResponse.builder()
                .summary(summary)
                .byProperty(buildByProperty(records))
                .byCoordinator(buildByCoordinator(records))
                .bySector(buildBySector(records))
                .build();
    }

    private List<DistributionStatsResponse.ByPropertyItem> buildByProperty(List<DistributionRecord> records) {
        Map<String, BigDecimal> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        DistributionRecord::getPropertyCompanyId,
                        Collectors.mapping(DistributionRecord::getPropertyShare,
                                Collectors.reducing(BigDecimal.ZERO, this::defaultDecimal, BigDecimal::add))));

        Map<String, PropertyCompany> companyMap = propertyCompanyRepository.findAllById(grouped.keySet()).stream()
                .collect(Collectors.toMap(PropertyCompany::getId, Function.identity()));

        return grouped.entrySet().stream()
                .map(entry -> DistributionStatsResponse.ByPropertyItem.builder()
                        .propertyCompanyId(entry.getKey())
                        .propertyName(Optional.ofNullable(companyMap.get(entry.getKey()))
                                .map(PropertyCompany::getName).orElse(null))
                        .amount(entry.getValue())
                        .build())
                .toList();
    }

    private List<DistributionStatsResponse.ByCoordinatorItem> buildByCoordinator(List<DistributionRecord> records) {
        Map<String, BigDecimal> grouped = new HashMap<>();
        for (DistributionRecord record : records) {
            if (!StringUtils.hasText(record.getCoordinatorId())) {
                continue;
            }
            grouped.merge(record.getCoordinatorId(),
                    defaultDecimal(record.getCoordinatorShare()), BigDecimal::add);
        }

        Map<String, Coordinator> coordinatorMap = coordinatorRepository.findAllById(grouped.keySet()).stream()
                .collect(Collectors.toMap(Coordinator::getId, Function.identity()));
        Set<String> residentIds = coordinatorMap.values().stream()
                .map(Coordinator::getResidentId)
                .collect(Collectors.toSet());
        Map<String, Resident> residentMap = residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Coordinator coordinator = coordinatorMap.get(entry.getKey());
                    Resident resident = coordinator != null ? residentMap.get(coordinator.getResidentId()) : null;
                    return DistributionStatsResponse.ByCoordinatorItem.builder()
                            .coordinatorId(entry.getKey())
                            .name(resident != null ? resident.getName() : null)
                            .amount(entry.getValue())
                            .build();
                })
                .toList();
    }

    private List<DistributionStatsResponse.BySectorItem> buildBySector(List<DistributionRecord> records) {
        Map<String, BigDecimal> grouped = new HashMap<>();
        for (DistributionRecord record : records) {
            if (!StringUtils.hasText(record.getSectorLeaderId())) {
                continue;
            }
            grouped.merge(record.getSectorLeaderId(),
                    defaultDecimal(record.getSectorLeaderShare()), BigDecimal::add);
        }

        Map<String, SectorLeader> leaderMap = sectorLeaderRepository.findAllById(grouped.keySet()).stream()
                .collect(Collectors.toMap(SectorLeader::getId, Function.identity()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    SectorLeader leader = leaderMap.get(entry.getKey());
                    String sector = leader != null ? leader.getSector() : null;
                    return DistributionStatsResponse.BySectorItem.builder()
                            .sector(sector)
                            .sectorName(distributionConverter.sectorLabel(sector))
                            .amount(entry.getValue())
                            .build();
                })
                .toList();
    }

    private PageResponse<DistributionRecordItemResponse> buildRecordPage(
            Page<DistributionRecord> recordPage,
            int page,
            int pageSize) {
        List<DistributionRecord> records = recordPage.getContent();
        Set<String> orderIds = records.stream().map(DistributionRecord::getOrderId).collect(Collectors.toSet());
        Set<String> merchantIds = records.stream().map(DistributionRecord::getMerchantId).collect(Collectors.toSet());
        Set<String> residentIds = new HashSet<>(records.stream().map(DistributionRecord::getResidentId).toList());

        for (DistributionRecord record : records) {
            if (StringUtils.hasText(record.getCoordinatorId())) {
                coordinatorRepository.findById(record.getCoordinatorId())
                        .ifPresent(c -> residentIds.add(c.getResidentId()));
            }
            if (StringUtils.hasText(record.getSectorLeaderId())) {
                sectorLeaderRepository.findById(record.getSectorLeaderId())
                        .ifPresent(s -> residentIds.add(s.getResidentId()));
            }
            if (StringUtils.hasText(record.getIndividualLeaderId())) {
                individualLeaderRepository.findById(record.getIndividualLeaderId())
                        .ifPresent(i -> residentIds.add(i.getResidentId()));
            }
        }

        Map<String, Order> orderMap = orderRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
        Map<String, Resident> residentMap = residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));

        Map<String, Resident> leaderNameMap = new HashMap<>();
        for (DistributionRecord record : records) {
            if (StringUtils.hasText(record.getCoordinatorId())) {
                coordinatorRepository.findById(record.getCoordinatorId()).ifPresent(c ->
                        leaderNameMap.put(record.getCoordinatorId(), residentMap.get(c.getResidentId())));
            }
            if (StringUtils.hasText(record.getSectorLeaderId())) {
                sectorLeaderRepository.findById(record.getSectorLeaderId()).ifPresent(s ->
                        leaderNameMap.put(record.getSectorLeaderId(), residentMap.get(s.getResidentId())));
            }
            if (StringUtils.hasText(record.getIndividualLeaderId())) {
                individualLeaderRepository.findById(record.getIndividualLeaderId()).ifPresent(i ->
                        leaderNameMap.put(record.getIndividualLeaderId(), residentMap.get(i.getResidentId())));
            }
        }

        List<DistributionRecordItemResponse> list = records.stream()
                .map(record -> distributionConverter.toListItem(
                        record,
                        orderMap.get(record.getOrderId()),
                        merchantMap.get(record.getMerchantId()),
                        residentMap.get(record.getResidentId()),
                        leaderNameMap))
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    private void absorbPoolDiff(String propertyCompanyId, BigDecimal distributableAmount, String orderId) {
        BigDecimal diff = distributableAmount.abs();
        PointPool pool = pointPoolRepository.findByPropertyCompanyId(propertyCompanyId).orElse(null);
        if (pool == null) {
            return;
        }

        BigDecimal newBalance = defaultDecimal(pool.getBalance()).subtract(diff);
        pool.setBalance(newBalance);
        pool.setTotalOut(defaultDecimal(pool.getTotalOut()).add(diff));
        pointPoolRepository.save(pool);

        PointPoolRecord poolRecord = new PointPoolRecord();
        poolRecord.setId(idGenerator.generateId("ppr"));
        poolRecord.setPropertyCompanyId(propertyCompanyId);
        poolRecord.setPoolType("pool_out");
        poolRecord.setAmount(diff);
        poolRecord.setBalance(newBalance);
        poolRecord.setSource("pool_diff");
        poolRecord.setSourceId(orderId);
        poolRecord.setDescription("分成负差额入积分池");
        pointPoolRecordRepository.save(poolRecord);
    }

    private String resolveScopedPropertyCompanyId(String propertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(propertyCompanyId)) {
                if (!propertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
            }
            return propertyCompanyId;
        }
        if (roleGuard.isPropertyAdmin()) {
            return UserContext.currentPropertyCompanyId();
        }
        Coordinator coordinator = resolveCurrentCoordinator();
        return coordinator.getPropertyCompanyId();
    }

    private String resolveScopedCoordinatorId(String coordinatorId) {
        if (roleGuard.isCoordinator()) {
            return resolveCurrentCoordinator().getId();
        }
        return coordinatorId;
    }

    private String resolvePreviewPropertyCompanyId(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId)) {
            if (!propertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return propertyCompanyId;
        }
        String current = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(current)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId不能为空");
        }
        return current;
    }

    private Coordinator resolveCurrentCoordinator() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return coordinatorRepository.findByResidentIdAndPropertyCompanyId(userId, propertyCompanyId)
                .filter(c -> "active".equals(c.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND));
    }

    private String resolveMerchantSector(Merchant merchant) {
        if (merchant == null || !StringUtils.hasText(merchant.getCategory())) {
            return "life_service";
        }
        return switch (merchant.getCategory()) {
            case "农家" -> "canteen";
            case "外卖", "外卖小吃" -> "life_service";
            default -> "life_service";
        };
    }

    private LocalDateTime parseIsoDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确");
        }
    }

    private void validateOptionalId(String id, String prefix) {
        if (StringUtils.hasText(id) && !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal defaultRate(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal negate(BigDecimal value) {
        return defaultDecimal(value).negate();
    }

    private record ShareBreakdown(
            BigDecimal pointCost,
            BigDecimal coinCost,
            BigDecimal distributableAmount,
            BigDecimal propertyShare,
            BigDecimal coordinatorShare,
            BigDecimal sectorLeaderShare,
            BigDecimal individualLeaderShare,
            String coordinatorId,
            String sectorLeaderId,
            String individualLeaderId) {
    }
}
