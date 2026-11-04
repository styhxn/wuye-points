package com.wuye.points.service;

import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.UboInfoRequest;
import com.wuye.points.exception.BizException;
import com.wuye.points.payment.PartnerApplymentApiService;
import com.wuye.points.payment.PartnerApplymentProperties;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * 进件表单校验回归：对齐官方《提交申请单》的账户名/场景/UBO 规则，
 * 并确认小程序 AppID 可由平台默认值补齐。
 */
class MerchantApplymentValidationTest {

    private MerchantApplymentService service;
    private WechatPayProperties pay;

    @BeforeEach
    void setUp() {
        PartnerApplymentProperties props = new PartnerApplymentProperties();
        pay = new WechatPayProperties();
        pay.setEnabled(true);
        pay.setMchId("1752012787");
        pay.setMerchantSerialNumber("SERIAL");
        pay.setPrivateKeyBase64("private-key");
        pay.setAppId("wx_platform_appid");

        service = new MerchantApplymentService(
                mock(MerchantApplymentRepository.class),
                mock(MerchantRepository.class),
                mock(MerchantWechatAccountRepository.class),
                mock(ResidentRepository.class),
                mock(PartnerApplymentApiService.class),
                props,
                pay,
                mock(WechatCertManager.class),
                mock(SnowflakeIdGenerator.class),
                mock(RoleGuard.class));
    }

    @Test
    @DisplayName("完整企业资料（无 UBO、无自定义 AppID）可通过校验")
    void enterpriseRequestPasses() {
        assertDoesNotThrow(() -> service.validateRequest(validEnterprise()));
    }

    @Test
    @DisplayName("对公开户名必须与营业执照商户名称一致")
    void corporateAccountNameMustMatchMerchantName() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setBankAccountName("别的公司");
        BizException e = assertThrows(BizException.class, () -> service.validateRequest(r));
        assertTrue(e.getMessage().contains("对公账户开户名"));
    }

    @Test
    @DisplayName("个人银行卡开户名必须与法人/经营者姓名一致")
    void personalAccountNameMustMatchLegalPerson() {
        ApplymentSubmitRequest r = validIndividual();
        r.setBankAccountType("BANK_ACCOUNT_TYPE_PERSONAL");
        r.setBankAccountName("王五");
        BizException e = assertThrows(BizException.class, () -> service.validateRequest(r));
        assertTrue(e.getMessage().contains("个人银行卡开户名"));
    }

    @Test
    @DisplayName("APP/网站/企业微信场景无资料结构，直接拒绝")
    void unsupportedSceneRejected() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setSalesScenesType("SALES_SCENES_APP");
        BizException e = assertThrows(BizException.class, () -> service.validateRequest(r));
        assertTrue(e.getMessage().contains("经营场景"));
    }

    @Test
    @DisplayName("小程序场景留空 AppID 时自动使用平台小程序 AppID")
    void miniProgramUsesPlatformAppIdWhenBlank() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setSalesAppId(null);
        r.setSalesSubAppId(null);
        assertDoesNotThrow(() -> service.validateRequest(r));
        assertEquals("wx_platform_appid", service.resolveSalesAppId(r));
    }

    @Test
    @DisplayName("企业仅有法人一名受益人时可留空 UBO（微信自动回填）")
    void enterpriseMayOmitUboWhenOnlyLegalPerson() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setUboList(null);
        assertDoesNotThrow(() -> service.validateRequest(r));
    }

    @Test
    @DisplayName("填写 UBO 时必须完整（证件照/姓名/号码/地址/有效期）")
    void uboMustBeCompleteWhenProvided() {
        ApplymentSubmitRequest r = validEnterprise();
        UboInfoRequest ubo = new UboInfoRequest();
        ubo.setName("王五");
        r.setUboList(List.of(ubo));
        BizException e = assertThrows(BizException.class, () -> service.validateRequest(r));
        assertTrue(e.getMessage().contains("最终受益人"));
    }

    @Test
    @DisplayName("超级管理员为法人时姓名必须与法人姓名一致")
    void contactNameMustMatchLegalPerson() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setContactName("另一个人");
        BizException e = assertThrows(BizException.class, () -> service.validateRequest(r));
        assertTrue(e.getMessage().contains("超级管理员"));
    }

    @Test
    @DisplayName("结算规则由平台控制：企业 716、个体户 719，商家无法通过请求覆盖")
    void settlementIdIsServerControlled() {
        assertEquals("716", service.effectiveSettlementId("ENTERPRISE"));
        assertEquals("719", service.effectiveSettlementId("INDIVIDUAL"));
    }

    private ApplymentSubmitRequest validEnterprise() {
        ApplymentSubmitRequest r = new ApplymentSubmitRequest();
        r.setOrganizationType("ENTERPRISE");
        r.setMerchantShortname("XX物业");
        r.setServicePhone("0427-8888888");
        r.setBusinessLicenseMedia("LIC");
        r.setBusinessLicenseNumber("91211100MA0TEST");
        r.setMerchantName("盘锦XX物业管理有限公司");
        r.setLegalPerson("张三");
        r.setLicenseAddress("辽宁省盘锦市XX路1号");
        r.setIdCardName("张三");
        r.setIdCardNumber("210101199001011234");
        r.setIdCardCopyMedia("ID_FRONT");
        r.setIdCardNationalMedia("ID_BACK");
        r.setIdCardPeriodBegin("2015-12-28");
        r.setIdCardPeriodEnd("2035-12-28");
        r.setIdCardAddress("辽宁省盘锦市XX小区");
        r.setUboList(null);
        r.setSalesScenesType("SALES_SCENES_MINI_PROGRAM");
        r.setSalesAppId(null);
        r.setSalesSubAppId(null);
        r.setBankAccountType("BANK_ACCOUNT_TYPE_CORPORATE");
        r.setBankAccountName("盘锦XX物业管理有限公司");
        r.setAccountBank("招商银行");
        r.setBankAccountNumber("6217000012345678901");
        r.setContactType("LEGAL");
        r.setContactName("张三");
        r.setContactMobile("13800138000");
        r.setContactEmail("wu@example.com");
        return r;
    }

    private ApplymentSubmitRequest validIndividual() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setOrganizationType("INDIVIDUAL");
        r.setMerchantName("盘锦XX便利店");
        r.setLegalPerson("李四");
        r.setIdCardName("李四");
        r.setContactName("李四");
        r.setIdCardAddress(null);
        r.setBankAccountType("BANK_ACCOUNT_TYPE_CORPORATE");
        r.setBankAccountName("盘锦XX便利店");
        return r;
    }
}
