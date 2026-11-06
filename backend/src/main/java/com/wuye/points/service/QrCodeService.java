package com.wuye.points.service;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 二维码图片生成与落盘。
 *
 * <p>两种码：{@code MINIPROGRAM_CODE} 由微信 {@code /wxa/getwxacode} 生成（官方口径：永久有效、
 * 有数量限制，path 可带 query 参数，扫码直达页面且前端零改动）；{@code QR_LINK} 由 zxing 本地生成，
 * 内容是指向 H5 的普通链接。落盘沿用 {@link FileService} 的目录与 URL 规则（{@code {uploadPath}/qrcode}
 * ↔ {@code {base-url}/qrcode}），文件名按业务主键固定，重复生成覆盖同名文件——天然幂等，
 * 也不需要为码类型/内容再加库表字段。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    public static final String TYPE_MINIPROGRAM = "MINIPROGRAM_CODE";
    public static final String TYPE_QR_LINK = "QR_LINK";

    private static final String CATEGORY = "qrcode";

    private final WechatMpService wechatMpService;

    @Value("${app.upload.path:/data/uploads}")
    private String uploadPath;

    @Value("${app.upload.base-url:http://localhost:8888/api/v1/static}")
    private String uploadBaseUrl;

    @Value("${app.qr-code.size:430}")
    private int size;

    public record Generated(String url, String type, String content) {
    }

    /**
     * 小程序码。
     *
     * @param pagePathWithQuery 形如 {@code pages/service/merchantProducts?merchantId=mch_x}
     * @param fileName          业务主键决定的固定文件名，保证幂等
     * @param reuseExisting     同名文件已存在时直接返回 URL，不再打微信接口
     */
    public Generated miniProgramCode(String pagePathWithQuery, String fileName, boolean reuseExisting) {
        if (!StringUtils.hasText(pagePathWithQuery)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "小程序页面路径不能为空");
        }
        if (reuseExisting && exists(fileName)) {
            return new Generated(publicUrl(fileName), TYPE_MINIPROGRAM, pagePathWithQuery);
        }
        byte[] png = wechatMpService.getMiniProgramCode(pagePathWithQuery, size);
        return new Generated(store(png, fileName), TYPE_MINIPROGRAM, pagePathWithQuery);
    }

    /** 本地链接二维码（不依赖微信，内容为任意 URL/文本）。 */
    public Generated linkQrCode(String content, String fileName, boolean reuseExisting) {
        if (reuseExisting && exists(fileName)) {
            return new Generated(publicUrl(fileName), TYPE_QR_LINK, content);
        }
        return new Generated(store(pngBytes(content), fileName), TYPE_QR_LINK, content);
    }

    /**
     * 附属场景（分享/海报）用：优先微信小程序码，微信不可用时降级为链接二维码，
     * 两者都失败才抛错——不能让「分享」因为一张附属码挂掉。
     */
    public Generated miniProgramCodeOrLink(String pagePathWithQuery, String linkContent, String fileName) {
        if (exists(fileName)) {
            return new Generated(publicUrl(fileName), TYPE_MINIPROGRAM, pagePathWithQuery);
        }
        try {
            return miniProgramCode(pagePathWithQuery, fileName, false);
        } catch (RuntimeException miniProgramFailure) {
            log.warn("小程序码生成失败，降级为链接二维码: path={}, reason={}",
                    pagePathWithQuery, miniProgramFailure.getMessage());
            try {
                return linkQrCode(linkContent, fileName, false);
            } catch (RuntimeException linkFailure) {
                log.warn("链接二维码也生成失败: file={}", fileName, linkFailure);
                throw miniProgramFailure;
            }
        }
    }

    public byte[] pngBytes(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "二维码内容不能为空");
        }
        try {
            return QrCodeUtil.generatePng(content, size, size);
        } catch (RuntimeException ex) {
            log.error("二维码生成失败: content={}", content, ex);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "二维码生成失败");
        }
    }

    /** 供只读展示：已经生成过就直接给 URL，不重新调微信。 */
    public String existingUrl(String fileName) {
        return exists(fileName) ? publicUrl(fileName) : null;
    }

    public static String merchantFileName(String merchantId) {
        return "merchant_" + merchantId + ".png";
    }

    public static String listingFileName(String listingId) {
        return "listing_" + listingId + ".png";
    }

    public static String applymentFileName(String merchantId) {
        return "applyment_" + merchantId + ".png";
    }

    private boolean exists(String fileName) {
        return Files.isReadable(dir().resolve(fileName));
    }

    private String store(byte[] png, String fileName) {
        if (png == null || png.length == 0) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "二维码生成失败");
        }
        Path target = dir().resolve(fileName);
        try {
            Files.createDirectories(dir());
            Files.write(target, png);
        } catch (IOException ex) {
            log.error("二维码落盘失败: file={}", target, ex);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "二维码存储失败");
        }
        return publicUrl(fileName);
    }

    private Path dir() {
        return Paths.get(uploadPath, CATEGORY);
    }

    private String publicUrl(String fileName) {
        String base = uploadBaseUrl.endsWith("/")
                ? uploadBaseUrl.substring(0, uploadBaseUrl.length() - 1)
                : uploadBaseUrl;
        return base + "/" + CATEGORY + "/" + fileName;
    }
}
