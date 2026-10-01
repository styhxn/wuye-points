package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.coordinator.AddCoordinatorMerchantRequest;
import com.wuye.points.dto.coordinator.AddIndividualLeaderRequest;
import com.wuye.points.dto.coordinator.AddSectorLeaderRequest;
import com.wuye.points.dto.coordinator.CoordinatorMerchantCreatedResponse;
import com.wuye.points.dto.coordinator.CoordinatorMyResponse;
import com.wuye.points.dto.coordinator.FreezeMerchantRequest;
import com.wuye.points.dto.coordinator.IndividualLeaderCreatedResponse;
import com.wuye.points.dto.coordinator.MerchantFreezeResponse;
import com.wuye.points.dto.coordinator.SectorLeaderCreatedResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.MerchantPropertyConfig;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PlatformMerchant;
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
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.SpecialOfferRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SectorCatalog;
import com.wuye.points.util.SnowflakeIdGenerator;
import com.wuye.points.support.RateDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoordinatorWorkbenchService {

    private static final Set<String> MERCHANT_OPERATION_STATUS = Set.of("active", "frozen");
    // 注意：这里是「负责人佣金率」，与 DistributionService 的「分账比例」是不同概念，不可混用
    private static final BigDecimal DEFAULT_SECTOR_LEADER_RATE = RateDefaults.SECTOR_LEADER_COMMISSION_RATE;
    private static final BigDecimal DEFAULT_INDIVIDUAL_LEADER_RATE = RateDefaults.INDIVIDUAL_LEADER_COMMISSION_RATE;

    private final CoordinatorRepository coordinatorRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    private final PlatformMerchantRepository platformMerchantRepository;
    private final SpecialOfferRepository specialOfferRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public CoordinatorMyResponse getMyInfo() {
        roleGuard.requireAnyRole("coordinator");

        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();

        Coordinator coordinator = coordinatorRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND, "当前用户不是统筹"));

        Resident resident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
        PropertyCompany company = propertyCompanyRepository.findById(coordinator.getPropertyCompanyId()).orElse(null);

        long sectorLeaderCount = sectorLeaderRepository.countByCoordinatorIdAndStatus(coordinator.getId(), "active");
        long merchantCount = merchantRepository.countByPropertyCompanyId(coordinator.getPropertyCompanyId());
        long activeOfferCount = specialOfferRepository.countByPropertyCompanyIdAndStatus(
                coordinator.getPropertyCompanyId(), "published");

        BigDecimal totalEarnings = distributionRecordRepository
                .sumCoordinatorShareByCoordinatorId(coordinator.getId());
        BigDecimal occupied = withdrawalRecordRepository
                .sumAmountByApplicant(coordinator.getId(), "coordinator", null);
        BigDecimal withdrawableAmount = defaultDecimal(totalEarnings)
                .subtract(defaultDecimal(occupied)).max(BigDecimal.ZERO);

        return CoordinatorMyResponse.builder()
                .id(coordinator.getId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(coordinator.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .commissionRate(coordinator.getCommissionRate())
                .sectorLeaderCount(sectorLeaderCount)
                .individualLeaderCount(0L)
                .merchantCount(merchantCount)
                .activeSpecialOfferCount(activeOfferCount)
                .totalEarnings(totalEarnings)
                .withdrawableAmount(withdrawableAmount)
                .status(coordinator.getStatus())
                .build();
    }

    @Transactional
    public SectorLeaderCreatedResponse addSectorLeader(String coordinatorId, AddSectorLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin");
        adminSupport.validateIdPrefix(coordinatorId, "coo_");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");
        String sector = SectorCatalog.normalizeOrThrow(request.getSector());

        Coordinator coordinator = getAccessibleCoordinator(coordinatorId);
        String propertyCompanyId = coordinator.getPropertyCompanyId();

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());

        sectorLeaderRepository.findByResidentIdAndPropertyCompanyIdAndSector(
                        resident.getId(), propertyCompanyId, sector)
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是此板块负责人");
                });

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        SectorLeader leader = new SectorLeader();
        leader.setId(idGenerator.generateId("sl"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setSector(sector);
        leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company,
                DEFAULT_SECTOR_LEADER_RATE, true));
        leader.setCoordinatorId(coordinator.getId());
        leader.setCanManageIndividualLeaders(true);
        leader.setStatus("active");
        sectorLeaderRepository.save(leader);

        return SectorLeaderCreatedResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .coordinatorId(leader.getCoordinatorId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .sector(leader.getSector())
                .commissionRate(leader.getCommissionRate())
                .status(leader.getStatus())
                .build();
    }

    @Transactional
    public IndividualLeaderCreatedResponse addIndividualLeader(String coordinatorId, AddIndividualLeaderRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin");
        adminSupport.validateIdPrefix(coordinatorId, "coo_");
        adminSupport.validateIdPrefix(request.getResidentId(), "res_");
        adminSupport.validateIdPrefix(request.getSectorLeaderId(), "sl_");
        String sector = SectorCatalog.normalizeOrThrow(request.getSector());

        Coordinator coordinator = getAccessibleCoordinator(coordinatorId);
        String propertyCompanyId = coordinator.getPropertyCompanyId();

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        adminSupport.assertSameProperty(resident.getPropertyCompanyId());

        SectorLeader sectorLeader = sectorLeaderRepository.findByIdAndPropertyCompanyId(
                        request.getSectorLeaderId(), propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND));
        if (sectorLeader.getCoordinatorId() == null
                || !sectorLeader.getCoordinatorId().equals(coordinator.getId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "板块负责人不属于当前统筹");
        }
        if (!sector.equals(sectorLeader.getSector())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "sector与板块负责人不匹配：该板块负责人负责「" + SectorCatalog.label(sectorLeader.getSector())
                            + "」，不能指派到「" + SectorCatalog.label(sector) + "」");
        }

        individualLeaderRepository.findByResidentIdAndPropertyCompanyIdAndSectorLeaderId(
                        resident.getId(), propertyCompanyId, sectorLeader.getId())
                .ifPresent(existing -> {
                    throw new BizException(ErrorCode.PARAM_INVALID, "该住户已是个体负责人");
                });

        PropertyCompany company = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));

        IndividualLeader leader = new IndividualLeader();
        leader.setId(idGenerator.generateId("il"));
        leader.setResidentId(resident.getId());
        leader.setPropertyCompanyId(propertyCompanyId);
        leader.setSectorLeaderId(sectorLeader.getId());
        leader.setSector(sector);
        leader.setName(StringUtils.hasText(request.getName()) ? request.getName().trim() : resident.getName());
        leader.setCommissionRate(resolveCommissionRate(request.getCommissionRate(), company,
                DEFAULT_INDIVIDUAL_LEADER_RATE, false));
        leader.setStatus("active");
        individualLeaderRepository.save(leader);

        return IndividualLeaderCreatedResponse.builder()
                .id(leader.getId())
                .residentId(leader.getResidentId())
                .sectorLeaderId(leader.getSectorLeaderId())
                .propertyCompanyId(leader.getPropertyCompanyId())
                .sector(leader.getSector())
                .name(leader.getName())
                .commissionRate(leader.getCommissionRate())
                .status(leader.getStatus())
                .build();
    }

    @Transactional
    public CoordinatorMerchantCreatedResponse addMerchant(String coordinatorId, AddCoordinatorMerchantRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin");
        adminSupport.validateIdPrefix(coordinatorId, "coo_");
        adminSupport.validateIdPrefix(request.getPlatformMerchantId(), "pm_");

        Coordinator coordinator = getAccessibleCoordinator(coordinatorId);
        String propertyCompanyId = coordinator.getPropertyCompanyId();

        platformMerchantRepository.findById(request.getPlatformMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND, "平台商家不存在"));

        if (merchantRepository.existsByPlatformMerchantIdAndPropertyCompanyId(
                request.getPlatformMerchantId(), propertyCompanyId)) {
            throw new BizException(ErrorCode.MERCHANT_DUPLICATE_BINDING);
        }

        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.generateId("mch"));
        merchant.setPlatformMerchantId(request.getPlatformMerchantId());
        merchant.setPropertyCompanyId(propertyCompanyId);
        merchant.setName(request.getName());
        merchant.setCategory(request.getCategory());
        merchant.setContactPhone(request.getContactPhone());
        if (StringUtils.hasText(request.getContactPhone())) {
            // 有对应用户的手机号时补全店主住户ID（聊天对端）
            residentRepository.findByPhoneAndPropertyCompanyId(
                            request.getContactPhone(), propertyCompanyId)
                    .ifPresent(resident -> merchant.setResidentId(resident.getId()));
        }
        merchant.setAddress(request.getAddress());
        merchant.setMerchantLevel("normal");
        merchant.setLevelWeight(0);
        merchant.setRankOrder(0);
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        merchant.setServeAllCommunities(false);
        merchantRepository.save(merchant);

        // 同步创建物业挂接配置：缺失时业主下单/支付会报 60006（商家在该物业下未完成挂接配置）
        merchantPropertyConfigRepository
                .findByMerchantIdAndPropertyCompanyId(merchant.getId(), propertyCompanyId)
                .orElseGet(() -> {
                    MerchantPropertyConfig config = new MerchantPropertyConfig();
                    config.setId(idGenerator.generateId("mpc"));
                    config.setMerchantId(merchant.getId());
                    config.setPropertyCompanyId(propertyCompanyId);
                    config.setCommissionRate(RateDefaults.MERCHANT_COMMISSION_RATE);
                    config.setPointExchangeRate(RateDefaults.MERCHANT_POINT_EXCHANGE_RATE);
                    config.setCoinRebateRate(BigDecimal.ZERO);
                    config.setCoinRebateEnabled(false);
                    config.setStatus("active");
                    return merchantPropertyConfigRepository.save(config);
                });

        return CoordinatorMerchantCreatedResponse.builder()
                .id(merchant.getId())
                .platformMerchantId(merchant.getPlatformMerchantId())
                .propertyCompanyId(merchant.getPropertyCompanyId())
                .coordinatorId(coordinator.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .auditStatus(merchant.getAuditStatus())
                .status(merchant.getStatus())
                .build();
    }

    @Transactional
    public MerchantFreezeResponse freezeMerchant(String coordinatorId, String merchantId, FreezeMerchantRequest request) {
        roleGuard.requireAnyRole("coordinator", "property_admin");
        adminSupport.validateIdPrefix(coordinatorId, "coo_");
        adminSupport.validateIdPrefix(merchantId, "mch_");

        Coordinator coordinator = getAccessibleCoordinator(coordinatorId);
        Merchant merchant = merchantRepository.findByIdAndPropertyCompanyId(merchantId, coordinator.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));

        String targetStatus = Boolean.TRUE.equals(request.getFrozen()) ? "frozen" : "active";
        if (!MERCHANT_OPERATION_STATUS.contains(targetStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status不合法");
        }
        merchant.setStatus(targetStatus);
        merchantRepository.save(merchant);

        return MerchantFreezeResponse.builder()
                .id(merchant.getId())
                .status(merchant.getStatus())
                .frozen("frozen".equals(merchant.getStatus()))
                .build();
    }

    private Coordinator getAccessibleCoordinator(String coordinatorId) {
        Coordinator coordinator = coordinatorRepository.findById(coordinatorId)
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND));
        adminSupport.assertSameProperty(coordinator.getPropertyCompanyId());
        if (roleGuard.isCoordinator()) {
            if (!"active".equals(coordinator.getStatus())) {
                throw new BizException(ErrorCode.COORDINATOR_NOT_FOUND, "统筹未激活");
            }
            String currentUserId = UserContext.currentUserId();
            if (coordinator.getResidentId() == null
                    || !coordinator.getResidentId().equals(currentUserId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "统筹仅可管理本人名下资源");
            }
        }
        return coordinator;
    }

    /**
     * 解析负责人佣金率。
     *
     * <p>原实现用 {@code defaultRate.compareTo(DEFAULT_SECTOR_LEADER_RATE) == 0}
     * 来反推"当前是在设置板块负责人还是个体负责人"，属于<b>用参数值判断调用场景</b>的脆弱写法：
     * 一旦两个默认值取到同一个数值，个体负责人就会错误地回退到公司的板块负责人费率。
     * 现改为由调用方显式声明是否回退到公司配置。
     *
     * @param fallbackToCompanyRate 未指定佣金率时，是否回退到物业公司的板块负责人费率
     */
    private BigDecimal resolveCommissionRate(BigDecimal requestedRate, PropertyCompany company,
                                             BigDecimal defaultRate, boolean fallbackToCompanyRate) {
        if (requestedRate != null) {
            return requestedRate;
        }
        if (fallbackToCompanyRate && company.getSectorLeaderRate() != null) {
            return company.getSectorLeaderRate();
        }
        return defaultRate;
    }

    /** 2026-09-08：板块字典统一走 {@link SectorCatalog}，并支持中文入参 */
    private void validateSector(String sector) {
        SectorCatalog.validate(sector);
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
