package com.wuye.points.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 服务商特约商户进件：请求报文与官方《提交申请单》字段逐项对齐的回归测试。
 *
 * <p>断言的是"发出去什么"，不依赖微信真实环境：字段路径、必填值、经营场景分支、
 * 银行列表接口路径、申请状态映射。</p>
 */
class PartnerApplymentApiServiceTest {

    private WechatHttpClient http;
    private PartnerApplymentApiService api;

    @BeforeEach
    void setUp() {
        WechatPayProperties props = new WechatPayProperties();
        props.setGateway("https://api.mch.weixin.qq.com");
        props.setMchId("1752012787");
        props.setAppId("wx_test_appid");
        http = mock(WechatHttpClient.class);
        api = new PartnerApplymentApiService(http, props);
    }

    @Test
    @DisplayName("企业主体提交：营业执照/证件/UBO/结算规则/银行/超管按文档字段上送")
    @SuppressWarnings("unchecked")
    void submit_enterpriseBodyMatchesDoc() {
        when(http.post(anyString(), any(), any())).thenReturn("{\"applyment_id\":\"2000001\"}");

        String id = api.submitApplyment(PartnerApplymentApiService.SubmitPayload.builder()
                .businessCode("bc_1")
                .certSerialNo("PUB_KEY_ID_TEST")
                .subjectType("SUBJECT_TYPE_ENTERPRISE")
                .financeInstitution(false)
                .licenseCopyMedia("LIC_MEDIA")
                .licenseNumber("91211100MA0TEST")
                .merchantName("盘锦XX物业管理有限公司")
                .legalPerson("张三")
                .idCardCopyMedia("ID_FRONT")
                .idCardNationalMedia("ID_BACK")
                .idCardNameEnc("ENC_NAME")
                .idCardNumberEnc("ENC_ID")
                .idCardAddressEnc("ENC_ADDRESS")
                .idCardPeriodBegin("2015-12-28")
                .idCardPeriodEnd("2035-12-28")
                .uboInfoList(List.of(Map.of("ubo_id_doc_type", "IDENTIFICATION_TYPE_IDCARD",
                        "ubo_id_doc_name", "ENC_UBO_NAME")))
                .merchantShortname("XX物业")
                .servicePhone("0427-8888888")
                .salesScenesType("SALES_SCENES_MINI_PROGRAM")
                .salesSubAppId("wxf7230afd38275659")
                .settlementId("716")
                .qualificationType("居民生活服务")
                .bankAccountType("BANK_ACCOUNT_TYPE_CORPORATE")
                .accountNameEnc("ENC_ACCOUNT")
                .accountBank("招商银行")
                .bankBranchId("B001")
                .accountNumberEnc("ENC_NUMBER")
                .contactType("LEGAL")
                .contactNameEnc("ENC_CONTACT")
                .contactMobileEnc("ENC_MOBILE")
                .contactEmailEnc("ENC_EMAIL")
                .build());

        assertEquals("2000001", id);

        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Map<String, String>> headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(http).post(anyString(), bodyCaptor.capture(), headerCaptor.capture());
        assertEquals("PUB_KEY_ID_TEST", headerCaptor.getValue().get("Wechatpay-Serial"));

        Map<String, Object> body = (Map<String, Object>) bodyCaptor.getValue();
        assertEquals("bc_1", body.get("business_code"));
        assertFalse(body.containsKey("organization_type"), "服务商进件用 subject_info.subject_type");

        Map<String, Object> subject = (Map<String, Object>) body.get("subject_info");
        assertEquals("SUBJECT_TYPE_ENTERPRISE", subject.get("subject_type"));
        Map<String, Object> license = (Map<String, Object>) subject.get("business_license_info");
        assertEquals("LIC_MEDIA", license.get("license_copy"));
        assertEquals("91211100MA0TEST", license.get("license_number"));
        assertEquals("盘锦XX物业管理有限公司", license.get("merchant_name"));
        assertEquals("张三", license.get("legal_person"));

        Map<String, Object> identity = (Map<String, Object>) subject.get("identity_info");
        assertEquals("IDENTIFICATION_TYPE_IDCARD", identity.get("id_doc_type"));
        Map<String, Object> idCard = (Map<String, Object>) identity.get("id_card_info");
        assertEquals("ENC_NAME", idCard.get("id_card_name"));
        assertEquals("ENC_ID", idCard.get("id_card_number"));
        assertEquals("ENC_ADDRESS", idCard.get("id_card_address"),
                "官方要求 id_card_address 必须 RSA 加密后上送");

        assertEquals(1, ((List<?>) subject.get("ubo_info_list")).size());

        Map<String, Object> business = (Map<String, Object>) body.get("business_info");
        assertEquals("XX物业", business.get("merchant_shortname"));
        assertEquals("0427-8888888", business.get("service_phone"));
        Map<String, Object> sales = (Map<String, Object>) business.get("sales_info");
        assertEquals(List.of("SALES_SCENES_MINI_PROGRAM"), sales.get("sales_scenes_type"));
        Map<String, Object> mini = (Map<String, Object>) sales.get("mini_program_info");
        assertEquals("wxf7230afd38275659", mini.get("mini_program_sub_appid"));

        Map<String, Object> settlement = (Map<String, Object>) body.get("settlement_info");
        assertEquals("716", settlement.get("settlement_id"));
        assertEquals("居民生活服务", settlement.get("qualification_type"));

        Map<String, Object> bank = (Map<String, Object>) body.get("bank_account_info");
        assertEquals("BANK_ACCOUNT_TYPE_CORPORATE", bank.get("bank_account_type"));
        assertEquals("招商银行", bank.get("account_bank"));
        assertEquals("B001", bank.get("bank_branch_id"));
        assertEquals("ENC_ACCOUNT", bank.get("account_name"));
        assertEquals("ENC_NUMBER", bank.get("account_number"));

        Map<String, Object> contact = (Map<String, Object>) body.get("contact_info");
        assertEquals("LEGAL", contact.get("contact_type"));
        assertEquals("ENC_CONTACT", contact.get("contact_name"));
        assertEquals("ENC_MOBILE", contact.get("mobile_phone"));
        assertEquals("ENC_EMAIL", contact.get("contact_email"));
    }

