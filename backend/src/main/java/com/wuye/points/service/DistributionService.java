package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distribution.DistributionCalculateResponse;
import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.dto.distribution.DistributionStatsResponse;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.PointPool;
import com.wuye.points.entity.PointPoolRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DeliveryRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointPoolRecordRepository;
import com.wuye.points.repository.PointPoolRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyManagerRepository;
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

    private static final BigDecimal DEFAULT_PLATFORM_SHARE = new BigDecimal("0.0000");
    private static final BigDecimal DEFAULT_PROPERTY_SHARE = new BigDecimal("0.3000");
    private static final BigDecimal DEFAULT_COORDINATOR_SHARE = new BigDecimal("0.7000");
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = new BigDecimal("0.1000");
    private static final BigDecimal DEFAULT_INDIVIDUAL_LEADER_RATE = new BigDecimal("0.0000");
    private static final BigDecimal DEFAULT_POINT_EXCHANGE_RATE = new BigDecimal("100.0000");
    private static final int MONEY_SCALE = 2;
    /** 计算版本标识（规范§8.5 distribution_b_v1） */
    private static final String CALCULATION_VERSION = "distribution_b_v1";
    /** 成本击穿差额默认承担方（不扣商家） */
    private static final String DEFAULT_DEFICIT_SPONSOR = "property";
    private static final String MERCHANT_SPONSOR = "merchant";

    private final DistributionRecordRepository distributionRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final MerchantRepository merchantRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final PointPoolRepository pointPoolRepository;
    private final PointPoolRecordRepository pointPoolRecordRepository;
    private final DistributionConverter distributionConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final PropertyManagerRepository propertyManagerRepository;
    private final WalletFlowWriter walletFlowWriter;
    private final WalletFlowService walletFlowService;

    /** 物业经理抽成流水的钱包类型（ownerType=property_manager，ownerId=经理 residentId） */
    private static final String ENTRY_PROPERTY_MANAGER_SHARE = "property_manager_share";

    @Transactional
    public DistributionRecord calculateAndSave(Order order, Resident resident, MerchantPropertyConfig config) {
        Optional<DistributionRecord> existing = distributionRecordRepository.findFirstByOrderId(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        ShareBreakdown breakdown = buildShareBreakdown(
                order, config, resident.getPropertyCompanyId(), resolveBuildingLeaderSection(order));
        DistributionRecord record = toRecord(order, resident, config.getCommissionRate(), breakdown);
        distributionRecordRepository.save(record);

        // 物业经理抽成入账（从楼长分成中抽取，楼长落库净额；幂等键按订单+角色唯一）
        recordManagerShareFlow(record, breakdown);

        if (breakdown.distributableAmount().compareTo(BigDecimal.ZERO) < 0) {
            absorbPoolDiff(resident.getPropertyCompanyId(), breakdown.distributableAmount(), order.getId());
        } else if (breakdown.propertyShare().compareTo(BigDecimal.ZERO) > 0) {
            addToPoolFromDistribution(resident.getPropertyCompanyId(), breakdown.propertyShare(), order.getId());
        }
        return record;
    }

    /**
     * 物业经理抽成流水：经理收益 = 楼长分成毛额 × 经理比例（快照），写入经理钱包（wallet_flows）。
     * 楼长落库的 buildingLeaderShare 已是扣除经理抽成后的净额，因此物业/统筹链路总额守恒不变。
     */
    private void recordManagerShareFlow(DistributionRecord record, ShareBreakdown breakdown) {
        if (!StringUtils.hasText(breakdown.managerId())
                || breakdown.managerShare() == null
                || breakdown.managerShare().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        PropertyManager manager = propertyManagerRepository.findById(breakdown.managerId()).orElse(null);
        if (manager == null || !"active".equals(manager.getStatus())) {
            return;
        }
        walletFlowWriter.write(record.getId(), record.getOrderId(), null,
                "property_manager", manager.getResidentId(), ENTRY_PROPERTY_MANAGER_SHARE,
                breakdown.managerShare(), "withdrawable",
                "distribution:settle:" + record.getOrderId() + ":property_manager_share:"
                        + manager.getResidentId(), null);
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
        reversal.setPlatformShare(negate(original.getPlatformShare()));
        reversal.setPropertyShare(negate(original.getPropertyShare()));
        reversal.setCoordinatorShare(negate(original.getCoordinatorShare()));
        reversal.setSectorLeaderShare(negate(original.getSectorLeaderShare()));
        reversal.setIndividualLeaderShare(negate(original.getIndividualLeaderShare()));
        reversal.setBuildingLeaderShare(negate(original.getBuildingLeaderShare()));
        reversal.setCoordinatorId(original.getCoordinatorId());
        reversal.setSectorLeaderId(original.getSectorLeaderId());
        reversal.setIndividualLeaderId(original.getIndividualLeaderId());
        reversal.setBuildingLeaderId(original.getBuildingLeaderId());
        reversal.setBuildingLeaderRate(original.getBuildingLeaderRate());
        reversal.setManagerId(original.getManagerId());
        reversal.setManagerRate(original.getManagerRate());
        distributionRecordRepository.save(reversal);

        // 钱包流水全主体冲正（P0-2 修复）：对原分账记录下的全部正向流水逐条取负冲回，
        // 覆盖 merchant/platform/property/coordinator/sector_leader/individual_leader/
        // property_manager 等所有主体。此前仅冲正物业经理一类，其余主体退款后余额不冲正，
        // 余额口径与审计账本长期不一致；且 WalletFlowService#recordReversal 无生产调用方。
        walletFlowService.recordReversal(orderId, original);
    }

    /** 物业经理抽成（订单快照比例）：经理收益=毛额×比例，楼长实得=毛额−经理抽成；无经理快照时全额归楼长。 */
    private ManagerCut applyManagerCut(Order order, BigDecimal grossBuildingLeaderShare) {
        BigDecimal rate = order.getManagerRate();
        if (!StringUtils.hasText(order.getManagerId()) || rate == null
                || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
            return new ManagerCut(grossBuildingLeaderShare, BigDecimal.ZERO);
        }
        BigDecimal managerShare = money(grossBuildingLeaderShare.multiply(rate));
        BigDecimal leaderNet = money(grossBuildingLeaderShare.subtract(managerShare));
        return new ManagerCut(leaderNet, managerShare);
    }

    /** 经理抽成结果：楼长净额 + 经理抽成 */
    private record ManagerCut(BigDecimal leaderNet, BigDecimal managerShare) {
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
        roleGuard.requireAnyRoleOrCoordinatorIdentity("coordinator", "property_admin", "platform_admin");
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

    /** 楼长查看自己的分成明细（只读，仅限本人） */
    @Transactional(readOnly = true)
    public PageResponse<DistributionRecordItemResponse> listMyRecordsForBuildingLeader(int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        BuildingLeader leader = buildingLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        UserContext.currentUserId(), UserContext.currentPropertyCompanyId(), "active")
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND, "当前用户不是楼长"));

        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DistributionRecord> recordPage = distributionRecordRepository
                .findByBuildingLeaderIdOrderByCreatedAtDesc(leader.getId(), pageable);
        return buildRecordPage(recordPage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public DistributionStatsResponse getStats(
            String startDate,
            String endDate,
            String dimension,
            String propertyCompanyId) {
        roleGuard.requireAnyRoleOrCoordinatorIdentity("coordinator", "property_admin", "platform_admin");

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

    /**
     * 分账试算（预览，不落库）。规范§21.3 支持两种模式：
     * <ul>
     *   <li>① 订单快照试算：传 {@code orderId}，金额与比例一律读订单落库快照，客户端不可篡改；</li>
     *   <li>② 管理端假设试算：传 {@code merchantId} + {@code productAmount} 等。</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public DistributionCalculateResponse calculatePreview(
            String orderId,
            String merchantId,
            BigDecimal productAmount,
            BigDecimal totalAmount,
            BigDecimal originalDeliveryFee,
            BigDecimal deliveryFee,
            String deliverySubsidySponsor,
            Integer pointUsed,
            BigDecimal coinUsed,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("coordinator", "property_admin", "platform_admin", "merchant", "resident");
        if (!StringUtils.hasText(orderId) && !StringUtils.hasText(merchantId)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "orderId与merchantId至少传一个");
        }

        Order previewOrder;
        String resolvedMerchantId;
        String resolvedPropertyCompanyId;
        BuildingLeaderSection buildingLeader;

        if (StringUtils.hasText(orderId)) {
            validateOptionalId(orderId, "ord_");
            previewOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BizException(ErrorCode.ORDER_NOT_FOUND));
            resolvedMerchantId = previewOrder.getMerchantId();
            resolvedPropertyCompanyId = previewOrder.getPropertyCompanyId();
            buildingLeader = resolveBuildingLeaderSection(previewOrder);
        } else {
            validateOptionalId(merchantId, "mch_");
            resolvedMerchantId = merchantId;
            resolvedPropertyCompanyId = resolvePreviewPropertyCompanyId(propertyCompanyId);
            BigDecimal goodsAmount = productAmount != null ? productAmount : totalAmount;
            if (goodsAmount == null || goodsAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "productAmount不合法");
            }
            previewOrder = new Order();
            previewOrder.setMerchantId(merchantId);
            previewOrder.setTotalAmount(goodsAmount);
            previewOrder.setCouponAmount(BigDecimal.ZERO);
            BigDecimal effectiveFee = defaultDecimal(deliveryFee);
            BigDecimal originalFee = originalDeliveryFee != null ? originalDeliveryFee : effectiveFee;
            previewOrder.setDeliveryFee(effectiveFee);
            previewOrder.setOriginalDeliveryFee(originalFee);
            previewOrder.setDeliverySubsidyAmount(originalFee.subtract(effectiveFee).max(BigDecimal.ZERO));
            previewOrder.setDeliverySubsidySponsor(
                    StringUtils.hasText(deliverySubsidySponsor) ? deliverySubsidySponsor : "merchant");
            previewOrder.setPointUsed(pointUsed != null ? pointUsed : 0);
            previewOrder.setCoinUsed(coinUsed != null ? coinUsed : BigDecimal.ZERO);
            buildingLeader = resolvePreviewBuildingLeader();
            if (buildingLeader.id() != null) {
                previewOrder.setBuildingLeaderId(buildingLeader.id());
                previewOrder.setBuildingLeaderRate(buildingLeader.rate());
            }
        }

        MerchantPropertyConfig config = merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(resolvedMerchantId, resolvedPropertyCompanyId)
                .filter(item -> "active".equals(item.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_CONFIG_NOT_FOUND));

        PropertyCompany propertyCompany = propertyCompanyRepository.findById(resolvedPropertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.SYSTEM_ERROR));

        ShareBreakdown breakdown = buildShareBreakdown(
                previewOrder, config, resolvedPropertyCompanyId, buildingLeader);
        DeliveryChain deliveryChain = resolveDeliveryChain(previewOrder, propertyCompany);

        BigDecimal productBase = money(defaultDecimal(previewOrder.getTotalAmount())
                .subtract(defaultDecimal(previewOrder.getCouponAmount())));
        BigDecimal merchantGoodsShare = money(productBase.subtract(breakdown.commissionBaseAmount()));
        BigDecimal merchantDeliverySubsidy = MERCHANT_SPONSOR.equals(deliveryChain.subsidySponsor())
                ? deliveryChain.subsidyAmount() : BigDecimal.ZERO;
        BigDecimal merchantAdjustmentAmount = BigDecimal.ZERO;
        BigDecimal merchantShare = money(merchantGoodsShare
                .subtract(merchantDeliverySubsidy)
                .add(merchantAdjustmentAmount));

        return DistributionCalculateResponse.builder()
                .totalAmount(money(productBase.add(deliveryChain.deliveryFee())))
                .productAmount(productBase)
                .commissionRate(config.getCommissionRate())
                .pointCost(breakdown.pointCost())
                .coinCost(breakdown.coinCost())
                .commissionBaseAmount(breakdown.commissionBaseAmount())
                .productCommission(breakdown.commissionBaseAmount())
                .productBase(money(productBase.subtract(breakdown.pointCost()).subtract(breakdown.coinCost())))
                .distributableAmount(breakdown.distributableAmount())
                .deficitAmount(breakdown.deficitAmount())
                .deficitSponsor(DEFAULT_DEFICIT_SPONSOR)
                .platformShareRate(defaultRate(propertyCompany.getPlatformShareRate(), DEFAULT_PLATFORM_SHARE))
                .platformShare(breakdown.platformShare())
                .propertyShare(breakdown.propertyShare())
                .managementPoolAmount(breakdown.managementPoolAmount())
                .sectorGrossAmount(money(breakdown.sectorLeaderShare().add(breakdown.individualLeaderShare())))
                .coordinatorShare(breakdown.coordinatorShare())
                .sectorLeaderShare(breakdown.sectorLeaderShare())
                .individualLeaderShare(breakdown.individualLeaderShare())
                .buildingLeaderShare(breakdown.buildingLeaderShare())
                .originalDeliveryFee(deliveryChain.originalDeliveryFee())
                .deliveryFee(deliveryChain.deliveryFee())
                .effectiveDeliveryFee(deliveryChain.deliveryFee())
                .deliveryWaiverAmount(deliveryChain.subsidyAmount())
                .deliverySubsidySponsor(deliveryChain.subsidySponsor())
                .deliverySubsidyAmount(deliveryChain.subsidyAmount())
                .deliverySettlementBase(deliveryChain.settlementBase())
                .merchantGoodsShare(merchantGoodsShare)
                .merchantDeliverySubsidy(merchantDeliverySubsidy)
                .merchantAdjustmentAmount(merchantAdjustmentAmount)
                .merchantShare(merchantShare)
                .platformDeliveryShare(deliveryChain.platformDeliveryShare())
                .courierEarning(deliveryChain.courierEarning())
                .courierShare(deliveryChain.courierEarning())
                .coordinatorId(breakdown.coordinatorId())
                .sectorLeaderId(breakdown.sectorLeaderId())
                .individualLeaderId(breakdown.individualLeaderId())
                .buildingLeaderId(breakdown.buildingLeaderId())
                .calculationVersion(CALCULATION_VERSION)
                .build();
    }

    /** 模式②：按住户当前「小区 + 单元」解析启用中的楼长（仅预览；正式以订单快照为准） */
    private BuildingLeaderSection resolvePreviewBuildingLeader() {
        String userId = UserContext.currentUserId();
        if (!StringUtils.hasText(userId)) {
            return new BuildingLeaderSection(null, BigDecimal.ZERO);
        }
        return residentRepository.findById(userId)
                .map(resident -> {
                    BuildingLeader leader = resolveActiveBuildingLeader(resident);
                    return leader != null
                            ? new BuildingLeaderSection(leader.getId(), defaultDecimal(leader.getCommissionRate()))
                            : new BuildingLeaderSection(null, BigDecimal.ZERO);
                })
                .orElseGet(() -> new BuildingLeaderSection(null, BigDecimal.ZERO));
    }

    /** 按住户当前所属「小区 + 单元」解析启用中的楼长（仅用于预览，正式以订单快照为准） */
    private BuildingLeader resolveActiveBuildingLeader(Resident resident) {
        if (!StringUtils.hasText(resident.getCommunityId()) || !StringUtils.hasText(resident.getUnit())) {
            return null;
        }
        return buildingLeaderRepository
                .findFirstByCommunityIdAndUnitAndStatusOrderByCreatedAtDesc(
                        resident.getCommunityId(), resident.getUnit(), "active")
                .orElse(null);
    }

    private ShareBreakdown buildShareBreakdown(Order order,
                                              MerchantPropertyConfig config,
                                              String propertyCompanyId,
                                              BuildingLeaderSection buildingLeader) {
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
        // 分账口径：优惠券由商家承担，平台佣金基数按「实付商品金额」= totalAmount − couponAmount
        BigDecimal totalAmount = defaultDecimal(order.getTotalAmount())
                .subtract(defaultDecimal(order.getCouponAmount()))
                .max(BigDecimal.ZERO);
        BigDecimal commissionRate = config.getCommissionRate();

        // 抽佣基础额（一级三档分账基数 = 商品实付 × 抽佣率）
        BigDecimal commissionBaseAmount = money(totalAmount.multiply(commissionRate));
        BigDecimal rawDistributableAmount = money(commissionBaseAmount.subtract(pointCost).subtract(coinCost));
        BigDecimal distributableAmount = rawDistributableAmount.max(BigDecimal.ZERO);
        BigDecimal deficitAmount = rawDistributableAmount.negate().max(BigDecimal.ZERO);

        if (distributableAmount.compareTo(BigDecimal.ZERO) <= 0 && deficitAmount.compareTo(BigDecimal.ZERO) > 0) {
            return new ShareBreakdown(
                    pointCost, coinCost, distributableAmount,
                    commissionBaseAmount, deficitAmount,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null, null, null, null, null,
                    BigDecimal.ZERO, null, null);
        }

        BigDecimal platformRate = defaultRate(propertyCompany.getPlatformShareRate(), DEFAULT_PLATFORM_SHARE);
        BigDecimal propertyRate = defaultRate(propertyCompany.getPropertyShareRate(), DEFAULT_PROPERTY_SHARE);
        BigDecimal sectorLeaderRate = defaultRate(propertyCompany.getSectorLeaderRate(), DEFAULT_SECTOR_LEADER_RATE);
        BigDecimal individualLeaderRate = defaultRate(propertyCompany.getIndividualLeaderRate(), DEFAULT_INDIVIDUAL_LEADER_RATE);

        // 一级三档（规范§4.2）：平台独立取整，管理盘用减法吸收分币误差，
        // 保证 平台 + 物业毛 + 管理盘 = 平台盘（抽佣基础额 − 积分/物业币成本），账目守恒。
        BigDecimal platformShare = money(distributableAmount.multiply(platformRate));
        BigDecimal propertyGrossShare = money(distributableAmount.multiply(propertyRate));
        BigDecimal managementPoolAmount = money(distributableAmount.subtract(platformShare).subtract(propertyGrossShare));

        Merchant merchant = merchantRepository.findById(order.getMerchantId()).orElse(null);
        String sector = resolveMerchantSector(merchant);

        Coordinator coordinator = coordinatorRepository
                .findFirstByPropertyCompanyIdAndStatusOrderByCommissionRateDesc(propertyCompanyId, "active")
                .orElse(null);

        // 楼长分成：以订单快照为准，从「物业公司分成」中定向扣除（保持总额守恒，不影响统筹链路）
        if (coordinator == null) {
            // 无统筹：物业取得「扣除平台盘后的全部分配额」（保持原有口径），再扣楼长分成
            BigDecimal buildingLeaderShare = BigDecimal.ZERO;
            BigDecimal managerShare = BigDecimal.ZERO;
            BigDecimal propertyShare = money(distributableAmount.subtract(platformShare));
            if (buildingLeader.rate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal grossShare = money(propertyShare.multiply(buildingLeader.rate()));
                propertyShare = money(propertyShare.subtract(grossShare));
                ManagerCut cut = applyManagerCut(order, grossShare);
                buildingLeaderShare = cut.leaderNet();
                managerShare = cut.managerShare();
            }
            return new ShareBreakdown(
                    pointCost, coinCost, distributableAmount,
                    commissionBaseAmount, deficitAmount,
                    platformShare, propertyShare,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    buildingLeaderShare,
                    null, null, null,
                    buildingLeader.id(), buildingLeader.rate(),
                    managerShare, order.getManagerId(), order.getManagerRate());
        }

        // 物业分成（毛）= 平台盘 × 物业比例；楼长分成从物业毛额中定向扣除（净额落库）
        BigDecimal buildingLeaderShare = BigDecimal.ZERO;
        BigDecimal managerShare = BigDecimal.ZERO;
        BigDecimal propertyShare = propertyGrossShare;
        if (buildingLeader.rate().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal grossShare = money(propertyGrossShare.multiply(buildingLeader.rate()));
            propertyShare = money(propertyGrossShare.subtract(grossShare));
            ManagerCut cut = applyManagerCut(order, grossShare);
            buildingLeaderShare = cut.leaderNet();
            managerShare = cut.managerShare();
        }

        SectorLeader sectorLeader = sectorLeaderRepository
                .findFirstByPropertyCompanyIdAndSectorAndCoordinatorIdAndStatus(
                        propertyCompanyId, sector, coordinator.getId(), "active")
                .orElse(null);

        // 管理盘级联（规范§4.3）：板块毛额 = 管理盘 × 板块比例；个体 = 板块毛额 × 个体比例；
        // 板块负责人 = 板块毛额 − 个体；统筹 = 管理盘 − 板块毛额（减法吸收分币误差）
        BigDecimal sectorLeaderGross = sectorLeader != null
                ? money(managementPoolAmount.multiply(sectorLeaderRate))
                : BigDecimal.ZERO;

        IndividualLeader individualLeader = sectorLeader != null
                ? individualLeaderRepository.findFirstBySectorLeaderIdAndSectorAndStatus(
                        sectorLeader.getId(), sector, "active").orElse(null)
                : null;

        BigDecimal individualLeaderShare = individualLeader != null
                ? money(sectorLeaderGross.multiply(individualLeaderRate))
                : BigDecimal.ZERO;
        BigDecimal sectorLeaderShare = money(sectorLeaderGross.subtract(individualLeaderShare));
        BigDecimal coordinatorShare = money(managementPoolAmount.subtract(sectorLeaderGross));

        return new ShareBreakdown(
                pointCost, coinCost, distributableAmount,
                commissionBaseAmount, deficitAmount,
                platformShare,
                propertyShare, coordinatorShare, sectorLeaderShare, individualLeaderShare,
                managementPoolAmount,
                buildingLeaderShare,
                coordinator.getId(),
                sectorLeader != null ? sectorLeader.getId() : null,
                individualLeader != null ? individualLeader.getId() : null,
                buildingLeader.id(),
                buildingLeader.rate(),
                managerShare, order.getManagerId(), order.getManagerRate());
    }

    /**
     * 解析订单的楼长分成绑定：严格以订单创建时写入的快照为准，
     * 保证"分成从订单产生那一刻算绑定关系"，后续修改/撤销楼长不影响已产生订单。
     */
    private BuildingLeaderSection resolveBuildingLeaderSection(Order order) {
        if (!StringUtils.hasText(order.getBuildingLeaderId())) {
            return new BuildingLeaderSection(null, BigDecimal.ZERO);
        }
        BigDecimal rate = defaultDecimal(order.getBuildingLeaderRate());
        if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            return new BuildingLeaderSection(null, BigDecimal.ZERO);
        }
        return new BuildingLeaderSection(order.getBuildingLeaderId(), rate);
    }

    /**
     * 配送分账链路（规范§5/§7.6）：
     * <pre>
     * 配送结算基数 = 实付配送费 + 配送补贴（满额减免额）
     * 公司配送收入 = 配送结算基数 × platformDeliveryShareRate
     * 配送员收入   = 配送结算基数 − 公司配送收入   ← 减法吸收分币误差
     * </pre>
     * 不再使用 courierPerOrder 固定每单金额。
     */
    private DeliveryChain resolveDeliveryChain(Order order, PropertyCompany propertyCompany) {
        BigDecimal deliveryFee = defaultDecimal(order.getDeliveryFee());
        BigDecimal originalFee = order.getOriginalDeliveryFee() != null
                ? order.getOriginalDeliveryFee()
                : deliveryFee;
        BigDecimal subsidyAmount = order.getDeliverySubsidyAmount() != null
                ? defaultDecimal(order.getDeliverySubsidyAmount())
                : originalFee.subtract(deliveryFee).max(BigDecimal.ZERO);
        String sponsor = StringUtils.hasText(order.getDeliverySubsidySponsor())
                ? order.getDeliverySubsidySponsor()
                : (subsidyAmount.compareTo(BigDecimal.ZERO) > 0 ? MERCHANT_SPONSOR : "none");

        BigDecimal base = money(deliveryFee.add(subsidyAmount));
        BigDecimal rate = defaultRate(propertyCompany.getPlatformDeliveryShareRate(), BigDecimal.ZERO);
        BigDecimal platformDeliveryShare = money(base.multiply(rate));
        BigDecimal courierEarning = money(base.subtract(platformDeliveryShare));

        return new DeliveryChain(originalFee, deliveryFee, subsidyAmount, sponsor, base,
                platformDeliveryShare, courierEarning);
    }

    /** 配送分账链计算结果 */
    private record DeliveryChain(
            BigDecimal originalDeliveryFee,
            BigDecimal deliveryFee,
            BigDecimal subsidyAmount,
            String subsidySponsor,
            BigDecimal settlementBase,
            BigDecimal platformDeliveryShare,
            BigDecimal courierEarning) {
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
        record.setPlatformShare(breakdown.platformShare());
        record.setPropertyShare(breakdown.propertyShare());
        record.setCoordinatorShare(breakdown.coordinatorShare());
        record.setSectorLeaderShare(breakdown.sectorLeaderShare());
        record.setIndividualLeaderShare(breakdown.individualLeaderShare());
        record.setCoordinatorId(breakdown.coordinatorId());
        record.setSectorLeaderId(breakdown.sectorLeaderId());
        record.setIndividualLeaderId(breakdown.individualLeaderId());
        record.setBuildingLeaderShare(breakdown.buildingLeaderShare());
        record.setBuildingLeaderId(breakdown.buildingLeaderId());
        record.setBuildingLeaderRate(breakdown.buildingLeaderRate());
        record.setManagerId(breakdown.managerId());
        record.setManagerRate(breakdown.managerRate());
        record.setManagerShare(breakdown.managerShare());
        return record;
    }

    private DistributionStatsResponse buildStats(String propertyCompanyId,
                                                 String coordinatorId,
                                                 LocalDateTime start,
                                                 LocalDateTime end,
                                                 String dimension) {
        // 汇总口径：仅按物业公司 + 时间区间过滤（与原有 sum* 查询一致，统筹筛选不影响汇总卡）
        List<DistributionRecord> summaryRecords = distributionRecordRepository.findAll(
                DistributionSpecifications.withFilters(
                        propertyCompanyId, null, null, null, null, null, start, end));
        DistributionStatsResponse.Summary summary = buildSummary(summaryRecords);

        if (!"detail".equals(dimension)) {
            return DistributionStatsResponse.builder()
                    .summary(summary)
                    .byProperty(List.of())
                    .byCoordinator(List.of())
                    .bySector(List.of())
                    .build();
        }

        List<DistributionRecord> records = StringUtils.hasText(coordinatorId)
                ? summaryRecords.stream()
                        .filter(record -> coordinatorId.equals(record.getCoordinatorId()))
                        .toList()
                : summaryRecords;

        return DistributionStatsResponse.builder()
                .summary(summary)
                .byProperty(buildByProperty(records))
                .byCoordinator(buildByCoordinator(records))
                .bySector(buildBySector(records))
                .build();
    }

    /**
     * 汇总统计（规范§21.2）。商品链字段由分成记录 + 订单派生，配送链字段以
     * deliveries.platform_earning / courier_earning 为唯一事实源（与商家自配单口径一致）。
     */
    private DistributionStatsResponse.Summary buildSummary(List<DistributionRecord> records) {
        Set<String> orderIds = records.stream()
                .map(DistributionRecord::getOrderId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Order> orderMap = orderIds.isEmpty() ? Map.of()
                : orderRepository.findAllById(orderIds).stream()
                        .collect(Collectors.toMap(Order::getId, Function.identity()));
        Map<String, Delivery> deliveryMap = orderIds.isEmpty() ? Map.of()
                : deliveryRepository.findByOrderIdIn(orderIds).stream()
                        .collect(Collectors.toMap(Delivery::getOrderId, Function.identity(), (a, b) -> a));

        BigDecimal productAmount = BigDecimal.ZERO;
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        BigDecimal totalDistributableAmount = BigDecimal.ZERO;
        BigDecimal merchantGoodsAmount = BigDecimal.ZERO;
        BigDecimal merchantDeliverySubsidyAmount = BigDecimal.ZERO;
        BigDecimal platformAmount = BigDecimal.ZERO;
        BigDecimal platformDeliveryAmount = BigDecimal.ZERO;
        BigDecimal courierAmount = BigDecimal.ZERO;
        BigDecimal propertyAmount = BigDecimal.ZERO;
        BigDecimal coordinatorAmount = BigDecimal.ZERO;
        BigDecimal sectorLeaderAmount = BigDecimal.ZERO;
        BigDecimal individualLeaderAmount = BigDecimal.ZERO;
        BigDecimal buildingLeaderAmount = BigDecimal.ZERO;
        BigDecimal deficitAmount = BigDecimal.ZERO;
        BigDecimal reversalAmount = BigDecimal.ZERO;

        for (DistributionRecord record : records) {
            BigDecimal recordTotal = defaultDecimal(record.getTotalAmount());
            // 冲正记录金额为负，派生字段需保持同号
            BigDecimal sign = recordTotal.compareTo(BigDecimal.ZERO) < 0
                    ? BigDecimal.ONE.negate() : BigDecimal.ONE;

            productAmount = productAmount.add(recordTotal);
            totalDistributableAmount = totalDistributableAmount.add(defaultDecimal(record.getDistributableAmount()));
            platformAmount = platformAmount.add(defaultDecimal(record.getPlatformShare()));
            propertyAmount = propertyAmount.add(defaultDecimal(record.getPropertyShare()));
            coordinatorAmount = coordinatorAmount.add(defaultDecimal(record.getCoordinatorShare()));
            sectorLeaderAmount = sectorLeaderAmount.add(defaultDecimal(record.getSectorLeaderShare()));
            individualLeaderAmount = individualLeaderAmount.add(defaultDecimal(record.getIndividualLeaderShare()));
            buildingLeaderAmount = buildingLeaderAmount.add(defaultDecimal(record.getBuildingLeaderShare()));

            BigDecimal distributable = defaultDecimal(record.getDistributableAmount());
            if (distributable.compareTo(BigDecimal.ZERO) < 0) {
                reversalAmount = reversalAmount.add(distributable.abs());
            }

            Order order = orderMap.get(record.getOrderId());
            BigDecimal commissionBaseAmount;
            if (order != null) {
                BigDecimal goodsAmount = defaultDecimal(order.getTotalAmount());
                BigDecimal productBase = money(goodsAmount
                        .subtract(defaultDecimal(order.getCouponAmount())).max(BigDecimal.ZERO));
                commissionBaseAmount = money(productBase.multiply(defaultDecimal(record.getCommissionRate())));
                merchantGoodsAmount = merchantGoodsAmount.add(
                        sign.multiply(money(productBase.subtract(commissionBaseAmount))));
                totalOrderAmount = totalOrderAmount.add(
                        recordTotal.add(sign.multiply(defaultDecimal(order.getDeliveryFee()))));
                if (MERCHANT_SPONSOR.equals(order.getDeliverySubsidySponsor())) {
                    merchantDeliverySubsidyAmount = merchantDeliverySubsidyAmount
                            .add(sign.multiply(defaultDecimal(order.getDeliverySubsidyAmount())));
                }
            } else {
                commissionBaseAmount = defaultDecimal(record.getDistributableAmount())
                        .add(defaultDecimal(record.getPointCost()))
                        .add(defaultDecimal(record.getCoinCost()));
                merchantGoodsAmount = merchantGoodsAmount.add(recordTotal.subtract(commissionBaseAmount));
                totalOrderAmount = totalOrderAmount.add(recordTotal);
            }

            // 成本击穿差额：仅正向记录可产生（冲正记录对应的原单必然 distributable>0）
            BigDecimal costs = defaultDecimal(record.getPointCost()).add(defaultDecimal(record.getCoinCost()));
            deficitAmount = deficitAmount.add(costs.subtract(sign.multiply(commissionBaseAmount))
                    .max(BigDecimal.ZERO));

            Delivery delivery = deliveryMap.get(record.getOrderId());
            if (delivery != null) {
                platformDeliveryAmount = platformDeliveryAmount.add(defaultDecimal(delivery.getPlatformEarning()));
                courierAmount = courierAmount.add(defaultDecimal(delivery.getCourierEarning()));
            }
        }

        BigDecimal merchantAmount = merchantGoodsAmount.subtract(merchantDeliverySubsidyAmount);
        BigDecimal managementPoolAmount = coordinatorAmount.add(sectorLeaderAmount).add(individualLeaderAmount);

        return DistributionStatsResponse.Summary.builder()
                .productAmount(money(productAmount))
                .totalOrderAmount(money(totalOrderAmount))
                .totalDistributableAmount(money(totalDistributableAmount))
                .merchantGoodsAmount(money(merchantGoodsAmount))
                .merchantDeliverySubsidyAmount(money(merchantDeliverySubsidyAmount))
                .merchantAmount(money(merchantAmount))
                .platformAmount(money(platformAmount))
                .platformDeliveryAmount(money(platformDeliveryAmount))
                .courierAmount(money(courierAmount))
                .propertyAmount(money(propertyAmount))
                .managementPoolAmount(money(managementPoolAmount))
                .coordinatorAmount(money(coordinatorAmount))
                .sectorLeaderAmount(money(sectorLeaderAmount))
                .individualLeaderAmount(money(individualLeaderAmount))
                .buildingLeaderAmount(money(buildingLeaderAmount))
                .deficitAmount(money(deficitAmount))
                .reversalAmount(money(reversalAmount))
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
            if (StringUtils.hasText(record.getBuildingLeaderId())) {
                buildingLeaderRepository.findById(record.getBuildingLeaderId())
                        .ifPresent(b -> residentIds.add(b.getResidentId()));
            }
        }

        Map<String, Order> orderMap = orderRepository.findAllById(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));
        Map<String, Delivery> deliveryMap = orderIds.isEmpty() ? Map.of()
                : deliveryRepository.findByOrderIdIn(orderIds).stream()
                        .collect(Collectors.toMap(Delivery::getOrderId, Function.identity(), (a, b) -> a));
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
            if (StringUtils.hasText(record.getBuildingLeaderId())) {
                buildingLeaderRepository.findById(record.getBuildingLeaderId()).ifPresent(b ->
                        leaderNameMap.put(record.getBuildingLeaderId(), residentMap.get(b.getResidentId())));
            }
        }

        List<DistributionRecordItemResponse> list = records.stream()
                .map(record -> distributionConverter.toListItem(
                        record,
                        orderMap.get(record.getOrderId()),
                        deliveryMap.get(record.getOrderId()),
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

    private void addToPoolFromDistribution(String propertyCompanyId, BigDecimal amount, String orderId) {
        PointPool pool = pointPoolRepository.findByPropertyCompanyId(propertyCompanyId).orElse(null);
        if (pool == null) {
            return;
        }

        BigDecimal newBalance = defaultDecimal(pool.getBalance()).add(amount);
        pool.setBalance(newBalance);
        pool.setTotalIn(defaultDecimal(pool.getTotalIn()).add(amount));
        pointPoolRepository.save(pool);

        PointPoolRecord poolRecord = new PointPoolRecord();
        poolRecord.setId(idGenerator.generateId("ppr"));
        poolRecord.setPropertyCompanyId(propertyCompanyId);
        poolRecord.setPoolType("pool_in");
        poolRecord.setAmount(amount);
        poolRecord.setBalance(newBalance);
        poolRecord.setSource("exchange_diff");
        poolRecord.setSourceId(orderId);
        poolRecord.setDescription("订单分成入积分池");
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
            BigDecimal commissionBaseAmount,
            BigDecimal deficitAmount,
            BigDecimal platformShare,
            BigDecimal propertyShare,
            BigDecimal coordinatorShare,
            BigDecimal sectorLeaderShare,
            BigDecimal individualLeaderShare,
            BigDecimal managementPoolAmount,
            BigDecimal buildingLeaderShare,
            String coordinatorId,
            String sectorLeaderId,
            String individualLeaderId,
            String buildingLeaderId,
            BigDecimal buildingLeaderRate,
            BigDecimal managerShare,
            String managerId,
            BigDecimal managerRate) {
    }

    /** 订单楼长绑定快照（id + 分成比例） */
    private record BuildingLeaderSection(String id, BigDecimal rate) {
    }
}
