package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.transfer.BindTransferAccountRequest;
import com.wuye.points.dto.transfer.TransferAccountResponse;
import com.wuye.points.entity.MerchantTransferAccount;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.WechatCrypto;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantTransferAccountRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final ResidentRepository residentRepository;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatPayProperties wechatPayProperties;
    private final WechatCertManager certManager;
    private final SnowflakeIdGenerator idGenerator;
    private final RoleGuard roleGuard;
    private final LeaderIdentityGuard leaderIdentityGuard;

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
            // openid 是打款地址，不接受客户端提交：一律取登录态住户自己的 residents.openid
            account.setOpenid(requireSelfOpenid());
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

    /**
     * 「进件成功默认微信零钱」绑定结果，供调用方决定落日志/待办，不抛异常打断主流程。
     */
    public enum ZeroBindOutcome {
        /** 新建或补齐 openid 后写入成功 */
        BOUND,
        /** 已有 zero 绑定且 openid 非空：本次未写库、未覆盖 */
        ALREADY_BOUND,
        /** 住户 residents.openid 为空：记待绑定，等下次登录补齐 */
        PENDING_OPENID,
        /** 住户没有有效四层树结算身份（无内部结算出款通道），不落"假绑定" */
        SKIPPED_NO_IDENTITY
    }

    /**
     * 把当前登录身份的微信零钱收款方式设为本人微信（幂等：已有绑定不重复写、不覆盖 openid）。
     *
     * <p>openid 一律取 {@code residents.openid}，既不接收客户端值，也不下发明文。</p>
     */
    @Transactional
    public TransferAccountResponse bindZeroForCurrentOwner() {
        OwnerRef owner = resolveCurrentOwner();
        String openid = requireSelfOpenid();
        MerchantTransferAccount account = accountRepository
                .findByOwnerTypeAndOwnerIdAndChannel(owner.type, owner.id, "zero").orElse(null);
        if (account != null && StringUtils.hasText(account.getOpenid())) {
            markVerifiedIfNeeded(account);
            return toResponse(account); // 幂等：已有绑定不重复写、不覆盖 openid
        }
        return toResponse(saveZero(owner, account, openid));
    }

    /**
     * 按"指定住户"绑定微信零钱 —— 进件成功自动默认零钱的入口。
     *
     * <p>与 {@link #bindZeroForCurrentOwner()} 的关键差别：<b>不依赖 {@link UserContext}</b>，
     * 供定时任务/进件回调等无登录态场景使用；ownerType 由该住户当前生效的四层树身份推导，
     * 因此绝不会绑到"代办进件的运营账号"（代客进件绑错人=绑错收款账户）。</p>
     *
     * <p>独立事务（REQUIRES_NEW）：绑定失败只回滚本次绑定，不影响调用方（进件）主流程。
     * 已有 zero 绑定且 openid 非空时直接返回；已有银行卡绑定是另一 channel 行，保留不删。</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ZeroBindOutcome bindZeroForResident(String residentId, String propertyCompanyId) {
        if (!StringUtils.hasText(residentId)) {
            return ZeroBindOutcome.SKIPPED_NO_IDENTITY;
        }
        OwnerRef owner = resolveOwnerOfResident(residentId, propertyCompanyId);
        if (owner == null) {
            log.info("[收款账号] 默认零钱跳过：住户 {} 无有效四层树身份 propertyCompanyId={}",
                    residentId, propertyCompanyId);
            return ZeroBindOutcome.SKIPPED_NO_IDENTITY;
        }
        MerchantTransferAccount account = accountRepository
                .findByOwnerTypeAndOwnerIdAndChannel(owner.type, owner.id, "zero").orElse(null);
        if (account != null && StringUtils.hasText(account.getOpenid())) {
            markVerifiedIfNeeded(account);
            return ZeroBindOutcome.ALREADY_BOUND; // 幂等：不重复写、不覆盖
        }
        String openid = residentRepository.findById(residentId)
                .map(Resident::getOpenid)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse(null);
        if (!StringUtils.hasText(openid)) {
            log.info("[收款账号] 默认零钱待绑定：住户 {} openid 为空 ownerType={}，待下次登录补齐",
                    residentId, owner.type);
            return ZeroBindOutcome.PENDING_OPENID;
        }
        saveZero(owner, account, openid);
        return ZeroBindOutcome.BOUND;
    }

    /** 已有 zero 行但未核验时仅补 verified 标记（不触碰 openid，修复历史遗留行）。 */
    private void markVerifiedIfNeeded(MerchantTransferAccount account) {
        if (!Boolean.TRUE.equals(account.getVerified())) {
            account.setVerified(true);
            accountRepository.save(account);
        }
    }

    /** 写 zero 行（新建或补齐 openid），不动同主体的 bank 行。 */
    private MerchantTransferAccount saveZero(OwnerRef owner, MerchantTransferAccount account, String openid) {
        if (account == null) {
            account = new MerchantTransferAccount();
            account.setId(idGenerator.generateId("mta"));
            account.setOwnerType(owner.type);
            account.setOwnerId(owner.id);
            account.setChannel("zero");
        }
        account.setOpenid(openid);
        clearBankFields(account);
        account.setVerified(true);
        accountRepository.save(account);
        log.info("[收款账号] 默认零钱绑定 ownerType={} ownerId={}", owner.type, owner.id);
        return account;
    }

    /** 解绑当前登录角色指定渠道的收款账号。 */
    @Transactional
    public void unbind(String channel) {
        // 条目33：以前任意取值都静默成功（channel=xxx 也返回 200），与 bind 的取值口径不一致
        if (!"zero".equals(channel) && !"bank".equals(channel)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "收款渠道仅支持 zero(微信零钱)/bank(银行卡)");
        }
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

    /** 由当前登录身份推导四层树（ownerType, ownerId）。 */
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
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        // 2026-10-20 负责人标签化：统筹/板块/个体负责人身份由业务表 active 记录判定（不再看 role），
        // 顺序与原 switch 一致：统筹 > 板块负责人 > 个体负责人。
        if (leaderIdentityGuard.activeCoordinator(userId, propertyCompanyId).isPresent()) {
            return new OwnerRef("COORDINATOR", userId);
        }
        if (!leaderIdentityGuard.activeSectorLeaders(userId, propertyCompanyId).isEmpty()) {
            return new OwnerRef("SECTOR_LEADER", userId);
        }
        if (leaderIdentityGuard.activeIndividualLeader(userId, propertyCompanyId).isPresent()) {
            return new OwnerRef("INDIVIDUAL_LEADER", userId);
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED,
                "当前角色不支持绑定结算收款账号（仅物业/统筹/板块/个体负责人）");
    }

    /**
     * 由"指定住户 + 物业公司"推导四层树身份（顺序与 {@link #resolveCurrentOwner()} 一致）。
     * 住户不具备四层树身份时返回 null：商家/普通住户的收入走收付通分账，不经过本表的内部结算通道。
     */
    private OwnerRef resolveOwnerOfResident(String residentId, String propertyCompanyId) {
        if (leaderIdentityGuard.activeCoordinator(residentId, propertyCompanyId).isPresent()) {
            return new OwnerRef("COORDINATOR", residentId);
        }
        if (!leaderIdentityGuard.activeSectorLeaders(residentId, propertyCompanyId).isEmpty()) {
            return new OwnerRef("SECTOR_LEADER", residentId);
        }
        if (leaderIdentityGuard.activeIndividualLeader(residentId, propertyCompanyId).isPresent()) {
            return new OwnerRef("INDIVIDUAL_LEADER", residentId);
        }
        return null;
    }

    /** 登录态住户本人的微信 openid；缺失时给出明确错误码，不再让前端兜底猜测。 */
    private String requireSelfOpenid() {
        String userId = UserContext.currentUserId();
        return residentRepository.findById(userId)
                .map(Resident::getOpenid)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElseThrow(() -> new BizException(ErrorCode.TRANSFER_OPENID_MISSING));
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
