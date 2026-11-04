package com.wuye.points.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 与微信官方文档（普通服务商 {@code /v3/profitsharing/*}）逐字段对齐的回归测试。
 *
 * <p>覆盖三处易被"看起来能用"掩盖的约定：
 * 分账回退 {@code order_id} 与 {@code out_order_no} 二选一；
 * {@code relation_type=CUSTOM} 时必须带 {@code custom_relation}、其余取值不得带；
 * 剩余待分金额应答字段为 {@code unsplit_amount}（分）。</p>
 */
class WechatSplitApiDocConformanceTest {

    private WechatHttpClient http;
    private WechatSplitApiService api;

    @BeforeEach
    void setUp() {
        WechatPayProperties props = new WechatPayProperties();
        props.setGateway("https://api.mch.weixin.qq.com");
        props.setMchId("1752012787");
        props.setAppId("wx_test_appid");
        http = mock(WechatHttpClient.class);
        WechatCertManager certManager = mock(WechatCertManager.class);
        when(certManager.anySerial()).thenReturn(null);
        api = new WechatSplitApiService(http, props, certManager);
    }

    @Test
    @DisplayName("分账回退：有微信分账单号时只传 order_id，不传 out_order_no（文档要求二选一）")
    @SuppressWarnings("unchecked")
    void reverseRefund_sendsOnlyOrderId() {
        when(http.post(anyString(), any())).thenReturn("{\"return_id\":\"RET1\"}");

        api.reverseRefund("sp_1", "tx1", "1900000109", "WX_ORDER_1", "86693852", new BigDecimal("1.00"));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(http).post(anyString(), captor.capture());
        Map<String, Object> body = (Map<String, Object>) captor.getValue();
        assertEquals("WX_ORDER_1", body.get("order_id"));
        assertFalse(body.containsKey("out_order_no"));
    }

    @Test
    @DisplayName("分账回退：无微信分账单号时只传 out_order_no")
    @SuppressWarnings("unchecked")
    void reverseRefund_sendsOnlyOutOrderNo() {
        when(http.post(anyString(), any())).thenReturn("{\"return_id\":\"RET1\"}");

        api.reverseRefund("sp_1", "tx1", "1900000109", null, "86693852", new BigDecimal("1.00"));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(http).post(anyString(), captor.capture());
        Map<String, Object> body = (Map<String, Object>) captor.getValue();
        assertEquals("sp_1", body.get("out_order_no"));
        assertFalse(body.containsKey("order_id"));
    }

    @Test
    @DisplayName("添加接收方：relation_type=CUSTOM 时必带 custom_relation")
    @SuppressWarnings("unchecked")
    void addReceiver_customRelationSentOnlyForCustom() {
        when(http.post(anyString(), any(), any())).thenReturn("{}");

        api.addReceiver("1900000109", "86693852", null, "CUSTOM", "物业合作");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(http).post(anyString(), captor.capture(), any());
        Map<String, Object> body = (Map<String, Object>) captor.getValue();
        assertEquals("CUSTOM", body.get("relation_type"));
        assertEquals("物业合作", body.get("custom_relation"));
    }

    @Test
    @DisplayName("添加接收方：非 CUSTOM 关系不得携带 custom_relation")
    @SuppressWarnings("unchecked")
    void addReceiver_noCustomRelationForPartner() {
        when(http.post(anyString(), any(), any())).thenReturn("{}");

        api.addReceiver("1900000109", "86693852", null, "PARTNER", "物业合作");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(http).post(anyString(), captor.capture(), any());
        Map<String, Object> body = (Map<String, Object>) captor.getValue();
        assertEquals("PARTNER", body.get("relation_type"));
        assertFalse(body.containsKey("custom_relation"));
    }

    @Test
    @DisplayName("查询剩余待分金额：解析 unsplit_amount（单位分）")
    void queryUnsplitAmount_parsesFen() {
        when(http.get(anyString())).thenReturn("{\"transaction_id\":\"tx1\",\"unsplit_amount\":995}");

        assertEquals(995L, api.queryUnsplitAmount("tx1"));
    }

    @Test
    @DisplayName("查询剩余待分金额：接口异常返回 null（调用方退回按订单金额校验）")
    void queryUnsplitAmount_failsSoft() {
        when(http.get(anyString())).thenThrow(new RuntimeException("boom"));

        assertNull(api.queryUnsplitAmount("tx1"));
        assertNull(api.queryUnsplitAmount(""));
    }
}
