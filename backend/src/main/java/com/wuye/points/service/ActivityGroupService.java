package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.activity.ActivityGroupBuildingLeaderItemResponse;
import com.wuye.points.dto.activity.ActivityGroupBuildingLeaderListResponse;
import com.wuye.points.dto.activity.ActivityGroupDetailResponse;
import com.wuye.points.dto.activity.ActivityGroupListItemResponse;
import com.wuye.points.dto.activity.ActivityGroupMemberListResponse;
import com.wuye.points.dto.activity.ActivityGroupSubscriptionItemResponse;
import com.wuye.points.dto.activity.CreateActivityGroupRequest;
import com.wuye.points.dto.activity.DisbandActivityGroupResponse;
import com.wuye.points.dto.activity.JoinActivityGroupResponse;
import com.wuye.points.dto.activity.LeaveActivityGroupResponse;
import com.wuye.points.dto.activity.MyJoinedGroupItemResponse;
import com.wuye.points.dto.activity.RemoveActivityGroupMemberResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupRequest;
import com.wuye.points.dto.activity.UpdateMemberProfileRequest;
import com.wuye.points.dto.activity.UpdateMemberProfileResponse;
import com.wuye.points.dto.activity.SubscribeActivityGroupResponse;
import com.wuye.points.dto.activity.SubscriptionStatusResponse;
import com.wuye.points.dto.activity.UpdateActivityGroupRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import com.wuye.points.entity.ActivityGroupSubscription;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PayChannel;
import com.wuye.points.payment.PaymentBizType;
import com.wuye.points.payment.WechatJsPayParams;
import com.wuye.points.repository.ActivityGroupMemberRepository;
import com.wuye.points.repository.ActivityGroupPricingTierRepository;
import com.wuye.points.repository.ActivityGroupRepository;
import com.wuye.points.repository.ActivityGroupSubscriptionRepository;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ActivityGroupSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityGroupService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.0100");
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "memberCount", "name");
    /** 重新发起订阅支付时，作废超过该时长的旧 pending 订阅（即时重试窗口，兜底于 PendingPaymentScheduler） */
    private static final int STALE_PENDING_MINUTES = 5;

    private final ActivityGroupRepository activityGroupRepository;
    private final ActivityGroupMemberRepository activityGroupMemberRepository;
    private final ActivityGroupSubscriptionRepository activityGroupSubscriptionRepository;
    private final ActivityGroupPricingTierRepository activityGroupPricingTierRepository;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final ResidentRepository residentRepository;
    private final CommunityRepository communityRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final ActivityGroupConverter activityGroupConverter;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PayChannel wechatPayService;

    @Transactional(readOnly = true)
    public PageResponse<ActivityGroupListItemResponse> listActivityGroups(
            int page,
            int pageSize,
            String communityId,
            Boolean mine,
            String sort) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        validateOptionalId(communityId, "com_");

        Resident resident = getActiveResident(UserContext.currentUserId());
        boolean isMine = Boolean.TRUE.equals(mine);

        // 组长查看"我管理的活动组"：按 leaderId 查，不过滤 status/reviewStatus
        // （2026-10-21 组长标签化：isActivityLeader 按 identity 表 active 记录判定，兼容存量 JWT）
        if (isMine && roleGuard.isActivityLeader()) {
            return listMyLedGroups(page, pageSize, sort);
        }

        String memberResidentId = isMine ? resident.getId() : null;

        Pageable pageable = buildPageable(page, pageSize, sort);
        Specification<ActivityGroup> spec = ActivityGroupSpecifications.withFilters(
                resident.getPropertyCompanyId(),
                communityId,
                "active",
                memberResidentId,
                isMine ? null : "approved");

        Page<ActivityGroup> groupPage = activityGroupRepository.findAll(spec, pageable);
        return buildListPage(groupPage, page, pageSize, resident);
    }

    /**
     * 我管理的活动组列表（§18.2.1）：仅返回本人担任组长（leaderId=当前用户）的活动组，
     * 不过滤 status/reviewStatus。组长身份按 identity 表 active 记录判定（含存量 JWT 兼容）。
     */
    @Transactional(readOnly = true)
    public PageResponse<ActivityGroupListItemResponse> listMyLedGroups(int page, int pageSize, String sort) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = buildPageable(page, pageSize, sort);
        Specification<ActivityGroup> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("propertyCompanyId"), resident.getPropertyCompanyId()),
                cb.equal(root.get("leaderId"), resident.getId()));
        Page<ActivityGroup> groupPage = activityGroupRepository.findAll(spec, pageable);
        return buildListPage(groupPage, page, pageSize, resident);
    }

    @Transactional(readOnly = true)
    public ActivityGroupDetailResponse getActivityGroup(String id) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);
        return toDetail(group, resident);
    }

    @Transactional(readOnly = true)
    public ActivityGroupMemberListResponse listMembers(String id) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");
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

    /**
     * 活动组内单元楼长列表（只读）。
     *
     * <p>楼长是「小区 + 单元」维度的物业侧身份，由物业/平台管理员指定（§99.1），与活动组无关，
     * 因此组长在本模块只有读权限，不提供设置/撤销；这里只是把组内成员与该身份做交集展示。
     */
    @Transactional(readOnly = true)
    public ActivityGroupBuildingLeaderListResponse listGroupBuildingLeaders(String id) {
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");
        validateActivityGroupId(id);

        Resident resident = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, resident);

        List<ActivityGroupMember> members = activityGroupMemberRepository.findByGroupIdAndStatus(id, "active");
        List<String> memberResidentIds = members.stream()
                .map(ActivityGroupMember::getResidentId)
                .distinct()
                .toList();

        Set<String> leaderResidentIds = memberResidentIds.isEmpty()
                ? Set.of()
                : buildingLeaderRepository.findByResidentIdInAndPropertyCompanyIdAndStatus(
                        memberResidentIds, group.getPropertyCompanyId(), "active").stream()
                        .map(BuildingLeader::getResidentId)
                        .collect(Collectors.toSet());

        List<ActivityGroupMember> leaders = members.stream()
                .filter(member -> leaderResidentIds.contains(member.getResidentId()))
                .toList();
        Map<String, Resident> leaderMap = loadResidents(leaders.stream()
                .map(ActivityGroupMember::getResidentId)
                .toList());

        return ActivityGroupBuildingLeaderListResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .buildingLeaderCount(leaders.size())
                .list(leaders.stream()
                        .map(member -> {
                            Resident leader = leaderMap.get(member.getResidentId());
                            return ActivityGroupBuildingLeaderItemResponse.builder()
                                    .memberId(member.getId())
                                    .residentId(member.getResidentId())
                                    .name(leader != null ? leader.getName() : null)
                                    .avatarUrl(leader != null ? leader.getAvatarUrl() : null)
                                    .leaderRole("building_leader")
                                    .build();
                        })
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
        Map<String, ActivityGroupMember> membershipMap = loadMemberships(
                subscriptions.stream().map(ActivityGroupSubscription::getGroupId).toList(),
                resident.getId());

        List<ActivityGroupSubscriptionItemResponse> list = subscriptions.stream()
                .map(subscription -> {
                    ActivityGroup group = groupMap.get(subscription.getGroupId());
                    if (group == null) {
                        group = new ActivityGroup();
                        group.setId(subscription.getGroupId());
                        group.setName("");
                    }
                    refreshSubscriptionStatus(subscription);
                    return activityGroupConverter.toSubscriptionItem(subscription, group, false,
                            membershipMap.get(subscription.getGroupId()));
                })
                .toList();

        return PageResponse.of(list, page, pageSize, subscriptionPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<MyJoinedGroupItemResponse> listMyJoinedGroups(
            int page,
            int pageSize) {
        roleGuard.requireAnyRole("resident");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }

        Resident resident = getActiveResident(UserContext.currentUserId());
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize, Sort.by(Sort.Direction.DESC, "joinedAt"));

        Page<ActivityGroupMember> memberPage = activityGroupMemberRepository
                .findByResidentIdAndStatus(resident.getId(), "active", pageable);

        List<ActivityGroupMember> members = memberPage.getContent();
        Map<String, ActivityGroup> groupMap = loadGroups(members.stream()
                .map(ActivityGroupMember::getGroupId)
                .toList());
        Map<String, Resident> leaderMap = loadResidents(groupMap.values().stream()
                .map(ActivityGroup::getLeaderId)
                .toList());

        List<MyJoinedGroupItemResponse> list = members.stream()
                .map(member -> {
                    ActivityGroup group = groupMap.get(member.getGroupId());
                    if (group == null) {
                        group = new ActivityGroup();
                        group.setId(member.getGroupId());
                        group.setName("");
                    }
                    Resident leader = leaderMap.get(group.getLeaderId());
                    return activityGroupConverter.toJoinedGroupItem(member, group, leader);
                })
                .toList();

        return PageResponse.of(list, page, pageSize, memberPage.getTotalElements());
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
        roleGuard.requireAnyRole("resident", "activity_leader", "property_admin", "platform_admin",
                "coordinator", "sector_leader", "individual_leader");

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
        group.setQuarterlyFee(request.getQuarterlyFee());
        group.setPerTimeFee(request.getPerTimeFee());
        // 2026-09-07 前端问题 F（产品确认）：住户端创建活动组即刻生效（active），管理端审核仅为事后兜底：
        // approve 无影响；reject 永久删除该活动组。
        group.setStatus("active");
        group.setReviewStatus("approved");
        activityGroupRepository.save(group);

        addMember(group, leader.getId(), "leader");
        return toDetail(group, leader);
    }

    @Transactional
    public ActivityGroupDetailResponse updateActivityGroup(String id, UpdateActivityGroupRequest request) {
        validateActivityGroupId(id);
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));

        // 组长类角色（业主/活动组长/协调员/板块负责人/个体负责人）仅可更新本人担任组长的活动组
        if (roleGuard.isLeaderRole()) {
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
        if (request.getQuarterlyFee() != null) {
            if (request.getQuarterlyFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "quarterlyFee不能为负数");
            }
            group.setQuarterlyFee(request.getQuarterlyFee());
        }
        if (request.getPerTimeFee() != null) {
            if (request.getPerTimeFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "perTimeFee不能为负数");
            }
            group.setPerTimeFee(request.getPerTimeFee());
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

    /**
     * 解散（硬删除）活动组。仅本组组长、物业管理员、平台管理员可操作，不可恢复。
     * 由于 activity_group_subscriptions 未配置 ON DELETE CASCADE，需先显式删除订阅，
     * 再删除成员与价格档位（二者配置了 ON DELETE CASCADE，但显式删除更稳妥），最后删除组本身。
     */
    @Transactional
    public DisbandActivityGroupResponse disbandActivityGroup(String id) {
        validateActivityGroupId(id);
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));

        // 组长类角色：仅本组组长可解散
        if (roleGuard.isLeaderRole()) {
            Resident operator = getActiveResident(UserContext.currentUserId());
            assertSameProperty(operator.getPropertyCompanyId(), group.getPropertyCompanyId());
            if (!operator.getId().equals(group.getLeaderId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        } else if (roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(group.getPropertyCompanyId());
        } else {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        activityGroupSubscriptionRepository.deleteByGroupId(id);
        activityGroupMemberRepository.deleteByGroupId(id);
        activityGroupPricingTierRepository.deleteByActivityGroupId(id);
        activityGroupRepository.delete(group);

        return DisbandActivityGroupResponse.builder()
                .groupId(id)
                .status("deleted")
                .build();
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
        roleGuard.requireAnyRole("resident", "activity_leader", "coordinator",
                "sector_leader", "individual_leader", "property_admin", "platform_admin");
        validateActivityGroupId(id);
        if (!StringUtils.hasText(memberId) || !memberId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "memberId格式不正确");
        }

        Resident operator = getActiveResident(UserContext.currentUserId());
        ActivityGroup group = getReadableGroup(id, operator);
        if (roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(group.getPropertyCompanyId());
        } else if (!operator.getId().equals(group.getLeaderId())) {
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

    /**
     * 更新组员个人简介（§68.1 PUT /activity-groups/{id}/members/{memberId}/profile）。
     * 校验：调用者为活动组组长（leaderId === 当前用户）或成员本人。
     * Roles：resident / activity_leader（及组长类角色）/ property_admin / platform_admin。
     */
    @Transactional
    public UpdateMemberProfileResponse updateMemberProfile(
            String id, String memberId, UpdateMemberProfileRequest request) {
        roleGuard.requireAnyRole("resident", "activity_leader", "coordinator",
                "sector_leader", "individual_leader", "property_admin", "platform_admin");
        validateActivityGroupId(id);
        if (!StringUtils.hasText(memberId) || !memberId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "memberId格式不正确");
        }

        String operatorId = UserContext.currentUserId();
        Resident operator = getActiveResident(operatorId);
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        assertSameProperty(operator.getPropertyCompanyId(), group.getPropertyCompanyId());

        ActivityGroupMember member = activityGroupMemberRepository
                .findByGroupIdAndResidentId(group.getId(), memberId)
                .filter(item -> "active".equals(item.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_NOT_MEMBER));

        boolean isSelf = operatorId.equals(member.getResidentId());
        boolean isLeader = operatorId.equals(group.getLeaderId());
        boolean isAdmin = roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin();
        if (!isSelf && !isLeader && !isAdmin) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        String introduction = request.getIntroduction();
        member.setIntroduction(introduction);
        activityGroupMemberRepository.save(member);

        return UpdateMemberProfileResponse.builder()
                .groupId(group.getId())
                .memberId(member.getId())
                .residentId(member.getResidentId())
                .introduction(introduction)
                .updatedAt(DateTimeUtil.nowIsoUtc())
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

        // 微信支付：先落 pending_payment 订阅，微信回调确认后才生效（严禁未收款即生效）
        if ("wechat".equals(request.getPaymentMethod())) {
            return initiateChannelSubscription(resident, group, amount, request);
        }

        executeSubscriptionPayment(resident, group, amount, request.getPaymentMethod());

        ActivityGroupSubscription subscription = buildSubscription(
                resident, group, amount, request.getSubType(), "active");
        activityGroupSubscriptionRepository.save(subscription);

        if (!activityGroupMemberRepository.existsByGroupIdAndResidentIdAndStatus(
                group.getId(), resident.getId(), "active")) {
            addMember(group, resident.getId(), "member");
        }

        return activityGroupConverter.toSubscribeResponse(subscription, group, request.getAutoRenew());
    }

    /**
     * 微信支付回调确认后活动组订阅生效并加入成员（幂等）。由 PaymentBizDispatcher 调用，无登录上下文。
     * 订阅在 {@link #initiateChannelSubscription} 时已以 pending_payment 落库，收到款项回调才置 active。
     */
    @Transactional
    public void completeChannelPayment(PaymentRecord record) {
        ActivityGroupSubscription subscription = activityGroupSubscriptionRepository
                .findById(record.getBizId())
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        if ("active".equals(subscription.getStatus())) {
            return; // 幂等：已生效
        }
        if ("cancelled".equals(subscription.getStatus())) {
            return; // 已取消（超时收尾），不再激活
        }
        subscription.setStatus("active");
        activityGroupSubscriptionRepository.save(subscription);

        ActivityGroup group = activityGroupRepository.findById(subscription.getGroupId()).orElse(null);
        if (group != null && !activityGroupMemberRepository.existsByGroupIdAndResidentIdAndStatus(
                group.getId(), subscription.getResidentId(), "active")) {
            addMember(group, subscription.getResidentId(), "member");
        }
    }

    /**
     * 付费订阅（微信）预下单：订阅先以 pending_payment 落库（不生效、不加成员、不占额度），
     * 微信回调 {@link #completeChannelPayment(PaymentRecord)} 确认后才置 active 并加入成员。
     * fail-closed：微信支付未配置直接拒绝，绝不回退为「直接生效」。
     */
    private SubscribeActivityGroupResponse initiateChannelSubscription(Resident resident, ActivityGroup group,
                                                                      BigDecimal amount,
                                                                      SubscribeActivityGroupRequest request) {
        if (!wechatPayService.isDirectEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "在线支付未开通，请使用物业币或积分支付");
        }
        // 作废超时未支付的旧 pending 订阅，允许用户立即重新发起
        closeStalePendingSubscriptions(group.getId(), resident.getId());
        // 防重复提交：同一住户同一活动组已有待支付订阅时拒绝，避免重复扣款
        if (!activityGroupSubscriptionRepository.findByGroupIdAndResidentIdAndStatus(
                group.getId(), resident.getId(), "pending_payment").isEmpty()) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "订阅支付处理中，请勿重复提交");
        }
        ActivityGroupSubscription subscription = buildSubscription(
                resident, group, amount, request.getSubType(), "pending_payment");
        activityGroupSubscriptionRepository.save(subscription);

        String openId = resolveOpenId(request.getOpenid(), resident);
        PaymentRecord record = wechatPayService.createIntent(
                PaymentBizType.ACTIVITY_SUBSCRIPTION, subscription.getId(), resident.getId(),
                resident.getPropertyCompanyId(), "wechat", amount,
                0, BigDecimal.ZERO, amount);
        WechatJsPayParams payParams = wechatPayService.miniPay(record, openId, "活动组订阅");

        SubscribeActivityGroupResponse response = activityGroupConverter.toSubscribeResponse(
                subscription, group, request.getAutoRenew());
        response.setPaymentRecordId(record.getId());
        response.setPayParams(payParams);
        return response;
    }

    private ActivityGroupSubscription buildSubscription(Resident resident, ActivityGroup group,
                                                        BigDecimal amount, String subType, String status) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = switch (subType) {
            case "yearly" -> startDate.plusYears(1);
            case "quarterly" -> startDate.plusMonths(3);
            case "per_time" -> startDate;
            default -> startDate.plusMonths(1);
        };
        ActivityGroupSubscription subscription = new ActivityGroupSubscription();
        subscription.setId(idGenerator.generateId("ags"));
        subscription.setGroupId(group.getId());
        subscription.setResidentId(resident.getId());
        subscription.setPropertyCompanyId(resident.getPropertyCompanyId());
        subscription.setSubType(subType);
        subscription.setAmount(amount);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(status);
        return subscription;
    }

    /**
     * 作废该住户在该活动组下超时未支付的 pending_payment 订阅及其流水，让用户可立即重新发起。
     * 兜底于 {@code PendingPaymentScheduler}（30 分钟），这里给出更短的即时重试窗口。
     */
    private void closeStalePendingSubscriptions(String groupId, String residentId) {
        List<ActivityGroupSubscription> stale = activityGroupSubscriptionRepository
                .findByGroupIdAndResidentIdAndStatus(groupId, residentId, "pending_payment").stream()
                .filter(s -> s.getCreatedAt() != null
                        && s.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(STALE_PENDING_MINUTES)))
                .toList();
        for (ActivityGroupSubscription subscription : stale) {
            subscription.setStatus("cancelled");
            activityGroupSubscriptionRepository.save(subscription);
            for (PaymentRecord record : paymentRecordRepository.findByBizTypeAndBizIdAndStatus(
                    PaymentBizType.ACTIVITY_SUBSCRIPTION, subscription.getId(), "pending")) {
                record.setStatus("closed");
                record.setErrorMsg("重新发起支付，原流水作废");
                paymentRecordRepository.save(record);
            }
        }
        if (!stale.isEmpty()) {
            log.info("[活动组订阅] 作废超时支付订阅 groupId={} residentId={} count={}",
                    groupId, residentId, stale.size());
        }
    }

    /** 解析微信小程序 openid：请求显式传入优先，否则取用户绑定 openid，两者皆无则报错。 */
    private String resolveOpenId(String requestOpenId, Resident resident) {
        if (StringUtils.hasText(requestOpenId)) {
            return requestOpenId;
        }
        if (StringUtils.hasText(resident.getOpenid())) {
            return resident.getOpenid();
        }
        throw new BizException(ErrorCode.PARAM_MISSING, "openid不能为空（微信小程序支付需要）");
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
        ActivityGroupDetailResponse detail = activityGroupConverter.toDetail(group, leader, community, membership);
        detail.setReviewStatus(group.getReviewStatus());
        detail.setReviewRemark(group.getReviewRemark());
        return detail;
    }

    private ActivityGroupMember addMember(ActivityGroup group, String residentId, String role) {
        Optional<ActivityGroupMember> existing = activityGroupMemberRepository
                .findByGroupIdAndResidentId(group.getId(), residentId);

        if (existing.isPresent()) {
            ActivityGroupMember member = existing.get();
            if ("active".equals(member.getStatus())) {
                throw new BizException(ErrorCode.ACTIVITY_ALREADY_MEMBER);
            }
            member.setStatus("active");
            member.setJoinedAt(LocalDateTime.now());
            if (!"leader".equals(member.getRole())) {
                member.setRole(role);
            }
            activityGroupMemberRepository.save(member);

            if (!"leader".equals(role)) {
                group.setMemberCount(activityGroupConverter.defaultCount(group.getMemberCount()) + 1);
                activityGroupRepository.save(group);
            }
            return member;
        }

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
        boolean useFamilyPool = StringUtils.hasText(resident.getFamilyId());
        switch (paymentMethod) {
            case "property_coin" -> propertyCoinService.spendCoins(
                    resident, amount, "manual", group.getId(), "活动组订阅", null, useFamilyPool);
            case "point" -> {
                BigDecimal rate = resolvePointToFeeRate(resident.getPropertyCompanyId());
                int points = amount.divide(rate, 0, RoundingMode.UP).intValue();
                pointService.spendPoints(
                        resident, points, "manual", group.getId(), "活动组订阅", null, useFamilyPool);
            }
            case "wechat" -> {
                // 微信支付由前端完成，后端记录订阅
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        }
    }

    private BigDecimal resolveSubscriptionAmount(ActivityGroup group, String subType) {
        return switch (subType) {
            case "yearly" -> activityGroupConverter.defaultFee(group.getYearlyFee());
            case "quarterly" -> activityGroupConverter.defaultFee(group.getQuarterlyFee());
            case "per_time" -> activityGroupConverter.defaultFee(group.getPerTimeFee());
            default -> activityGroupConverter.defaultFee(group.getMonthlyFee());
        };
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
        boolean isLeader = resident.getId().equals(group.getLeaderId());
        boolean isAdmin = roleGuard.isPropertyAdmin() || roleGuard.isPlatformAdmin();
        if (!"approved".equals(group.getReviewStatus()) && !isLeader && !isAdmin) {
            throw new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND);
        }
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
        if (!StringUtils.hasText(id) || (!id.startsWith("act_") && !id.startsWith("ag_"))) {
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

    @Transactional(readOnly = true)
    public PageResponse<ActivityGroupListItemResponse> listReviewRequests(
            int page, int pageSize, String status) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String scoped = roleGuard.resolveListPropertyCompanyId(null);
        Pageable pageable = PageRequest.of(
                Math.max(page, 1) - 1, Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityGroup> groupPage = activityGroupRepository.findAll(
                ActivityGroupSpecifications.withFilters(scoped, null, null, null, status), pageable);

        List<ActivityGroupListItemResponse> list = groupPage.getContent().stream()
                .map(group -> {
                    Resident leader = residentRepository.findById(group.getLeaderId()).orElse(null);
                    Community community = StringUtils.hasText(group.getCommunityId())
                            ? communityRepository.findById(group.getCommunityId()).orElse(null)
                            : null;
                    return activityGroupConverter.toListItem(group, leader, community, null);
                })
                .toList();
        return PageResponse.of(list, page, pageSize, groupPage.getTotalElements());
    }

    /**
     * 活动组审核（2026-09-07 前端问题 F 与产品对齐）：
     * - 创建即刻生效（active/approved），审核列表仅展示；
     * - approve：无业务影响（幂等，不改变组状态）；
     * - reject：永久删除该活动组（硬删除，含订阅/成员/价格档位），并回填审核备注后再删。
     */
    @Transactional
    public void reviewActivityGroup(String id, String action, String remark) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateActivityGroupId(id);
        ActivityGroup group = activityGroupRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ACTIVITY_GROUP_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(group.getPropertyCompanyId());
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action 仅支持 approve/reject");
        }
        if ("approve".equals(action)) {
            // 通过：无影响（组创建即生效），仅补记备注，供审计查询
            if (StringUtils.hasText(remark)) {
                group.setReviewRemark(remark);
                activityGroupRepository.save(group);
            }
            return;
        }
        // reject：硬删除（与组长解散同套清理逻辑），先删订阅/成员/价格档位再删组
        group.setReviewRemark(remark);
        activityGroupRepository.save(group);

        activityGroupSubscriptionRepository.deleteByGroupId(id);
        activityGroupMemberRepository.deleteByGroupId(id);
        activityGroupPricingTierRepository.deleteByActivityGroupId(id);
        activityGroupRepository.delete(group);
    }
}
