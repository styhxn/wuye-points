package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.auth.AdminLoginRequest;
import com.wuye.points.dto.auth.AuthLoginResponse;
import com.wuye.points.dto.auth.BindPhoneRequest;
import com.wuye.points.dto.auth.BindPhoneResponse;
import com.wuye.points.dto.auth.ProfileResponse;
import com.wuye.points.dto.auth.RefreshTokenRequest;
import com.wuye.points.dto.auth.ResidentAuthSummaryResponse;
import com.wuye.points.dto.auth.TokenResponse;
import com.wuye.points.dto.auth.WechatLoginRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.JwtUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.SnowflakeIdGenerator;
import cn.hutool.crypto.digest.BCrypt;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final Set<String> PROFILE_ROLES = Set.of("resident", "property_admin", "platform_admin");

    private final WechatMpService wechatMpService;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final CommunityRepository communityRepository;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public AuthLoginResponse wechatLogin(WechatLoginRequest request) {
        WechatMpService.WechatSession session = wechatMpService.code2Session(request.getCode());

        Resident resident = residentRepository.findByOpenid(session.getOpenid()).orElse(null);
        boolean newUser = false;

        if (resident == null) {
            resident = createResident(session, request.getPropertyCompanyId());
            newUser = true;
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

        assertAccountActive(resident);

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
        return buildTokenResponse(resident);
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
        String role = UserContext.currentRole();
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

        return toProfile(resident, propertyName, communityName);
    }

    private Resident createResident(WechatMpService.WechatSession session, String propertyCompanyId) {
        String resolvedPropertyCompanyId = resolvePropertyCompanyId(propertyCompanyId);

        Resident resident = new Resident();
        resident.setId(idGenerator.generateId("res"));
        resident.setOpenid(session.getOpenid());
        resident.setUnionid(session.getUnionid());
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
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    private TokenResponse buildTokenResponse(Resident resident) {
        String accessToken = jwtUtil.generateAccessToken(
                resident.getId(),
                resident.getRole(),
                resident.getPropertyCompanyId(),
                resident.getFamilyId());
        String refreshToken = jwtUtil.generateRefreshToken(resident.getId());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType(TOKEN_TYPE_BEARER)
                .build();
    }

    private ResidentAuthSummaryResponse toAuthSummary(Resident resident, boolean newUser) {
        return ResidentAuthSummaryResponse.builder()
                .id(resident.getId())
                .name(resident.getName())
                .avatarUrl(resident.getAvatarUrl())
                .phone(PhoneUtil.mask(resident.getPhone()))
                .role(resident.getRole())
                .propertyCompanyId(resident.getPropertyCompanyId())
                .communityId(resident.getCommunityId())
                .familyId(resident.getFamilyId())
                .wechatBound(Boolean.TRUE.equals(resident.getWechatBound()))
                .newUser(newUser)
                .status(resident.getStatus())
                .build();
    }

    private ProfileResponse toProfile(Resident resident, String propertyName, String communityName) {
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
                .propertyCompanyId(resident.getPropertyCompanyId())
                .propertyName(propertyName)
                .communityId(resident.getCommunityId())
                .communityName(communityName)
                .building(resident.getBuilding())
                .unit(resident.getUnit())
                .room(resident.getRoom())
                .familyId(resident.getFamilyId())
                .pointBalance(resident.getPointBalance() != null ? resident.getPointBalance() : 0)
                .coinBalance(resident.getCoinBalance() != null ? resident.getCoinBalance() : BigDecimal.ZERO)
                .coinFrozen(Boolean.TRUE.equals(resident.getCoinFrozen()))
                .coinHidden(Boolean.TRUE.equals(resident.getCoinHidden()))
                .wechatBound(Boolean.TRUE.equals(resident.getWechatBound()))
                .status(resident.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(resident.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(resident.getUpdatedAt()))
                .build();
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
