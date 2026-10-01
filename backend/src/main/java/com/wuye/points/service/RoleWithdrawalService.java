package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.CourierManager;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CoordinatorRepository;
import com.wuye.points.repository.CourierManagerRepository;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.IndividualLeaderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SectorLeaderRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import com.wuye.points.repository.spec.WithdrawalRecordSpecifications;
import com.wuye.points.util.DateTimeUtil;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * 多角色提现服务（个体负责人、快递员）
 */
@Service
@RequiredArgsConstructor
public class RoleWithdrawalService {

    private static final String WITHDRAWAL_TYPE_INDIVIDUAL_LEADER = "individual_leader";
    private static final String WITHDRAWAL_TYPE_COURIER = "courier";
    private static final String WITHDRAWAL_TYPE_COORDINATOR = "coordinator";
    private static final String WITHDRAWAL_TYPE_SECTOR_LEADER = "sector_leader";
    private static final Set<String> WITHDRAWAL_AUDIT_STATUS = Set.of(
            "pending", "approved", "rejected", "completed");

    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final IndividualLeaderRepository individualLeaderRepository;
    private final CourierManagerRepository courierManagerRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final SectorLeaderRepository sectorLeaderRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ResidentRepository residentRepository;
    private final WithdrawableService withdrawableService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final PlatformSettingService platformSetting;

    // ============== 个体负责人提现 ==============

