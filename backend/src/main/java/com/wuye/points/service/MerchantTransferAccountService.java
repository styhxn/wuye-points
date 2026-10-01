package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.transfer.BindTransferAccountRequest;
import com.wuye.points.dto.transfer.TransferAccountResponse;
import com.wuye.points.entity.MerchantTransferAccount;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.WechatCrypto;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantTransferAccountRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.util.List;

/**
 * 四层树角色收款账号服务（阶段2：商家转账前置）。
 *
 * <p>电商收付通模式下，四层分账树（物业/统筹/板块/个体）不在微信侧分账，
 * 由平台内部结算后通过商家转账出款。本服务维护各角色本人收款账号
 * （zero=微信零钱 openid / bank=银行卡），由当前登录角色自服务绑定（前端「我的收款方式」页）。</p>
 *
 * <p>银行卡号/持卡人以微信平台证书 RSA-OAEP 加密落库（不存明文），并记录加密所用证书序列号
 * {@code cert_serial}：证书轮换后旧密文不可用，转账前检测到不一致会提示重新绑定，fail-closed。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantTransferAccountService {

    private final MerchantTransferAccountRepository accountRepository;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatPayProperties wechatPayProperties;
    private final WechatCertManager certManager;
    private final SnowflakeIdGenerator idGenerator;
    private final RoleGuard roleGuard;

    /** 绑定当前登录角色自己的收款账号（zero/bank 二选一，同渠道幂等覆盖）。 */
    @Transactional
    public TransferAccountResponse bind(BindTransferAccountRequest request) {
        OwnerRef owner = resolveCurrentOwner();
        if (!"zero".equals(request.getChannel()) && !"bank".equals(request.getChannel())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "收款渠道仅支持 zero(微信零钱)/bank(银行卡)");
        }
        MerchantTransferAccount account = accountRepository
                .findByOwnerTypeAndOwnerIdAndChannel(owner.type, owner.id, request.getChannel()).orElse(null);
        if (account == null) {
            account = new MerchantTransferAccount();
            account.setId(idGenerator.generateId("mta"));
            account.setOwnerType(owner.type);
            account.setOwnerId(owner.id);
            account.setChannel(request.getChannel());
        }
        if ("zero".equals(request.getChannel())) {
            if (!StringUtils.hasText(request.getOpenid())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "微信零钱渠道必须填写 openid");
            }
            account.setOpenid(request.getOpenid().trim());
            clearBankFields(account);
        } else {
            if (!StringUtils.hasText(request.getBankAccountNo())
                    || !StringUtils.hasText(request.getBankName())
                    || !StringUtils.hasText(request.getAccountName())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "银行卡渠道必须填写银行卡号、开户银行、持卡人姓名");
            }
            CertRef cert = requireCert();
            account.setOpenid(null);
            account.setBankName(request.getBankName().trim());
            account.setBankAccountNoEnc(WechatCrypto.encryptOaep(cert.publicKey, request.getBankAccountNo().trim()));
            account.setAccountNameEnc(WechatCrypto.encryptOaep(cert.publicKey, request.getAccountName().trim()));
            account.setCertSerial(cert.serialNo);
        }
        account.setVerified(true);
        accountRepository.save(account);
        log.info("[收款账号] 绑定成功 ownerType={} ownerId={} channel={}", owner.type, owner.id, request.getChannel());
        return toResponse(account);
    }

    /** 解绑当前登录角色指定渠道的收款账号。 */
    @Transactional
    public void unbind(String channel) {
        OwnerRef owner = resolveCurrentOwner();
        accountRepository.findByOwnerTypeAndOwnerIdAndChannel(owner.type, owner.id, channel)
                .ifPresent(account -> {
                    accountRepository.delete(account);
                    log.info("[收款账号] 解绑 ownerType={} ownerId={} channel={}", owner.type, owner.id, channel);
                });
    }

    /** 查询当前登录角色的全部收款账号。 */
    public List<TransferAccountResponse> listMine() {
        OwnerRef owner = resolveCurrentOwner();
        return accountRepository.findByOwnerTypeAndOwnerId(owner.type, owner.id)
                .stream().map(this::toResponse).toList();
    }

    // ---------------------------------------------------------------------
    // 辅助
    // ---------------------------------------------------------------------

    /** 由当前登录角色推导四层树（ownerType, ownerId）。 */
    private OwnerRef resolveCurrentOwner() {
        String role = RoleGuard.normalizeRole(UserContext.currentRole());
        if ("property_admin".equals(role)) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (!StringUtils.hasText(propertyCompanyId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "当前物业账号未绑定物业公司，无法绑定收款账号");
            }
            return new OwnerRef("PROPERTY", propertyCompanyId);
        }
        String userId = UserContext.currentUserId();
        return switch (role) {
            case "coordinator" -> new OwnerRef("COORDINATOR", userId);
            case "sector_leader" -> new OwnerRef("SECTOR_LEADER", userId);
            case "individual_leader" -> new OwnerRef("INDIVIDUAL_LEADER", userId);
            default -> throw new BizException(ErrorCode.PERMISSION_DENIED,
                    "当前角色不支持绑定结算收款账号（仅物业/统筹/板块/个体负责人）");
        };
    }

    /** 取当前平台证书公钥 + 序列号；无证书（占位未配置）时 fail-closed。 */
    private CertRef requireCert() {
        if (!ecommerceProps.isEnabled() || !ecommerceProps.isConfigured(wechatPayProperties)) {
            throw new BizException(ErrorCode.WECHAT_ECOMMERCE_NOT_CONFIGURED);
        }
        String serial = certManager.anySerial();
        PublicKey cert = StringUtils.hasText(serial) ? certManager.getCertificate(serial) : null;
        if (cert == null) {
            throw new BizException(ErrorCode.WECHAT_CERT_NOT_READY,
                    "微信平台证书未就绪（请确认 wechat.pay 平台证书配置），无法绑定银行卡收款账号");
        }
        return new CertRef(serial, cert);
    }

    private static void clearBankFields(MerchantTransferAccount account) {
        account.setBankName(null);
        account.setBankAccountNoEnc(null);
        account.setAccountNameEnc(null);
        account.setCertSerial(null);
    }

    private TransferAccountResponse toResponse(MerchantTransferAccount account) {
        return TransferAccountResponse.builder()
                .id(account.getId())
                .ownerType(account.getOwnerType())
                .channel(account.getChannel())
                .openid(maskOpenid(account.getOpenid()))
                .bankName(account.getBankName())
                .verified(account.getVerified())
                .build();
    }

    private static String maskOpenid(String openid) {
        if (!StringUtils.hasText(openid) || openid.length() < 8) {
            return openid;
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }

    private record OwnerRef(String type, String id) {
    }

    private record CertRef(String serialNo, PublicKey publicKey) {
    }
}
