package com.wuye.points.util;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 店铺归属解析回归：归属只认 {@code merchant.resident_id}，手机号仅作存量兜底。
 *
 * <p>背景（2026-11-05）：管理端用「元序」账号的手机号 13900000099 建了一个食堂商家，
 * 这家新店的 resident_id 为空，而所有「我的店铺」入口都按手机号取最新一家，
 * 导致该账号登录后进了别人的商家工作台。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MerchantSupport 店铺归属解析")
class MerchantOwnershipResolutionTest {

    private static final String PHONE = "13900000099";
    private static final String PC = "pc_demo001";

    @Mock
    private MerchantRepository merchantRepository;

    private Resident account(String id, String phone) {
        Resident resident = new Resident();
        resident.setId(id);
        resident.setPhone(phone);
        resident.setPropertyCompanyId(PC);
        return resident;
    }

    private Merchant merchant(String id, String residentId, String contactPhone) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setResidentId(residentId);
        merchant.setContactPhone(contactPhone);
        merchant.setPropertyCompanyId(PC);
        return merchant;
    }

    @Test
    @DisplayName("resident_id 命中即用，同号但未绑定的新店不会顶替")
    void prefersResidentIdOverNewerSamePhoneShop() {
        Resident owner = account("res_owner", PHONE);
        Merchant owned = merchant("mch_owner", "res_owner", PHONE);
        Merchant borrowed = merchant("mch_borrowed", null, PHONE);

        when(merchantRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc("res_owner", PC))
                .thenReturn(Optional.of(owned));
        // 手机号口径下「最新那家」正是借用了手机号的店
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(PHONE, PC))
                .thenReturn(Optional.of(borrowed));

        Optional<Merchant> resolved = MerchantSupport.findOwnedMerchant(merchantRepository, owner);

        assertTrue(resolved.isPresent());
        assertEquals("mch_owner", resolved.get().getId());
    }

    @Test
    @DisplayName("手机号命中的店已绑定别的住户时不放行（不越权进入他人店铺）")
    void rejectsShopBoundToAnotherAccount() {
        Resident user = account("res_user", PHONE);
        Merchant othersShop = merchant("mch_other", "res_other", PHONE);

        when(merchantRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc("res_user", PC))
                .thenReturn(Optional.empty());
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(PHONE, PC))
                .thenReturn(Optional.of(othersShop));

        assertTrue(MerchantSupport.findOwnedMerchant(merchantRepository, user).isEmpty());
    }

    @Test
    @DisplayName("历史未绑定住户的店仍按手机号兜底，老账号不会因此失去店铺")
    void fallsBackToPhoneForLegacyShopWithoutResidentId() {
        Resident user = account("res_legacy", PHONE);
        Merchant legacy = merchant("mch_legacy", null, PHONE);

        when(merchantRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc("res_legacy", PC))
                .thenReturn(Optional.empty());
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(PHONE, PC))
                .thenReturn(Optional.of(legacy));

        Optional<Merchant> resolved = MerchantSupport.findOwnedMerchant(merchantRepository, user);

        assertTrue(resolved.isPresent());
        assertEquals("mch_legacy", resolved.get().getId());
    }

    @Test
    @DisplayName("没有手机号也没有 resident_id 绑定时不报错，返回空")
    void returnsEmptyWhenNothingMatches() {
        Resident user = account("res_none", PHONE);
        when(merchantRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertTrue(MerchantSupport.findOwnedMerchant(merchantRepository, user).isEmpty());
    }

    @Test
    @DisplayName("isOwnedBy：已绑定店只看 resident_id，未绑定的存量店才比手机号")
    void isOwnedByMatchesBoundShopByOwnerId() {
        Resident owner = account("res_owner", PHONE);
        Resident stranger = account("res_stranger", "13800000000");

        Merchant bound = merchant("mch_bound", "res_owner", "010-00000000");
        assertTrue(MerchantSupport.isOwnedBy(bound, owner));
        assertFalse(MerchantSupport.isOwnedBy(bound, stranger));

        Merchant legacy = merchant("mch_legacy", null, PHONE);
        assertTrue(MerchantSupport.isOwnedBy(legacy, owner));
        assertFalse(MerchantSupport.isOwnedBy(legacy, stranger));
    }
}
