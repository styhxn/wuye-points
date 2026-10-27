package com.wuye.points.controller;

import com.wuye.points.payment.EcommerceSplitNotifyService;
import com.wuye.points.payment.PaymentCompletionService;
import com.wuye.points.service.MerchantTransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 微信回调鉴权回归：三个 notify 路径必须允许微信服务器无 Bearer 调用。
 *
 * <p>历史问题：AuthInterceptor 对 /** 全量拦截，微信回调被 401，支付结果永远进不来。
 * 本测试用真实拦截器 + MockMvc 验证白名单生效；控制器依赖的 Service 用 mock 替代。</p>
 */
@DisplayName("微信支付/分账/转账回调 · 免登录白名单")
class PaymentNotifyAuthTest extends ControllerTestBase {

    @MockBean
    private PaymentCompletionService paymentCompletionService;
    @MockBean
    private EcommerceSplitNotifyService ecommerceSplitNotifyService;
    @MockBean
    private MerchantTransferService merchantTransferService;

    @Test
    @DisplayName("POST /payment/notify/wechatpay 无令牌 → 200（不再 401）")
    void wechatPayNotify_noToken_reachesController() throws Exception {
        when(paymentCompletionService.handleWechatNotify(anyString(), any(HttpHeaders.class)))
                .thenReturn(Map.of("code", "SUCCESS", "message", "ok"));

        mockMvc.perform(post("/payment/notify/wechatpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /payment/ecommerce-split/notify 无令牌 → 200（不再 401）")
    void ecommerceSplitNotify_noToken_reachesController() throws Exception {
        when(ecommerceSplitNotifyService.handleSplitNotify(anyString(), any(HttpHeaders.class)))
                .thenReturn(Map.of("code", "SUCCESS", "message", "ok"));

        mockMvc.perform(post("/payment/ecommerce-split/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /payment/ecommerce-transfer/notify 无令牌 → 200（不再 401）")
    void ecommerceTransferNotify_noToken_reachesController() throws Exception {
        when(merchantTransferService.handleTransferNotify(anyString(), any(HttpHeaders.class)))
                .thenReturn(Map.of("code", "SUCCESS", "message", "ok"));

        mockMvc.perform(post("/payment/ecommerce-transfer/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
