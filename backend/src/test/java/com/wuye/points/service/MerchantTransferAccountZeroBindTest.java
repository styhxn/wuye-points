package com.wuye.points.service;

import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.MerchantTransferAccount;
import com.wuye.points.entity.Resident;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantTransferAccountRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.RoleGuard;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 进件成功默认微信零钱绑定测试：openid 为空不阻断、已有绑定不覆盖、无四层树身份不写假绑定。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MerchantTransferAccountZeroBindTest {

    @Mock
    private MerchantTransferAccountRepository accountRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private EcommerceSplitProperties ecommerceProps;
    @Mock
    private WechatPayProperties wechatPayProperties;
    @Mock
    private WechatCertManager certManager;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private LeaderIdentityGuard leaderIdentityGuard;

    @InjectMocks
    private MerchantTransferAccountService service;

    private static final String RESIDENT_ID = "res_001";
    private static final String PROPERTY_COMPANY_ID = "pc_001";

    @Test
    @DisplayName("openid 为空：返回待绑定，不写库（进件主流程不受影响）")
    void openidMissing_returnsPendingWithoutWrite() {
        when(leaderIdentityGuard.activeCoordinator(RESIDENT_ID, PROPERTY_COMPANY_ID))
                .thenReturn(Optional.of(new Coordinator()));
        Resident resident = new Resident();
        resident.setId(RESIDENT_ID);
        when(residentRepository.findById(RESIDENT_ID)).thenReturn(Optional.of(resident));

        MerchantTransferAccountService.ZeroBindOutcome outcome =
                service.bindZeroForResident(RESIDENT_ID, PROPERTY_COMPANY_ID);

        assertEquals(MerchantTransferAccountService.ZeroBindOutcome.PENDING_OPENID, outcome);
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("已有 zero 绑定且 openid 非空：幂等返回，不重复写、不覆盖")
    void alreadyBound_doesNotOverwrite() {
        when(leaderIdentityGuard.activeCoordinator(RESIDENT_ID, PROPERTY_COMPANY_ID))
                .thenReturn(Optional.of(new Coordinator()));
        MerchantTransferAccount existing = new MerchantTransferAccount();
        existing.setOwnerType("COORDINATOR");
        existing.setOwnerId(RESIDENT_ID);
        existing.setChannel("zero");
        existing.setOpenid("o_existing_openid");
        existing.setVerified(true);
        when(accountRepository.findByOwnerTypeAndOwnerIdAndChannel("COORDINATOR", RESIDENT_ID, "zero"))
                .thenReturn(Optional.of(existing));

        MerchantTransferAccountService.ZeroBindOutcome outcome =
                service.bindZeroForResident(RESIDENT_ID, PROPERTY_COMPANY_ID);

        assertEquals(MerchantTransferAccountService.ZeroBindOutcome.ALREADY_BOUND, outcome);
        verify(accountRepository, never()).save(any());
        assertEquals("o_existing_openid", existing.getOpenid());
    }

    @Test
    @DisplayName("住户没有四层树身份：跳过，不写假绑定（商家商品收入走收付通分账）")
    void noLeaderIdentity_skips() {
        when(leaderIdentityGuard.activeCoordinator(anyString(), anyString())).thenReturn(Optional.empty());
        when(leaderIdentityGuard.activeSectorLeaders(anyString(), anyString())).thenReturn(List.of());
        when(leaderIdentityGuard.activeIndividualLeader(anyString(), anyString())).thenReturn(Optional.empty());

        MerchantTransferAccountService.ZeroBindOutcome outcome =
                service.bindZeroForResident(RESIDENT_ID, PROPERTY_COMPANY_ID);

        assertEquals(MerchantTransferAccountService.ZeroBindOutcome.SKIPPED_NO_IDENTITY, outcome);
        verify(accountRepository, never()).save(any());
        verify(residentRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("residentId 为空：直接跳过")
    void blankResident_skips() {
        assertEquals(MerchantTransferAccountService.ZeroBindOutcome.SKIPPED_NO_IDENTITY,
                service.bindZeroForResident(null, PROPERTY_COMPANY_ID));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("正常绑定：ownerId 为 residents.id，openid 取自 residents.openid，verified=true")
    void bindsWithResidentKeyAndOpenid() {
        when(leaderIdentityGuard.activeCoordinator(RESIDENT_ID, PROPERTY_COMPANY_ID))
                .thenReturn(Optional.of(new Coordinator()));
        Resident resident = new Resident();
        resident.setId(RESIDENT_ID);
        resident.setOpenid("o_resident_openid");
        when(residentRepository.findById(RESIDENT_ID)).thenReturn(Optional.of(resident));
        when(idGenerator.generateId("mta")).thenReturn("mta_001");

        MerchantTransferAccountService.ZeroBindOutcome outcome =
                service.bindZeroForResident(RESIDENT_ID, PROPERTY_COMPANY_ID);

        assertEquals(MerchantTransferAccountService.ZeroBindOutcome.BOUND, outcome);
        ArgumentCaptor<MerchantTransferAccount> captor = ArgumentCaptor.forClass(MerchantTransferAccount.class);
        verify(accountRepository).save(captor.capture());
        MerchantTransferAccount saved = captor.getValue();
        assertEquals("COORDINATOR", saved.getOwnerType());
        assertEquals(RESIDENT_ID, saved.getOwnerId());
        assertEquals("zero", saved.getChannel());
        assertEquals("o_resident_openid", saved.getOpenid());
        assertEquals(true, saved.getVerified());
        assertNull(saved.getBankName());
    }
}
