package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.DeleteAccountResponse;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ResidentStatusResponse;
import com.wuye.points.dto.resident.CreateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentStatusRequest;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ResidentSpecifications;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "name", "pointBalance");

    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final FamilyRepository familyRepository;
    private final OrderRepository orderRepository;
    private final PointRecordRepository pointRecordRepository;
    private final PropertyCoinRecordRepository propertyCoinRecordRepository;
    private final ResidentConverter residentConverter;
    private final RoleGuard roleGuard;
    private final WebSocketService webSocketService;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<ResidentListItemResponse> listResidents(
            int page,
            int pageSize,
            String keyword,
            String communityId,
            String building,
            String status,
            String role,
            String propertyCompanyId,
            String sort) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(status) && !RoleGuard.RESIDENT_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
        if (StringUtils.hasText(communityId) && !communityId.startsWith("com_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "communityId格式不正确");
        }
        if (StringUtils.hasText(propertyCompanyId) && !propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }

        String scopedPropertyCompanyId = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);
        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<Resident> residentPage = residentRepository.findAll(
                ResidentSpecifications.withFilters(
                        scopedPropertyCompanyId, keyword, communityId, building, status, role),
                pageable);

        Map<String, String> communityNameMap = residentConverter.loadCommunityNameMap(residentPage.getContent());
        List<ResidentListItemResponse> list = residentPage.getContent().stream()
                .map(resident -> residentConverter.toListItem(resident, communityNameMap))
                .toList();

        return PageResponse.of(list, page, pageSize, residentPage.getTotalElements());
    }

    @Transactional
    public ResidentDetailResponse createResident(CreateResidentRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        if (!request.getRole().equals("resident")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "管理员创建住户角色只能是resident");
        }

        String propertyCompanyId = roleGuard.resolveListPropertyCompanyId(request.getPropertyCompanyId());

        if (residentRepository.existsByPhoneAndPropertyCompanyId(request.getPhone(), propertyCompanyId)) {
            throw new BizException(ErrorCode.RESIDENT_DUPLICATE_PHONE);
        }

        Resident resident = new Resident();
        resident.setId(idGenerator.generateId("res"));
        resident.setPhone(request.getPhone());
        resident.setName(request.getName());
        resident.setAvatarUrl(request.getAvatarUrl());
        resident.setGender(request.getGender());
        resident.setAge(request.getAge());
        resident.setMaritalStatus(request.getMaritalStatus());
        resident.setHasChildren(request.getHasChildren());
        resident.setUserType(request.getUserType());
        resident.setRole(request.getRole());
        resident.setPropertyCompanyId(propertyCompanyId);
        resident.setCommunityId(request.getCommunityId());
        resident.setBuilding(request.getBuilding());
        resident.setUnit(request.getUnit());
        resident.setRoom(request.getRoom());
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setCoinFrozen(false);
        resident.setCoinHidden(false);
        resident.setWechatBound(false);
        resident.setStatus(ResidentStatus.active.name());

        residentRepository.save(resident);
        return buildDetail(resident);
    }

    @Transactional(readOnly = true)
    public ResidentDetailResponse getResident(String id) {
        Resident resident = getResidentOrThrow(id);
        assertCanViewDetail(resident);
        return buildDetail(resident);
    }

    @Transactional(readOnly = true)
    public ResidentDetailResponse getCurrentResident() {
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = getResidentOrThrow(userId);
        return buildDetail(resident);
    }

    @Transactional
    public ResidentDetailResponse updateResident(String id, UpdateResidentRequest request) {
        Resident resident = getResidentOrThrow(id);
        assertCanUpdate(resident);
        applyUpdate(resident, request, canUpdateAddress());
        residentRepository.save(resident);
        return buildDetail(resident);
    }

    @Transactional
    public ResidentDetailResponse updateCurrentResident(UpdateResidentRequest request) {
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        return updateResident(userId, request);
    }

    @Transactional
    public ResidentStatusResponse updateStatus(String id, UpdateResidentStatusRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        if (!RoleGuard.RESIDENT_STATUS.contains(request.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
        if (isReasonRequired(request.getStatus()) && !StringUtils.hasText(request.getReason())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "冻结/禁用操作必须填写reason");
        }

        Resident resident = getResidentOrThrow(id);
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        resident.setStatus(request.getStatus());
        residentRepository.save(resident);

        return ResidentStatusResponse.builder()
                .id(resident.getId())
                .status(resident.getStatus())
                .updatedAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public MyPointsResponse getMyPoints() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        Integer earnedThisMonth = pointRecordRepository.sumEarnedBetween(
                resident.getId(), monthStart, monthEnd);
        Integer spentThisMonth = pointRecordRepository.sumSpentBetween(
                resident.getId(), monthStart, monthEnd);

        Integer familyPointBalance = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyPointBalance = familyRepository.findById(resident.getFamilyId())
                    .map(Family::getPointBalance)
                    .orElse(0);
        }

        return MyPointsResponse.builder()
                .residentId(resident.getId())
                .pointBalance(resident.getPointBalance() != null ? resident.getPointBalance() : 0)
                .familyId(resident.getFamilyId())
                .familyPointBalance(familyPointBalance)
                .earnedThisMonth(earnedThisMonth != null ? earnedThisMonth : 0)
                .spentThisMonth(spentThisMonth != null ? spentThisMonth : 0)
                .expiringSoon(0)
                .expiryDate(null)
                .build();
    }

    @Transactional(readOnly = true)
    public MyCoinsResponse getMyCoins() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal earnedThisMonth = propertyCoinRecordRepository.sumEarnedBetween(
                resident.getId(), monthStart, monthEnd);
        BigDecimal spentThisMonth = propertyCoinRecordRepository.sumSpentBetween(
                resident.getId(), monthStart, monthEnd);

        BigDecimal familyCoinBalance = null;
        if (StringUtils.hasText(resident.getFamilyId())) {
            familyCoinBalance = familyRepository.findById(resident.getFamilyId())
                    .map(Family::getCoinBalance)
                    .orElse(BigDecimal.ZERO);
        }

        boolean coinDisplayEnabled = true;
        if (StringUtils.hasText(resident.getPropertyCompanyId())) {
            coinDisplayEnabled = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                    .map(PropertyCompany::getCoinDisplayEnabled)
                    .orElse(true);
        }

        return MyCoinsResponse.builder()
                .residentId(resident.getId())
                .coinBalance(resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO)
                .coinFrozen(Boolean.TRUE.equals(resident.getCoinFrozen()))
                .coinHidden(Boolean.TRUE.equals(resident.getCoinHidden()))
                .coinDisplayEnabled(coinDisplayEnabled)
                .familyId(resident.getFamilyId())
                .familyCoinBalance(familyCoinBalance)
                .earnedThisMonth(earnedThisMonth != null ? earnedThisMonth : BigDecimal.ZERO)
                .spentThisMonth(spentThisMonth != null ? spentThisMonth : BigDecimal.ZERO)
                .expiringSoon(BigDecimal.ZERO)
                .expiryDate(null)
                .build();
    }

    private Resident getResidentOrThrow(String id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (resident.getDeletedAt() != null) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND);
        }
        return resident;
    }

    private Resident getActiveResident(String userId) {
        Resident resident = getResidentOrThrow(userId);
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private void assertCanViewDetail(Resident resident) {
        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(resident.getId())) {
            return;
        }
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertCanUpdate(Resident resident) {
        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(resident.getId())) {
            return;
        }
        if (roleGuard.isPlatformAdmin()) {
            return;
        }
        if (roleGuard.isPropertyAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());
            return;
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private boolean canUpdateAddress() {
        return roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin();
    }

    private void applyUpdate(Resident resident, UpdateResidentRequest request, boolean allowAddressUpdate) {
        if (StringUtils.hasText(request.getName())) {
            resident.setName(request.getName());
        }
        if (request.getAvatarUrl() != null) {
            resident.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getGender() != null) {
            resident.setGender(request.getGender());
        }
        if (request.getAge() != null) {
            resident.setAge(request.getAge());
        }
        if (request.getMaritalStatus() != null) {
            if (!RoleGuard.MARITAL_STATUS.contains(request.getMaritalStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "maritalStatus值无效");
            }
            resident.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getHasChildren() != null) {
            resident.setHasChildren(request.getHasChildren());
        }
        if (allowAddressUpdate) {
            if (request.getBuilding() != null) {
                resident.setBuilding(request.getBuilding());
            }
            if (request.getUnit() != null) {
                resident.setUnit(request.getUnit());
            }
            if (request.getRoom() != null) {
                resident.setRoom(request.getRoom());
            }
        }
        if (allowAddressUpdate && StringUtils.hasText(request.getRole())) {
            if (!RoleGuard.VALID_ROLES.contains(request.getRole())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "role值无效");
            }
            resident.setRole(request.getRole());
        }
    }

    private ResidentDetailResponse buildDetail(Resident resident) {
        BigDecimal totalConsumption = orderRepository.sumCompletedOrderAmount(resident.getId());
        Long totalOrders = orderRepository.countCompletedOrders(resident.getId());
        return residentConverter.toDetail(resident, totalConsumption, totalOrders);
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        return PageRequest.of(safePage - 1, safePageSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String field = sort;
        if (sort.startsWith("-")) {
            field = sort.substring(1);
            direction = Sort.Direction.DESC;
        } else if (sort.startsWith("+")) {
            field = sort.substring(1);
            direction = Sort.Direction.ASC;
        }
        if (!SORTABLE_FIELDS.contains(field)) {
            field = "createdAt";
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    private boolean isReasonRequired(String status) {
        return ResidentStatus.frozen.name().equals(status)
                || ResidentStatus.disabled.name().equals(status);
    }

    @Transactional
    public DeleteAccountResponse requestAccountDeletion() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        resident.setStatus("disabled");
        resident.setDeletedAt(LocalDateTime.now());
        residentRepository.save(resident);

        webSocketService.broadcast("account_deletion_request", Map.of(
                "residentId", resident.getId(),
                "residentName", resident.getName() != null ? resident.getName() : "",
                "propertyCompanyId", resident.getPropertyCompanyId() != null ? resident.getPropertyCompanyId() : ""
        ));

        return DeleteAccountResponse.builder()
                .residentId(resident.getId())
                .status("disabled")
                .message("账号注销申请已提交，管理员将尽快处理")
                .build();
    }

    @Transactional
    public void deleteResident(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        Resident resident = getResidentOrThrow(id);
        roleGuard.assertSamePropertyOrPlatform(resident.getPropertyCompanyId());

        resident.setStatus(ResidentStatus.deleted.name());
        resident.setDeletedAt(LocalDateTime.now());
        residentRepository.save(resident);
    }
}
