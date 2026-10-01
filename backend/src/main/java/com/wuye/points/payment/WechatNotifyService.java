package com.wuye.points.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

/**
 * 微信支付 APIv3 回调统一验签 + 解密（支付 / 分账 / 提现共用，单一事实来源）。
 *
 * <p>严格遵循官方规范：
 * <ol>
 *   <li>待签报文串 = {@code Wechatpay-Timestamp + "\n" + Wechatpay-Nonce + "\n" + body + "\n"}；</li>
 *   <li>用 {@code Wechatpay-Serial} 对应的平台证书（{@link WechatCertManager} 自动拉取并缓存）做 SHA256-RSA 验签；</li>
 *   <li>验签通过后用 APIv3 密钥对 resource（algorithm=AEAD_AES_256_GCM）解密，返回业务明文 Map。</li>
 * </ol>
 *
 * @throws WechatNotifyVerifyException 缺头 / 验签失败 / 解密异常时抛出
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatNotifyService {

    private final WechatPayProperties props;
    private final WechatCertManager certManager;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> decrypt(String rawBody, HttpHeaders headers) {
        String timestamp = headers.getFirst("Wechatpay-Timestamp");
        String nonce = headers.getFirst("Wechatpay-Nonce");
        String signature = headers.getFirst("Wechatpay-Signature");
        String serial = headers.getFirst("Wechatpay-Serial");
        if (timestamp == null || nonce == null || signature == null || serial == null) {
            throw new WechatNotifyVerifyException("微信回调缺少必要请求头");
        }
        String message = timestamp + "\n" + nonce + "\n" + rawBody + "\n";
        PublicKey cert = certManager.getCertificate(serial);
        if (cert == null || !WechatCrypto.verify(cert, message, signature)) {
            throw new WechatNotifyVerifyException("微信回调验签失败 serial=" + serial);
        }
        try {
            Map<String, Object> envelope = objectMapper.readValue(rawBody, Map.class);
            Object resObj = envelope.get("resource");
            if (!(resObj instanceof Map)) {
                throw new WechatNotifyVerifyException("微信回调 resource 缺失或格式错误");
            }
            Map<String, Object> res = (Map<String, Object>) resObj;
            String plain = WechatCrypto.decryptAesGcm(props.getApiV3Key(),
                    (String) res.get("associated_data"), (String) res.get("nonce"), (String) res.get("ciphertext"));
            return objectMapper.readValue(plain, Map.class);
        } catch (WechatNotifyVerifyException e) {
            throw e;
        } catch (Exception e) {
            throw new WechatNotifyVerifyException("微信回调解密失败", e);
        }
    }
}
