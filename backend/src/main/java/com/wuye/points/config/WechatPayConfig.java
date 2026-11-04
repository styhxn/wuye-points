package com.wuye.points.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatDirectProperties;
import com.wuye.points.payment.WechatHttpClient;
import com.wuye.points.payment.WechatNotifyService;
import com.wuye.points.payment.WechatPayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * 微信支付双通道 bean 装配。
 *
 * <p>平台接入两个独立微信商户主体，各自持有独立的 HTTP 客户端 / 验签公钥管理 / 回调验签服务：</p>
 * <ul>
 *   <li><b>服务商通道</b>（{@code wechat.pay}，sp_mchid）：商城订单收款 + 官方分账。
 *       作为 {@code @Primary}，供分账、收付通、进件、商家转账等服务默认注入；</li>
 *   <li><b>直连通道</b>（{@code wechat.direct}）：平台自营业务收款（物业费/充值/食堂/广告等），
 *       通过 {@code directXxx} bean 名显式注入。</li>
 * </ul>
 *
 * <p>两套凭据不可混用：请求签名、回调验签、APIv3 解密全部按通道隔离。私钥与公钥均懒加载，
 * 未配置真实商户信息时不会导致容器启动失败。</p>
 */
@Configuration
@Slf4j
public class WechatPayConfig {

    // ------------------------------------------------------------------ 服务商通道（primary）

    @Bean
    @Primary
    public WechatHttpClient wechatHttpClient(WechatPayProperties props,
                                             RestTemplate restTemplate,
                                             ObjectMapper objectMapper) {
        return new WechatHttpClient(props, restTemplate, objectMapper);
    }

    @Bean
    @Primary
    public WechatCertManager wechatCertManager(WechatPayProperties props,
                                               @Qualifier("wechatHttpClient") WechatHttpClient httpClient) {
        return new WechatCertManager(props, httpClient);
    }

    @Bean
    @Primary
    public WechatNotifyService wechatNotifyService(WechatPayProperties props,
                                                   @Qualifier("wechatCertManager") WechatCertManager certManager,
                                                   ObjectMapper objectMapper) {
        return new WechatNotifyService(props, certManager, objectMapper);
    }

    // ------------------------------------------------------------------ 直连商户通道

    @Bean
    public WechatHttpClient directWechatHttpClient(WechatDirectProperties props,
                                                   RestTemplate restTemplate,
                                                   ObjectMapper objectMapper) {
        return new WechatHttpClient(props, restTemplate, objectMapper);
    }

    @Bean
    public WechatCertManager directWechatCertManager(WechatDirectProperties props,
                                                     @Qualifier("directWechatHttpClient") WechatHttpClient directWechatHttpClient) {
        return new WechatCertManager(props, directWechatHttpClient);
    }

    @Bean
    public WechatNotifyService directWechatNotifyService(WechatDirectProperties props,
                                                         @Qualifier("directWechatCertManager") WechatCertManager directWechatCertManager,
                                                         ObjectMapper objectMapper) {
        return new WechatNotifyService(props, directWechatCertManager, objectMapper);
    }

    /**
     * 启动时打印双通道就绪情况（只含商户号/模式，不含任何密钥），避免部署后才发现某个通道没配。
     * 未就绪的通道不会阻塞启动：对应业务在发起支付时会 fail-closed 并给出明确错误。
     */
    @Bean
    public ApplicationRunner wechatChannelStartupReport(WechatPayProperties partnerProps,
                                                        WechatDirectProperties directProps) {
        return args -> {
            log.info("[微信支付] 服务商通道 configured={} mchId={} verifyMode={}",
                    partnerProps.isConfigured(), partnerProps.getMchId(),
                    partnerProps.isPublicKeyMode() ? "PUBLIC_KEY" : "PLATFORM_CERT");
            log.info("[微信支付] 直连商户通道 configured={} mchId={} verifyMode={}",
                    directProps.isConfigured(), directProps.getMchId(),
                    directProps.isPublicKeyMode() ? "PUBLIC_KEY" : "PLATFORM_CERT");
            if (!partnerProps.isConfigured()) {
                log.warn("[微信支付] 服务商通道未就绪：商城订单一律拒绝在线支付（含分账）；"
                        + "请检查 wechat.pay.* 与 WECHAT_PAY_* 环境变量");
            }
            if (!directProps.isConfigured()) {
                log.warn("[微信支付] 直连商户通道未就绪：物业费/充值/食堂/广告/服务预约等平台自营在线支付将被拒绝；"
                        + "请检查 wechat.direct.* 与 WECHAT_DIRECT_* 环境变量");
            }
        };
    }
}
