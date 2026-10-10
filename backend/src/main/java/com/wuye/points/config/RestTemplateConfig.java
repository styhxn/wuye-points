package com.wuye.points.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 通用 RestTemplate（微信支付 HTTP 客户端 {@code WechatHttpClient} 使用）。
 *
 * <p>必须显式设置连接/读取超时：JDK 默认无超时，微信侧 TCP 半开或长时间无响应时会
 * 让调用线程与连接被无限占用（回调处理、调度任务、下单接口均会被拖死）。
 */
@Configuration
public class RestTemplateConfig {

    @Value("${wechat.pay.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${wechat.pay.read-timeout-ms:20000}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
