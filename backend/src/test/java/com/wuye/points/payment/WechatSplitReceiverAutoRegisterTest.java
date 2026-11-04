package com.wuye.points.payment;

import com.wuye.points.dto.admin.settlement.AutoRegisterReceiverResult;
import com.wuye.points.dto.admin.settlement.AutoRegisterReceiversRequest;
import com.wuye.points.dto.admin.settlement.ReceiverPrincipalResponse;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverRequest;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WechatSplitReceiverAccount;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.WechatSplitReceiverAccountRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 分账个人接收方「按主体自动登记」单元测试。
 *
 * <p>回归目标：前端 2026-11-02 的登记页要求运营手填/自动带出 openid。openid 是打款地址，
 * 抄错一位会静默打给另一个人，因此改为前端只传 ownerId、后端反查 openid，且响应只回脱敏值。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WechatSplitReceiverAutoRegisterTest {

    private static final String OPENID_LI = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o";

    @Mock
    private WechatSplitReceiverAccountRepository repository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private SplitReceiverPrincipalResolver principalResolver;

    @InjectMocks
    private WechatSplitReceiverAdminService service;

    private Resident resident(String id, String name, String openid) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setName(name);
        resident.setPhone("13900000008");
        resident.setPropertyCompanyId("pc_demo001");
        resident.setOpenid(openid);
        return resident;
    }

    private AutoRegisterReceiversRequest request(String ownerType, List<String> ownerIds) {
        AutoRegisterReceiversRequest request = new AutoRegisterReceiversRequest();
        request.setOwnerType(ownerType);
        request.setOwnerIds(ownerIds);
        return request;
    }

    @Test
    @DisplayName("有 openid 的主体登记成功，写入的账户号就是后端反查到的 openid")
    void autoRegister_registersWithBackendResolvedOpenid() {
        when(principalResolver.supportsPersonal("COURIER")).thenReturn(true);
        when(principalResolver.loadResident("COURIER", "cm_1"))
                .thenReturn(resident("res_li", "李四", OPENID_LI));
        when(repository.findByOwnerTypeAndOwnerId("COURIER", "cm_1")).thenReturn(Optional.empty());
        when(idGenerator.generateId(anyString())).thenReturn("wsra_1");

        List<AutoRegisterReceiverResult> results = service.autoRegister(request("COURIER", List.of("cm_1")));

        assertEquals(1, results.size());
        assertEquals("registered", results.get(0).getStatus());
        assertEquals("wsra_1", results.get(0).getReceiverAccountId());
        ArgumentCaptor<WechatSplitReceiverAccount> captor =
                ArgumentCaptor.forClass(WechatSplitReceiverAccount.class);
        verify(repository).save(captor.capture());
        assertEquals(OPENID_LI, captor.getValue().getAccountNo());
        assertEquals("COURIER", captor.getValue().getOwnerType());
        assertTrue(captor.getValue().isVerified());
    }

    @Test
    @DisplayName("未认领微信身份的主体逐条 skipped，不阻断整批、不写库")
    void autoRegister_skipsPrincipalWithoutWechatIdentity() {
        when(principalResolver.supportsPersonal("COURIER")).thenReturn(true);
        when(principalResolver.loadResident("COURIER", "cm_1"))
                .thenReturn(resident("res_li", "李四", null));
        when(principalResolver.loadResident("COURIER", "cm_2"))
                .thenReturn(resident("res_wang", "王五", OPENID_LI));
        when(repository.findByOwnerTypeAndOwnerId("COURIER", "cm_2")).thenReturn(Optional.empty());
        when(idGenerator.generateId(anyString())).thenReturn("wsra_2");

        List<AutoRegisterReceiverResult> results =
                service.autoRegister(request("COURIER", List.of("cm_1", "cm_2")));

        assertEquals("skipped", results.get(0).getStatus());
        assertTrue(results.get(0).getReason().contains("未认领微信身份"), results.get(0).getReason());
        assertEquals("registered", results.get(1).getStatus());
    }

    @Test
    @DisplayName("物业公司不支持按 openid 自动登记（应登记商户号）")
    void autoRegister_rejectsPropertyOwnerType() {
        when(principalResolver.supportsPersonal("PROPERTY")).thenReturn(false);

        BizException e = assertThrows(BizException.class,
                () -> service.autoRegister(request("PROPERTY", List.of("pc_demo001"))));
        assertTrue(e.getMessage().contains("商户号"), e.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("响应只回 openid 脱敏值，明文不出接口")
    void autoRegister_neverReturnsRawOpenid() {
        when(principalResolver.supportsPersonal("COURIER")).thenReturn(true);
        when(principalResolver.loadResident("COURIER", "cm_1"))
                .thenReturn(resident("res_li", "李四", OPENID_LI));
        when(repository.findByOwnerTypeAndOwnerId("COURIER", "cm_1")).thenReturn(Optional.empty());
        when(idGenerator.generateId(anyString())).thenReturn("wsra_1");

        String json = service.autoRegister(request("COURIER", List.of("cm_1"))).toString();
        assertFalse(json.contains(OPENID_LI), json);
        assertTrue(json.contains("oUpF****eS6o"), json);
    }

    @Test
    @DisplayName("预检：已登记的主体回 registered=true 与账户ID，供前端显示状态")
    void listPrincipals_reportsRegisteredState() {
        when(principalResolver.supportsPersonal("COURIER")).thenReturn(true);
        when(principalResolver.loadResident("COURIER", "cm_1"))
                .thenReturn(resident("res_li", "李四", OPENID_LI));
        WechatSplitReceiverAccount existing = new WechatSplitReceiverAccount();
        existing.setId("wsra_exist");
        when(repository.findByOwnerTypeAndOwnerId("COURIER", "cm_1")).thenReturn(Optional.of(existing));

        List<ReceiverPrincipalResponse> items = service.listPrincipals("COURIER", List.of("cm_1"));

        assertEquals(1, items.size());
        assertTrue(items.get(0).getOpenidAvailable());
        assertTrue(items.get(0).getRegistered());
        assertEquals("wsra_exist", items.get(0).getReceiverAccountId());
        assertEquals("139****0008", items.get(0).getPhoneMasked());
    }

    @Test
    @DisplayName("预检：openid 格式异常时给出可操作的不可登记原因")
    void listPrincipals_flagsMalformedOpenid() {
        when(principalResolver.supportsPersonal("COURIER")).thenReturn(true);
        when(principalResolver.loadResident("COURIER", "cm_1"))
                .thenReturn(resident("res_li", "李四", "bad id"));

        ReceiverPrincipalResponse item = service.listPrincipals("COURIER", List.of("cm_1")).get(0);

        assertFalse(item.getOpenidAvailable());
        assertTrue(item.getUnavailableReason().contains("格式异常"), item.getUnavailableReason());
    }

    @Test
    @DisplayName("手填通道拒绝 openid：必须走 /receivers/auto，且不落库")
    void save_rejectsManuallyEnteredOpenid() {
        WechatSplitReceiverRequest request = new WechatSplitReceiverRequest();
        request.setOwnerType("COURIER");
        request.setOwnerId("cm_1");
        request.setAccountNo(OPENID_LI);

        BizException e = assertThrows(BizException.class, () -> service.save(request));

        assertTrue(e.getMessage().contains("/receivers/auto"), e.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("手填通道仍接受商户号，且明文回传（商户号不是登录凭证）")
    void save_acceptsMerchantIdInPlaintext() {
        WechatSplitReceiverRequest request = new WechatSplitReceiverRequest();
        request.setOwnerType("PROPERTY");
        request.setOwnerId("pc_demo001");
        request.setAccountNo("1900000109");
        request.setAccountName("连云港元序软件有限公司");
        when(repository.findByOwnerTypeAndOwnerId("PROPERTY", "pc_demo001")).thenReturn(Optional.empty());
        when(idGenerator.generateId(anyString())).thenReturn("wsra_m1");

        WechatSplitReceiverResponse saved = service.save(request);

        assertEquals("MERCHANT_ID", saved.getReceiverType());
        assertEquals("1900000109", saved.getAccountNo());
        assertEquals("1900000109", saved.getAccountNoMasked());
        assertTrue(saved.isReversable());
    }

    @Test
    @DisplayName("已登记列表：openid 只回脱敏值，明文不下发")
    void list_masksStoredOpenid() {
        WechatSplitReceiverAccount account = new WechatSplitReceiverAccount();
        account.setId("wsra_1");
        account.setOwnerType("COURIER");
        account.setOwnerId("cm_1");
        account.setAccountNo(OPENID_LI);
        account.setVerified(true);
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(account));

        WechatSplitReceiverResponse item = service.list(null).get(0);

        assertNull(item.getAccountNo());
        assertEquals("oUpF****eS6o", item.getAccountNoMasked());
        assertEquals("PERSONAL_OPENID", item.getReceiverType());
        assertFalse(item.isReversable());
        assertFalse(item.toString().contains(OPENID_LI), item.toString());
    }
}
