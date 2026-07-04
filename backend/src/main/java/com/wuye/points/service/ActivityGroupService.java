package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.activity.ActivityGroupDetailResponse;
import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ActivityGroupMemberListResponse;
import com.wuye.points.dto.activity.ActivityGroupSubscriptionItemResponse;
import com.wuye.points.dto.activity.CreateActivityGroupRequest;
import com.wuye.points.dto.activity.JoinActivityGroupResponse;
import com.wuye.points.dto.activity.LeaveActivityGroupResponse;
import com.wuye.points.dto.activity.RemoveActivityGroupMemberResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupRequest;
import com.wuye.points.dto.activity.SubscribeActivityGroupResponse;
import com.wuye.points.dto.activity.SubscriptionStatusResponse;
import com.wuye.points.dto.activity.UpdateActivityGroupRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import com.wuye.points.entity.ActivityGroupSubscription;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityGroupMemberRepository;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.ActivityGroupSubscriptionRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ActivityGroupSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityGroupService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "memberCount", "name");

    private final ActivityGroupRepository activityGroupRepository;
    private final ActivityGroupMemberRepository activityGroupMemberRepository;
    private final ActivityGroupSubscriptionRepository activityGroupSubscriptionRepository;
    private final ResidentRepository residentRepository;
    private final CommunityRepository communityRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ActivityGroupConverter activityGroupConverter;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<ActivityGroupListItemResponse> listActivityGroups(
            int page,
            int pageSize,
            String communityId,
            Boolean mine,
            String sort) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOptionalId(communityId, "com_");

        Resident resident = getActiveResident(UserContext.currentUserId());
        String memberResidentId = Boolean.TRUE.equals(mine) ? resident.getId() : null;

        Pageable pageable = buildPageable(page, pageSize, sort);
        Specification<ActivityGroup> spec = ActivityGroupSpecifications.withFilters(
                resident.getPropertyCompanyId(),
                communityId,
                "active",
                memberResidentId);

        Page<ActivityGroup> groupPage = activityGroupRepository.findAll(spec, pageable);
        return buildListPage(groupPage, page, pageSize, resident);
    }

    @Transactional(readOnly = true)
    public ActivityGroupDetailResponse getActivityGroup(String id) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);
        return toDetail(group, resident);
    }

    @Transactional(readOnly = true)
    public ActivityGroupMemberListResponse listMembers(String id) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);

        List<ActivityGroupMember> members = activityGroupMemberRepository.findByGroupIdAndStatus(id, "active");
        Map<String, Resident> residentMap = loadResidents(members.stream()
                .map(ActivityGroupMember::getResidentId)
                .toList());

        return ActivityGroupMemberListResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .memberCount(activityGroupConverter.defaultCount(group.getMemberCount()))
                .list(members.stream()
                        .map(member -> activityGroupConverter.toMemberItem(
                                member, residentMap.get(member.getResidentId())))
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityGroupSubscriptionItemResponse> listMySubscriptions(
            int page,
            int pageSize,
            String status) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateSubscriptionStatus(status);

        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ActivityGroupSubscription> subscriptionPage;
        if (StringUtils.hasText(status)) {
            subscriptionPage = activityGroupSubscriptionRepository.findByResidentIdAndStatus(
                    resident.getId(), status, pageable);
        } else {
            subscriptionPage = activityGroupSubscriptionRepository.findByResidentId(resident.getId(), pageable);
        }

        List<ActivityGroupSubscription> subscriptions = subscriptionPage.getContent();
        Map<String, ActivityGroup> groupMap = loadGroups(subscriptions.stream()
                .map(ActivityGroupSubscription::getGroupId)
                .toList());

        List<ActivityGroupSubscriptionItemResponse> list = subscriptions.stream()
                .map(subscription -> {
                    ActivityGroup group = groupMap.get(subscription.getGroupId());
                    if (group == null) {
                        group = new ActivityGroup();
                        group.setId(subscription.getGroupId());
                        group.setName("");
                    }
                    refreshSubscriptionStatus(subscription);
                    return activityGroupConverter.toSubscriptionItem(subscription, group, false);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, subscriptionPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(String id) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        LocalDate today = LocalDate.now();

        Optional<ActivityGroupSubscription> subscriptionOpt = activityGroupSubscriptionRepository
                .findFirstByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqualOrderByEndDateDesc(
                        id, resident.getId(), "active", today);

        if (subscriptionOpt.isEmpty()) {
            return SubscriptionStatusResponse.builder()
                    .subscribed(false)
                    .build();
        }

        ActivityGroupSubscription subscription = subscriptionOpt.get();
        refreshSubscriptionStatus(subscription);

        return SubscriptionStatusResponse.builder()
                .subscribed("active".equals(subscription.getStatus()))
                .subscriptionType(subscription.getSubType())
                .startDate(subscription.getStartDate() != null ? subscription.getStartDate().toString() : null)
                .endDate(subscription.getEndDate() != null ? subscription.getEndDate().toString() : null)
                .status(subscription.getStatus())
                .build();
    }

    @Transactional
    public ActivityGroupDetailResponse createActivityGroup(CreateActivityGroupRequest request) {
        roleGuard.requireAnyRole("resident");

        Resident leader = getActiveResident(UserContext.currentUserId());
        validateOptionalId(request.getCommunityId(), "com_");

        if (StringUtils.hasText(request.getCommunityId())) {
            Community community = communityRepository.findById(request.getCommunityId())
                    .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
            assertSameProperty(leader.getPropertyCompanyId(), community.getPropertyCompanyId());
        }

        ActivityGroup group = new ActivityGroup();
        group.setId(idGenerator.generateId("act"));
        group.setName(request.getName().trim());
        group.setDescription(request.getDescription());
        group.setCoverUrl(request.getCoverUrl());
        group.setLeaderId(leader.getId());
        group.setPropertyCompanyId(leader.getPropertyCompanyId());
        group.setCommunityId(StringUtils.hasText(request.getCommunityId())
                ? request.getCommunityId()
                : leader.getCommunityId());
        group.setMemberCount(1);
        group.setMonthlyFee(request.getMonthlyFee() != null ? request.getMonthlyFee() : BigDecimal.ZERO);
        group.setYearlyFee(request.getYearlyFee() != null ? request.getYearlyFee() : BigDecimal.ZERO);
        group.setStatus("active");
        activityGroupRepository.save(group);

        addMember(group, leader.getId(), "leader");
        return toDetail(group, leader);
    }

    @Transactional
    public ActivityGroupDetailResponse updateActivityGroup(String id, UpdateActivityGroupRequest request) {
        validateActivityGroupId(id);
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));

        if (roleGuard.isResident()) {
            Resident resident = getActiveResident(UserContext.currentUserId());
            assertSameProperty(resident.getPropertyCompanyId(), group.getPropertyCompanyId());
            if (!resident.getId().equals(group.getLeaderId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        } else {
            roleGuard.requireAnyRole("property_admin", "platform_admin");
            roleGuard.assertSamePropertyOrPlatform(group.getPropertyCompanyId());
        }

        if (StringUtils.hasText(request.getName())) {
            group.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getCoverUrl() != null) {
            group.setCoverUrl(request.getCoverUrl());
        }
        if (request.getCommunityId() != null) {
            if (StringUtils.hasText(request.getCommunityId())) {
                validateOptionalId(request.getCommunityId(), "com_");
                Community community = communityRepository.findById(request.getCommunityId())
                        .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
                assertSameProperty(group.getPropertyCompanyId(), community.getPropertyCompanyId());
            }
            group.setCommunityId(request.getCommunityId());
        }
        if (request.getMonthlyFee() != null) {
            if (request.getMonthlyFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "monthlyFee不能为负数");
            }
            group.setMonthlyFee(request.getMonthlyFee());
        }
        if (request.getYearlyFee() != null) {
            if (request.getYearlyFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "yearlyFee不能为负数");
            }
            group.setYearlyFee(request.getYearlyFee());
        }
        if (StringUtils.hasText(request.getStatus())) {
            if (!RoleGuard.ACTIVITY_GROUP_STATUS.contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
            }
            group.setStatus(request.getStatus());
        }

        activityGroupRepository.save(group);
        Resident viewer;
        if (roleGuard.isResident()) {
            viewer = getActiveResident(UserContext.currentUserId());
        } else {
            viewer = residentRepository.findById(group.getLeaderId())
                    .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        }
        return toDetail(group, viewer);
    }

    @Transactional
    public JoinActivityGroupResponse joinActivityGroup(String id) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);
        assertGroupJoinable(group);

        if (activityGroupMemberRepository.existsByGroupIdAndResidentIdAndStatus(
                group.getId(), resident.getId(), "active")) {
            throw new BizException(ErrorCode.ACTIVITY_ALREADY_MEMBER);
        }

        if (activityGroupConverter.isPaidGroup(group)) {
            LocalDate today = LocalDate.now();
            boolean hasActiveSubscription = activityGroupSubscriptionRepository
                    .existsByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
                            group.getId(), resident.getId(), "active", today);
            if (!hasActiveSubscription) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "请先订阅活动组");
            }
        }

        ActivityGroupMember member = addMember(group, resident.getId(), "member");
        return JoinActivityGroupResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .memberId(member.getId())
                .residentId(resident.getId())
                .role(member.getRole())
                .memberCount(activityGroupConverter.defaultCount(group.getMemberCount()))
                .joinedAt(DateTimeUtil.toIsoUtc(member.getJoinedAt()))
                .build();
    }

    @Transactional
    public LeaveActivityGroupResponse leaveActivityGroup(String id) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);

        if (group.getLeaderId().equals(resident.getId())) {
            throw new BizException(ErrorCode.ACTIVITY_LEADER_CANNOT_LEAVE);
        }

        ActivityGroupMember member = activityGroupMemberRepository
                .findByGroupIdAndResidentId(group.getId(), resident.getId())
                .filter(item -> "active".equals(item.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_NOT_MEMBER));

        member.setStatus("inactive");
        activityGroupMemberRepository.save(member);
        group.setMemberCount(Math.max(activityGroupConverter.defaultCount(group.getMemberCount()) - 1, 0));
        activityGroupRepository.save(group);

        return LeaveActivityGroupResponse.builder()
                .groupId(group.getId())
                .residentId(resident.getId())
                .memberCount(group.getMemberCount())
                .leftAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional
    public RemoveActivityGroupMemberResponse removeMember(String id, String memberId) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);
        if (!StringUtils.hasText(memberId) || !memberId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "memberId格式不正确");
        }

        Resident operator = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, operator);
        if (!operator.getId().equals(group.getLeaderId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        if (group.getLeaderId().equals(memberId)) {
            throw new BizException(ErrorCode.ACTIVITY_LEADER_CANNOT_LEAVE);
        }

        ActivityGroupMember member = activityGroupMemberRepository
                .findByGroupIdAndResidentId(group.getId(), memberId)
                .filter(item -> "active".equals(item.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_NOT_MEMBER));

        member.setStatus("inactive");
        activityGroupMemberRepository.save(member);
        group.setMemberCount(Math.max(activityGroupConverter.defaultCount(group.getMemberCount()) - 1, 0));
        activityGroupRepository.save(group);

        return RemoveActivityGroupMemberResponse.builder()
                .groupId(group.getId())
                .memberId(member.getId())
                .residentId(memberId)
                .memberCount(group.getMemberCount())
                .removedAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional
    public SubscribeActivityGroupResponse subscribe(String id, SubscribeActivityGroupRequest request) {
        roleGuard.requireAnyRole("resident");
        validateActivityGroupId(id);
        validateSubType(request.getSubType());
        validatePaymentMethod(request.getPaymentMethod());

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);
        assertGroupJoinable(group);

        LocalDate today = LocalDate.now();
        if (activityGroupSubscriptionRepository.existsByGroupIdAndResidentIdAndStatusAndEndDateGreaterThanEqual(
                group.getId(), resident.getId(), "active", today)) {
            throw new BizException(ErrorCode.ACTIVITY_ALREADY_SUBSCRIBED);
        }

        BigDecimal amount = resolveSubscriptionAmount(group, request.getSubType());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该活动组无需订阅");
        }

        executeSubscriptionPayment(resident, group, amount, request.getPaymentMethod());

        LocalDate startDate = today;
        LocalDate endDate = "yearly".equals(request.getSubType())
                ? startDate.plusYears(1)
                : startDate.plusMonths(1);

        ActivityGroupSubscription subscription = new ActivityGroupSubscription();
        subscription.setId(idGenerator.generateId("ags"));
        subscription.setGroupId(group.getId());
        subscription.setResidentId(resident.getId());
        subscription.setPropertyCompanyId(resident.getPropertyCompanyId());
        subscription.setSubType(request.getSubType());
        subscription.setAmount(amount);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus("active");
        activityGroupSubscriptionRepository.save(subscription);

        if (!activityGroupMemberRepository.existsByGroupIdAndResidentIdAndStatus(
                group.getId(), resident.getId(), "active")) {
            addMember(group, resident.getId(), "member");
        }

        return activityGroupConverter.toSubscribeResponse(subscription, group, request.getAutoRenew());
    }

    private PageResponse<ActivityGroupListItemResponse> buildListPage(Page<ActivityGroup> groupPage,
                                                                      int page,
                                                                      int pageSize,
                                                                      Resident resident) {
        List<ActivityGroup> groups = groupPage.getContent();
        Map<String, Resident> leaderMap = loadResidents(groups.stream()
                .map(ActivityGroup::getLeaderId)
                .toList());
        Map<String, Community> communityMap = loadCommunities(groups.stream()
                .map(ActivityGroup::getCommunityId)
                .filter(StringUtils::hasText)
                .toList());
        Map<String, ActivityGroupMember> membershipMap = loadMemberships(
                groups.stream().map(ActivityGroup::getId).toList(), resident.getId());

        List<ActivityGroupListItemResponse> list = groups.stream()
                .map(group -> activityGroupConverter.toListItem(
                        group,
                        leaderMap.get(group.getLeaderId()),
                        communityMap.get(group.getCommunityId()),
                        membershipMap.get(group.getId())))
                .toList();

        return PageResponse.of(list, page, pageSize, groupPage.getTotalElements());
    }

    private ActivityGroupDetailResponse toDetail(ActivityGroup group, Resident viewer) {
        Resident leader = residentRepository.findById(group.getLeaderId()).orElse(null);
        Community community = StringUtils.hasText(group.getCommunityId())
                ? communityRepository.findById(group.getCommunityId()).orElse(null)
                : null;
        ActivityGroupMember membership = activityGroupMemberRepository
                .findByGroupIdAndResidentId(group.getId(), viewer.getId())
                .filter(item -> "active".equals(item.getStatus()))
                .orElse(null);
        return activityGroupConverter.toDetail(group, leader, community, membership);
    }

    private ActivityGroupMember addMember(ActivityGroup group, String residentId, String role) {
        ActivityGroupMember member = new ActivityGroupMember();
        member.setId(idGenerator.generateId("agm"));
        member.setGroupId(group.getId());
        member.setResidentId(residentId);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        member.setStatus("active");
        try {
            activityGroupMemberRepository.save(member);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.ACTIVITY_ALREADY_MEMBER);
        }

        if (!"leader".equals(role)) {
            group.setMemberCount(activityGroupConverter.defaultCount(group.getMemberCount()) + 1);
            activityGroupRepository.save(group);
        }
        return member;
    }

    private void executeSubscriptionPayment(Resident resident,
                                            ActivityGroup group,
                                            BigDecimal amount,
                                            String paymentMethod) {
        switch (paymentMethod) {
            case "property_coin" -> propertyCoinService.spendCoins(
                    resident, amount, "manual", group.getId(), "活动组订阅", null, false);
            case "point" -> {
                BigDecimal rate = resolvePointToFeeRate(resident.getPropertyCompanyId());
                int points = amount.divide(rate, 0, RoundingMode.UP).intValue();
                pointService.spendPoints(
                        resident, points, "manual", group.getId(), "活动组订阅", null, false);
            }
            case "wechat" -> {
                // 微信支付由前端完成，后端记录订阅
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private BigDecimal resolveSubscriptionAmount(ActivityGroup group, String subType) {
        return "yearly".equals(subType)
                ? activityGroupConverter.defaultFee(group.getYearlyFee())
                : activityGroupConverter.defaultFee(group.getMonthlyFee());
    }

    private BigDecimal resolvePointToFeeRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return DEFAULT_POINT_TO_FEE_RATE;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPointToFeeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_POINT_TO_FEE_RATE);
    }

    private void refreshSubscriptionStatus(ActivityGroupSubscription subscription) {
        if ("cancelled".equals(subscription.getStatus())) {
            return;
        }
        if (subscription.getEndDate() != null
                && subscription.getEndDate().isBefore(LocalDate.now())
                && "active".equals(subscription.getStatus())) {
            subscription.setStatus("expired");
        }
    }

    private ActivityGroup getReadableGroup(String id, Resident resident) {
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        assertSameProperty(resident.getPropertyCompanyId(), group.getPropertyCompanyId());
        return group;
    }

    private void assertGroupJoinable(ActivityGroup group) {
        if (!"active".equals(group.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "活动组不可加入");
        }
    }

    private void assertSameProperty(String left, String right) {
        if (left == null || right == null || !left.equals(right)) {
            throw new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND);
        }
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Resident::getId, Function.identity()));
    }

    private Map<String, Community> loadCommunities(List<String> communityIds) {
        if (communityIds.isEmpty()) {
            return Map.of();
        }
        return communityRepository.findAllById(communityIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Community::getId, Function.identity()));
    }

    private Map<String, ActivityGroup> loadGroups(List<String> groupIds) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        return activityGroupRepository.findAllById(groupIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(ActivityGroup::getId, Function.identity()));
    }

    private Map<String, ActivityGroupMember> loadMemberships(List<String> groupIds, String residentId) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        return activityGroupMemberRepository
                .findByGroupIdInAndResidentIdAndStatus(groupIds, residentId, "active").stream()
                .collect(Collectors.toMap(ActivityGroupMember::getGroupId, Function.identity()));
    }

    private void validateActivityGroupId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("act_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "活动组ID格式不正确");
        }
    }

    private void validateOptionalId(String id, String prefix) {
        if (StringUtils.hasText(id) && !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, prefix + "格式不正确");
        }
    }

    private void validateSubType(String subType) {
        if (!StringUtils.hasText(subType) || !RoleGuard.SUBSCRIPTION_TYPES.contains(subType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "subType值无效");
        }
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod)
                || !Set.of("property_coin", "point", "wechat").contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private void validateSubscriptionStatus(String status) {
        if (StringUtils.hasText(status) && !RoleGuard.ACTIVITY_SUBSCRIPTION_STATUS.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status值无效");
        }
    }

    private Resident getActiveResident(String userId) {
        if (userId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
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
}
