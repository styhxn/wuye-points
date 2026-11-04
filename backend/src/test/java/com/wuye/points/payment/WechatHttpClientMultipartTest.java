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
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 官方《文件上传》回归测试：multipart 必须包含 meta(filename+sha256)，
 * 且签名主体是 meta 的 JSON 串（不是文件二进制）。
 */
class WechatHttpClientMultipartTest {

    private RestTemplate restTemplate;
    private WechatPayProperties props;
    private WechatHttpClient client;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();

        props = new WechatPayProperties();
        props.setGateway("https://api.mch.weixin.qq.com");
        props.setMchId("1752012787");
        props.setMerchantSerialNumber("SERIAL123");
        props.setPrivateKeyBase64(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        restTemplate = mock(RestTemplate.class);
        client = new WechatHttpClient(props, restTemplate, new ObjectMapper());
    }

    @Test
    @DisplayName("文件上传：发送 meta(filename+sha256)，并按 meta JSON 签名")
    void uploadSignsMetaJson() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"media_id\":\"M1\"}", HttpStatus.OK));

        byte[] bytes = "fake-image-bytes".getBytes(StandardCharsets.UTF_8);
        String mediaId = new PartnerApplymentApiService(client, props).uploadMedia(bytes, "门店门头.JPG");
        assertEquals("M1", mediaId);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        HttpEntity<?> entity = captor.getValue();
        String body = new String((byte[]) entity.getBody(), StandardCharsets.UTF_8);

        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        assertTrue(body.contains("Content-Disposition: form-data; name=\"meta\""));
        String metaMarker = "name=\"meta\"\r\nContent-Type: application/json\r\n\r\n";
        int metaStart = body.indexOf(metaMarker) + metaMarker.length();
        String metaJson = body.substring(metaStart, body.indexOf("\r\n--", metaStart));
        assertTrue(metaJson.matches("\\{\"filename\":\"applyment_[0-9a-f]{32}\\.jpg\",\"sha256\":\"[0-9a-f]{64}\"\\}"),
                "meta 必须是 filename + sha256 的 JSON: " + metaJson);
        assertTrue(metaJson.contains("\"sha256\":\"" + sha + "\""));
        String uploadedName = metaJson.substring(metaJson.indexOf(':') + 2, metaJson.indexOf('"', metaJson.indexOf(':') + 2));
        assertTrue(body.contains("Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadedName + "\""));

        String auth = entity.getHeaders().getFirst("Authorization");
        assertNotNull(auth);
        String timestamp = extract(auth, "timestamp=\"(\\d+)\"");
        String nonce = extract(auth, "nonce_str=\"([^\"]+)\"");
        String signature = extract(auth, "signature=\"([^\"]+)\"");
        String message = "POST\n/v3/merchant/media/upload\n" + timestamp + "\n" + nonce + "\n"
                + metaJson + "\n";
        assertTrue(WechatCrypto.verify(keyPair.getPublic(), message, signature),
                "签名主体必须是 meta JSON，而不是文件二进制");
    }

    @Test
    @DisplayName("文件上传：非图片后缀直接拒绝，避免微信 400")
    void rejectsNonImageFileName() {
        WechatPayException e = assertThrows(WechatPayException.class,
                () -> client.postMultipart("https://api.mch.weixin.qq.com/v3/merchant/media/upload",
                        "资料.pdf", new byte[]{1, 2, 3}));
        assertEquals("FILE_TYPE_INVALID", e.getCode());
    }

    @Test
    @DisplayName("文件上传：200 但响应里没有 media_id 时，异常要带微信原文")
    void throwsWithRawBodyWhenMediaIdMissing() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("<html>bad gateway</html>", HttpStatus.OK));

        WechatPayException e = assertThrows(WechatPayException.class,
                () -> new PartnerApplymentApiService(client, props).uploadMedia(new byte[]{1, 2, 3}, "门头.jpg"));
        assertEquals("MEDIA_ID_MISSING", e.getCode());
        assertTrue(e.getMessage().contains("bad gateway"), "异常必须带微信原文，否则只剩「未返回 media_id」无从排障");
    }

    private static String extract(String auth, String regex) {
        Matcher m = Pattern.compile(regex).matcher(auth);
        assertTrue(m.find(), "Authorization 缺少字段: " + regex);
        return m.group(1);
    }
}