    @Transactional
    public RoleWithdrawalItemResponse applyForIndividualLeader(
            String individualLeaderId, ApplyRoleWithdrawalRequest request) {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        IndividualLeaderWorkbenchService.assertSelf(current, individualLeaderId);
        assertNotBlocked(current.getResidentId());

        // 可提现余额校验：已产生分成 - 已申请（含在途）提现
        BigDecimal earned = defaultDecimal(distributionRecordRepository
                .sumIndividualLeaderShareByIndividualLeaderId(current.getId()));
        BigDecimal occupied = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(current.getId(), WITHDRAWAL_TYPE_INDIVIDUAL_LEADER, null));
        BigDecimal withdrawable = earned.subtract(occupied).max(BigDecimal.ZERO);
        if (request.getAmount().compareTo(withdrawable) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "可提现余额不足");
        }

        PropertyCompany propertyCompany = propertyCompanyRepository.findById(current.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = request.getAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = request.getAmount().subtract(feeAmount);
        BigDecimal platformFeeShare = resolvePlatformFeeShare(feeAmount, propertyCompany);

        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setId(idGenerator.generateId("wr"));
        withdrawal.setWithdrawalType(WITHDRAWAL_TYPE_INDIVIDUAL_LEADER);
        withdrawal.setApplicantId(current.getId());
        withdrawal.setResidentId(current.getResidentId());
        withdrawal.setPropertyCompanyId(current.getPropertyCompanyId());
        withdrawal.setAmount(request.getAmount());
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setPlatformFeeShare(platformFeeShare);
        withdrawal.setAuditStatus("pending");
        withdrawalRecordRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleWithdrawalItemResponse> listForIndividualLeader(
            String individualLeaderId,
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("individual_leader");
        IndividualLeader current = getCurrentIndividualLeader();
        IndividualLeaderWorkbenchService.assertSelf(current, individualLeaderId);

        validatePageAndStatus(pageSize, auditStatus);
        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<WithdrawalRecord> withdrawalPage = withdrawalRecordRepository.findAll(
                WithdrawalRecordSpecifications.withFilters(
                        WITHDRAWAL_TYPE_INDIVIDUAL_LEADER, current.getId(),
                        current.getPropertyCompanyId(), auditStatus, start, end),
                pageable);

        return PageResponse.of(
                withdrawalPage.getContent().stream().map(this::toItemResponse).toList(),
                page,
                pageSize,
                withdrawalPage.getTotalElements());
    }

    // ============== 统筹提现 ==============

    @Transactional
    public RoleWithdrawalItemResponse applyForCoordinator(
            String coordinatorId, ApplyRoleWithdrawalRequest request) {
        roleGuard.requireAnyRole("coordinator");
        Coordinator current = getCurrentCoordinator();
        if (!current.getId().equals(coordinatorId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他统筹");
        }
        assertNotBlocked(current.getResidentId());

        BigDecimal earned = defaultDecimal(distributionRecordRepository
                .sumCoordinatorShareByCoordinatorId(current.getId()));
        BigDecimal occupied = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(current.getId(), WITHDRAWAL_TYPE_COORDINATOR, null));
        BigDecimal withdrawable = earned.subtract(occupied).max(BigDecimal.ZERO);
        if (request.getAmount().compareTo(withdrawable) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "可提现余额不足");
        }

        return buildAndSave(current.getId(), current.getResidentId(), current.getPropertyCompanyId(),
                WITHDRAWAL_TYPE_COORDINATOR, request.getAmount());
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleWithdrawalItemResponse> listForCoordinator(
            String coordinatorId,
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("coordinator");
        Coordinator current = getCurrentCoordinator();
        if (!current.getId().equals(coordinatorId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他统筹");
        }
        return listByType(WITHDRAWAL_TYPE_COORDINATOR, current.getId(),
                current.getPropertyCompanyId(), page, pageSize, auditStatus, startDate, endDate);
    }

    // ============== 板块负责人提现 ==============

    @Transactional
    public RoleWithdrawalItemResponse applyForSectorLeader(
            String sectorLeaderId, ApplyRoleWithdrawalRequest request) {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader current = getCurrentSectorLeader();
        if (!current.getId().equals(sectorLeaderId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他板块负责人");
        }
        assertNotBlocked(current.getResidentId());

        BigDecimal earned = defaultDecimal(distributionRecordRepository
                .sumSectorLeaderShareBySectorLeaderId(current.getId()));
        BigDecimal occupied = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(current.getId(), WITHDRAWAL_TYPE_SECTOR_LEADER, null));
        BigDecimal withdrawable = earned.subtract(occupied).max(BigDecimal.ZERO);
        if (request.getAmount().compareTo(withdrawable) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "可提现余额不足");
        }

        return buildAndSave(current.getId(), current.getResidentId(), current.getPropertyCompanyId(),
                WITHDRAWAL_TYPE_SECTOR_LEADER, request.getAmount());
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleWithdrawalItemResponse> listForSectorLeader(
            String sectorLeaderId,
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("sector_leader");
        SectorLeader current = getCurrentSectorLeader();
        if (!current.getId().equals(sectorLeaderId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作其他板块负责人");
        }
        return listByType(WITHDRAWAL_TYPE_SECTOR_LEADER, current.getId(),
                current.getPropertyCompanyId(), page, pageSize, auditStatus, startDate, endDate);
    }

    // ============== 快递员提现 ==============

    @Transactional
    public RoleWithdrawalItemResponse applyForCourier(ApplyRoleWithdrawalRequest request) {
        roleGuard.requireAnyRole("courier");
        CourierApplicant applicant = resolveCourierApplicant();
        assertNotBlocked(applicant.residentId());

        if (request.getAmount().compareTo(applicant.withdrawableAmount()) > 0) {
            throw new BizException(ErrorCode.MERCHANT_WITHDRAWAL_INVALID, "可提现余额不足");
        }

        PropertyCompany propertyCompany = propertyCompanyRepository.findById(applicant.propertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = request.getAmount().multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = request.getAmount().subtract(feeAmount);
        BigDecimal platformFeeShare = resolvePlatformFeeShare(feeAmount, propertyCompany);

        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setId(idGenerator.generateId("wr"));
        withdrawal.setWithdrawalType(WITHDRAWAL_TYPE_COURIER);
        withdrawal.setApplicantId(applicant.applicantId());
        withdrawal.setResidentId(applicant.residentId());
        withdrawal.setPropertyCompanyId(applicant.propertyCompanyId());
        withdrawal.setAmount(request.getAmount());
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setPlatformFeeShare(platformFeeShare);
        withdrawal.setAuditStatus("pending");
        withdrawalRecordRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleWithdrawalItemResponse> listForCourier(
            int page,
            int pageSize,
            String auditStatus,
            String startDate,
            String endDate) {
        roleGuard.requireAnyRole("courier");
        CourierApplicant applicant = resolveCourierApplicant();

        validatePageAndStatus(pageSize, auditStatus);
        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<WithdrawalRecord> withdrawalPage = withdrawalRecordRepository.findAll(
                WithdrawalRecordSpecifications.withFilters(
                        WITHDRAWAL_TYPE_COURIER, applicant.applicantId(),
                        applicant.propertyCompanyId(), auditStatus, start, end),
                pageable);

        return PageResponse.of(
                withdrawalPage.getContent().stream().map(this::toItemResponse).toList(),
                page,
                pageSize,
                withdrawalPage.getTotalElements());
    }

    // ============== 私有工具 ==============

    private Coordinator getCurrentCoordinator() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return coordinatorRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.COORDINATOR_NOT_FOUND, "当前用户不是统筹"));
    }

    private SectorLeader getCurrentSectorLeader() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return sectorLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.SECTOR_LEADER_NOT_FOUND, "当前用户不是板块负责人"));
    }

    /**
     * 统一创建提现记录（手续费率/平台分成口径与个体负责人、快递员一致）
     */
    private RoleWithdrawalItemResponse buildAndSave(String applicantId, String residentId,
                                                    String propertyCompanyId, String withdrawalType,
                                                    BigDecimal amount) {
        PropertyCompany propertyCompany = propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        BigDecimal feeRate = resolveFeeRate(propertyCompany);
        BigDecimal feeAmount = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = amount.subtract(feeAmount);
        BigDecimal platformFeeShare = resolvePlatformFeeShare(feeAmount, propertyCompany);

        WithdrawalRecord withdrawal = new WithdrawalRecord();
        withdrawal.setId(idGenerator.generateId("wr"));
        withdrawal.setWithdrawalType(withdrawalType);
        withdrawal.setApplicantId(applicantId);
        withdrawal.setResidentId(residentId);
        withdrawal.setPropertyCompanyId(propertyCompanyId);
        withdrawal.setAmount(amount);
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setPlatformFeeShare(platformFeeShare);
        withdrawal.setAuditStatus("pending");
        withdrawalRecordRepository.save(withdrawal);

        return toItemResponse(withdrawal);
    }

    private PageResponse<RoleWithdrawalItemResponse> listByType(
            String withdrawalType, String applicantId, String propertyCompanyId,
            int page, int pageSize, String auditStatus, String startDate, String endDate) {
        validatePageAndStatus(pageSize, auditStatus);
        Pageable pageable = buildPageable(page, pageSize);
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        Page<WithdrawalRecord> withdrawalPage = withdrawalRecordRepository.findAll(
                WithdrawalRecordSpecifications.withFilters(
                        withdrawalType, applicantId, propertyCompanyId, auditStatus, start, end),
                pageable);

        return PageResponse.of(
                withdrawalPage.getContent().stream().map(this::toItemResponse).toList(),
                page,
                pageSize,
                withdrawalPage.getTotalElements());
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private IndividualLeader getCurrentIndividualLeader() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        return individualLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.INDIVIDUAL_LEADER_NOT_FOUND, "当前用户不是个体负责人"));
    }

    private CourierManager getCurrentCourierManager() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND);
        }
        return courierManagerRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(userId, propertyCompanyId, "active")
                .orElseThrow(() -> new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND, "当前用户非快递负责人"));
    }

    /**
     * 解析配送提现申请人：快递负责人优先（汇总名下+本人）；否则按普通配送员个人收益提现。
     */
    private CourierApplicant resolveCourierApplicant() {
        String userId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND);
        }

        CourierManager manager = courierManagerRepository
                .findByResidentIdAndPropertyCompanyIdAndStatus(userId, propertyCompanyId, "active")
                .orElse(null);
        if (manager != null) {
            BigDecimal withdrawable = withdrawableService
                    .calcCourierManagerWithdrawable(manager)
                    .withdrawableAmount();
            return new CourierApplicant(
                    manager.getId(),
                    manager.getResidentId(),
                    manager.getPropertyCompanyId(),
                    withdrawable);
        }

        Resident courier = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!"courier".equals(courier.getRole())) {
            throw new BizException(ErrorCode.COURIER_MANAGER_NOT_FOUND, "当前用户非配送员");
        }
        BigDecimal withdrawable = withdrawableService
                .calcCourierResidentWithdrawable(userId)
                .withdrawableAmount();
        return new CourierApplicant(userId, userId, courier.getPropertyCompanyId(), withdrawable);
    }

    private record CourierApplicant(
            String applicantId,
            String residentId,
            String propertyCompanyId,
            BigDecimal withdrawableAmount
    ) {
    }

    /**
     * 校验关联住户是否被阻止提现
     */
    private void assertNotBlocked(String residentId) {
        if (!StringUtils.hasText(residentId)) {
            return;
        }
        Resident resident = residentRepository.findById(residentId).orElse(null);
        if (resident != null && Boolean.TRUE.equals(resident.getWithdrawalBlocked())) {
            throw new BizException(ErrorCode.WITHDRAWAL_BLOCKED);
        }
    }

    private BigDecimal resolveFeeRate(PropertyCompany propertyCompany) {
        return platformSetting.resolveWithdrawalFeeRate(
                propertyCompany.getWithdrawalFeeRate(), PlatformSettingService.FEE_CATEGORY_ROLE);
    }

    private BigDecimal resolvePlatformFeeShare(BigDecimal feeAmount, PropertyCompany propertyCompany) {
        BigDecimal shareRate = platformSetting.resolvePlatformFeeShareRate(
                propertyCompany.getPlatformWithdrawalFeeShareRate());
        return feeAmount.multiply(shareRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void validatePageAndStatus(int pageSize, String auditStatus) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(auditStatus) && !WITHDRAWAL_AUDIT_STATUS.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditStatus值无效");
        }
    }

    private Pageable buildPageable(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private RoleWithdrawalItemResponse toItemResponse(WithdrawalRecord withdrawal) {
        return RoleWithdrawalItemResponse.builder()
                .id(withdrawal.getId())
                .withdrawalType(withdrawal.getWithdrawalType())
                .applicantId(withdrawal.getApplicantId())
                .residentId(withdrawal.getResidentId())
                .propertyCompanyId(withdrawal.getPropertyCompanyId())
                .amount(withdrawal.getAmount())
                .feeRate(withdrawal.getFeeRate())
                .feeAmount(withdrawal.getFeeAmount())
                .actualAmount(withdrawal.getActualAmount())
                .status(withdrawal.getAuditStatus())
                .auditedAt(DateTimeUtil.toIsoUtc(withdrawal.getAuditedAt()))
                .completedAt(DateTimeUtil.toIsoUtc(withdrawal.getCompletedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(withdrawal.getCreatedAt()))
                .build();
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
