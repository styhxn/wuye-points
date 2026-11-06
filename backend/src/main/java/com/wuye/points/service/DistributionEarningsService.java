package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.distribution.DistributionEarningsDetailItem;
import com.wuye.points.dto.distribution.DistributionEarningsResponse;
import com.wuye.points.dto.distribution.DistributionEarningsSummaryItem;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 「我的分账所得」聚合查询。
 *
 * <p>口径：</p>
 * <ul>
 *   <li>先枚举登录住户的<b>全部历史身份</b>（含已停用、跨物业），再按 owner_type + owner_id 聚合；</li>
 *   <li>物业公司只作为出参展示，<b>不参与查询过滤</b>；</li>
 *   <li>应付分成取 {@code distribution_records} 台账，已到账取 {@code wechat_split_record} 已真实结算的状态集；</li>
 *   <li>商家/技师的商品收入是分账出资方，不属于「分账所得」，本接口不聚合；</li>
 *   <li>分账所得并入各角色可提现余额、走同一条提现通道（2026-11-06 定稿口径），
 *       每角色的余额与费率由 {@link RoleWithdrawableService} 统一算出，与提现申请校验同源。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DistributionEarningsService {

    private static final String ROLE_COORDINATOR = "COORDINATOR";
    private static final String ROLE_SECTOR_LEADER = "SECTOR_LEADER";
    private static final String ROLE_INDIVIDUAL_LEADER = "INDIVIDUAL_LEADER";
    private static final String ROLE_BUILDING_LEADER = "BUILDING_LEADER";
    private static final String ROLE_PROPERTY_MANAGER = "PROPERTY_MANAGER";

    /** 角色展示顺序 + 中文名（与 WechatSplitOwnerTypes 取值一致）。 */
    private static final Map<String, String> ROLE_NAMES = new LinkedHashMap<>();
    /** 个人接收方「已真实结算」状态集合，与 WithdrawableService 的可提现口径保持一致。 */
    private static final List<String> SETTLED_STATUSES = List.of("frozen", "finished", "reverted");
    private static final List<String> PROCESSING_STATUSES = List.of("frozen_pending");
    private static final List<String> FAILED_STATUSES = List.of("freeze_failed", "skipped", "reverse_failed");

    static {
        ROLE_NAMES.put(ROLE_COORDINATOR, "统筹负责人");
        ROLE_NAMES.put(ROLE_SECTOR_LEADER, "板块负责人");
        ROLE_NAMES.put(ROLE_INDIVIDUAL_LEADER, "个体负责人");
        ROLE_NAMES.put(ROLE_BUILDING_LEADER, "单元楼长");
        ROLE_NAMES.put(ROLE_PROPERTY_MANAGER, "物业经理");
    }

    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final PropertyManagerRepository propertyManagerRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final WechatSplitRecordRepository wechatSplitRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final RoleWithdrawableService roleWithdrawableService;

    @Transactional(readOnly = true)
    public DistributionEarningsResponse listMine(String role, String startDate, String endDate,
                                                 int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        String residentId = UserContext.currentUserId();
        Resident self = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String currentPropertyId = self.getPropertyCompanyId();
        String selfName = StringUtils.hasText(self.getName()) ? self.getName() : "历史身份";
        LocalDateTime start = parseIsoDate(startDate);
        LocalDateTime endExclusive = parseIsoDate(endDate);

        Set<String> roleFilter = parseRoleFilter(role);
        List<Identity> identities = enumerateIdentities(residentId, currentPropertyId, selfName);
        if (identities.isEmpty()) {
            return DistributionEarningsResponse.builder()
                    .walletIncluded(true)
                    .summary(List.of())
                    .list(List.of())
                    .page(safePage)
                    .pageSize(safeSize)
                    .total(0L)
                    .currentPropertyCompanyId(currentPropertyId)
                    .build();
        }

        Map<String, Identity> identityMap = identities.stream()
                .collect(Collectors.toMap(i -> i.role + "|" + i.identityId, Function.identity(),
                        (a, b) -> a));

        List<Row> rows = new ArrayList<>();
        loadRows(rows, identityMap, roleFilter, ROLE_COORDINATOR,
                collectIds(identities, ROLE_COORDINATOR),
                ids -> distributionRecordRepository.findByCoordinatorIdIn(ids).stream()
                        .map(dr -> new Candidate(dr, dr.getCoordinatorId(), dr.getCoordinatorShare()))
                        .toList());
        loadRows(rows, identityMap, roleFilter, ROLE_SECTOR_LEADER,
                collectIds(identities, ROLE_SECTOR_LEADER),
                ids -> distributionRecordRepository.findBySectorLeaderIdIn(ids).stream()
                        .map(dr -> new Candidate(dr, dr.getSectorLeaderId(), dr.getSectorLeaderShare()))
                        .toList());
        loadRows(rows, identityMap, roleFilter, ROLE_INDIVIDUAL_LEADER,
                collectIds(identities, ROLE_INDIVIDUAL_LEADER),
                ids -> distributionRecordRepository.findByIndividualLeaderIdIn(ids).stream()
                        .map(dr -> new Candidate(dr, dr.getIndividualLeaderId(), dr.getIndividualLeaderShare()))
                        .toList());
        loadRows(rows, identityMap, roleFilter, ROLE_BUILDING_LEADER,
                collectIds(identities, ROLE_BUILDING_LEADER),
                ids -> distributionRecordRepository.findByBuildingLeaderIdIn(ids).stream()
                        .map(dr -> new Candidate(dr, dr.getBuildingLeaderId(), dr.getBuildingLeaderShare()))
                        .toList());
        loadRows(rows, identityMap, roleFilter, ROLE_PROPERTY_MANAGER,
                collectIds(identities, ROLE_PROPERTY_MANAGER),
                ids -> distributionRecordRepository.findByManagerIdIn(ids).stream()
                        .map(dr -> new Candidate(dr, dr.getManagerId(), dr.getManagerShare()))
                        .toList());

        // 时间窗按分成落账时间（应付口径）收敛；已到账按同一订单行透出，不单独放宽时间窗
        rows.removeIf(r -> (start != null && r.record.getCreatedAt().isBefore(start))
                || (endExclusive != null && !r.record.getCreatedAt().isBefore(endExclusive)));

        Map<String, List<WechatSplitRecord>> splitsByOrder = loadSplits(rows);
        Map<String, String> orderNos = loadOrderNos(rows);
        Map<String, String> propertyNames = loadPropertyNames(rows);

        List<DistributionEarningsDetailItem> details = new ArrayList<>(rows.size());
        Map<String, RoleAgg> aggMap = new LinkedHashMap<>();
        for (Row row : rows) {
            Identity identity = row.identity;
            BigDecimal payable = row.share;
            boolean reversal = payable.compareTo(BigDecimal.ZERO) < 0;
            List<WechatSplitRecord> mine = splitsByOrder.getOrDefault(row.record.getOrderId(), List.of())
                    .stream()
                    .filter(s -> identity.role.equals(s.getOwnerType()))
                    .filter(s -> Objects.equals(identity.payoutOwnerId, s.getOwnerId()))
                    .toList();
            BigDecimal received = reversal ? BigDecimal.ZERO : sumSettled(mine);
            String payoutStatus = reversal ? "REVERSED" : resolvePayoutStatus(mine);
            String propertyId = StringUtils.hasText(row.record.getPropertyCompanyId())
                    ? row.record.getPropertyCompanyId() : identity.propertyCompanyId;

            details.add(DistributionEarningsDetailItem.builder()
                    .orderId(row.record.getOrderId())
                    .orderNo(orderNos.get(row.record.getOrderId()))
                    .role(identity.role)
                    .roleName(ROLE_NAMES.get(identity.role))
                    .identityId(identity.identityId)
                    .identityName(identity.displayName)
                    .payableAmount(payable)
                    .receivedAmount(received)
                    .payoutStatus(payoutStatus)
                    .isHistorical(identity.historical)
                    .identityStatus(identity.historical ? "historical" : "active")
                    .propertyCompanyId(propertyId)
                    .propertyCompanyName(propertyNames.get(propertyId))
                    .createdAt(DateTimeUtil.toIsoUtc(row.record.getCreatedAt()))
                    .build());

            RoleAgg agg = aggMap.computeIfAbsent(identity.role, RoleAgg::new);
            agg.payable = agg.payable.add(payable);
            agg.received = agg.received.add(received);
            agg.identityCount++;
            agg.historicalAll = agg.historicalAll && identity.historical;
            if (StringUtils.hasText(propertyId)) {
                agg.propertyIds.add(propertyId);
            }
        }

        details.sort(Comparator.comparing(DistributionEarningsDetailItem::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        int from = Math.min((safePage - 1) * safeSize, details.size());
        int to = Math.min(from + safeSize, details.size());

        List<DistributionEarningsSummaryItem> summary = new ArrayList<>();
        for (String roleKey : ROLE_NAMES.keySet()) {
            RoleAgg agg = aggMap.get(roleKey);
            if (agg == null) {
                continue;
            }
            List<String> propertyIds = new ArrayList<>(agg.propertyIds);
            RoleWithdrawableService.RoleBalance balance = resolveBalance(roleKey, residentId, currentPropertyId);
            summary.add(DistributionEarningsSummaryItem.builder()
                    .role(roleKey)
                    .roleName(ROLE_NAMES.get(roleKey))
                    .payableAmount(agg.payable)
                    .receivedAmount(agg.received)
                    .walletCreditedAmount(balance != null ? balance.creditedAmount() : null)
                    .withdrawableAmount(balance != null ? balance.withdrawableAmount() : null)
                    .pendingWithdrawalAmount(balance != null ? balance.pendingWithdrawalAmount() : null)
                    .feeRate(balance != null ? balance.feeRate() : null)
                    .withdrawEnabled(balance != null)
                    .isHistorical(agg.historicalAll)
                    .identityStatus(agg.historicalAll ? "historical" : "active")
                    .identityCount(agg.identityCount)
                    .propertyCompanyIds(propertyIds)
                    .propertyNames(propertyIds.stream().map(propertyNames::get)
                            .filter(Objects::nonNull).distinct().toList())
                    .build());
        }

        return DistributionEarningsResponse.builder()
                .walletIncluded(true)
                .summary(summary)
                .list(details.subList(from, to))
                .page(safePage)
                .pageSize(safeSize)
                .total((long) details.size())
                .currentPropertyCompanyId(currentPropertyId)
                .build();
    }

    // ------------------------------------------------------------------ 内部实现

    /**
     * 该角色「当前可提现 / 在途提现 / 手续费率」，按提现接口解析身份的同一条件
     * （在职 + 当前物业）取数，保证与提现校验是同一个函数、同一个数。
     *
     * @return 解析不到在职身份时返回 {@code null}，此时账面分成不可通过小程序提现，前端隐藏提现入口
     */
    private RoleWithdrawableService.RoleBalance resolveBalance(String roleKey, String residentId,
                                                               String currentPropertyId) {
        return switch (roleKey) {
            case ROLE_COORDINATOR -> {
                Coordinator identity = coordinatorRepository
                        .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                                residentId, currentPropertyId, "active")
                        .orElse(null);
                yield identity == null ? null : roleWithdrawableService.forShareLedgerRole(
                        RoleWithdrawableService.ShareLedgerRole.COORDINATOR,
                        identity.getId(), identity.getPropertyCompanyId());
            }
            case ROLE_SECTOR_LEADER -> {
                SectorLeader identity = sectorLeaderRepository
                        .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                                residentId, currentPropertyId, "active")
                        .orElse(null);
                yield identity == null ? null : roleWithdrawableService.forShareLedgerRole(
                        RoleWithdrawableService.ShareLedgerRole.SECTOR_LEADER,
                        identity.getId(), identity.getPropertyCompanyId());
            }
            case ROLE_INDIVIDUAL_LEADER -> {
                IndividualLeader identity = individualLeaderRepository
                        .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                                residentId, currentPropertyId, "active")
                        .orElse(null);
                yield identity == null ? null : roleWithdrawableService.forShareLedgerRole(
                        RoleWithdrawableService.ShareLedgerRole.INDIVIDUAL_LEADER,
                        identity.getId(), identity.getPropertyCompanyId());
            }
            case ROLE_BUILDING_LEADER -> {
                BuildingLeader identity = buildingLeaderRepository
                        .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                                residentId, currentPropertyId, "active")
                        .orElse(null);
                yield identity == null ? null : roleWithdrawableService.forBuildingLeader(
                        identity.getId(), identity.getResidentId(), identity.getPropertyCompanyId());
            }
            case ROLE_PROPERTY_MANAGER -> {
                PropertyManager identity = propertyManagerRepository
                        .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByIdAsc(
                                residentId, currentPropertyId, "active")
                        .orElse(null);
                yield identity == null ? null : roleWithdrawableService.forPropertyManager(
                        identity.getId(), identity.getResidentId(), identity.getPropertyCompanyId());
            }
            default -> null;
        };
    }

    /** 一条历史身份：{@code payoutOwnerId} 是该角色在 wechat_split_record 里使用的收款主体键。 */
    private record Identity(String role, String identityId, String payoutOwnerId,
                            String propertyCompanyId, boolean historical, String displayName) {
    }

    /** 台账候选行：尚未绑定到具体身份。 */
    private record Candidate(DistributionRecord record, String identityId, BigDecimal share) {
    }

    private static final class Row {
        private final DistributionRecord record;
        private final Identity identity;
        private final BigDecimal share;

        private Row(DistributionRecord record, Identity identity, BigDecimal share) {
            this.record = record;
            this.identity = identity;
            this.share = share == null ? BigDecimal.ZERO : share;
        }
    }

    private static final class RoleAgg {
        private final String role;
        private BigDecimal payable = BigDecimal.ZERO;
        private BigDecimal received = BigDecimal.ZERO;
        private int identityCount;
        private boolean historicalAll = true;
        private final Set<String> propertyIds = new LinkedHashSet<>();

        private RoleAgg(String role) {
            this.role = role;
        }
    }

    private List<Identity> enumerateIdentities(String residentId, String currentPropertyId,
                                               String selfName) {
        List<Identity> list = new ArrayList<>();
        for (Coordinator c : coordinatorRepository.findAllByResidentId(residentId)) {
            list.add(new Identity(ROLE_COORDINATOR, c.getId(), c.getId(), c.getPropertyCompanyId(),
                    isHistorical(c.getStatus(), c.getPropertyCompanyId(), currentPropertyId), selfName));
        }
        for (SectorLeader s : sectorLeaderRepository.findAllByResidentId(residentId)) {
            list.add(new Identity(ROLE_SECTOR_LEADER, s.getId(), s.getId(), s.getPropertyCompanyId(),
                    isHistorical(s.getStatus(), s.getPropertyCompanyId(), currentPropertyId), selfName));
        }
        for (IndividualLeader i : individualLeaderRepository.findAllByResidentId(residentId)) {
            String name = StringUtils.hasText(i.getName()) ? i.getName() : selfName;
            list.add(new Identity(ROLE_INDIVIDUAL_LEADER, i.getId(), i.getId(), i.getPropertyCompanyId(),
                    isHistorical(i.getStatus(), i.getPropertyCompanyId(), currentPropertyId), name));
        }
        for (BuildingLeader b : buildingLeaderRepository.findByResidentId(residentId)) {
            list.add(new Identity(ROLE_BUILDING_LEADER, b.getId(), b.getId(), b.getPropertyCompanyId(),
                    isHistorical(b.getStatus(), b.getPropertyCompanyId(), currentPropertyId), selfName));
        }
        // 物业经理：wechat_split_record.owner_id 存的是经理的 residentId，故 payoutOwnerId 取住户 ID
        for (PropertyManager m : propertyManagerRepository.findByResidentId(residentId)) {
            list.add(new Identity(ROLE_PROPERTY_MANAGER, m.getId(), residentId, m.getPropertyCompanyId(),
                    isHistorical(m.getStatus(), m.getPropertyCompanyId(), currentPropertyId), selfName));
        }
        return list;
    }

    /**
     * 身份是否「历史」：身份已停用，或身份所属物业与登录住户当前物业不一致。
     * 登录住户没有物业时只按身份状态判断。
     */
    private boolean isHistorical(String status, String identityPropertyId, String currentPropertyId) {
        boolean inactive = StringUtils.hasText(status) && !"active".equalsIgnoreCase(status);
        boolean otherProperty = StringUtils.hasText(currentPropertyId)
                && !Objects.equals(identityPropertyId, currentPropertyId);
        return inactive || otherProperty;
    }

    private Set<String> parseRoleFilter(String role) {
        if (!StringUtils.hasText(role)) {
            return ROLE_NAMES.keySet();
        }
        Set<String> filter = new LinkedHashSet<>();
        for (String part : role.split(",")) {
            String code = part.trim().toUpperCase(Locale.ROOT);
            if (code.isEmpty()) {
                continue;
            }
            if (!ROLE_NAMES.containsKey(code)) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "role 取值不合法，可选：COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/BUILDING_LEADER/PROPERTY_MANAGER");
            }
            filter.add(code);
        }
        return filter;
    }

    private List<String> collectIds(List<Identity> identities, String role) {
        return identities.stream().filter(i -> role.equals(i.role))
                .map(Identity::identityId).toList();
    }

    /**
     * 按角色装载台账行：一行台账可能同时挂多个角色，这里按角色拆行，避免汇总串账；
     * 该角色金额为 0 / 空的列不产生行。
     */
    private void loadRows(List<Row> target, Map<String, Identity> identityMap, Set<String> roleFilter,
                          String role, List<String> identityIds,
                          Function<List<String>, List<Candidate>> loader) {
        if (!roleFilter.contains(role) || identityIds.isEmpty()) {
            return;
        }
        for (Candidate candidate : loader.apply(identityIds)) {
            Identity identity = identityMap.get(role + "|" + candidate.identityId());
            if (identity == null || candidate.share() == null
                    || candidate.share().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            target.add(new Row(candidate.record(), identity, candidate.share()));
        }
    }

    private Map<String, List<WechatSplitRecord>> loadSplits(List<Row> rows) {
        List<String> orderIds = rows.stream().map(r -> r.record.getOrderId())
                .filter(StringUtils::hasText).distinct().toList();
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return wechatSplitRecordRepository.findByOrderIdIn(orderIds).stream()
                .filter(s -> StringUtils.hasText(s.getOrderId()))
                .collect(Collectors.groupingBy(WechatSplitRecord::getOrderId));
    }

    private Map<String, String> loadOrderNos(List<Row> rows) {
        List<String> orderIds = rows.stream().map(r -> r.record.getOrderId())
                .filter(StringUtils::hasText).distinct().toList();
        if (orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> orderNos = new HashMap<>();
        for (Order order : orderRepository.findAllById(orderIds)) {
            if (StringUtils.hasText(order.getOrderNo())) {
                orderNos.put(order.getId(), order.getOrderNo());
            }
        }
        return orderNos;
    }

    private BigDecimal sumSettled(List<WechatSplitRecord> mine) {
        return mine.stream()
                .filter(s -> SETTLED_STATUSES.contains(s.getStatus()))
                .map(s -> s.getFreezeAmount() == null ? BigDecimal.ZERO : s.getFreezeAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String resolvePayoutStatus(List<WechatSplitRecord> mine) {
        if (mine.isEmpty()) {
            return "NOT_SPLIT";
        }
        if (sumSettled(mine).compareTo(BigDecimal.ZERO) > 0) {
            return "RECEIVED";
        }
        if (mine.stream().anyMatch(s -> PROCESSING_STATUSES.contains(s.getStatus()))) {
            return "PROCESSING";
        }
        if (mine.stream().anyMatch(s -> FAILED_STATUSES.contains(s.getStatus()))) {
            return "FAILED";
        }
        return "PROCESSING";
    }

    private Map<String, String> loadPropertyNames(List<Row> rows) {
        Set<String> ids = rows.stream().map(r -> r.record.getPropertyCompanyId())
                .filter(StringUtils::hasText).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> names = new HashMap<>();
        for (PropertyCompany company : propertyCompanyRepository.findAllById(ids)) {
            names.put(company.getId(), company.getName());
        }
        return names;
    }

    private LocalDateTime parseIsoDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(java.time.Instant.parse(value), java.time.ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "日期格式不正确，请传 ISO8601（如 2026-01-01T00:00:00.000Z）");
        }
    }
}
