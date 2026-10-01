package com.wuye.points.payment;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付平台证书管理：自动获取并缓存平台证书（用于回调验签 / 下载证书时验签）。
 *
 * <p>平台证书会轮换，按 {@link WechatPayProperties#getCertRefreshMillis()} 刷新。接入真实商户后生效；
 * 占位阶段无真实证书时 {@link #getCertificate(String)} 返回 null（仅影响验签，不影响下单框架编译）。
 */
@Slf4j
@Component
public class WechatCertManager {

    private final WechatPayProperties props;
    private final WechatHttpClient httpClient;

    private final Map<String, PublicKey> cache = new ConcurrentHashMap<>();
    private long lastFetch = 0;

    public WechatCertManager(WechatPayProperties props, WechatHttpClient httpClient) {
        this.props = props;
        this.httpClient = httpClient;
    }

    /** 取平台证书公钥，按序列号缓存。未命中时强制刷新一次再查，规避证书轮换后首次回调验签失败。 */
    public PublicKey getCertificate(String serialNo) {
        refreshIfNeeded(false);
        PublicKey cert = cache.get(serialNo);
        if (cert == null) {
            refreshIfNeeded(true);
            cert = cache.get(serialNo);
        }
        return cert;
    }

    /** 平台证书自动刷新（每小时），避免证书轮换后回调验签全失败。 */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledRefresh() {
        refreshIfNeeded(true);
    }

    /** 取任一已缓存平台证书的序列号（用于敏感信息加密时设置 Wechatpay-Serial 头）。无证书时返回 null。 */
    public String anySerial() {
        refreshIfNeeded(false);
        if (cache.isEmpty()) {
            refreshIfNeeded(true);
        }
        return cache.isEmpty() ? null : cache.keySet().iterator().next();
    }

    private synchronized void refreshIfNeeded(boolean force) {
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
            log.warn("获取微信平台证书失败（占位阶段可忽略）: {}", e.getMessage());
        }
    }
}
