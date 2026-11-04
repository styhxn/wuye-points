package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.auth.AdminLoginRequest;
import com.wuye.points.dto.auth.AuthLoginResponse;
import com.wuye.points.dto.auth.BindPhoneRequest;
import com.wuye.points.dto.auth.BindPhoneResponse;
import com.wuye.points.dto.auth.MyPermissionsResponse;
import com.wuye.points.dto.auth.ProfileResponse;
import com.wuye.points.dto.auth.RefreshTokenRequest;
import com.wuye.points.dto.auth.ResidentAuthSummaryResponse;
import com.wuye.points.dto.auth.TokenResponse;
import com.wuye.points.dto.auth.WechatLoginRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.IndividualLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentMerchantApplication;
import com.wuye.points.entity.SectorLeader;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.JwtUtil;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import cn.hutool.crypto.digest.BCrypt;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final Set<String> PROFILE_ROLES = Set.of(
            "resident", "property_admin", "platform_admin", "community_admin");

    // 测试模式：放开历史初始口令，并允许普通住户登录管理端；上线前必须设为 false。
    @Value("${app.auth.test-mode:true}")
    private boolean testMode;

    private final WechatMpService wechatMpService;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final BuildingLeaderRepository buildingLeaderRepository;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator idGenerator;
    private final LeaderIdentityGuard leaderIdentityGuard;
    private final ResidentIdentityTags residentIdentityTags;
    private final PermissionGuard permissionGuard;
    private final RoleGuard roleGuard;

    @Transactional
    public AuthLoginResponse wechatLogin(WechatLoginRequest request) {
        WechatMpService.WechatSession session = wechatMpService.code2Session(request.getCode());

        Resident resident = residentRepository.findByOpenid(session.getOpenid()).orElse(null);
        boolean newUser = false;

        if (resident == null) {
            // 首次微信登录（该 openid 没见过）必须授权手机号：管理端预建账号只有手机号，
            // 不要求授权就会新建一条「有 openid 没手机号」的幽灵记录，本人永远对不上号。
            if (!StringUtils.hasText(request.getPhoneCode())) {
                throw new BizException(ErrorCode.WECHAT_PHONE_AUTH_REQUIRED);
            }
            String phone = wechatMpService.getPhoneNumber(request.getPhoneCode());
            resident = bindWechatToExistingAccount(session, request, phone);
            if (resident == null) {
                resident = createResident(session, request.getPropertyCompanyId(), phone);
                newUser = true;
            }
        } else {
            updateWechatSession(resident, session, request.getPropertyCompanyId());
        }

        assertAccountActive(resident);

        TokenResponse tokens = buildTokenResponse(resident);
        return AuthLoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .expiresIn(tokens.getExpiresIn())
                .tokenType(tokens.getTokenType())
                .resident(toAuthSummary(resident, newUser))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthLoginResponse adminLogin(AdminLoginRequest request) {
        Resident resident = residentRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "手机号或密码错误"));

        if (resident.getPassword() == null || !BCrypt.checkpw(request.getPassword(), resident.getPassword())) {
            throw new BizException(ErrorCode.RESIDENT_NOT_FOUND, "手机号或密码错误");
        }

        // 测试模式放开历史初始口令；关闭测试模式后必须拒绝该弱口令。
        if (!testMode && "admin123456".equals(request.getPassword())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "初始密码已禁用，请重置密码后再登录");
        }

        assertAccountActive(resident);

        String role = RoleGuard.normalizeRole(resident.getRole());
        // 2026-10-20 负责人标签化：统筹/板块/个体负责人 role 归一化为 resident，
        // 但其管理端登录权限不变——持有任意一种启用中负责人身份的住户仍可登录管理端。
        // 2026-10-21 组长标签化：活动组组长同为住户子身份，持组长身份的住户同样可登录管理端
        // （组长小店/提现等能力经 admin-login 使用）。
        // 测试模式下跳过管理端角色校验，方便普通住户测试账号联调；上线前必须关闭。
        if (!testMode
                && ("resident".equals(role) || "tenant".equals(role))
                && !leaderIdentityGuard.hasAnyLeaderIdentity(
                        resident.getId(), resident.getPropertyCompanyId())
                && !residentIdentityTags.isActivityLeader(resident)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "该账号无管理端登录权限");
        }

        // 2026-09-08：物业公司停用后不可再经营（配合 §51.5 PATCH /{id}/status=inactive 的两档删除语义）
        assertPropertyCompanyActive(resident);

        TokenResponse tokens = buildTokenResponse(resident);
        return AuthLoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .expiresIn(tokens.getExpiresIn())
                .tokenType(tokens.getTokenType())
                .resident(toAuthSummary(resident, false))
                .build();
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        Claims claims = jwtUtil.parseRefreshToken(request.getRefreshToken());
        Resident resident = residentRepository.findById(claims.getSubject())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        assertAccountActive(resident);
        Integer tokenVersion = jwtUtil.getTokenVersion(claims);
        // 新版本 refresh token 必须携带 tv；上线前签发的旧 refresh token 统一失效，用户重新登录一次。
        if (tokenVersion == null || tokenVersion != tokenVersion(resident)) {
            throw new BizException(ErrorCode.TOKEN_INVALID, "登录状态已失效，请重新登录");
        }
        return buildTokenResponse(resident);
    }

    /**
     * 登出：递增令牌版本号，使该账号所有旧 access/refresh token 立即失效。
     */
    @Transactional
    public void logout() {
        String userId = UserContext.currentUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        resident.setTokenVersion(tokenVersion(resident) + 1);
        residentRepository.save(resident);
    }

    @Transactional
    public BindPhoneResponse bindPhone(BindPhoneRequest request) {
        requireRole("resident");

        String userId = UserContext.currentUserId();
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        String phone = wechatMpService.getPhoneNumber(request.getCode());
        residentRepository.findByPhone(phone).ifPresent(existing -> {
            if (!existing.getId().equals(resident.getId())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "该手机号已被其他账号绑定");
            }
        });

        resident.setPhone(phone);
        residentRepository.save(resident);

        return BindPhoneResponse.builder()
                .phone(PhoneUtil.mask(phone))
                .bound(true)
                .boundAt(DateTimeUtil.nowIsoUtc())
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        String role = RoleGuard.normalizeRole(UserContext.currentRole());
        if (role == null || !PROFILE_ROLES.contains(role)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }

        String userId = UserContext.currentUserId();
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        String propertyName = propertyCompanyRepository.findById(resident.getPropertyCompanyId())
                .map(PropertyCompany::getName)
                .orElse(null);

        String communityName = null;
        if (StringUtils.hasText(resident.getCommunityId())) {
            communityName = communityRepository.findById(resident.getCommunityId())
                    .map(Community::getName)
                    .orElse(null);
        }

        return enrichProfilePermissions(toProfile(resident, propertyName, communityName), resident);
    }

    /** 当前用户有效权限码，供前端权限配置页/按钮级渲染直接使用。 */
    @Transactional(readOnly = true)
    public MyPermissionsResponse getMyPermissions() {
        Resident resident = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        Set<String> roleCodes = permissionGuard.listEffectiveRoleCodes(
                resident.getId(), resident.getPropertyCompanyId());
        List<String> permissions = permissionGuard
                .listEffectivePermissionCodes(resident.getId(), resident.getPropertyCompanyId())
                .stream().map(code -> code.replace('.', ':')).sorted().toList();
        return MyPermissionsResponse.builder()
                .role(resident.getRole())
                .normalizedRole(PermissionGuard.normalizeBaseRole(resident.getRole()))
                .propertySubRole(resident.getPropertySubRole())
                .propertyCompanyId(resident.getPropertyCompanyId())
                .adminWorkspace(roleGuard.isAdminWorkspaceUser(resident))
                .roleCodes(roleCodes.stream().sorted().toList())
                .permissions(permissions)
                .build();
    }

    private ProfileResponse enrichProfilePermissions(ProfileResponse profile, Resident resident) {
        Set<String> roleCodes = permissionGuard.listEffectiveRoleCodes(
                resident.getId(), resident.getPropertyCompanyId());
        List<String> permissions = permissionGuard
                .listEffectivePermissionCodes(resident.getId(), resident.getPropertyCompanyId())
                .stream().map(code -> code.replace('.', ':')).sorted().toList();
        var preset = permissionGuard.findPrimaryPreset(roleCodes).orElse(null);
        profile.setNormalizedRole(PermissionGuard.normalizeBaseRole(resident.getRole()));
        profile.setPropertySubRole(resident.getPropertySubRole());
        profile.setEffectiveRoleCodes(roleCodes.stream().sorted().toList());
        profile.setEffectivePermissions(permissions);
        if (preset != null) {
            profile.setRolePresetId(preset.getId());
            profile.setRolePresetName(preset.getName());
        }
        return profile;
    }

    /**
     * 首次微信登录时，把微信身份绑到该手机号已有的账号上。
     *
     * <p>管理端预建的商家/组长/技工/配送员账号 openid 为空，此前登录只按 openid 匹配，本人一进小程序就新建一条记录，
     * 于是「有手机号的账号没 openid、有 openid 的账号没手机号」，分账个人接收方永远登记不到人。</p>
     *
     * <p>{@code residents.openid} 是唯一索引：一旦把 openid 绑到错的账号，正确账号就再也绑不上，
     * 只能走管理端「微信身份认领」解开。所以同手机号命中多条账号时一律拒绝自动绑定，不做「取第一条」的猜测。</p>
     *
     * @param phone 已由 {@code phoneCode} 换取的手机号
     * @return 绑定完成的已有账号；该手机号下没有可绑账号时返回 null（按新建处理）
     */
    private Resident bindWechatToExistingAccount(WechatMpService.WechatSession session,
                                                 WechatLoginRequest request,
                                                 String phone) {
        String scopedPropertyCompanyId = resolvePropertyCompanyId(request.getPropertyCompanyId());
        List<Resident> candidates = residentRepository.findAllByPhone(phone).stream()
                .filter(r -> r.getDeletedAt() == null && !ResidentStatus.deleted.name().equals(r.getStatus()))
                .filter(r -> !StringUtils.hasText(scopedPropertyCompanyId)
                        || scopedPropertyCompanyId.equals(r.getPropertyCompanyId()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() > 1) {
            throw new BizException(ErrorCode.WECHAT_PHONE_ACCOUNT_AMBIGUOUS,
                    "手机号 " + PhoneUtil.mask(phone) + " 在当前范围内匹配到 " + candidates.size()
                            + " 个账号，无法自动绑定，请联系物业在管理端做微信身份认领");
        }
        Resident existing = candidates.get(0);
        if (StringUtils.hasText(existing.getOpenid()) && !existing.getOpenid().equals(session.getOpenid())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "该手机号账号已绑定其他微信身份，请先在管理端做微信身份认领");
        }
        existing.setOpenid(session.getOpenid());
        existing.setUnionid(session.getUnionid());
        existing.setWechatBound(true);
        Resident saved = residentRepository.save(existing);
        log.info("[微信登录] 微信身份绑到已有账号 residentId={} propertyCompanyId={}",
                saved.getId(), saved.getPropertyCompanyId());
        return saved;
    }

    private Resident createResident(WechatMpService.WechatSession session, String propertyCompanyId, String phone) {
        String resolvedPropertyCompanyId = resolvePropertyCompanyId(propertyCompanyId);

        Resident resident = new Resident();
        resident.setId(idGenerator.generateId("res"));
        resident.setOpenid(session.getOpenid());
        resident.setUnionid(session.getUnionid());
        // 新建即带手机号：此后不再存在「有 openid 没手机号」的记录形态
        resident.setPhone(phone);
        resident.setPropertyCompanyId(resolvedPropertyCompanyId);
        resident.setRole("resident");
        resident.setUserType("owner");
        resident.setGender(0);
        resident.setHasChildren(false);
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setCoinFrozen(false);
        resident.setCoinHidden(false);
        resident.setWechatBound(true);
        resident.setStatus(ResidentStatus.active.name());
        return residentRepository.save(resident);
    }

    private void updateWechatSession(Resident resident, WechatMpService.WechatSession session, String propertyCompanyId) {
        resident.setWechatBound(true);
        if (StringUtils.hasText(session.getUnionid())) {
            resident.setUnionid(session.getUnionid());
        }
        if (!StringUtils.hasText(resident.getPropertyCompanyId()) && StringUtils.hasText(propertyCompanyId)) {
            resident.setPropertyCompanyId(resolvePropertyCompanyId(propertyCompanyId));
        }
        residentRepository.save(resident);
    }

    private String resolvePropertyCompanyId(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return null;
        }
        if (!propertyCompanyId.startsWith("pc_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
        }
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "物业公司不存在"));
        return propertyCompanyId;
    }

    private void assertAccountActive(Resident resident) {
        if (resident.getDeletedAt() != null
                || ResidentStatus.deleted.name().equals(resident.getStatus())
                || ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    private int tokenVersion(Resident resident) {
        return resident.getTokenVersion() != null ? resident.getTokenVersion() : 0;
    }

    /**
     * 2026-09-08：物业公司停用后不可再经营。
     * 仅拦截管理端登录（住户端已由 {@code GET /property-companies/current} 拦截），
     * 且只在状态明确为 inactive 时拒绝，避免历史脏数据误伤。
     */
    private void assertPropertyCompanyActive(Resident resident) {
        // 平台超管不属于任何单一物业，停用某物业不应影响其登录
        if ("platform_admin".equals(resident.getRole())) {
            return;
        }
        String propertyCompanyId = resident.getPropertyCompanyId();
        if (propertyCompanyId == null) {
            return;
        }
        propertyCompanyRepository.findById(propertyCompanyId).ifPresent(company -> {
            if ("inactive".equals(company.getStatus())) {
                throw new BizException(ErrorCode.ACCOUNT_DISABLED, "物业公司已停用");
            }
        });
    }

    private TokenResponse buildTokenResponse(Resident resident) {
        int tokenVersion = tokenVersion(resident);
        String accessToken = jwtUtil.generateAccessToken(
                resident.getId(),
                resident.getRole(),
                resident.getPropertyCompanyId(),
                resident.getFamilyId(),
                tokenVersion);
        String refreshToken = jwtUtil.generateRefreshToken(resident.getId(), tokenVersion);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType(TOKEN_TYPE_BEARER)
                .build();
    }

    private ResidentAuthSummaryResponse toAuthSummary(Resident resident, boolean newUser) {
        BuildingLeader leader = findActiveBuildingLeader(resident);
        List<String> sectorLeaderIds = leaderIdentityGuard.activeSectorLeaders(
                        resident.getId(), resident.getPropertyCompanyId()).stream()
                .map(SectorLeader::getId)
                .toList();
        ActivityLeaderIdentity activityLeader = residentIdentityTags.activeActivityLeader(resident).orElse(null);
        ResidentMerchantApplication residentMerchant = residentIdentityTags
                .activeResidentMerchant(resident.getId()).orElse(null);
        Merchant distributor = residentIdentityTags.enabledDistributor(resident.getId()).orElse(null);
        return ResidentAuthSummaryResponse.builder()
                .id(resident.getId())
                .name(resident.getName())
                .avatarUrl(resident.getAvatarUrl())
                .phone(PhoneUtil.mask(resident.getPhone()))
                .role(resident.getRole())
                .adminWorkspace(roleGuard.isAdminWorkspaceUser(resident))
                .propertyCompanyId(resident.getPropertyCompanyId())
                .communityId(resident.getCommunityId())
                .familyId(resident.getFamilyId())
                .wechatBound(Boolean.TRUE.equals(resident.getWechatBound()))
                .isBuildingLeader(leader != null)
                // 2026-10-20 负责人标签：统筹/板块负责人/个体负责人身份（住户子身份，role 仍为 resident）
                .isCoordinator(leaderIdentityGuard.activeCoordinator(
                        resident.getId(), resident.getPropertyCompanyId()).isPresent())
                .coordinatorId(leaderIdentityGuard.activeCoordinator(
                        resident.getId(), resident.getPropertyCompanyId())
                        .map(Coordinator::getId).orElse(null))
                .isSectorLeader(!sectorLeaderIds.isEmpty())
                .sectorLeaderIds(sectorLeaderIds)
                .isIndividualLeader(leaderIdentityGuard.activeIndividualLeader(
                        resident.getId(), resident.getPropertyCompanyId()).isPresent())
                .individualLeaderId(leaderIdentityGuard.activeIndividualLeader(
                        resident.getId(), resident.getPropertyCompanyId())
                        .map(IndividualLeader::getId).orElse(null))
                // 2026-10-21：「我的」页四图标标签（小店/组长/一级代理，均为住户子身份）
                .isActivityLeader(activityLeader != null)
                .activityLeaderId(activityLeader != null ? activityLeader.getId() : null)
                .isResidentMerchant(residentMerchant != null)
                .residentMerchantId(residentMerchant != null ? residentMerchant.getId() : null)
                .isDistributor(distributor != null)
                .distributorId(distributor != null ? distributor.getId() : null)
                .newUser(newUser)
                .status(resident.getStatus())
                .build();
    }

    private ProfileResponse toProfile(Resident resident, String propertyName, String communityName) {
        BuildingLeader leader = findActiveBuildingLeader(resident);
        List<String> sectorLeaderIds = leaderIdentityGuard.activeSectorLeaders(
                        resident.getId(), resident.getPropertyCompanyId()).stream()
                .map(SectorLeader::getId)
                .toList();
        Coordinator coordinator = leaderIdentityGuard.activeCoordinator(
                resident.getId(), resident.getPropertyCompanyId()).orElse(null);
        IndividualLeader individualLeader = leaderIdentityGuard.activeIndividualLeader(
                resident.getId(), resident.getPropertyCompanyId()).orElse(null);
        ActivityLeaderIdentity activityLeader = residentIdentityTags.activeActivityLeader(resident).orElse(null);
        ResidentMerchantApplication residentMerchant = residentIdentityTags
                .activeResidentMerchant(resident.getId()).orElse(null);
        Merchant distributor = residentIdentityTags.enabledDistributor(resident.getId()).orElse(null);
        return ProfileResponse.builder()
                .id(resident.getId())
                .openid(resident.getOpenid())
                .unionid(resident.getUnionid())
                .phone(PhoneUtil.mask(resident.getPhone()))
                .name(resident.getName())
                .avatarUrl(resident.getAvatarUrl())
                .gender(resident.getGender())
                .age(resident.getAge())
                .maritalStatus(resident.getMaritalStatus())
                .hasChildren(resident.getHasChildren())
                .userType(resident.getUserType())
                .role(resident.getRole())
                .adminWorkspace(roleGuard.isAdminWorkspaceUser(resident))
                .propertyCompanyId(resident.getPropertyCompanyId())
                .propertyName(propertyName)
                .communityId(resident.getCommunityId())
                .communityName(communityName)
                .building(resident.getBuilding())
                .floor(resident.getFloor())
                .unit(resident.getUnit())
                .room(resident.getRoom())
                .familyId(resident.getFamilyId())
                .pointBalance(resident.getPointBalance() != null ? resident.getPointBalance() : 0)
                .coinBalance(resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO)
                .coinFrozen(Boolean.TRUE.equals(resident.getCoinFrozen()))
                .coinHidden(Boolean.TRUE.equals(resident.getCoinHidden()))
                .wechatBound(Boolean.TRUE.equals(resident.getWechatBound()))
                .isBuildingLeader(leader != null)
                .buildingLeaderId(leader != null ? leader.getId() : null)
                // 2026-10-20 负责人标签：统筹/板块负责人/个体负责人身份（住户子身份，role 仍为 resident）
                .isCoordinator(coordinator != null)
                .coordinatorId(coordinator != null ? coordinator.getId() : null)
                .isSectorLeader(!sectorLeaderIds.isEmpty())
                .sectorLeaderIds(sectorLeaderIds)
                .isIndividualLeader(individualLeader != null)
                .individualLeaderId(individualLeader != null ? individualLeader.getId() : null)
                // 2026-10-21：「我的」页四图标标签（小店/组长/一级代理，均为住户子身份）
                .isActivityLeader(activityLeader != null)
                .activityLeaderId(activityLeader != null ? activityLeader.getId() : null)
                .isResidentMerchant(residentMerchant != null)
                .residentMerchantId(residentMerchant != null ? residentMerchant.getId() : null)
                .isDistributor(distributor != null)
                .distributorId(distributor != null ? distributor.getId() : null)
                .status(resident.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(resident.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
    }

    /**
     * 楼长身份为「住户 + 楼长」复合身份：住户 role 保持 resident，
     * 是否楼长由 building_leaders 表存在 active 记录判定。
     */
    private BuildingLeader findActiveBuildingLeader(Resident resident) {
        if (!StringUtils.hasText(resident.getPropertyCompanyId())) {
            return null;
        }
        return buildingLeaderRepository
                .findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                        resident.getId(), resident.getPropertyCompanyId(), "active")
                .orElse(null);
    }

    private void requireRole(String... allowedRoles) {
        String role = UserContext.currentRole();
        if (role == null) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(role)) {
                return;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }
}
