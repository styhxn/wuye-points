package com.wuye.points.payment;

import com.wuye.points.dto.admin.merchant.MerchantWechatAccountRequest;
import com.wuye.points.dto.admin.merchant.MerchantWechatAccountResponse;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 商家特约商户号（sub_mchid）绑定管理。
 *
 * <p>服务商模式下，商家必须持有微信特约商户号，订单资金才能直进商家账户并参与分账。
 * 两个入口会写本表：进件审核通过自动回填（{@code MerchantApplymentService}），
 * 或运营在微信服务商平台手动创建特约商户后通过本服务补录。</p>
 *
 * <p>WeChat 硬规则：服务商自身的 sp_mchid 不能作为 sub_mchid，平台直连商户号也不应混作子商户，
 * 绑定前显式拒绝，避免下单时才被微信以 PARAM_ERROR 拒绝。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantWechatAccountAdminService {

    private static final Set<String> STATUSES = Set.of("active", "auditing", "frozen");

    private final MerchantWechatAccountRepository wechatAccountRepository;
    private final MerchantRepository merchantRepository;
    private final WechatPayProperties partnerProps;
    private final WechatDirectProperties directProps;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public MerchantWechatAccountResponse get(String merchantId) {
        requireMerchant(merchantId);
        return wechatAccountRepository.findByMerchantId(merchantId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public MerchantWechatAccountResponse bind(String merchantId, MerchantWechatAccountRequest request) {
        requireMerchant(merchantId);
        String subMchid = request.getSubMchid().trim();
        if (!subMchid.matches("\\d{8,20}")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "特约商户号应为 8~20 位数字: " + subMchid);
        }
        if (subMchid.equals(partnerProps.getMchId())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "服务商自身商户号不能作为特约商户号（微信硬规则），请填写商家的特约商户号");
        }
        if (subMchid.equals(directProps.getMchId())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "平台直连商户号是平台自营收款主体，不能绑定为商家特约商户号");
        }
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus().trim().toLowerCase() : "active";
        if (!STATUSES.contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "状态仅支持 active / auditing / frozen: " + request.getStatus());
        }

        MerchantWechatAccount account = wechatAccountRepository.findByMerchantId(merchantId)
                .orElseGet(() -> {
                    MerchantWechatAccount a = new MerchantWechatAccount();
                    a.setId(idGenerator.generateId("mwa_"));
                    a.setMerchantId(merchantId);
                    return a;
                });
        if (StringUtils.hasText(account.getSubMchid()) && !account.getSubMchid().equals(subMchid)) {
            log.warn("[商家进件] 变更已绑定特约商户号 merchantId={} old={} new={}（历史流水按原 sub_mchid 退款，不受影响）",
                    merchantId, account.getSubMchid(), subMchid);
        }
        account.setSubMchid(subMchid);
        account.setMerchantName(StringUtils.hasText(request.getMerchantName())
                ? request.getMerchantName().trim() : account.getMerchantName());
        account.setStatus(status);
        wechatAccountRepository.save(account);
        log.info("[商家进件] 绑定特约商户号 merchantId={} subMchid={} status={}", merchantId, subMchid, status);
        return toResponse(account);
    }

    private void requireMerchant(String merchantId) {
        if (!StringUtils.hasText(merchantId) || !merchantRepository.existsById(merchantId)) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
    }

    private MerchantWechatAccountResponse toResponse(MerchantWechatAccount a) {
        return MerchantWechatAccountResponse.builder()
                .id(a.getId())
                .merchantId(a.getMerchantId())
                .subMchid(a.getSubMchid())
                .merchantName(a.getMerchantName())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
