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

    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident owner = getActiveResident(UserContext.currentUserId());

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
        Family family = getFamilyOrThrow(current.getFamilyId());
        assertFamilyOwner(family);
        applyFamilyUpdate(family, request);
        familyRepository.save(family);
        return buildDetail(family);
    }

    @Transactional
    public AddFamilyMemberResponse addMember(String familyId, AddFamilyMemberRequest request) {
        roleGuard.requireAnyRole("resident");
        Family family = getFamilyOrThrow(familyId);
        assertFamilyOwner(family);
        assertSameProperty(family.getPropertyCompanyId(), UserContext.currentPropertyCompanyId());

        if (!RoleGuard.FAMILY_RELATIONS.contains(request.getRelation())
                || "owner".equals(request.getRelation())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "relation值无效");
        }
        if (family.getMemberCount() != null && family.getMemberCount() >= RoleGuard.FAMILY_MEMBER_MAX) {
            throw new BizException(ErrorCode.FAMILY_MEMBER_LIMIT);
        }

        Resident member = residentRepository.findByPhone(request.getPhone())
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
        Family family = getFamilyOrThrow(familyId);
        assertFamilyOwner(family);

        if (family.getOwnerId().equals(memberId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "户主不可直接移除，请先转让或解散家庭");
        }

        Resident member = residentRepository.findById(memberId)
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

        Family family = getFamilyOrThrow(current.getFamilyId());
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
