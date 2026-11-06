package com.wuye.points.service;

import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.dto.merchant.MerchantQrCodeResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("商家专属收款二维码")
class MerchantQrCodeServiceTest {

    private static final String STORE_URL = "https://test.example.com/api/v1/static/qrcode/merchant_mch_1.png";

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private MerchantService merchantService;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private RoleGuard roleGuard;

    private MerchantQrCodeService service;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        service = new MerchantQrCodeService(merchantRepository, merchantService, qrCodeService, roleGuard);
        org.springframework.test.util.ReflectionTestUtils.setField(
                service, "merchantPage", "pages/service/merchantProducts");

        merchant = new Merchant();
        merchant.setId("mch_1");
        merchant.setName("社区超市");
        merchant.setPropertyCompanyId("pc_demo001");
        merchant.setAuditStatus("approved");
        merchant.setStatus("active");
        when(merchantRepository.findById("mch_1")).thenReturn(Optional.of(merchant));
        when(merchantRepository.save(any(Merchant.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenMiniProgramCode() {
        when(qrCodeService.miniProgramCode(anyString(), anyString(), anyBoolean()))
                .thenReturn(new QrCodeService.Generated(STORE_URL, QrCodeService.TYPE_MINIPROGRAM,
                        "pages/service/merchantProducts?merchantId=mch_1"));
    }

    @Test
    @DisplayName("生成后写回 qr_code_url，前端展示位无需改造即可显示")
    void generate_persistsQrCodeUrl() {
        givenMiniProgramCode();

        MerchantQrCodeResponse response = service.generateForAdmin("mch_1", false);

        assertEquals(STORE_URL, response.getQrCodeUrl());
        assertEquals(STORE_URL, merchant.getQrCodeUrl());
        assertEquals(QrCodeService.TYPE_MINIPROGRAM, response.getCodeType());
        assertTrue(response.getRegenerated());
        verify(merchantRepository).save(merchant);
    }

    @Test
    @DisplayName("码路径按配置的店铺页拼 merchantId")
    void generate_usesConfiguredPagePath() {
        givenMiniProgramCode();

        service.generateForAdmin("mch_1", true);

        verify(qrCodeService).miniProgramCode(
                "pages/service/merchantProducts?merchantId=mch_1", "merchant_mch_1.png", false);
    }

    @Test
    @DisplayName("已有码且未强制时直接复用，不打微信接口")
    void generate_reusesExistingCode() {
        merchant.setQrCodeUrl(STORE_URL);
        when(qrCodeService.existingUrl("merchant_mch_1.png")).thenReturn(STORE_URL);

        MerchantQrCodeResponse response = service.generateForAdmin("mch_1", false);

        assertEquals(STORE_URL, response.getQrCodeUrl());
        assertFalse(response.getRegenerated());
        verify(qrCodeService, never()).miniProgramCode(anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("审核未通过的商家不能生成收款码")
    void generate_requiresApprovedAudit() {
        merchant.setAuditStatus("pending");

        BizException ex = assertThrows(BizException.class, () -> service.generateForAdmin("mch_1", false));

        assertEquals(ErrorCode.MERCHANT_AUDITING.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("已停用的商家不能生成收款码")
    void generate_rejectsInactiveMerchant() {
        merchant.setStatus("inactive");

        BizException ex = assertThrows(BizException.class, () -> service.generateForAdmin("mch_1", false));

        assertEquals(ErrorCode.MERCHANT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("跨物业公司生成直接被拒")
    void generate_blocksOtherPropertyCompany() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(roleGuard).assertSamePropertyOrPlatform("pc_other");
        Merchant other = new Merchant();
        other.setId("mch_9");
        other.setPropertyCompanyId("pc_other");
        other.setAuditStatus("approved");
        other.setStatus("active");
        when(merchantRepository.findById("mch_9")).thenReturn(Optional.of(other));

        assertThrows(BizException.class, () -> service.generateForAdmin("mch_9", false));
    }

    @Test
    @DisplayName("商家自助入口只取本人名下店铺")
    void generateForOwner_usesMyMerchant() {
        givenMiniProgramCode();
        when(merchantService.getMyMerchant()).thenReturn(
                MerchantDetailResponse.builder().id("mch_1").name("社区超市").build());

        MerchantQrCodeResponse response = service.generateForOwner(false);

        assertEquals("mch_1", response.getMerchantId());
        verify(merchantService).getMyMerchant();
    }

    @Test
    @DisplayName("从未生成过时查询返回空，不触发微信调用")
    void get_neverGeneratedReturnsBlank() {
        when(qrCodeService.existingUrl(anyString())).thenReturn(null);

        MerchantQrCodeResponse response = service.getForAdmin("mch_1");

        assertNull(response.getQrCodeUrl());
        assertNull(response.getCodeType());
        verify(qrCodeService, never()).miniProgramCode(anyString(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("商家ID前缀非法直接参数错误")
    void generate_rejectsBadIdFormat() {
        BizException ex = assertThrows(BizException.class, () -> service.generateForAdmin("res_1", false));

        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("补生成：单个店铺微信失败只记日志，不影响其余店铺")
    void backfill_continuesOnSingleFailure() {
        Merchant second = new Merchant();
        second.setId("mch_2");
        second.setPropertyCompanyId("pc_demo001");
        second.setAuditStatus("approved");
        second.setStatus("active");
        when(merchantRepository.findByAuditStatusAndStatusAndQrCodeUrlIsNull("approved", "active"))
                .thenReturn(java.util.List.of(merchant, second));
        when(merchantRepository.findById("mch_2")).thenReturn(Optional.of(second));
        when(qrCodeService.miniProgramCode(anyString(), anyString(), anyBoolean()))
                .thenThrow(new BizException(ErrorCode.WECHAT_API_ERROR, "小程序码生成失败"))
                .thenReturn(new QrCodeService.Generated(
                        "https://test.example.com/api/v1/static/qrcode/merchant_mch_2.png",
                        QrCodeService.TYPE_MINIPROGRAM, "pages/service/merchantProducts?merchantId=mch_2"));

        int generated = service.backfillMissing();

        assertEquals(1, generated);
        assertEquals("https://test.example.com/api/v1/static/qrcode/merchant_mch_2.png", second.getQrCodeUrl());
    }

    @Test
    @DisplayName("补生成只挑审核通过且在营的店铺")
    void backfill_queriesApprovedActiveOnly() {
        when(merchantRepository.findByAuditStatusAndStatusAndQrCodeUrlIsNull("approved", "active"))
                .thenReturn(java.util.List.of());

        assertEquals(0, service.backfillMissing());

        verify(merchantRepository).findByAuditStatusAndStatusAndQrCodeUrlIsNull("approved", "active");
    }
}
