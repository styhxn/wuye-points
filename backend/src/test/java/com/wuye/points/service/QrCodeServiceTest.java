package com.wuye.points.service;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("二维码生成与落盘")
class QrCodeServiceTest {

    @Mock
    private WechatMpService wechatMpService;

    @TempDir
    Path tempDir;

    private QrCodeService service;

    @BeforeEach
    void setUp() {
        service = new QrCodeService(wechatMpService);
        ReflectionTestUtils.setField(service, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(service, "uploadBaseUrl", "https://test.example.com/api/v1/static");
        ReflectionTestUtils.setField(service, "size", 300);
    }

    @Test
    @DisplayName("本地二维码生成的 PNG 能被解回原文")
    void linkQrCode_pngIsDecodable() {
        String content = "https://test.yuanxusoftware.top/h5/shop?listingId=lst_1&resellerId=res_1";

        byte[] png = service.pngBytes(content);

        assertTrue(png.length > 100);
        assertEquals((byte) 0x89, png[0]);
        assertEquals(content, QrCodeUtil.decode(new ByteArrayInputStream(png)));
    }

    @Test
    @DisplayName("空内容直接拒绝，不产出废码")
    void pngBytes_blankContentRejected() {
        BizException ex = assertThrows(BizException.class, () -> service.pngBytes("  "));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("小程序码落盘到 qrcode 目录并按上传基址拼 URL")
    void miniProgramCode_storesAndReturnsUrl() throws IOException {
        String path = "pages/service/merchantProducts?merchantId=mch_1";
        when(wechatMpService.getMiniProgramCode(path, 300)).thenReturn(service.pngBytes(path));

        QrCodeService.Generated generated = service.miniProgramCode(
                path, QrCodeService.merchantFileName("mch_1"), false);

        assertEquals("https://test.example.com/api/v1/static/qrcode/merchant_mch_1.png", generated.url());
        assertEquals(QrCodeService.TYPE_MINIPROGRAM, generated.type());
        assertEquals(path, generated.content());
        assertTrue(Files.exists(tempDir.resolve("qrcode").resolve("merchant_mch_1.png")));
    }

    @Test
    @DisplayName("复用已有码时不打微信接口")
    void miniProgramCode_reusesExistingFile() throws IOException {
        Files.createDirectories(tempDir.resolve("qrcode"));
        Files.write(tempDir.resolve("qrcode").resolve("listing_lst_1.png"), new byte[]{1, 2, 3});

        QrCodeService.Generated generated = service.miniProgramCode(
                "pages/service/productDetail?listingId=lst_1", QrCodeService.listingFileName("lst_1"), true);

        assertNotNull(generated.url());
        verify(wechatMpService, never()).getMiniProgramCode(anyString(), anyInt());
    }

    @Test
    @DisplayName("微信不可用时降级为链接二维码，分享不因此失败")
    void miniProgramCodeOrLink_fallsBackToLinkCode() {
        when(wechatMpService.getMiniProgramCode(anyString(), anyInt()))
                .thenThrow(new BizException(ErrorCode.WECHAT_API_ERROR, "小程序码生成失败"));

        QrCodeService.Generated generated = service.miniProgramCodeOrLink(
                "pages/service/productDetail?listingId=lst_2",
                "https://test.yuanxusoftware.top/h5/shop?listingId=lst_2",
                QrCodeService.listingFileName("lst_2"));

        assertEquals(QrCodeService.TYPE_QR_LINK, generated.type());
        assertTrue(Files.exists(tempDir.resolve("qrcode").resolve("listing_lst_2.png")));
    }

    @Test
    @DisplayName("未生成过时 existingUrl 返回 null，供只读查询判定")
    void existingUrl_nullWhenNotGenerated() {
        assertNull(service.existingUrl("merchant_mch_none.png"));
    }
}
