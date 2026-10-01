package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.resident.DeleteAccountResponse;
import com.wuye.points.dto.resident.MyCoinsResponse;
import com.wuye.points.dto.resident.MyPointsResponse;
import com.wuye.points.dto.resident.RequestAccountDeletionRequest;
import com.wuye.points.dto.resident.ResidentDetailResponse;
import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.dto.resident.ResidentStatusResponse;
import com.wuye.points.dto.resident.BuildingChangeListItemResponse;
import com.wuye.points.dto.resident.BuildingChangeResponse;
import com.wuye.points.dto.resident.CreateBuildingChangeRequest;
import com.wuye.points.dto.resident.CreateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentRequest;
import com.wuye.points.dto.resident.UpdateResidentStatusRequest;
import com.wuye.points.dto.resident.BindCommunityRequest;
import com.wuye.points.dto.resident.BookingContactInfoResponse;
import com.wuye.points.entity.BuildingChangeRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingChangeRequestRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ResidentSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final PropertyFeeRepository propertyFeeRepository;
    private final WebSocketService webSocketService;
    private final SnowflakeIdGenerator idGenerator;
    private final BuildingChangeRequestRepository buildingChangeRequestRepository;
    private final CommunityRepository communityRepository;

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
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator");
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

    /**
     * 预约预填信息（v7.8）：返回完整手机号 + 默认服务地址 + 是否已登记电话/地址，
     * 供前端预约服务时自动带出。登录即可调用。
     */
    @Transactional(readOnly = true)
    public BookingContactInfoResponse getBookingInfo() {
        String userId = UserContext.currentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = getResidentOrThrow(userId);
        String communityName = null;
        if (StringUtils.hasText(resident.getCommunityId())) {
            communityName = communityRepository.findById(resident.getCommunityId())
                    .map(c -> c.getName()).orElse(null);
        }
        String building = resident.getBuilding();
        String unit = resident.getUnit();
        String room = resident.getRoom();
        boolean hasPhone = StringUtils.hasText(resident.getPhone());
        boolean hasAddress = StringUtils.hasText(building) && StringUtils.hasText(room);
        String address = hasAddress
                ? String.join("", stripNull(building), stripNull(unit), stripNull(room))
                : null;
        return BookingContactInfoResponse.builder()
                .residentId(resident.getId())
                .contactPhone(resident.getPhone())
                .address(address)
                .building(building)
                .unit(unit)
                .room(room)
                .communityId(resident.getCommunityId())
                .communityName(communityName)
                .propertyCompanyId(resident.getPropertyCompanyId())
                .hasPhone(hasPhone)
                .hasAddress(hasAddress)
                .build();
    }

    private static String stripNull(String s) {
        return s == null ? "" : s;
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
    public DeleteAccountResponse requestAccountDeletion(RequestAccountDeletionRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        // 1. 校验 confirmText
        if (!"确认注销".equals(request.getConfirmText())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请确认注销操作");
        }

        // 2. 四项前置校验
        // 2.1 存在未完成订单
        long pendingOrders = orderRepository.countPendingOrdersByResidentId(resident.getId());
        if (pendingOrders > 0) {
            throw new BizException(ErrorCode.ACCOUNT_DELETION_PENDING_ORDER);
        }

        // 2.2 存在未缴物业费
        long unpaidFees = propertyFeeRepository.countByResidentIdAndPaymentStatusNotIn(resident.getId());
        if (unpaidFees > 0) {
            throw new BizException(ErrorCode.ACCOUNT_DELETION_PENDING_FEE);
        }

        // 2.3 物业币余额不为 0
        BigDecimal coinBalance = resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO;
        if (coinBalance.compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(ErrorCode.ACCOUNT_DELETION_COIN_BALANCE);
        }

        // 2.4 户主家庭有其他成员
        familyRepository.findByOwnerId(resident.getId()).ifPresent(family -> {
            if (family.getMemberCount() != null && family.getMemberCount() > 1) {
                throw new BizException(ErrorCode.ACCOUNT_DELETION_FAMILY_MEMBERS);
            }
        });

        // 3. 设置 pending_deletion 状态和 15 天冷静期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(15);
        resident.setStatus("pending_deletion");
        resident.setDeletedAt(expiresAt);
        residentRepository.save(resident);

        // 4. 生成请求 ID
        String requestId = idGenerator.generateId("del");

        webSocketService.broadcast("account_deletion_request", Map.of(
                "residentId", resident.getId(),
                "residentName", resident.getName() != null ? resident.getName() : "",
                "propertyCompanyId", resident.getPropertyCompanyId() != null ? resident.getPropertyCompanyId() : ""
        ));

        return DeleteAccountResponse.builder()
                .residentId(resident.getId())
                .status("pending")
                .message("账号注销申请已提交，请在15天冷静期内确认")
                .requestId(requestId)
                .expiresAt(DateTimeUtil.toLocalDateTimeString(expiresAt))
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ResidentListItemResponse> listDeletionRequests(int page, int pageSize) {
        roleGuard.requireAnyRole("platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "deletedAt"));
        Page<Resident> residentPage = residentRepository.findAll(
                ResidentSpecifications.withFilters(null, null, null, null, "pending_deletion", null),
                pageable);
        Map<String, String> communityNameMap = residentConverter.loadCommunityNameMap(residentPage.getContent());
        List<ResidentListItemResponse> list = residentPage.getContent().stream()
                .map(r -> residentConverter.toListItem(r, communityNameMap))
                .toList();
        return PageResponse.of(list, page, pageSize, residentPage.getTotalElements());
    }

    @Transactional
    public void reviewAccountDeletion(String residentId, String action, String rejectReason) {
        roleGuard.requireAnyRole("platform_admin");
        Resident resident = getResidentOrThrow(residentId);
        if (!"pending_deletion".equals(resident.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该住户当前不处于注销申请状态");
        }
        if ("approve".equals(action)) {
            resident.setStatus("deleted");
            resident.setDeletedAt(LocalDateTime.now());
        } else if ("reject".equals(action)) {
            resident.setStatus("active");
            resident.setDeletedAt(null);
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "action 仅支持 approve/reject");
        }
        residentRepository.save(resident);
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

    @Transactional(readOnly = true)
    public PageResponse<BuildingChangeListItemResponse> listBuildingChanges(
            int page, int pageSize, String status, String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String scoped = roleGuard.resolveListPropertyCompanyId(propertyCompanyId);

        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<BuildingChangeRequest> reqPage = buildingChangeRequestRepository.findAll((root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (scoped != null) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), scoped));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);

        List<String> residentIds = reqPage.getContent().stream()
                .map(BuildingChangeRequest::getResidentId).distinct().toList();
        Map<String, Resident> residentMap = residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));

        List<BuildingChangeListItemResponse> list = reqPage.getContent().stream()
                .map(req -> {
                    Resident resident = residentMap.get(req.getResidentId());
                    return BuildingChangeListItemResponse.builder()
                            .id(req.getId())
                            .residentId(req.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .residentPhone(resident != null ? PhoneUtil.mask(resident.getPhone()) : null)
                            .currentBuilding(resident != null ? resident.getBuilding() : null)
                            .currentUnit(resident != null ? resident.getUnit() : null)
                            .currentRoom(resident != null ? resident.getRoom() : null)
                            .communityId(req.getCommunityId())
                            .building(req.getBuilding())
                            .unit(req.getUnit())
                            .floor(req.getFloor())
                            .room(req.getRoom())
                            .status(req.getStatus())
                            .rejectReason(req.getRejectReason())
                            .appliedAt(DateTimeUtil.toIsoUtc(req.getAppliedAt()))
                            .auditedAt(DateTimeUtil.toIsoUtc(req.getAuditedAt()))
                            .build();
                })
                .toList();

        return PageResponse.of(list, page, pageSize, reqPage.getTotalElements());
    }

    @Transactional
    public BuildingChangeResponse auditBuildingChange(String id, boolean approved, String reason) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");

        BuildingChangeRequest req = buildingChangeRequestRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_CHANGE_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(req.getPropertyCompanyId());

        if (!"pending".equals(req.getStatus()) && !"pending_audit".equals(req.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "申请不在待审批状态");
        }

        LocalDateTime now = LocalDateTime.now();
        if (approved) {
            req.setStatus("approved");
            req.setAuditedAt(now);
            req.setAuditorId(UserContext.currentUserId());

            Resident resident = residentRepository.findById(req.getResidentId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
            resident.setCommunityId(req.getCommunityId());
            resident.setBuilding(req.getBuilding());
            resident.setUnit(req.getUnit());
            resident.setRoom(req.getRoom());
            residentRepository.save(resident);
        } else {
            if (!StringUtils.hasText(reason)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "驳回必须填写原因");
            }
            req.setStatus("rejected");
            req.setRejectReason(reason);
            req.setAuditedAt(now);
            req.setAuditorId(UserContext.currentUserId());
        }
        buildingChangeRequestRepository.save(req);

        return BuildingChangeResponse.builder()
                .id(req.getId())
                .status(req.getStatus())
                .communityId(req.getCommunityId())
                .building(req.getBuilding())
                .unit(req.getUnit())
                .floor(req.getFloor())
                .room(req.getRoom())
                .rejectReason(req.getRejectReason())
                .appliedAt(DateTimeUtil.toIsoUtc(req.getAppliedAt()))
                .auditedAt(DateTimeUtil.toIsoUtc(req.getAuditedAt()))
                .build();
    }

    /**
     * 住户提交楼栋变更申请（此前仅存在管理端审批入口，导致审批流无来源、形成“半套”断链）。
     * 同一住户同时只允许存在一条待审批申请；审批通过后由管理端写入 residents 地址。
     */
    @Transactional
    public BuildingChangeResponse createBuildingChangeRequest(CreateBuildingChangeRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        BuildingChangeRequest pending = buildingChangeRequestRepository
                .findFirstByResidentIdAndStatusInOrderByAppliedAtDesc(
                        resident.getId(), List.of("pending", "pending_audit"))
                .orElse(null);
        if (pending != null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "已存在待审批的楼栋变更申请，请等待审批结果");
        }

        BuildingChangeRequest req = new BuildingChangeRequest();
        req.setId(idGenerator.generateId("bcr"));
        req.setResidentId(resident.getId());
        req.setPropertyCompanyId(resident.getPropertyCompanyId());
        req.setCommunityId(request.getCommunityId());
        req.setBuilding(request.getBuilding());
        req.setUnit(request.getUnit());
        req.setFloor(request.getFloor());
        req.setRoom(request.getRoom());
        req.setStatus("pending");
        req.setAppliedAt(LocalDateTime.now());
        buildingChangeRequestRepository.save(req);

        return toBuildingChangeResponse(req);
    }

    /** 我的楼栋变更申请（按申请时间倒序）。 */
    @Transactional(readOnly = true)
    public List<BuildingChangeResponse> listMyBuildingChanges() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        return buildingChangeRequestRepository.findByResidentIdOrderByAppliedAtDesc(resident.getId())
                .stream()
                .map(this::toBuildingChangeResponse)
                .toList();
    }

    private BuildingChangeResponse toBuildingChangeResponse(BuildingChangeRequest req) {
        return BuildingChangeResponse.builder()
                .id(req.getId())
                .status(req.getStatus())
                .communityId(req.getCommunityId())
                .building(req.getBuilding())
                .unit(req.getUnit())
                .floor(req.getFloor())
                .room(req.getRoom())
                .rejectReason(req.getRejectReason())
                .appliedAt(DateTimeUtil.toIsoUtc(req.getAppliedAt()))
                .auditedAt(DateTimeUtil.toIsoUtc(req.getAuditedAt()))
                .build();
    }

    /**
     * 住户自主绑定/完善小区（§2.6.1）。小区必须存在且属于住户所属物业公司，否则返回 10002。
     */
    @Transactional
    public ResidentDetailResponse bindMyCommunity(BindCommunityRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());

        String communityId = request.getCommunityId();
        if (!StringUtils.hasText(communityId) || !communityId.startsWith("com_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "communityId格式不正确");
        }
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        if (StringUtils.hasText(resident.getPropertyCompanyId())
                && !resident.getPropertyCompanyId().equals(community.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "小区不属于当前住户所属物业公司");
        }

        resident.setCommunityId(communityId);
        residentRepository.save(resident);
        return buildDetail(resident);
    }

    /** 我的楼栋变更申请 — 最近一条（§2.6.2.2），无申请时返回 null。 */
    @Transactional(readOnly = true)
    public BuildingChangeResponse getMyLatestBuildingChange() {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        return buildingChangeRequestRepository
                .findFirstByResidentIdAndStatusInOrderByAppliedAtDesc(
                        resident.getId(), List.of("pending", "pending_audit", "approved", "rejected"))
                .map(this::toBuildingChangeResponse)
                .orElse(null);
    }
}
