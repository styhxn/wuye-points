package com.wuye.points.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.payment.WechatHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 微信支付（平台微信支付分账）相关 bean 装配。
 *
 * <p>WechatHttpClient / WechatCertManager 是普通类（非 @Component），必须由本配置显式注册为 Spring Bean，
 * 否则 WechatPayService / WechatSplitApiService / PaymentCompletionService 构造注入时会因
 * 找不到 Bean 而启动失败（NoSuchBeanDefinitionException）。
 *
 * <p>私钥在 {@link WechatHttpClient} 内部懒加载，未配置真实商户信息时（占位）不会导致容器启动失败。
 */
@Configuration
public class WechatPayConfig {

    @Bean
    public WechatHttpClient wechatHttpClient(WechatPayProperties props,
                                              RestTemplate restTemplate,
                                              ObjectMapper objectMapper) {
        return new WechatHttpClient(props, restTemplate, objectMapper);
    }

    @Bean
    public WechatCertManager wechatCertManager(WechatPayProperties props,
                                               WechatHttpClient httpClient) {
        return new WechatCertManager(props, httpClient);
    }
}
