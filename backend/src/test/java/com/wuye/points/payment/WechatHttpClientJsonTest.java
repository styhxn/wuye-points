package com.wuye.points.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微信 JSON 请求回归测试：中文必须按 UTF-8 字节发送，且签名主体与发送字节一致。
 *
 * <p>Spring 的 StringHttpMessageConverter 默认 ISO-8859-1，若直接发送 String，
 * 中文会变成 '?'，导致微信 401 SIGN_ERROR。</p>
 */
class WechatHttpClientJsonTest {

    private RestTemplate restTemplate;
    private WechatHttpClient client;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        WechatPayProperties props = new WechatPayProperties();
        props.setMchId("1752012787");
        props.setMerchantSerialNumber("SERIAL123");
        props.setPrivateKeyBase64(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        restTemplate = mock(RestTemplate.class);
        client = new WechatHttpClient(props, restTemplate, new ObjectMapper());
    }

    @Test
    @DisplayName("JSON 中文按 UTF-8 发送，签名与发送字节完全一致")
    void postJsonUsesUtf8AndSignsSameBody() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("business_code", "bc_1");
        body.put("merchant_name", "盘锦XX物业管理有限公司");
        client.post("https://api.mch.weixin.qq.com/v3/applyment4sub/applyment/", body,
                Map.of("Wechatpay-Serial", "PUB_KEY_ID_TEST"));

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        HttpEntity<?> entity = captor.getValue();
        assertInstanceOf(byte[].class, entity.getBody(), "必须发送 UTF-8 字节，绕开 StringHttpMessageConverter");
        String sent = new String((byte[]) entity.getBody(), StandardCharsets.UTF_8);
        assertTrue(sent.contains("盘锦XX物业管理有限公司"), "中文必须原样出现在发送字节中");

        String auth = entity.getHeaders().getFirst("Authorization");
        assertNotNull(auth);
        String timestamp = extract(auth, "timestamp=\"(\\d+)\"");
        String nonce = extract(auth, "nonce_str=\"([^\"]+)\"");
        String signature = extract(auth, "signature=\"([^\"]+)\"");
        String message = "POST\n/v3/applyment4sub/applyment/\n" + timestamp + "\n" + nonce + "\n" + sent + "\n";
        assertTrue(WechatCrypto.verify(keyPair.getPublic(), message, signature),
                "签名主体必须与发送的 UTF-8 JSON 完全一致");
    }

    private static String extract(String auth, String regex) {
        Matcher m = Pattern.compile(regex).matcher(auth);
        assertTrue(m.find(), "Authorization 缺少字段: " + regex);
        return m.group(1);
    }
}
