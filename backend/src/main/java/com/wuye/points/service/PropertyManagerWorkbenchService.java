package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.buildingleader.AppointBuildingLeaderRequest;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderDetailResponse;
import com.wuye.points.dto.admin.buildingleader.BuildingLeaderListItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerEarningItemResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerMyResponse;
import com.wuye.points.dto.propertymanager.PropertyManagerWalletResponse;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.PropertyManager;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WalletFlow;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyManagerRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 物业经理工作台：本人信息、楼长管理、抽成收益、钱包概览。
 *
 * <p>经理抽成流水落在 wallet_flows（ownerType=property_manager，ownerId=经理 residentId，
 * entryType=property_manager_share，含退款冲正负向流水），钱包余额实时聚合。
 */
@Service
@RequiredArgsConstructor
public class PropertyManagerWorkbenchService {

    private static final String OWNER_TYPE_PROPERTY_MANAGER = "property_manager";
    private static final String WITHDRAWAL_TYPE_PROPERTY_MANAGER = "property_manager";

    private final PropertyManagerRepository propertyManagerRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final WalletFlowRepository walletFlowRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final RoleWithdrawableService roleWithdrawableService;
    private final AdminBuildingLeaderService adminBuildingLeaderService;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public PropertyManagerMyResponse getMyInfo() {
        PropertyManager manager = requireCurrentManager();
        Resident resident = residentRepository.findById(manager.getResidentId()).orElse(null);
        PropertyCompany company = propertyCompanyRepository.findById(manager.getPropertyCompanyId()).orElse(null);
        return PropertyManagerMyResponse.builder()
                .propertyManagerId(manager.getId())
                .residentId(manager.getResidentId())
                .residentName(resident != null ? resident.getName() : null)
                .propertyCompanyId(manager.getPropertyCompanyId())
                .propertyCompanyName(company != null ? company.getName() : null)
                .commissionRate(manager.getCommissionRate())
                .status(manager.getStatus())
                .build();
    }

    /** 名下楼长列表（本物业公司范围） */
    @Transactional(readOnly = true)
    public PageResponse<BuildingLeaderListItemResponse> listMyBuildingLeaders(
            int page,
            int pageSize,
            String keyword,
            String status) {
        PropertyManager manager = requireCurrentManager();
        return adminBuildingLeaderService.listBuildingLeadersForScope(
                manager.getPropertyCompanyId(), page, pageSize, keyword, status, null, null);
    }

    /** 指定单元楼长（复用管理端楼长指定核心逻辑，限定本物业公司） */
    @Transactional
    public BuildingLeaderDetailResponse appointMyBuildingLeader(AppointBuildingLeaderRequest request) {
        PropertyManager manager = requireCurrentManager();
        return adminBuildingLeaderService.appointBuildingLeaderForScope(
                request, manager.getPropertyCompanyId());
    }

    /** 抽成收益流水（含退款冲正负向流水） */
    @Transactional(readOnly = true)
    public PageResponse<PropertyManagerEarningItemResponse> listMyEarnings(int page, int pageSize) {
        PropertyManager manager = requireCurrentManager();
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WalletFlow> flowPage = walletFlowRepository
                .findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                        OWNER_TYPE_PROPERTY_MANAGER, manager.getResidentId(), pageable);
        return PageResponse.of(
                flowPage.getContent().stream().map(this::toEarningItem).toList(),
                page,
                pageSize,
                flowPage.getTotalElements());
    }

    /** 钱包概览：累计抽成 − 已提现 − 在途 − 微信分账已实时到账 = 可提现（与提现校验同源） */
    @Transactional(readOnly = true)
    public PropertyManagerWalletResponse getMyWallet() {
        PropertyManager manager = requireCurrentManager();
        RoleWithdrawableService.RoleBalance balance = roleWithdrawableService.forPropertyManager(
                manager.getId(), manager.getResidentId(), manager.getPropertyCompanyId());
        BigDecimal withdrawn = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(manager.getId(), WITHDRAWAL_TYPE_PROPERTY_MANAGER, "completed"));

        return PropertyManagerWalletResponse.builder()
                .totalEarning(balance.creditedAmount())
                .withdrawnAmount(withdrawn)
                .pendingAmount(balance.pendingWithdrawalAmount())
                .withdrawableAmount(balance.withdrawableAmount())
                .feeRate(balance.feeRate())
                .withdrawFeeBearer(RoleWithdrawableService.FEE_BEARER_PAYEE)
                .build();
    }

    /** 解析当前登录住户的在职经理身份；有记录但已停用时返回明确的停用提示 */
    private PropertyManager requireCurrentManager() {
        roleGuard.requireAnyRole("resident");
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        PropertyManager manager = propertyManagerRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByIdAsc(
                        userId, propertyCompanyId, "active")
                .orElse(null);
        if (manager == null) {
            boolean inactiveExists = propertyManagerRepository.findByResidentId(userId).stream()
                    .anyMatch(item -> propertyCompanyId == null
                            || item.getPropertyCompanyId() == null
                            || propertyCompanyId.equals(item.getPropertyCompanyId()));
            if (inactiveExists) {
                throw new BizException(ErrorCode.PROPERTY_MANAGER_NOT_ACTIVE);
            }
            throw new BizException(ErrorCode.PROPERTY_MANAGER_NOT_FOUND, "当前用户不是物业经理");
        }
        return manager;
    }

    private PropertyManagerEarningItemResponse toEarningItem(WalletFlow flow) {
        return PropertyManagerEarningItemResponse.builder()
                .id(flow.getId())
                .orderId(flow.getOrderId())
                .distributionRecordId(flow.getDistributionRecordId())
                .entryType(flow.getEntryType())
                .amount(flow.getAmount())
                .balanceType(flow.getBalanceType())
                .refRecordId(flow.getRefRecordId())
                .createdAt(DateTimeUtil.toIsoUtc(flow.getCreatedAt()))
                .build();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
