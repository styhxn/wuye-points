package com.wuye.points.service;

import com.wuye.points.dto.merchant.MerchantQrCodeResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商家专属收款二维码（PRD P0-05「商家入驻与专属二维码」）。
 *
 * <p>取微信 {@code /wxa/getwxacode} 生成的小程序码：官方口径「永久有效，有数量限制」，
 * 且 path 可带 query 参数，顾客扫码直达店铺页后走既有 JSAPI 支付与平台分账链路，
 * 不改变资金归属。生成的图片 URL 写回既有列 {@code merchants.qr_code_url}，
 * 因此 App 商家中心与管理端商家详情的展示位无需前端改造即可显示。</p>
 *
 * <p>文件名按商家ID固定（{@code qrcode/merchant_{id}.png}），重复生成覆盖同名文件，
 * 幂等且不留垃圾文件；未审核通过或已停用的店铺不允许生成。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantQrCodeService {

    private final MerchantRepository merchantRepository;
    private final MerchantService merchantService;
    private final QrCodeService qrCodeService;
    private final RoleGuard roleGuard;

    @Value("${app.qr-code.merchant-page:pages/service/merchantProducts}")
    private String merchantPage;

    /** 管理端生成/重生成：限本物业公司（平台管理员不限），且商家须审核通过并在营。 */
    @Transactional
    public MerchantQrCodeResponse generateForAdmin(String merchantId, boolean force) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant merchant = requireMerchant(merchantId);
        roleGuard.assertSamePropertyOrPlatform(merchant.getPropertyCompanyId());
        return generate(merchant, force);
    }

    /** 商家自助生成：只能取本人名下店铺。 */
    @Transactional
    public MerchantQrCodeResponse generateForOwner(boolean force) {
        String merchantId = merchantService.getMyMerchant().getId();
        return generate(requireMerchant(merchantId), force);
    }

    /** 查询当前码；从未生成过返回 {@code qrCodeUrl=null}，不触发微信调用。 */
    @Transactional(readOnly = true)
    public MerchantQrCodeResponse getForAdmin(String merchantId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Merchant merchant = requireMerchant(merchantId);
        roleGuard.assertSamePropertyOrPlatform(merchant.getPropertyCompanyId());
        return describe(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantQrCodeResponse getForOwner() {
        return describe(requireMerchant(merchantService.getMyMerchant().getId()));
    }

    /**
     * 给「审核通过、在营、还没有专属二维码」的店铺补码。
     *
     * <p>两端目前都没有「生成收款码」按钮（前端只读），PRD P0-05 的收款码要能直接验收，
     * 所以由启动/定时任务补齐；单个店铺失败只记日志，不影响其余店铺。</p>
     */
    @Transactional
    public int backfillMissing() {
        List<Merchant> pending = merchantRepository
                .findByAuditStatusAndStatusAndQrCodeUrlIsNull("approved", "active");
        int generated = 0;
        for (Merchant merchant : pending) {
            try {
                generate(merchant, false);
                generated++;
            } catch (RuntimeException ex) {
                log.warn("[收款码] 补生成失败 merchantId={}: {}", merchant.getId(), ex.getMessage());
            }
        }
        return generated;
    }

    private MerchantQrCodeResponse generate(Merchant merchant, boolean force) {
        assertAuditedAndActive(merchant);

        String fileName = QrCodeService.merchantFileName(merchant.getId());
        String storedUrl = qrCodeService.existingUrl(fileName);
        if (!force && StringUtils.hasText(merchant.getQrCodeUrl())
                && merchant.getQrCodeUrl().equals(storedUrl)) {
            return describe(merchant);
        }

        QrCodeService.Generated generated = qrCodeService.miniProgramCode(
                codePath(merchant.getId()), fileName, false);
        merchant.setQrCodeUrl(generated.url());
        merchantRepository.save(merchant);
        log.info("[收款码] 商家 {} 生成小程序码 path={}", merchant.getId(), generated.content());

        return MerchantQrCodeResponse.builder()
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .qrCodeUrl(generated.url())
                .codeType(generated.type())
                .codeContent(generated.content())
                .regenerated(true)
                .build();
    }

    private MerchantQrCodeResponse describe(Merchant merchant) {
        String fileName = QrCodeService.merchantFileName(merchant.getId());
        boolean fileExists = merchant.getQrCodeUrl() != null
                && merchant.getQrCodeUrl().equals(qrCodeService.existingUrl(fileName));
        return MerchantQrCodeResponse.builder()
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .qrCodeUrl(merchant.getQrCodeUrl())
                .codeType(fileExists ? QrCodeService.TYPE_MINIPROGRAM : null)
                .codeContent(fileExists ? codePath(merchant.getId()) : null)
                .regenerated(false)
                .build();
    }

    private String codePath(String merchantId) {
        return merchantPage + "?merchantId=" + merchantId;
    }

    private Merchant requireMerchant(String merchantId) {
        // 商家主键有两种历史前缀：业务建店 mch_、平台商家库挂接时补建 m_
        if (!StringUtils.hasText(merchantId) || !(merchantId.startsWith("mch_") || merchantId.startsWith("m_"))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "merchantId格式不正确");
        }
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private void assertAuditedAndActive(Merchant merchant) {
        if (!"approved".equals(merchant.getAuditStatus())) {
            throw new BizException(ErrorCode.MERCHANT_AUDITING, "商家审核通过后才能生成收款二维码");
        }
        if ("inactive".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家已停用，无法生成收款二维码");
        }
    }
}
