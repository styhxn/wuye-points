package com.wuye.points.service;

import com.wuye.points.dto.auth.WechatLoginRequest;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.BuildingLeaderRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.JwtUtil;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微信登录强制按手机号并入已有账号的单元测试。
 *
 * <p>回归目标：登录原先只按 openid 找人，管理端预建账号（openid 为空）被本人登录时必然新建一条记录，
 * 造成「有手机号的账号没 openid、有 openid 的账号没手机号」，分账个人接收方永远登记不到人。</p>
 *
 * <p>V36 起收紧为强制：openid 未命中时必须带 phoneCode，否则返回 30010 让前端拉起手机号授权，
 * 不再存在「不传 phoneCode 照旧新建匿名记录」这条退路。residents.openid 是唯一索引，
 * 绑错账号只能靠管理端认领解开，所以同手机号命中多条时一律拒绝自动绑定。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceWechatPhoneBindTest {

    private static final String OPENID = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o";
    private static final String PHONE = "13900000008";
    private static final String PC = "pc_demo001";

    @Mock
    private WechatMpService wechatMpService;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private BuildingLeaderRepository buildingLeaderRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private LeaderIdentityGuard leaderIdentityGuard;
    @Mock
    private ResidentIdentityTags residentIdentityTags;
    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(wechatMpService.code2Session(anyString())).thenReturn(
                WechatMpService.WechatSession.builder().openid(OPENID).unionid("u_1").build());
        when(residentRepository.findByOpenid(OPENID)).thenReturn(Optional.empty());
        when(residentRepository.save(org.mockito.ArgumentMatchers.any(Resident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyCompanyRepository.findById(PC)).thenReturn(Optional.of(new PropertyCompany()));
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(7200000L);
    }

    private WechatLoginRequest loginRequest(String phoneCode) {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("login_code");
        request.setPhoneCode(phoneCode);
        request.setPropertyCompanyId(PC);
        return request;
    }

    private Resident account(String id, String openid, String propertyCompanyId) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName("测试管理员");
        resident.setPhone(PHONE);
        resident.setPropertyCompanyId(propertyCompanyId);
        resident.setRole("property_admin");
        resident.setStatus(ResidentStatus.active.name());
        resident.setOpenid(openid);
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        return resident;
    }

    @Test
    @DisplayName("openid 未命中且未授权手机号 → 30010 要求授权，不新建记录")
    void wechatLogin_withoutPhoneCode_requiresPhoneAuthorization() {
        BizException e = assertThrows(BizException.class, () -> authService.wechatLogin(loginRequest(null)));

        assertEquals(ErrorCode.WECHAT_PHONE_AUTH_REQUIRED.getCode(), e.getCode());
        verify(wechatMpService, never()).getPhoneNumber(anyString());
        verify(idGenerator, never()).generateId("res");
    }

    @Test
    @DisplayName("带 phoneCode 且手机号命中已有账号：绑 openid 到该账号，不再新建住户记录")
    void wechatLogin_bindsExistingAccountInsteadOfCreatingGhost() {
        Resident existing = account("res_admin", null, PC);
        when(wechatMpService.getPhoneNumber("phone_code")).thenReturn(PHONE);
        when(residentRepository.findAllByPhone(PHONE)).thenReturn(List.of(existing));

        authService.wechatLogin(loginRequest("phone_code"));

        assertEquals(OPENID, existing.getOpenid());
        assertTrue(existing.getWechatBound());
        verify(residentRepository).save(existing);
        verify(idGenerator, never()).generateId("res");
    }

    @Test
    @DisplayName("同手机号命中多条账号 → 拒绝自动绑定，避免 openid 绑错后唯一索引锁死")
    void wechatLogin_rejectsWhenPhoneMatchesMultipleAccounts() {
        when(wechatMpService.getPhoneNumber("phone_code")).thenReturn(PHONE);
        when(residentRepository.findAllByPhone(PHONE)).thenReturn(List.of(
                account("res_admin", null, PC),
                account("res_owner", null, PC)));

        BizException e = assertThrows(BizException.class, () -> authService.wechatLogin(loginRequest("phone_code")));

        assertEquals(ErrorCode.WECHAT_PHONE_ACCOUNT_AMBIGUOUS.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("微信身份认领"), e.getMessage());
        verify(idGenerator, never()).generateId("res");
    }

    @Test
    @DisplayName("其它物业公司的同名账号不参与匹配：按 propertyCompanyId 过滤后仍可唯一绑定")
    void wechatLogin_ignoresAccountsOfOtherPropertyCompanies() {
        Resident inScope = account("res_admin", null, PC);
        Resident outOfScope = account("res_other", null, "pc_other001");
        when(wechatMpService.getPhoneNumber("phone_code")).thenReturn(PHONE);
        when(residentRepository.findAllByPhone(PHONE)).thenReturn(List.of(inScope, outOfScope));

        authService.wechatLogin(loginRequest("phone_code"));

        assertEquals(OPENID, inScope.getOpenid());
        assertNull(outOfScope.getOpenid());
        verify(idGenerator, never()).generateId("res");
    }

    @Test
    @DisplayName("该手机号下没有账号 → 新建住户，且新建即带手机号（不再产生无手机号记录）")
    void wechatLogin_createsResidentWithPhoneWhenNoExistingAccount() {
        when(wechatMpService.getPhoneNumber("phone_code")).thenReturn(PHONE);
        when(residentRepository.findAllByPhone(PHONE)).thenReturn(List.of());
        when(idGenerator.generateId("res")).thenReturn("res_new");

        authService.wechatLogin(loginRequest("phone_code"));

        ArgumentCaptor<Resident> saved = ArgumentCaptor.forClass(Resident.class);
        verify(residentRepository).save(saved.capture());
        assertEquals(PHONE, saved.getValue().getPhone());
        assertEquals(OPENID, saved.getValue().getOpenid());
        // residents.token_version 是 NOT NULL：新建不带值会被 Hibernate 写成显式 NULL，微信注册直接 500
        assertEquals(Integer.valueOf(0), saved.getValue().getTokenVersion());
    }

    @Test
    @DisplayName("该手机号账号已绑其他微信 → 明确报错，引导走微信身份认领，不静默换绑")
    void wechatLogin_rejectsWhenExistingAccountBoundToAnotherWechat() {
        when(wechatMpService.getPhoneNumber("phone_code")).thenReturn(PHONE);
        when(residentRepository.findAllByPhone(PHONE))
                .thenReturn(List.of(account("res_admin", "oSomeoneElseElseElseElseElse0", PC)));

        BizException e = assertThrows(BizException.class, () -> authService.wechatLogin(loginRequest("phone_code")));
        assertTrue(e.getMessage().contains("微信身份认领"), e.getMessage());
    }
}
