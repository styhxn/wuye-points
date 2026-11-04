package com.wuye.points.payment;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付「应答/回调验签公钥」管理，按商户通道独立实例化。
 *
 * <p>支持微信两种验签模式，由配置自动选择：</p>
 * <ul>
 *   <li><b>微信支付公钥模式</b>（2024 年后新开商户默认）：配置 {@code public-key-id} + 公钥文件/文本，
 *       回调头 {@code Wechatpay-Serial} 即为公钥 ID（{@code PUB_KEY_ID_*}），直接用本地公钥验签；
 *       该模式不再请求 {@code /v3/certificates}（新商户未签发平台证书，调用会失败）。</li>
 *   <li><b>平台证书模式</b>（老商户）：从 {@code /v3/certificates} 拉取并按序列号缓存，
 *       按 {@link WechatMerchantProperties#getCertRefreshMillis()} 刷新。</li>
 * </ul>
 *
 * <p>缺失可用公钥时验签一律返回 null（fail-closed），绝不跳过验签。</p>
 */
@Slf4j
public class WechatCertManager {

    private final WechatMerchantProperties props;
    private final WechatHttpClient httpClient;

    private final Map<String, PublicKey> cache = new ConcurrentHashMap<>();
    private volatile PublicKey configuredPublicKey;
    private volatile boolean publicKeyLoadFailed;
    private long lastFetch = 0;

    public WechatCertManager(WechatMerchantProperties props, WechatHttpClient httpClient) {
        this.props = props;
        this.httpClient = httpClient;
    }

    /**
     * 取验签公钥：公钥模式按公钥 ID 匹配本地公钥；平台证书模式按序列号查缓存。
     * 未命中时强制刷新一次再查，规避证书轮换后首次回调验签失败。
     */
    public PublicKey getCertificate(String serialNo) {
        if (props.isPublicKeyMode()) {
            if (serialNo == null || !serialNo.equals(props.getPublicKeyId())) {
                log.warn("[微信验签] 回调序列号与配置的微信支付公钥 ID 不一致 mch={} configured={} actual={}",
                        props.getMchId(), props.getPublicKeyId(), serialNo);
                return null;
            }
            return loadConfiguredPublicKey();
        }
        refreshIfNeeded(false);
        PublicKey cert = cache.get(serialNo);
        if (cert == null) {
            refreshIfNeeded(true);
            cert = cache.get(serialNo);
        }
        return cert;
    }

    /** 公钥/平台证书自动刷新（每小时），避免轮换后回调验签全失败。公钥模式为本地文件，无需刷新。 */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledRefresh() {
        if (props.isPublicKeyMode()) {
            return;
        }
        refreshIfNeeded(true);
    }

    /**
     * 取验签/加密所用的序列号（用于敏感信息加密时设置 {@code Wechatpay-Serial} 头）：
     * 公钥模式返回公钥 ID；平台证书模式返回任一已缓存证书序列号。无可用凭据时返回 null。
     */
    public String anySerial() {
        if (props.isPublicKeyMode()) {
            return props.getPublicKeyId();
        }
        refreshIfNeeded(false);
        if (cache.isEmpty()) {
            refreshIfNeeded(true);
        }
        return cache.isEmpty() ? null : cache.keySet().iterator().next();
    }

    /** 当前通道是否使用微信支付公钥模式。 */
    public boolean isPublicKeyMode() {
        return props.isPublicKeyMode();
    }

    private PublicKey loadConfiguredPublicKey() {
        PublicKey key = configuredPublicKey;
        if (key != null) {
            return key;
        }
        synchronized (this) {
            if (configuredPublicKey != null) {
                return configuredPublicKey;
            }
            if (publicKeyLoadFailed) {
                return null;
            }
            try {
                configuredPublicKey = WechatCrypto.loadPublicKey(readConfiguredPublicKeyPem());
                log.info("[微信验签] 已加载微信支付公钥 mch={} publicKeyId={}", props.getMchId(), props.getPublicKeyId());
                return configuredPublicKey;
            } catch (Exception e) {
                publicKeyLoadFailed = true;
                log.error("[微信验签] 加载微信支付公钥失败，回调验签将全部拒绝 mch={} publicKeyId={}",
                        props.getMchId(), props.getPublicKeyId(), e);
                return null;
            }
        }
    }

    private String readConfiguredPublicKeyPem() {
        if (props.getPublicKeyBase64() != null && !props.getPublicKeyBase64().isBlank()) {
            return props.getPublicKeyBase64();
        }
        if (props.getPublicKeyPath() != null && !props.getPublicKeyPath().isBlank()) {
            try {
                return new String(Files.readAllBytes(Path.of(props.getPublicKeyPath())), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("读取微信支付公钥文件失败: " + props.getPublicKeyPath(), e);
            }
        }
        throw new IllegalStateException("微信支付公钥未配置（public-key-path / public-key-base64 二选一）");
    }

    private synchronized void refreshIfNeeded(boolean force) {
        if (props.isPublicKeyMode()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && now - lastFetch < props.getCertRefreshMillis() && !cache.isEmpty()) {
            return;
        }
        try {
            String body = httpClient.get(props.getGateway() + "/v3/certificates");
            JSONObject root = JSONUtil.parseObj(body);
            JSONArray list = root.getJSONArray("data");
            if (list != null) {
                for (Object o : list) {
                    JSONObject item = (JSONObject) o;
                    String serial = item.getStr("serial_no");
                    JSONObject enc = item.getJSONObject("encrypt_certificate");
                    String cert = WechatCrypto.decryptAesGcm(
                            props.getApiV3Key(), enc.getStr("associated_data"), enc.getStr("nonce"), enc.getStr("ciphertext"));
                    cache.put(serial, WechatCrypto.loadPublicKey(cert));
                }
            }
            lastFetch = now;
        } catch (Exception e) {
            log.warn("获取微信平台证书失败 mch={}: {}", props.getMchId(), e.getMessage());
        }
    }
}
