package com.wuye.points.payment;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付 APIv3 通用 HTTP 客户端：自动携带 {@code Authorization: WECHATPAY2-SHA256-RSA2048} 请求签名头。
 *
 * <p>POST 签名串：{@code METHOD\nURL\nTIMESTAMP\nNONCE\nBODY\n}；GET 的 BODY 为空串。
 */
@Slf4j
public class WechatHttpClient {

    private final WechatPayProperties props;
    private volatile PrivateKey privateKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WechatHttpClient(WechatPayProperties props, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.privateKey = null; // 懒加载：仅在实际签名时解析，占位未配不会致启动失败
    }

    private static PrivateKey resolvePrivateKey(WechatPayProperties props) {
        if (props.getPrivateKeyBase64() != null && !props.getPrivateKeyBase64().isBlank()) {
            return WechatCrypto.loadPrivateKey(props.getPrivateKeyBase64());
        }
        if (props.getPrivateKeyPath() != null && !props.getPrivateKeyPath().isBlank()) {
            try {
                String pem = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(props.getPrivateKeyPath())), StandardCharsets.UTF_8);
                return WechatCrypto.loadPrivateKey(pem);
            } catch (Exception e) {
                throw new IllegalStateException("读取商户 API 私钥文件失败: " + props.getPrivateKeyPath(), e);
            }
        }
        return null;
    }

    public PrivateKey getPrivateKey() {
        PrivateKey k = privateKey;
        if (k == null) {
            synchronized (this) {
                k = privateKey;
                if (k == null) {
                    k = resolvePrivateKey(props);
                    privateKey = k;
                }
            }
        }
        return k;
    }

    private HttpHeaders authHeaders(String method, String url, String body, Map<String, String> extraHeaders) {
        PrivateKey key = getPrivateKey();
        if (key == null) {
            throw new WechatPayException("CONFIG_MISSING",
                    "微信支付商户私钥未配置（private-key-base64 / private-key-path），无法签名", null);
        }
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        // 微信 APIv3 签名规范：签名串中的 URL 仅取 path+query（不含 https://域名），否则 401 签名错误
        String message = method + "\n" + toSignUrl(url) + "\n" + timestamp + "\n" + nonce + "\n" + (body == null ? "" : body) + "\n";
        String signature = WechatCrypto.sign(key, message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization",
                "WECHATPAY2-SHA256-RSA2048 mchid=\"" + props.getMchId()
                        + "\",nonce_str=\"" + nonce + "\",signature=\"" + signature
                        + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + props.getMerchantSerialNumber() + "\"");
        headers.set("User-Agent", "wuye-points/1.0");
        if (extraHeaders != null) {
            for (Map.Entry<String, String> e : extraHeaders.entrySet()) {
                headers.set(e.getKey(), e.getValue());
            }
        }
        return headers;
    }

    public String post(String url, Object body) {
        return post(url, body, null);
    }

    public String post(String url, Object body, Map<String, String> extraHeaders) {
        String json = body == null ? "{}" : (body instanceof String ? (String) body : JSONUtil.toJsonStr(body));
        HttpEntity<String> entity = new HttpEntity<>(json, authHeaders("POST", url, json, extraHeaders));
        return exchange(url, HttpMethod.POST, entity);
    }

    public String get(String url) {
        return get(url, null);
    }

    public String get(String url, Map<String, String> extraHeaders) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders("GET", url, "", extraHeaders));
        return exchange(url, HttpMethod.GET, entity);
    }

    /**
     * 微信媒体上传（POST /v3/merchant/media/upload 等）。
     *
     * <p>微信 APIv3 约定：media 上传的 HTTP body 为 {@code multipart/form-data}，
     * 但签名串中的 BODY 为该文件原始内容（二进制按 ISO-8859-1 编码为字符串），
     * 其余（时间戳/随机串/URL path）与 JSON 请求一致。</p>
     *
     * @param url       完整请求地址
     * @param fileName  上传文件名（微信侧保存用）
     * @param fileBytes 文件原始字节
     * @return 微信接口响应体（含 media_id）
     */
    public String postMultipart(String url, String fileName, byte[] fileBytes) {
        String boundary = "----WuyePointsBoundary" + UUID.randomUUID().toString().replace("-", "");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            writeUtf8(bos, "--" + boundary + "\r\n");
            writeUtf8(bos, "Content-Disposition: form-data; name=\"file\"; filename=\"" + sanitize(fileName) + "\"\r\n");
            writeUtf8(bos, "Content-Type: application/octet-stream\r\n\r\n");
            bos.write(fileBytes);
            writeUtf8(bos, "\r\n--" + boundary + "--\r\n");
        } catch (Exception e) {
            throw new IllegalStateException("构造媒体上传报文失败", e);
        }
        byte[] body = bos.toByteArray();

        PrivateKey key = getPrivateKey();
        if (key == null) {
            throw new WechatPayException("CONFIG_MISSING",
                    "微信支付商户私钥未配置（private-key-base64 / private-key-path），无法签名", null);
        }
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        // 签名串 BODY = 文件原始内容（非 multipart 报文）
        String fileContent = new String(fileBytes, StandardCharsets.ISO_8859_1);
        String message = "POST\n" + toSignUrl(url) + "\n" + timestamp + "\n" + nonce + "\n" + fileContent + "\n";
        String signature = WechatCrypto.sign(key, message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("multipart/form-data; boundary=" + boundary));
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization",
                "WECHATPAY2-SHA256-RSA2048 mchid=\"" + props.getMchId()
                        + "\",nonce_str=\"" + nonce + "\",signature=\"" + signature
                        + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + props.getMerchantSerialNumber() + "\"");
        headers.set("User-Agent", "wuye-points/1.0");
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
        return exchange(url, HttpMethod.POST, entity);
    }

    private static void writeUtf8(ByteArrayOutputStream bos, String s) throws Exception {
        bos.write(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file.bin";
        }
        return fileName.replaceAll("[\\r\\n\"\\\\]", "_");
    }

    private String exchange(String url, HttpMethod method, HttpEntity<?> entity) {
        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, method, entity, String.class);
            String result = resp.getBody();
            if (result != null && (result.contains("\"code\":\"FAIL\"") || result.contains("\"code\":\"SYSTEM_ERROR\""))) {
                throw new WechatPayException(extract(result, "code"), extract(result, "message"), result);
            }
            return result;
        } catch (HttpClientErrorException e) {
            String r = e.getResponseBodyAsString();
            throw new WechatPayException(extract(r, "code"), extract(r, "message"), r);
        }
    }

    /**
     * 从完整 URL 提取「path+query」用于 APIv3 签名（不含 https://域名）。
     * 例：{@code https://api.mch.weixin.qq.com/v3/profitsharing/orders?foo=bar → /v3/profitsharing/orders?foo=bar}。
     */
    private static String toSignUrl(String url) {
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url; // 已是 path+query
        }
        int pathStart = url.indexOf('/', schemeEnd + 3);
        return pathStart < 0 ? "/" : url.substring(pathStart);
    }

    private String extract(String body, String key) {
        try {
            return JSONUtil.parseObj(body).getStr(key);
        } catch (Exception ignore) {
            return null;
        }
    }
}
