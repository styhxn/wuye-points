package com.wuye.points.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 双通道微信支付回归测试（纯函数/本地逻辑，不发起外部请求）：
 * <ul>
 *   <li>历史流水通道路由推断（pay_mode 缺失时按 sub_mchid 回退）；</li>
 *   <li>微信支付公钥模式验签：公钥 ID 匹配才返回公钥，不匹配一律拒绝（fail-closed）；</li>
 *   <li>分账接收方类型推导（商户号可回退 / openid 不可回退）。</li>
 * </ul>
 */
class WechatDualChannelTest {

    @Test
    @DisplayName("通道路由：落库 pay_mode 优先")
    void resolvePrefersStoredMode() {
        assertEquals(WechatPayModes.DIRECT, WechatPayModes.resolve("direct", "1900000109", true));
        assertEquals(WechatPayModes.PARTNER, WechatPayModes.resolve("PARTNER", null, true));
        assertEquals(WechatPayModes.ECOMMERCE, WechatPayModes.resolve("ecommerce", "1900000109", false));
    }

    @Test
    @DisplayName("通道路由：历史流水无 pay_mode 时按 sub_mchid + 收付通开关推断")
    void resolveFallsBackForLegacyRecords() {
        assertEquals(WechatPayModes.DIRECT, WechatPayModes.resolve(null, null, true));
        assertEquals(WechatPayModes.DIRECT, WechatPayModes.resolve("", " ", true));
        assertEquals(WechatPayModes.PARTNER, WechatPayModes.resolve(null, "1900000109", false));
        assertEquals(WechatPayModes.ECOMMERCE, WechatPayModes.resolve(null, "1900000109", true));
    }

    @Test
    @DisplayName("公钥模式：序列号等于配置的公钥 ID 时返回本地公钥，不等时返回 null")
    void certManagerPublicKeyModeIsFailClosed() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        String publicKeyBase64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String publicKeyId = "PUB_KEY_ID_01TEST";

        StubMerchantProps props = new StubMerchantProps(publicKeyId, publicKeyBase64);
        WechatCertManager manager = new WechatCertManager(props, null);

        PublicKey key = manager.getCertificate(publicKeyId);
        assertNotNull(key);
        assertEquals("RSA", key.getAlgorithm());
        assertEquals(publicKeyId, manager.anySerial());

        assertNull(manager.getCertificate("OTHER_SERIAL"));
        assertNull(manager.getCertificate(null));
    }

    @Test
    @DisplayName("公钥模式：公钥内容非法时返回 null（不抛异常穿透到回调入口）")
    void certManagerInvalidPublicKeyReturnsNull() {
        StubMerchantProps props = new StubMerchantProps("PUB_KEY_ID_01BAD", "not-a-valid-key");
        WechatCertManager manager = new WechatCertManager(props, null);
        assertNull(manager.getCertificate("PUB_KEY_ID_01BAD"));
    }

    @Test
    @DisplayName("接收方类型：8~20 位纯数字为商户号，其余为个人 openid")
    void receiverTypeInference() {
        assertEquals("MERCHANT_ID", WechatSplitReceiverAdminService.receiverType("1900000109"));
        assertEquals("PERSONAL_OPENID", WechatSplitReceiverAdminService.receiverType("oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"));
        assertEquals("PERSONAL_OPENID", WechatSplitReceiverAdminService.receiverType("1234567"));
        assertEquals("PERSONAL_OPENID", WechatSplitReceiverAdminService.receiverType(null));
    }

    /** 仅实现公钥模式所需字段的最小配置桩。 */
    private static final class StubMerchantProps implements WechatMerchantProperties {
        private final String publicKeyId;
        private final String publicKeyBase64;

        private StubMerchantProps(String publicKeyId, String publicKeyBase64) {
            this.publicKeyId = publicKeyId;
            this.publicKeyBase64 = publicKeyBase64;
        }

        @Override public boolean isEnabled() { return true; }
        @Override public String getMchId() { return "1900000001"; }
        @Override public String getApiV3Key() { return "01234567890123456789012345678901"; }
        @Override public String getPrivateKeyPath() { return null; }
        @Override public String getPrivateKeyBase64() { return null; }
        @Override public String getMerchantSerialNumber() { return "SERIAL"; }
        @Override public String getAppId() { return "wx_test"; }
        @Override public String getNotifyUrl() { return "https://example.com/notify"; }
        @Override public String getRefundNotifyUrl() { return null; }
        @Override public String getGateway() { return "https://api.mch.weixin.qq.com"; }
        @Override public long getCertRefreshMillis() { return 3_600_000L; }
        @Override public String getPublicKeyId() { return publicKeyId; }
        @Override public String getPublicKeyPath() { return null; }
        @Override public String getPublicKeyBase64() { return publicKeyBase64; }
        @Override public boolean isConfigured() { return true; }
    }
}
