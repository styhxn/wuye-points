package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.individualleader.IndividualLeaderMerchantRevenueItemResponse;
import com.wuye.points.dto.individualleader.IndividualLeaderMyResponse;
import com.wuye.points.dto.sectorleader.MerchantDeliveryFeeResponse;
import com.wuye.points.dto.sectorleader.PriceApprovalResponse;
import com.wuye.points.dto.sectorleader.SetFreeDeliveryThresholdRequest;
import com.wuye.points.dto.sectorleader.SetMerchantDistributionRequest;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PriceApproval;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PriceApprovalRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndividualLeaderWorkbenchService {

    private static final String ITEM_TYPE_MERCHANT_DISTRIBUTION = "merchant_distribution";

    private final IndividualLeaderRepository individualLeaderRepository;
    private final MerchantRepository merchantRepository;
    private final PriceApprovalRepository priceApprovalRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public IndividualLeaderMyResponse getMyInfo() {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        Resident resident = residentRepository.findById(current.getResidentId()).orElse(null);
        PropertyCompany company = propertyCompanyRepository.findById(current.getPropertyCompanyId()).orElse(null);

        return IndividualLeaderMyResponse.builder()
                .id(current.getId())
                .residentId(current.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .phone(resident != null ? resident.getPhone() : null)
                .name(current.getName())
                .propertyCompanyId(current.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .sectorLeaderId(current.getSectorLeaderId())
                .sector(current.getSector())
                .commissionRate(current.getCommissionRate())
                .totalEarnings(distributionRecordRepository
                        .sumIndividualLeaderShareByIndividualLeaderId(current.getId()))
                .status(current.getStatus())
                .build();
    }

    /**
     * 接口6：个体负责人设置与商家分成（需提交模块负责人同意）
     */
    @Transactional
    public PriceApprovalResponse setMerchantDistribution(
            String individualLeaderId, SetMerchantDistributionRequest request) {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        assertSelf(current, individualLeaderId);

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!current.getPropertyCompanyId().equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "商家不属于当前物业公司");
        }

        BigDecimal newRate = request.getCommissionRate();

        PriceApproval approval = new PriceApproval();
        approval.setId(idGenerator.generateId("pa"));
        approval.setPropertyCompanyId(current.getPropertyCompanyId());
        approval.setApplicantId(current.getResidentId());
        approval.setApplicantRole("individual_leader");
        approval.setItemType(ITEM_TYPE_MERCHANT_DISTRIBUTION);
        approval.setItemId(merchant.getId());
        approval.setOldValue(null);
        approval.setNewValue(newRate.toPlainString());
        approval.setReason(request.getReason());
        approval.setStatus("pending");
        priceApprovalRepository.save(approval);

        return toPriceApprovalResponse(approval);
    }

    /**
     * 接口7：个体负责人设置满额配送费
     */
    @Transactional
    public MerchantDeliveryFeeResponse setDeliveryFee(
            String individualLeaderId, SetFreeDeliveryThresholdRequest request) {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        assertSelf(current, individualLeaderId);

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!current.getPropertyCompanyId().equals(merchant.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "商家不属于当前物业公司");
        }

        merchant.setFreeDeliveryThreshold(request.getFreeDeliveryThreshold());
        merchantRepository.save(merchant);

        return MerchantDeliveryFeeResponse.builder()
                .id(merchant.getId())
                .name(merchant.getName())
                .deliveryFee(merchant.getDeliveryFee())
                .freeDeliveryThreshold(merchant.getFreeDeliveryThreshold())
                .status(merchant.getStatus())
                .build();
    }

    private IndividualLeader getCurrentIndividualLeader() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return individualLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.INDIVIDUAL_LEADER_NOT_FOUND, "当前用户不是个体负责人"));
    }

    /**
     * 路径 id 可为：个体负责人记录 id（il_）、登录住户 id（res_）、或 "me"。
     */
    static void assertSelf(IndividualLeader current, String pathId) {
        if (!StringUtils.hasText(pathId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他个体负责人");
        }
        if ("me".equalsIgnoreCase(pathId)
                || current.getId().equals(pathId)
                || current.getResidentId().equals(pathId)) {
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他个体负责人");
    }

    /**
     * 我新增/归属的商家：含审核状态、营收情况（营收=该商家产生且归属我的分成记录订单总额）。
     */
    @Transactional(readOnly = true)
    public List<IndividualLeaderMerchantRevenueItemResponse> listMyMerchantRevenues() {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        List<Merchant> merchants = merchantRepository.findByIndividualLeaderId(current.getId());
        if (merchants.isEmpty()) {
            return List.of();
        }
        Map<String, List<DistributionRecord>> byMerchant =
                distributionRecordRepository.findByIndividualLeaderId(current.getId()).stream()
                        .filter(r -> r.getMerchantId() != null)
                        .collect(Collectors.groupingBy(DistributionRecord::getMerchantId));
        return merchants.stream()
                .map(merchant -> {
                    List<DistributionRecord> records =
                            byMerchant.getOrDefault(merchant.getId(), List.of());
                    BigDecimal revenue = records.stream()
                            .map(DistributionRecord::getTotalAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal myShare = records.stream()
                            .map(DistributionRecord::getIndividualLeaderShare)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return IndividualLeaderMerchantRevenueItemResponse.builder()
                            .merchantId(merchant.getId())
                            .merchantName(merchant.getName())
                            .auditStatus(merchant.getAuditStatus())
                            .revenue(revenue)
                            .orderCount(records.size())
                            .myShare(myShare)
                            .build();
                })
                .toList();
    }

    /**
     * 查看商家审批情况：我提交的商家分成审批记录（含待审/通过/驳回）。
     */
    @Transactional(readOnly = true)
    public List<PriceApprovalResponse> listMyApprovals() {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        return priceApprovalRepository.findByApplicantIdOrderByCreatedAtDesc(current.getResidentId()).stream()
                .filter(a -> ITEM_TYPE_MERCHANT_DISTRIBUTION.equals(a.getItemType()))
                .map(this::toPriceApprovalResponse)
                .toList();
    }

    private PriceApprovalResponse toPriceApprovalResponse(PriceApproval approval) {
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