    @Test
    @DisplayName("门店场景：sales_info.biz_store_info 携带门头照与店内照")
    @SuppressWarnings("unchecked")
    void submit_storeScene() {
        when(http.post(anyString(), any(), any())).thenReturn("{\"applyment_id\":\"1\"}");
        api.submitApplyment(PartnerApplymentApiService.SubmitPayload.builder()
                .businessCode("bc_2").certSerialNo("S")
                .subjectType("SUBJECT_TYPE_INDIVIDUAL")
                .salesScenesType("SALES_SCENES_STORE")
                .bizStoreName("XX便利店").bizAddressCode("211100").bizStoreAddress("XX路1号")
                .storeEntrancePics(List.of("P1")).storeIndoorPics(List.of("P2"))
                .build());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(http).post(anyString(), captor.capture(), any());
        Map<String, Object> body = (Map<String, Object>) captor.getValue();
        Map<String, Object> sales = (Map<String, Object>)
                ((Map<String, Object>) body.get("business_info")).get("sales_info");
        Map<String, Object> store = (Map<String, Object>) sales.get("biz_store_info");
        assertEquals("XX便利店", store.get("biz_store_name"));
        assertEquals(List.of("P1"), store.get("store_entrance_pic"));
        assertEquals(List.of("P2"), store.get("indoor_pic"));
    }

    @Test
    @DisplayName("银行列表：对公/对私走不同官方路径")
    void listBanks_paths() {
        when(http.get(anyString())).thenReturn("{\"data\":[]}");

        api.listBanks("BANK_ACCOUNT_TYPE_CORPORATE");
        api.listBanks("BANK_ACCOUNT_TYPE_PERSONAL");

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(http, org.mockito.Mockito.times(2)).get(url.capture());
        assertTrue(url.getAllValues().get(0).contains("/banks/corporate-banking"));
        assertTrue(url.getAllValues().get(1).contains("/banks/personal-banking"));
    }

    @Test
    @DisplayName("支行列表：city_code 必填并透传 offset/limit")
    void listBranches_includesCityAndPaging() {
        when(http.get(anyString())).thenReturn("{\"data\":[]}");

        api.listBranches("1000009561", "211100", 100, 50);

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(http).get(url.capture());
        assertTrue(url.getValue().contains("city_code=211100"));
        assertTrue(url.getValue().contains("offset=100"));
        assertTrue(url.getValue().contains("limit=50"));
    }
}
