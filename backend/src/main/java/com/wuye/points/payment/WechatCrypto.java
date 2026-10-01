package com.wuye.points.payment;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 微信支付 APIv3 基础密码学工具：商户 RSA 请求签名、平台证书验签、回调 AES-256-GCM 解密。
 *
 * <p>签名算法 SHA256withRSA；对称解密算法 AES/GCM/NoPadding（微信把 16 字节 tag 追加在密文末尾）。
 * 全部使用 JDK 内置 Provider（SunJCE / SunRsaSign），不依赖 BouncyCastle。
 */
@Slf4j
public final class WechatCrypto {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final String RSA_SIGN_ALGO = "SHA256withRSA";

    private WechatCrypto() {
    }

    /** 去除 PEM 头尾与空白，得到纯 Base64。 */
    public static String stripPem(String pem) {
        return pem.replaceAll("-----BEGIN (?:.*?)-----", "")
                .replaceAll("-----END (?:.*?)-----", "")
                .replaceAll("\\s+", "");
    }

    public static PrivateKey loadPrivateKey(String pemOrBase64) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pemOrBase64));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("加载商户 API 私钥失败", e);
        }
    }

    public static PublicKey loadPublicKey(String pemOrBase64) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pemOrBase64));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("加载平台证书公钥失败", e);
        }
    }

    /** 商户侧：对请求签名串做 SHA256withRSA。 */
    public static String sign(PrivateKey key, String message) {
        try {
            Signature sig = Signature.getInstance(RSA_SIGN_ALGO);
            sig.initSign(key);
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new IllegalStateException("微信请求签名失败", e);
        }
    }

    /** 平台侧：用平台证书公钥验签（回调头验签）。 */
    public static boolean verify(PublicKey platformCert, String message, String signatureBase64) {
        try {
            Signature sig = Signature.getInstance(RSA_SIGN_ALGO);
            sig.initVerify(platformCert);
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.warn("微信回调验签异常", e);
            return false;
        }
    }

    /** 回调 resource AES-256-GCM 解密。ciphertext 已含 16 字节 tag。 */
    public static String decryptAesGcm(String apiV3Key, String associatedData, String nonce, String ciphertext) {
        try {
            byte[] key = apiV3Key.getBytes(StandardCharsets.UTF_8);
            byte[] iv = nonce.getBytes(StandardCharsets.UTF_8);
            byte[] data = Base64.getDecoder().decode(ciphertext);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            byte[] plain = cipher.doFinal(data);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("微信回调解密失败", e);
        }
    }

    /**
     * 敏感信息 RSAES-OAEP 加密（如分账 receiver_name）。用平台证书公钥加密，
     * 对应 HTTP 头 {@code Wechatpay-Serial} = 所用证书序列号。
     * 算法：{@code RSA/ECB/OAEPWithSHA-1AndMGF1Padding}（微信官方约定 MGF1-SHA1）。
     */
    public static String encryptOaep(PublicKey publicKey, String plain) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] enc = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new IllegalStateException("微信 OAEP 加密失败", e);
        }
    }
}
