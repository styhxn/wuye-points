package com.wuye.points.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 微信小程序 OpenAPI 封装
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMpService {

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    private static final String ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String GET_PHONE_URL =
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";
    private static final String GET_WXACODE_URL =
            "https://api.weixin.qq.com/wxa/getwxacode?access_token=%s";

    @Value("${wechat.mp.app-id}")
    private String appId;

    @Value("${wechat.mp.app-secret}")
    private String appSecret;

    @Value("${app.wechat.skip-api:false}")
    private boolean skipApi;

    private final AtomicReference<CachedAccessToken> accessTokenCache = new AtomicReference<>();

    public WechatSession code2Session(String code) {
        // dev_ 伪造登录只在显式 skip-api=true 时生效（dev 配置或测试服环境变量）；
        // 生产默认 false，绝不能凭 code 前缀绕过微信鉴权。
        if (skipApi) {
            return WechatSession.builder()
                    .openid("dev_openid_" + sanitizeDevSuffix(code))
                    .sessionKey("dev_session_key")
                    .build();
        }

        String url = String.format(CODE2SESSION_URL, appId, appSecret, code);
        String body = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(body);
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.warn("微信 code2Session 失败: {}", body);
            throw new BizException(ErrorCode.WECHAT_AUTH_FAILED);
        }
        return WechatSession.builder()
                .openid(json.getStr("openid"))
                .unionid(json.getStr("unionid"))
                .sessionKey(json.getStr("session_key"))
                .build();
    }

    public String getPhoneNumber(String code) {
        if (skipApi) {
            return extractDevPhone(code);
        }

        String accessToken = getAccessToken();
        String url = String.format(GET_PHONE_URL, accessToken);
        JSONObject requestBody = JSONUtil.createObj().set("code", code);
        String response = HttpUtil.post(url, requestBody.toString());
        JSONObject json = JSONUtil.parseObj(response);
        if (json.getInt("errcode", 0) != 0) {
            log.warn("微信 getPhoneNumber 失败: {}", response);
            throw new BizException(ErrorCode.WECHAT_AUTH_FAILED);
        }
        JSONObject phoneInfo = json.getJSONObject("phone_info");
        if (phoneInfo == null || phoneInfo.getStr("phoneNumber") == null) {
            throw new BizException(ErrorCode.WECHAT_AUTH_FAILED);
        }
        return phoneInfo.getStr("phoneNumber");
    }

    /**
     * 生成小程序码（官方 {@code /wxa/getwxacode}：永久有效、有数量限制，
     * path 可带 query 参数，因此扫码后 onLoad 能直接拿到业务参数，前端无需解析 scene）。
     *
     * <p>成功时微信把图片字节直接放在响应体里，失败时才返回 JSON，需按首字节区分。</p>
     */
    public byte[] getMiniProgramCode(String path, int width) {
        if (skipApi) {
            throw new BizException(ErrorCode.WECHAT_API_ERROR, "开发模式未接通微信接口，无法生成小程序码");
        }

        String url = String.format(GET_WXACODE_URL, getAccessToken());
        JSONObject requestBody = JSONUtil.createObj().set("path", path).set("width", width);
        byte[] response = HttpUtil.createPost(url).body(requestBody.toString()).execute().bodyBytes();

        JSONObject error = parseError(response);
        if (error != null) {
            log.warn("微信小程序码生成失败: path={}, errcode={}, errmsg={}",
                    path, error.getStr("errcode"), error.getStr("errmsg"));
            throw new BizException(ErrorCode.WECHAT_API_ERROR, "小程序码生成失败：" + error.getStr("errmsg"));
        }
        return response;
    }

    private JSONObject parseError(byte[] response) {
        if (response == null || response.length == 0) {
            return JSONUtil.createObj().set("errmsg", "微信返回空响应");
        }
        if (response[0] != '{') {
            return null;
        }
        try {
            JSONObject json = JSONUtil.parseObj(new String(response, StandardCharsets.UTF_8));
            return json.getInt("errcode", 0) == 0 ? null : json;
        } catch (Exception ex) {
            return JSONUtil.createObj().set("errmsg", "微信响应解析失败");
        }
    }

    private String getAccessToken() {
        CachedAccessToken cached = accessTokenCache.get();
        if (cached != null && cached.isValid()) {
            return cached.token;
        }
        String url = String.format(ACCESS_TOKEN_URL, appId, appSecret);
        String body = HttpUtil.get(url);
        JSONObject json = JSONUtil.parseObj(body);
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.error("微信 access_token 获取失败: {}", body);
            throw new BizException(ErrorCode.WECHAT_AUTH_FAILED);
        }
        String token = json.getStr("access_token");
        int expiresIn = json.getInt("expires_in", 7200);
        accessTokenCache.set(new CachedAccessToken(token, System.currentTimeMillis() + (expiresIn - 300L) * 1000));
        return token;
    }

    private String sanitizeDevSuffix(String code) {
        return code.replace("dev_", "").replaceAll("[^a-zA-Z0-9]", "");
    }

    private String extractDevPhone(String code) {
        String phone = code.substring("dev_phone_".length());
        if (!phone.matches("^1\\d{10}$")) {
            throw new BizException(ErrorCode.WECHAT_AUTH_FAILED, "开发模式手机号格式不正确");
        }
        return phone;
    }

    @Data
    @Builder
    public static class WechatSession {
        private String openid;
        private String unionid;
        private String sessionKey;
    }

    private static class CachedAccessToken {
        private final String token;
        private final long expireAtMs;

        private CachedAccessToken(String token, long expireAtMs) {
            this.token = token;
            this.expireAtMs = expireAtMs;
        }

        private boolean isValid() {
            return System.currentTimeMillis() < expireAtMs;
        }
    }
}
