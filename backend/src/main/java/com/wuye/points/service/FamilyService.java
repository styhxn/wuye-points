package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.family.AddFamilyMemberRequest;
import com.wuye.points.dto.family.AddFamilyMemberResponse;
import com.wuye.points.dto.family.CreateFamilyRequest;
import com.wuye.points.dto.family.FamilyDetailResponse;
import com.wuye.points.dto.family.FamilyMemberContributionResponse;
import com.wuye.points.dto.family.FamilyMemberListResponse;
import com.wuye.points.dto.family.FamilyResponse;
import com.wuye.points.dto.family.RemoveFamilyMemberResponse;
import com.wuye.points.dto.family.UpdateFamilyRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.Family;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.enums.UserType;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.FamilyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final String POINTS_HANDLING_TRANSFER = "transfer_to_personal";
    private static final String COINS_HANDLING_TRANSFER = "transfer_to_personal";

    private final FamilyRepository familyRepository;
    private final ResidentRepository residentRepository;
    private final CommunityRepository communityRepository;
    private final FamilyConverter familyConverter;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;
    private final RoomOccupancyGuard roomOccupancyGuard;

    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident owner = getActiveResident(UserContext.currentUserId());
        // 锁住户行：同一住户并发创建家庭时串行化，避免生成两个家庭并把余额搬两次。
        owner = residentRepository.findByIdForUpdate(owner.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        if (UserType.tenant.name().equals(owner.getUserType())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "租住人员不能创建家庭");
        }
        if (StringUtils.hasText(owner.getFamilyId())) {
            throw new BizException(ErrorCode.FAMILY_MEMBER_LIMIT, "已属于某家庭，无法重复创建");
        }
        if (familyRepository.findByOwnerId(owner.getId()).isPresent()) {
            throw new BizException(ErrorCode.FAMILY_MEMBER_LIMIT, "已是户主");
        }
        if (!request.getCommunityId().startsWith("com_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "communityId格式不正确");
        }

        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        assertSameProperty(owner.getPropertyCompanyId(), community.getPropertyCompanyId());

        int ownerPoints = owner.getPointBalance() != null ? owner.getPointBalance() : 0;
        BigDecimal ownerCoins = owner.getCoinBalance() != null ? owner.getCoinBalance() : BigDecimal.ZERO;

        // 一个房号只允许一个家庭：此前只有前端选择器在挡，接口层能绕过造出两个家庭同房号。
        // 已被别人占住时不再简单拒死，而是引导走「户主添加家庭成员」这条正确路径。
        roomOccupancyGuard.assertRoomNotTaken(request.getCommunityId(), request.getBuilding(),
                request.getUnit(), request.getRoom(), null, owner.getId());

        Family family = new Family();
        family.setId(idGenerator.generateId("fam"));
        family.setName(request.getName());
        family.setOwnerId(owner.getId());
        family.setPropertyCompanyId(owner.getPropertyCompanyId());
        family.setCommunityId(request.getCommunityId());
        family.setBuilding(request.getBuilding());
        family.setUnit(request.getUnit());
        family.setRoom(request.getRoom());
        family.setMemberCount(1);
        family.setPointBalance(ownerPoints);
        family.setCoinBalance(ownerCoins);
        family.setVoteWeight(1);
        family.setAuditStatus("pending");
        family.setStatus("active");
        familyRepository.save(family);

        owner.setFamilyId(family.getId());
        owner.setFamilyRelation("owner");
        owner.setPointBalance(0);
        owner.setCoinBalance(BigDecimal.ZERO);
        owner.setCommunityId(request.getCommunityId());
        owner.setBuilding(request.getBuilding());
        owner.setUnit(request.getUnit());
        owner.setRoom(request.getRoom());
        residentRepository.save(owner);

        return familyConverter.toResponse(family);
    }

    @Transactional(readOnly = true)
    public FamilyDetailResponse getMyFamily() {
        roleGuard.requireAnyRole("resident");
        Resident current = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(current.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在");
        }
        Family family = getFamilyOrThrow(current.getFamilyId());
        if (!family.getId().equals(current.getFamilyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return buildDetail(family);
    }

    @Transactional
    public FamilyDetailResponse updateMyFamily(UpdateFamilyRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident current = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(current.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在");
        }
        Family family = getFamilyForUpdate(current.getFamilyId());
        assertFamilyOwner(family);
        String beforeAddress = addressKey(family.getCommunityId(), family.getBuilding(), family.getUnit(), family.getRoom());
        applyFamilyUpdate(family, request);
        if (!beforeAddress.equals(addressKey(family.getCommunityId(), family.getBuilding(),
                family.getUnit(), family.getRoom()))) {
            roomOccupancyGuard.assertRoomNotTaken(family.getCommunityId(), family.getBuilding(),
                    family.getUnit(), family.getRoom(), family.getId(), current.getId());
            syncMemberAddress(family);
        }
        familyRepository.save(family);
        return buildDetail(family);
    }

    @Transactional
    public AddFamilyMemberResponse addMember(String familyId, AddFamilyMemberRequest request) {
        roleGuard.requireAnyRole("resident");
        Family family = getFamilyForUpdate(familyId);
        assertFamilyOwner(family);
        assertSameProperty(family.getPropertyCompanyId(), UserContext.currentPropertyCompanyId());

        if (!RoleGuard.FAMILY_RELATIONS.contains(request.getRelation())
                || "owner".equals(request.getRelation())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "relation值无效");
        }
        if (family.getMemberCount() != null && family.getMemberCount() >= RoleGuard.FAMILY_MEMBER_MAX) {
            throw new BizException(ErrorCode.FAMILY_MEMBER_LIMIT);
        }

        Resident memberSnapshot = residentRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "成员未注册"));
        // 锁顺序：家庭 -> 成员，与 PointService/PropertyCoinService 保持一致。
        Resident member = residentRepository.findByIdForUpdate(memberSnapshot.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "成员未注册"));
        if (UserType.tenant.name().equals(member.getUserType())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "租住人员不能加入家庭");
        }
        if (StringUtils.hasText(member.getFamilyId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该用户已属于其他家庭");
        }
        assertSameProperty(member.getPropertyCompanyId(), family.getPropertyCompanyId());
        assertAccountActive(member);

        if (StringUtils.hasText(request.getName())) {
            member.setName(request.getName());
        }

        mergeMemberBalanceToFamily(family, member);
        family.setMemberCount(defaultMemberCount(family) + 1);

        member.setFamilyId(family.getId());
        member.setFamilyRelation(request.getRelation());
        // 家庭成员的住址以家庭为准：此前只搬余额不写地址，被添加进来的人仍是「无房号住户」，
        // 定向通告按楼栋筛选、邻居列表、楼栋长范围都查不到他（2026-11-06 家庭与房号冲突整改）。
        applyAddress(member, family);

        familyRepository.save(family);
        residentRepository.save(member);

        return AddFamilyMemberResponse.builder()
                .memberId(member.getId())
                .familyId(family.getId())
                .name(member.getName())
                .phone(PhoneUtil.mask(member.getPhone()))
                .relation(request.getRelation())
                .isOwner(false)
                .joinedAt(DateTimeUtil.toIsoUtc(member.getUpdatedAt()))
                .build();
    }

    @Transactional
    public RemoveFamilyMemberResponse removeMember(String familyId, String memberId, String reason) {
        roleGuard.requireAnyRole("resident");
        Family family = getFamilyForUpdate(familyId);
        assertFamilyOwner(family);

        if (family.getOwnerId().equals(memberId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "户主不可直接移除，请先转让或解散家庭");
        }

        Resident member = residentRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "成员不存在"));
        if (!familyId.equals(member.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "成员不存在");
        }

        int memberCount = defaultMemberCount(family);
        if (memberCount <= 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "家庭至少保留一名成员");
        }

        int avgPoints = defaultPointBalance(family) / memberCount;
        BigDecimal avgCoin = defaultCoinBalance(family)
                .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.DOWN);

        member.setPointBalance(avgPoints);
        member.setCoinBalance(avgCoin);
        member.setFamilyId(null);
        member.setFamilyRelation(null);
        releaseRoom(member);

        family.setPointBalance(defaultPointBalance(family) - avgPoints);
        family.setCoinBalance(defaultCoinBalance(family).subtract(avgCoin));
        family.setMemberCount(memberCount - 1);

        familyRepository.save(family);
        residentRepository.save(member);

        return RemoveFamilyMemberResponse.builder()
                .memberId(memberId)
                .familyId(familyId)
                .removedAt(DateTimeUtil.nowIsoUtc())
                .pointsHandling(POINTS_HANDLING_TRANSFER)
                .coinsHandling(COINS_HANDLING_TRANSFER)
                .build();
    }

    @Transactional(readOnly = true)
    public FamilyMemberListResponse listMembers(String familyId) {
        Family family = getFamilyOrThrow(familyId);
        if (roleGuard.isPlatformAdmin() || roleGuard.isPropertyAdmin()) {
            roleGuard.assertSamePropertyOrPlatform(family.getPropertyCompanyId());
        } else {
            roleGuard.requireAnyRole("resident");
            Resident current = getActiveResident(UserContext.currentUserId());
            if (!family.getId().equals(current.getFamilyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }

        List<Resident> members = residentRepository.findByFamilyId(familyId);
        return FamilyMemberListResponse.builder()
                .list(members.stream()
                        .map(member -> familyConverter.toMemberItem(member, family.getOwnerId()))
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberContributionResponse> getMemberContributions() {
        roleGuard.requireAnyRole("resident");
        Resident current = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(current.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在");
        }

        Family family = getFamilyOrThrow(current.getFamilyId());
        List<Resident> members = residentRepository.findByFamilyId(family.getId());

        int totalPoints = members.stream()
                .mapToInt(m -> m.getPointBalance() != null ? m.getPointBalance() : 0)
                .sum();

        return members.stream()
                .map(member -> {
                    int memberPoints = member.getPointBalance() != null ? member.getPointBalance() : 0;
                    BigDecimal percent = totalPoints > 0
                            ? BigDecimal.valueOf(memberPoints)
                                    .divide(BigDecimal.valueOf(totalPoints), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return FamilyMemberContributionResponse.builder()
                            .residentId(member.getId())
                            .name(member.getName())
                            .familyRelation(member.getFamilyRelation())
                            .pointBalance(memberPoints)
                            .contributionPercent(percent)
                            .build();
                })
                .toList();
    }

    private FamilyDetailResponse buildDetail(Family family) {
        List<Resident> members = residentRepository.findByFamilyId(family.getId());
        return familyConverter.toDetail(family, members);
    }

    /**
     * 家庭地址变更后把住址同步到全部成员（含户主）：`residents` 与 `families` 的地址必须一致，
     * 否则「同房号」判定、定向通告楼栋筛选、家庭池口径会各自读到不同答案。
     */
    private void syncMemberAddress(Family family) {
        for (Resident member : residentRepository.findByFamilyId(family.getId())) {
            applyAddress(member, family);
            residentRepository.save(member);
        }
    }

    /**
     * 脱离家庭即不再占住房号（2026-11-06 老板口径）。
     *
     * <p>住址随家庭写入成员后，若退出/被移出时仍留着楼栋单元房号，这个家庭就永久占着一个房号：
     * 可选房号列表里不会出现，别人也建不了家庭，而系统里又查不到这个人和该房号的关系。
     * 只清 楼栋/单元/房号，保留 {@code communityId}（他仍属于这个小区账号，与刚注册未填地址的人同状态）。</p>
     */
    private void releaseRoom(Resident member) {
        member.setBuilding(null);
        member.setUnit(null);
        member.setRoom(null);
    }

    private void applyAddress(Resident member, Family family) {
        // 家庭本身没有地址（历史数据/管理端预建）时不写空值，否则会把成员原有的住址抹掉。
        if (!StringUtils.hasText(family.getBuilding()) || !StringUtils.hasText(family.getRoom())) {
            return;
        }
        member.setCommunityId(family.getCommunityId());
        member.setBuilding(family.getBuilding());
        member.setUnit(family.getUnit());
        member.setRoom(family.getRoom());
    }

    /** 地址比较键：单元/房号的历史值可能是 null 或带空格，统一按空串等价处理。 */
    private String addressKey(String communityId, String building, String unit, String room) {
        return String.join("|", nullToEmpty(communityId), nullToEmpty(building),
                nullToEmpty(unit), nullToEmpty(room));
    }

    private String nullToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private void applyFamilyUpdate(Family family, UpdateFamilyRequest request) {
        if (StringUtils.hasText(request.getName())) {
            family.setName(request.getName());
        }
        if (request.getBuilding() != null) {
            family.setBuilding(request.getBuilding());
        }
        if (request.getUnit() != null) {
            family.setUnit(request.getUnit());
        }
        if (request.getRoom() != null) {
            family.setRoom(request.getRoom());
        }
    }

    private void mergeMemberBalanceToFamily(Family family, Resident member) {
        int memberPoints = member.getPointBalance() != null ? member.getPointBalance() : 0;
        BigDecimal memberCoins = member.getCoinBalance() != null ? member.getCoinBalance() : BigDecimal.ZERO;

        family.setPointBalance(defaultPointBalance(family) + memberPoints);
        family.setCoinBalance(defaultCoinBalance(family).add(memberCoins));

        member.setPointBalance(0);
        member.setCoinBalance(BigDecimal.ZERO);
    }

    private Family getFamilyOrThrow(String familyId) {
        return familyRepository.findById(familyId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
    }

    /** 家庭余额变更场景统一走行锁；锁顺序：家庭 -> 住户。 */
    private Family getFamilyForUpdate(String familyId) {
        return familyRepository.findByIdForUpdate(familyId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "家庭不存在"));
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

    @Transactional
    public RemoveFamilyMemberResponse leaveFamily() {
        roleGuard.requireAnyRole("resident");
        Resident current = getActiveResident(UserContext.currentUserId());
        if (!StringUtils.hasText(current.getFamilyId())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "未加入任何家庭");
        }

        Family family = getFamilyForUpdate(current.getFamilyId());
        Resident lockedCurrent = residentRepository.findByIdForUpdate(current.getId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        current = lockedCurrent;
        if (family.getOwnerId().equals(current.getId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "户主不可退出家庭，请先转让或解散家庭");
        }

        int memberCount = defaultMemberCount(family);
        if (memberCount <= 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "家庭至少保留一名成员");
        }

        int avgPoints = defaultPointBalance(family) / memberCount;
        BigDecimal avgCoin = defaultCoinBalance(family)
                .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.DOWN);

        current.setPointBalance(avgPoints);
        current.setCoinBalance(avgCoin);
        current.setFamilyId(null);
        current.setFamilyRelation(null);
        releaseRoom(current);

        family.setPointBalance(defaultPointBalance(family) - avgPoints);
        family.setCoinBalance(defaultCoinBalance(family).subtract(avgCoin));
        family.setMemberCount(memberCount - 1);

        familyRepository.save(family);
        residentRepository.save(current);

        return RemoveFamilyMemberResponse.builder()
                .memberId(current.getId())
                .familyId(family.getId())
                .removedAt(DateTimeUtil.nowIsoUtc())
                .pointsHandling(POINTS_HANDLING_TRANSFER)
                .coinsHandling(COINS_HANDLING_TRANSFER)
                .build();
    }

    private void assertFamilyOwner(Family family) {
        String currentUserId = UserContext.currentUserId();
        if (currentUserId == null || !currentUserId.equals(family.getOwnerId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
    }

    private void assertSameProperty(String leftPropertyCompanyId, String rightPropertyCompanyId) {
        if (leftPropertyCompanyId == null
                || rightPropertyCompanyId == null
                || !leftPropertyCompanyId.equals(rightPropertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "不可跨物业操作");
        }
    }

    private void assertAccountActive(Resident resident) {
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    private int defaultMemberCount(Family family) {
        return family.getMemberCount() != null ? family.getMemberCount() : 0;
    }

    private int defaultPointBalance(Family family) {
        return family.getPointBalance() != null ? family.getPointBalance() : 0;
    }

    private BigDecimal defaultCoinBalance(Family family) {
        return family.getCoinBalance() != null ? family.getCoinBalance() : BigDecimal.ZERO;
    }
}
