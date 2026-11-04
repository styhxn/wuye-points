package com.wuye.points.service;

import com.wuye.points.dto.admin.resident.ClaimWechatIdentityRequest;
import com.wuye.points.dto.admin.resident.WechatClaimSourceResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微信身份认领单元测试。
 *
 * <p>回归目标：管理端预建账号 openid 为空、本人小程序登录又被新建到另一条记录上，
 * 导致分账个人接收方按 ownerId 取不到 openid。认领只迁微信身份，且必须校验手机号一致、
 * 来源无资金残留，写入顺序还要避开 residents.openid 唯一索引。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WechatIdentityClaimServiceTest {

    private static final String OPENID = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o";
    private static final String PHONE = "13900000008";

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private WechatIdentityClaimService service;

    private Resident resident(String id, String openid, String phone) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName("测试员");
        resident.setPhone(phone);
        resident.setPropertyCompanyId("pc_demo001");
        resident.setOpenid(openid);
        resident.setPointBalance(0);
        resident.setCoinBalance(BigDecimal.ZERO);
        resident.setWechatBound(openid != null);
        return resident;
    }

    private ClaimWechatIdentityRequest claimRequest(String sourceId) {
        ClaimWechatIdentityRequest request = new ClaimWechatIdentityRequest();
        request.setSourceResidentId(sourceId);
        return request;
    }

    @Test
    @DisplayName("认领成功：openid 落到目标账号，来源被清空且不可登录微信")
    void claim_movesOpenidToTarget() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", OPENID, PHONE);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        var response = service.claim("res_admin", claimRequest("res_ghost"));

        assertEquals(OPENID, target.getOpenid());
        assertTrue(target.getWechatBound());
        assertNull(source.getOpenid());
        assertFalse(source.getWechatBound());
        assertEquals("res_admin", response.getResidentId());
        assertEquals("oUpF****eS6o", response.getOpenidMasked());
    }

    @Test
    @DisplayName("写入顺序：必须先清空来源再写目标，否则撞 residents.openid 唯一索引")
    void claim_clearsSourceBeforeWritingTarget() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", OPENID, PHONE);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        service.claim("res_admin", claimRequest("res_ghost"));

        InOrder inOrder = inOrder(residentRepository);
        inOrder.verify(residentRepository).saveAndFlush(source);
        inOrder.verify(residentRepository).save(target);
    }

    @Test
    @DisplayName("目标已绑定微信身份 → 拒绝，不覆盖既有绑定")
    void claim_rejectsTargetAlreadyBound() {
        Resident target = resident("res_admin", "oOtherOtherOtherOtherOther1", PHONE);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));

        BizException e = assertThrows(BizException.class,
                () -> service.claim("res_admin", claimRequest("res_ghost")));
        assertTrue(e.getMessage().contains("已绑定微信身份"), e.getMessage());
    }

    @Test
    @DisplayName("两条记录手机号不一致 → 拒绝，防止把别人的微信绑到该账号")
    void claim_rejectsPhoneMismatch() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", OPENID, "13900000009");
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        BizException e = assertThrows(BizException.class,
                () -> service.claim("res_admin", claimRequest("res_ghost")));
        assertTrue(e.getMessage().contains("手机号不一致"), e.getMessage());
    }

    @Test
    @DisplayName("来源仍挂物业币 → 拒绝，避免钱锁在失去微信登录入口的记录上")
    void claim_rejectsSourceWithFunds() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", OPENID, PHONE);
        source.setCoinBalance(new BigDecimal("12.00"));
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        BizException e = assertThrows(BizException.class,
                () -> service.claim("res_admin", claimRequest("res_ghost")));
        assertTrue(e.getMessage().contains("物业币"), e.getMessage());
    }

    @Test
    @DisplayName("来源没有 openid → 拒绝并提示先登录")
    void claim_rejectsSourceWithoutOpenid() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", null, PHONE);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        BizException e = assertThrows(BizException.class,
                () -> service.claim("res_admin", claimRequest("res_ghost")));
        assertTrue(e.getMessage().contains("小程序登录"), e.getMessage());
    }

    @Test
    @DisplayName("候选列表排除目标自身，并给出可认领判定")
    void listClaimSources_excludesTargetAndFlagsClaimable() {
        Resident target = resident("res_admin", null, PHONE);
        Resident ghost = resident("res_ghost", OPENID, PHONE);
        Resident dirty = resident("res_dirty", "oZzzZzzZzzZzzZzzZzzZzz1", PHONE);
        dirty.setPointBalance(500);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findAllByPhone(PHONE)).thenReturn(List.of(target, ghost, dirty));

        List<WechatClaimSourceResponse> items = service.listClaimSources("res_admin");

        assertEquals(2, items.size());
        assertTrue(items.get(0).getClaimable());
        assertFalse(items.get(1).getClaimable());
        assertTrue(items.get(1).getReason().contains("物业币"), items.get(1).getReason());
        assertFalse(items.toString().contains(OPENID));
    }

    @Test
    @DisplayName("认领动作先过物业公司数据范围校验")
    void claim_enforcesPropertyScope() {
        Resident target = resident("res_admin", null, PHONE);
        Resident source = resident("res_ghost", OPENID, PHONE);
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(target));
        when(residentRepository.findById("res_ghost")).thenReturn(Optional.of(source));

        service.claim("res_admin", claimRequest("res_ghost"));

        verify(roleGuard).assertSamePropertyOrPlatform("pc_demo001");
    }
}
