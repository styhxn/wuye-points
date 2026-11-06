package com.wuye.points.service;

import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.UboInfoRequest;
import com.wuye.points.entity.MerchantApplyment;
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
 * 确认小程序 AppID 可由平台默认值补齐，并锁定「草稿只校验已填内容 / 提交才校验完整性」的分段口径。
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
                mock(MerchantTransferAccountService.class),
                mock(PartnerApplymentApiService.class),
                props,
                pay,
                mock(WechatCertManager.class),
                mock(SnowflakeIdGenerator.class),
                mock(RoleGuard.class),
                mock(QrCodeService.class));
    }

    @Test
    @DisplayName("完整企业资料（无 UBO、无自定义 AppID）可通过校验")
    void enterpriseRequestPasses() {
        assertDoesNotThrow(() -> service.validateDraft(validEnterprise()));
    }

    @Test
    @DisplayName("对公开户名必须与营业执照商户名称一致")
    void corporateAccountNameMustMatchMerchantName() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setBankAccountName("别的公司");
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("对公账户开户名"));
    }

    @Test
    @DisplayName("个人银行卡开户名必须与法人/经营者姓名一致")
    void personalAccountNameMustMatchLegalPerson() {
        ApplymentSubmitRequest r = validIndividual();
        r.setBankAccountType("BANK_ACCOUNT_TYPE_PERSONAL");
        r.setBankAccountName("王五");
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("个人银行卡开户名"));
    }

    @Test
    @DisplayName("APP/网站/企业微信场景无资料结构，直接拒绝")
    void unsupportedSceneRejected() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setSalesScenesType("SALES_SCENES_APP");
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("经营场景"));
    }

    @Test
    @DisplayName("小程序场景留空 AppID 时自动使用平台小程序 AppID")
    void miniProgramUsesPlatformAppIdWhenBlank() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setSalesAppId(null);
        r.setSalesSubAppId(null);
        assertDoesNotThrow(() -> service.validateDraft(r));
        assertEquals("wx_platform_appid", service.resolveSalesAppId(r));
    }

    @Test
    @DisplayName("企业仅有法人一名受益人时可留空 UBO（微信自动回填）")
    void enterpriseMayOmitUboWhenOnlyLegalPerson() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setUboList(null);
        assertDoesNotThrow(() -> service.validateDraft(r));
    }

    @Test
    @DisplayName("填写 UBO 时必须完整（证件照/姓名/号码/地址/有效期）")
    void uboMustBeCompleteWhenProvided() {
        ApplymentSubmitRequest r = validEnterprise();
        UboInfoRequest ubo = new UboInfoRequest();
        ubo.setName("王五");
        r.setUboList(List.of(ubo));
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("最终受益人"));
    }

    @Test
    @DisplayName("超级管理员为法人时姓名必须与法人姓名一致")
    void contactNameMustMatchLegalPerson() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setContactName("另一个人");
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("超级管理员"));
    }

    @Test
    @DisplayName("结算规则由平台控制：企业 716、个体户 719，商家无法通过请求覆盖")
    void settlementIdIsServerControlled() {
        assertEquals("716", service.effectiveSettlementId("ENTERPRISE"));
        assertEquals("719", service.effectiveSettlementId("INDIVIDUAL"));
    }

    @Test
    @DisplayName("草稿保存：只填主体类型与商户简称即可（前端「保存资料」分步填写）")
    void draftAcceptsPartialForm() {
        ApplymentSubmitRequest r = new ApplymentSubmitRequest();
        r.setOrganizationType("ENTERPRISE");
        r.setMerchantShortname("XX物业");
        assertDoesNotThrow(() -> service.validateDraft(r));
    }

    @Test
    @DisplayName("草稿保存：缺邮箱/银行账号/执照图等提交必填项不再拦截")
    void draftIgnoresMissingSubmitFields() {
        ApplymentSubmitRequest r = validEnterprise();
        r.setContactEmail(null);
        r.setBankAccountNumber(null);
        r.setBusinessLicenseMedia(null);
        r.setIdCardAddress(null);
        assertDoesNotThrow(() -> service.validateDraft(r));
    }

    @Test
    @DisplayName("草稿保存：已填字段的取值非法仍然拒绝")
    void draftStillRejectsIllegalEnums() {
        ApplymentSubmitRequest r = new ApplymentSubmitRequest();
        r.setOrganizationType("MICRO");
        BizException e = assertThrows(BizException.class, () -> service.validateDraft(r));
        assertTrue(e.getMessage().contains("主体类型"));
    }

    @Test
    @DisplayName("草稿保存：UBO 一条都没填（前端会过滤空行）不拦截")
    void draftIgnoresEmptyUboRows() {
        ApplymentSubmitRequest r = validEnterprise();
        UboInfoRequest emptyRow = new UboInfoRequest();
        emptyRow.setName("");
        r.setUboList(List.of(emptyRow));
        assertDoesNotThrow(() -> service.validateDraft(r));
    }

    @Test
    @DisplayName("提交校验：资料齐备（含平台兜底 AppID）可通过")
    void submitAcceptsCompleteApplyment() {
        assertDoesNotThrow(() -> service.validateForSubmit(completeApplyment()));
    }

    @Test
    @DisplayName("提交校验：缺超级管理员邮箱时一次性列出缺项")
    void submitRejectsWhenContactEmailMissing() {
        MerchantApplyment a = completeApplyment();
        a.setContactEmailEnc(null);
        BizException e = assertThrows(BizException.class, () -> service.validateForSubmit(a));
        assertTrue(e.getMessage().contains("资料不完整"));
        assertTrue(e.getMessage().contains("联系邮箱"));
    }

    @Test
    @DisplayName("提交校验：企业主体缺身份证居住地址、小程序缺 AppID 一并列出")
    void submitListsEveryMissingField() {
        MerchantApplyment a = completeApplyment();
        a.setIdCardAddressEnc(null);
        a.setSalesAppId(null);
        BizException e = assertThrows(BizException.class, () -> service.validateForSubmit(a));
        assertTrue(e.getMessage().contains("身份证居住地址"));
        assertTrue(e.getMessage().contains("小程序/公众号 AppID"));
    }

    private MerchantApplyment completeApplyment() {
        MerchantApplyment a = new MerchantApplyment();
        a.setOrganizationType("ENTERPRISE");
        a.setMerchantShortname("XX物业");
        a.setServicePhone("0427-8888888");
        a.setBusinessLicenseMedia("LIC");
        a.setBusinessLicenseNumber("91211100MA0TEST");
        a.setMerchantName("盘锦XX物业管理有限公司");
        a.setLegalPerson("张三");
        a.setIdCardCopyMedia("ID_FRONT");
        a.setIdCardNationalMedia("ID_BACK");
        a.setIdCardNameEnc("ENC_NAME");
        a.setIdCardNumberEnc("ENC_NUMBER");
        a.setIdCardAddressEnc("ENC_ADDRESS");
        a.setIdCardPeriodBegin("2015-12-28");
        a.setIdCardPeriodEnd("2035-12-28");
        a.setSalesScenesType("SALES_SCENES_MINI_PROGRAM");
        a.setSalesAppId("wx_platform_appid");
        a.setBankAccountType("BANK_ACCOUNT_TYPE_CORPORATE");
        a.setBankAccountNameEnc("ENC_ACCOUNT_NAME");
        a.setAccountBank("招商银行");
        a.setBankAccountNumberEnc("ENC_ACCOUNT_NUMBER");
        a.setContactType("LEGAL");
        a.setContactName("张三");
        a.setContactMobile("13800138000");
        a.setContactEmailEnc("ENC_EMAIL");
        a.setQualificationType("居民生活服务");
        return a;
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
