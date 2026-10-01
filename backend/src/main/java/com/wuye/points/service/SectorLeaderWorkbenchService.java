package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.sectorleader.AuditMerchantJoinRequest;
import com.wuye.points.dto.sectorleader.MerchantUploadResponse;
import com.wuye.points.dto.sectorleader.PriceApprovalResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderEarningItemResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderEarningsResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMerchantItemResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMerchantRevenueItemResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMyResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PriceApproval;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PriceApprovalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorLeaderWorkbenchService {

    private final SectorLeaderRepository sectorLeaderRepository;
    private final ResidentRepository residentRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final MerchantRepository merchantRepository;
    private final PriceApprovalRepository priceApprovalRepository;
    private final PriceApprovalService priceApprovalService;
    private final DistributionConverter distributionConverter;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public SectorLeaderMyResponse getMyInfo() {
        roleGuard.requireAnyRole("sector_leader");

        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();

        SectorLeader leader = sectorLeaderRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND, "当前用户不是板块负责人"));

        Resident resident = residentRepository.findById(leader.getResidentId()).orElse(null);
        PropertyCompany company = propertyCompanyRepository.findById(leader.getPropertyCompanyId()).orElse(null);

        String coordinatorName = null;
        if (leader.getCoordinatorId() != null) {
            Coordinator coordinator = coordinatorRepository.findById(leader.getCoordinatorId()).orElse(null);
            if (coordinator != null) {
                Resident cooResident = residentRepository.findById(coordinator.getResidentId()).orElse(null);
                coordinatorName = cooResident != null ? cooResident.getName() : null;
            }
        }

        long individualLeaderCount = individualLeaderRepository.countBySectorLeaderIdAndStatus(leader.getId(), "active");
        long merchantCount = merchantRepository.countByPropertyCompanyId(leader.getPropertyCompanyId());
        long activeOfferCount = 0;
        if (leader.getPropertyCompanyId() != null) {
            activeOfferCount = distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(leader.getId()) != null ? 0 : 0;
        }

        return SectorLeaderMyResponse.builder()
                .id(leader.getId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .propertyCompanyId(leader.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .sector(leader.getSector())
                .sectorName(distributionConverter.sectorLabel(leader.getSector()))
                .coordinatorId(leader.getCoordinatorId())
                .coordinatorName(coordinatorName)
                .individualLeaderCount(individualLeaderCount)
                .merchantCount(merchantCount)
                .activeSpecialOfferCount(activeOfferCount)
                .totalEarnings(distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(leader.getId()))
                .status(leader.getStatus())
                .build();
    }

    /**
     * 统筹审核板块提交的商家加入申请。
     */
    @Transactional
    public MerchantUploadResponse auditMerchantJoin(String coordinatorId, AuditMerchantJoinRequest request) {
        roleGuard.requireAnyRole("coordinator");

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!"pending_audit".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家不在待审核状态");
        }

        if (request.getApproved()) {
            merchant.setAuditStatus("approved");
            merchant.setStatus("active");
        } else {
            merchant.setAuditStatus("rejected");
        }
        merchantRepository.save(merchant);

        return MerchantUploadResponse.builder()
                .id(merchant.getId())
                .platformMerchantId(merchant.getPlatformMerchantId())
                .propertyCompanyId(merchant.getPropertyCompanyId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .deliveryFee(merchant.getDeliveryFee())
                .freeDeliveryThreshold(merchant.getFreeDeliveryThreshold())
                .auditStatus(merchant.getAuditStatus())
                .status(merchant.getStatus())
                .build();
    }

    /**
     * 当前板块负责人（按当前登录住户+物业公司解析）。
     */
    private SectorLeader resolveCurrentLeader() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return sectorLeaderRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND, "当前用户不是板块负责人"));
    }

    /**
     * 本板块商家：以本负责人名下产生过分成记录的商家为准。
     */
    @Transactional(readOnly = true)
    public List<SectorLeaderMerchantItemResponse> listMyMerchants() {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        Set<String> merchantIds = distributionRecordRepository.findBySectorLeaderId(leader.getId()).stream()
                .map(DistributionRecord::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return List.of();
        }
        Map<String, Merchant> merchantMap = merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
        return merchantIds.stream()
                .map(merchantMap::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Merchant::getId))
                .map(this::toMerchantItem)
                .toList();
    }

    /**
     * 收入概览：累计分成 + 明细（最近200条）。
     */
    @Transactional(readOnly = true)
    public SectorLeaderEarningsResponse getMyEarnings() {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        List<DistributionRecord> records = distributionRecordRepository.findBySectorLeaderId(leader.getId());
        BigDecimal total = records.stream()
                .map(DistributionRecord::getSectorLeaderShare)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Merchant> merchantMap = resolveMerchantMap(records);
        List<SectorLeaderEarningItemResponse> items = records.stream()
                .sorted(Comparator.comparing(DistributionRecord::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(200)
                .map(record -> SectorLeaderEarningItemResponse.builder()
                        .id(record.getId())
                        .orderId(record.getOrderId())
                        .merchantId(record.getMerchantId())
                        .merchantName(merchantMap.get(record.getMerchantId()) != null
                                ? merchantMap.get(record.getMerchantId()).getName() : null)
                        .amount(record.getSectorLeaderShare())
                        .createdAt(record.getCreatedAt())
                        .build())
                .toList();
        return SectorLeaderEarningsResponse.builder()
                .totalEarnings(total)
                .recordCount(records.size())
                .records(items)
                .build();
    }

    /**
     * 待我关注的审批：本板块个体负责人发起、尚未审批的商家分成审批。
     */
    @Transactional(readOnly = true)
    public List<PriceApprovalResponse> listMyApprovals() {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        Set<String> applicantResidentIds = individualLeaderRepository.findBySectorLeaderId(leader.getId()).stream()
                .map(IndividualLeader::getResidentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (applicantResidentIds.isEmpty()) {
            return List.of();
        }
        return priceApprovalRepository
                .findByPropertyCompanyIdAndStatusOrderByCreatedAtDesc(leader.getPropertyCompanyId(), "pending")
                .stream()
                .filter(a -> "merchant_distribution".equals(a.getItemType()))
                .filter(a -> applicantResidentIds.contains(a.getApplicantId()))
                .map(this::toApprovalResponse)
                .toList();
    }

    /**
     * 审批商家的加入：仅可审批本板块个体负责人新增的商家。同意后商家即可登录商家平台。
     */
    @Transactional
    public SectorLeaderMerchantItemResponse auditMerchantJoinByLeader(
            String merchantId, boolean approved, String remark) {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        Set<String> myIlIds = individualLeaderRepository.findBySectorLeaderId(leader.getId()).stream()
                .map(IndividualLeader::getId)
                .collect(Collectors.toSet());
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (merchant.getIndividualLeaderId() == null || !myIlIds.contains(merchant.getIndividualLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "该商家不属于本板块个体负责人新增，无需审批");
        }
        if (!"pending_audit".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家不在待审核状态");
        }
        merchant.setAuditStatus(Boolean.TRUE.equals(approved) ? "approved" : "rejected");
        merchant.setStatus(Boolean.TRUE.equals(approved) ? "active" : "inactive");
        merchantRepository.save(merchant);
        return toMerchantItem(merchant);
    }

    /**
     * 审批商家的分成比例：审批本板块个体负责人提交的 merchant_distribution 申请。
     */
    @Transactional
    public Object auditMerchantDistribution(String approvalId, boolean approved, String remark) {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        return priceApprovalService.auditAsSectorLeader(approvalId, leader.getId(), approved, remark);
    }

    /**
     * 所负责商家的数量与营收情况：按本负责人分成记录中的商家聚合。
     */
    @Transactional(readOnly = true)
    public List<SectorLeaderMerchantRevenueItemResponse> listMyMerchantRevenues() {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader leader = resolveCurrentLeader();
        List<DistributionRecord> records = distributionRecordRepository.findBySectorLeaderId(leader.getId());
        Map<String, Merchant> merchantMap = resolveMerchantMap(records);
        return records.stream()
                .filter(r -> r.getMerchantId() != null)
                .collect(Collectors.groupingBy(DistributionRecord::getMerchantId))
                .entrySet().stream()
                .map(e -> {
                    List<DistributionRecord> list = e.getValue();
                    BigDecimal revenue = list.stream()
                            .map(DistributionRecord::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal myShare = list.stream()
                            .map(DistributionRecord::getSectorLeaderShare)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Merchant merchant = merchantMap.get(e.getKey());
                    return SectorLeaderMerchantRevenueItemResponse.builder()
                            .merchantId(e.getKey())
                            .merchantName(merchant != null ? merchant.getName() : null)
                            .revenue(revenue)
                            .orderCount(list.size())
                            .myShare(myShare)
                            .build();
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .toList();
    }

    private Map<String, Merchant> resolveMerchantMap(List<DistributionRecord> records) {
        Set<String> merchantIds = records.stream()
                .map(DistributionRecord::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity()));
    }

    private SectorLeaderMerchantItemResponse toMerchantItem(Merchant merchant) {
        return SectorLeaderMerchantItemResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .category(merchant.getCategory())
                .contactPhone(merchant.getContactPhone())
                .address(merchant.getAddress())
                .auditStatus(merchant.getAuditStatus())
                .status(merchant.getStatus())
                .build();
    }

    private PriceApprovalResponse toApprovalResponse(PriceApproval approval) {
        return PriceApprovalResponse.builder()
                .id(approval.getId())
                .propertyCompanyId(approval.getPropertyCompanyId())
                .applicantId(approval.getApplicantId())
                .applicantRole(approval.getApplicantRole())
                .itemType(approval.getItemType())
                .itemId(approval.getItemId())
                .oldValue(approval.getOldValue())
                .newValue(approval.getNewValue())
                .reason(approval.getReason())
                .status(approval.getStatus())
                .approverId(approval.getApproverId())
                .approverRemark(approval.getApproverRemark())
                .createdAt(DateTimeUtil.toIsoUtc(approval.getCreatedAt()))
                .build();
    }
}
