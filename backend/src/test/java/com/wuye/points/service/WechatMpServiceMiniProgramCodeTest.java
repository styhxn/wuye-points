package com.wuye.points.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * 小程序码接口：微信成功时把图片字节直接放在响应体里，失败时才返回 JSON，
 * 判错必须按首字节区分，否则会把错误 JSON 当图片存盘。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("微信小程序码接口")
class WechatMpServiceMiniProgramCodeTest {

    private WechatMpService service;

    @BeforeEach
    void setUp() {
        service = new WechatMpService();
        ReflectionTestUtils.setField(service, "appId", "wxf7230afd38275659");
        ReflectionTestUtils.setField(service, "appSecret", "secret");
        ReflectionTestUtils.setField(service, "skipApi", false);
    }

    private MockedStatic<HttpUtil> stubTokenAndPost(byte[] responseBody) {
        MockedStatic<HttpUtil> httpUtil = mockStatic(HttpUtil.class);
        httpUtil.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"access_token\":\"tok_123\",\"expires_in\":7200}");
        HttpRequest request = mock(HttpRequest.class, RETURNS_DEEP_STUBS);
        httpUtil.when(() -> HttpUtil.createPost(anyString())).thenReturn(request);
        when(request.body(anyString()).execute().bodyBytes()).thenReturn(responseBody);
        return httpUtil;
    }

    @Test
    @DisplayName("成功：直接返回微信给的图片字节")
    void success_returnsImageBytes() {
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4};
        try (MockedStatic<HttpUtil> httpUtil = stubTokenAndPost(png)) {
            byte[] result = service.getMiniProgramCode("pages/service/merchantProducts?merchantId=mch_1", 430);

            assertEquals(8, result.length);
            assertEquals((byte) 0x89, result[0]);
        }
    }

    @Test
    @DisplayName("失败：响应是错误 JSON 时按 errcode 抛业务错，不当图片存盘")
    void failure_throwsOnJsonErrorBody() {
        byte[] errorJson = "{\"errcode\":41030,\"errmsg\":\"page not exist\"}"
                .getBytes(StandardCharsets.UTF_8);
        try (MockedStatic<HttpUtil> ignored = stubTokenAndPost(errorJson)) {
            BizException ex = assertThrows(BizException.class, () ->
                    service.getMiniProgramCode("pages/not/published?merchantId=mch_1", 430));

            assertEquals(ErrorCode.WECHAT_API_ERROR.getCode(), ex.getCode());
            assertTrue(ex.getMessage().contains("page not exist"));
        }
    }

    @Test
    @DisplayName("开发模式（skip-api）不接通微信接口，明确报错而不是发废码")
    void skipApi_failsFast() {
        ReflectionTestUtils.setField(service, "skipApi", true);

        BizException ex = assertThrows(BizException.class,
                () -> service.getMiniProgramCode("pages/service/merchantProducts?merchantId=mch_1", 430));

        assertEquals(ErrorCode.WECHAT_API_ERROR.getCode(), ex.getCode());
    }
}
