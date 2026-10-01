package com.wuye.points.service;

import cn.hutool.json.JSONObject;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.MediaUploadResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantApplyment;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.EcommerceApplymentApiService;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatCrypto;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.security.PublicKey;
import java.util.List;
import java.util.Set;

/**
 * 商家微信进件业务服务（电商收付通阶段1）。
 *
 * <p>状态机（本地）：init → submitted → auditing → legal_validating → signing → finished / rejected。
 * 敏感字段（证件号/银行账号/姓名）在落库与提交前一律用微信平台证书 RSA-OAEP 加密；
 * 进件成功后 sub_mchid 回填 {@code merchant_wechat_accounts}（status=active），
 * 支付链路据此按商家路由入账。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EcommerceApplymentService {

    /** 非终态（可被调度推进）的本地状态。 */
    public static final Set<String> ACTIVE_STATES =
            Set.of("submitted", "auditing", "legal_validating", "signing");

    /** 微信侧状态 → 本地状态映射（键为微信 applyment_state 原值）。 */
    private static final java.util.Map<String, String> WX_STATE_MAP = java.util.Map.ofEntries(
            java.util.Map.entry("ACCEPTED", "auditing"),
            java.util.Map.entry("AUDITING", "auditing"),
            java.util.Map.entry("LEGAL_VALIDATION", "legal_validating"),
            java.util.Map.entry("SIGNING", "signing"),
            java.util.Map.entry("FINISH", "finished"),
            java.util.Map.entry("REJECTED", "rejected"),
            java.util.Map.entry("PARTNER_REJECT", "rejected"),
            java.util.Map.entry("FROZEN", "rejected"));

    private final MerchantApplymentRepository applymentRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantWechatAccountRepository wechatAccountRepository;
    private final ResidentRepository residentRepository;
    private final EcommerceApplymentApiService applymentApiService;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatPayProperties wechatPayProperties;
    private final WechatCertManager certManager;
    private final SnowflakeIdGenerator idGenerator;

    // ---------------------------------------------------------------------
    // 商家端
    // ---------------------------------------------------------------------

    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file) {
        requireEcommerceEnabled();
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "上传文件不能为空");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上传文件不能超过10MB");
        }
        try {
            String mediaId = applymentApiService.uploadMedia(file.getBytes(), file.getOriginalFilename());
            if (!StringUtils.hasText(mediaId)) {
                throw new BizException(ErrorCode.APPLYMENT_UPLOAD_FAILED, "微信未返回 media_id");
            }
            return MediaUploadResponse.builder().mediaId(mediaId).build();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[进件] 媒体上传失败", e);
            throw new BizException(ErrorCode.APPLYMENT_UPLOAD_FAILED, "进件资料上传微信失败: " + e.getMessage());
        }
    }

    /**
     * 商家保存/更新进件草稿（init 或 rejected 可编辑）。敏感字段以微信平台证书加密落库。
     */
    @Transactional
    public ApplymentDetailResponse saveApplyment(String merchantId, ApplymentSubmitRequest request) {
        Merchant merchant = getMerchantForCurrentUser(merchantId);
        validateRequest(request);

        MerchantApplyment applyment = applymentRepository.findByMerchantId(merchantId).orElse(null);
        if (applyment != null && ACTIVE_STATES.contains(applyment.getApplymentState())) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID,
                    "进件已在微信审核中，不可修改资料");
        }

        String serialNo = requireCertSerial();
        if (applyment == null) {
            applyment = new MerchantApplyment();
            applyment.setId(idGenerator.generateId("ma"));
            applyment.setMerchantId(merchantId);
            applyment.setApplymentState("init");
            applyment.setSubmitMode(ecommerceProps.isApplymentAutoSubmit() ? "auto" : "manual");
            applyment.setRetryCount(0);
        } else {
            // 驳回重提：清空微信侧回执，重新走提交流程
            applyment.setApplymentId(null);
            applyment.setWxState(null);
            applyment.setApplymentState("init");
            applyment.setRejectReason(null);
            applyment.setLegalValidationUrl(null);
            applyment.setSignUrl(null);
            applyment.setRetryCount(0);
            applyment.setLastError(null);
        }

        applyment.setOrganizationType(request.getOrganizationType());
        applyment.setMerchantShortname(request.getMerchantShortname());
        applyment.setMerchantCategory(request.getMerchantCategory());
        applyment.setContactName(request.getContactName());
        applyment.setContactMobile(request.getContactMobile());
        applyment.setContactEmail(request.getContactEmail());
        applyment.setIdCardNameEnc(encrypt(request.getIdCardName(), serialNo));
        applyment.setIdCardNumberEnc(encrypt(request.getIdCardNumber(), serialNo));
        applyment.setIdCardCopyMedia(request.getIdCardCopyMedia());
        applyment.setIdCardNationalMedia(request.getIdCardNationalMedia());
        applyment.setBusinessLicenseMedia(request.getBusinessLicenseMedia());
        applyment.setBankAccountType(request.getBankAccountType());
        applyment.setBankAccountNameEnc(encrypt(request.getBankAccountName(), serialNo));
        applyment.setBankAccountNumberEnc(encrypt(request.getBankAccountNumber(), serialNo));
        applyment.setBankName(request.getBankName());
        applymentRepository.save(applyment);
        log.info("[进件] 商家 {} 保存进件资料，状态 init", merchantId);

        // 自动提交模式：资料保存完成即尝试提交微信（失败仅记 lastError，状态保持 init 可重试）
        if ("auto".equals(applyment.getSubmitMode())) {
            try {
                submitInternal(applyment, merchant);
            } catch (Exception e) {
                // 资料已保存，仅提交失败：不中断保存事务，由商家/运营手动重试提交
                log.warn("[进件] 自动提交失败（资料已保存，可手动重试）merchantId={}: {}",
                        merchantId, e.getMessage());
            }
        }
        return toResponse(applyment);
    }

    /**
     * 提交进件到微信（商家端手动提交 / 运营后台触发共用）。状态 init/rejected → submitted。
     */
    @Transactional
    public ApplymentDetailResponse submitApplyment(String merchantId) {
        requireEcommerceEnabled();
        Merchant merchant = getMerchantForCurrentUser(merchantId);
        MerchantApplyment applyment = getApplymentEntity(merchantId);
        submitInternal(applyment, merchant);
        return toResponse(applyment);
    }

    /** 提交进件核心逻辑（幂等：已提交/审核中不可重复提交）。 */
    private void submitInternal(MerchantApplyment applyment, Merchant merchant) {
        if (applyment.getApplymentId() != null) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID, "该申请已提交过微信");
        }
        if (!Set.of("init", "rejected").contains(applyment.getApplymentState())) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID,
                    "当前状态（" + applyment.getApplymentState() + "）不可提交进件");
        }
        String serialNo = requireCertSerial();
        EcommerceApplymentApiService.SubmitPayload payload = buildPayload(applyment, merchant, serialNo);
        try {
            String applymentId = applymentApiService.submitApplyment(payload);
            if (!StringUtils.hasText(applymentId)) {
                throw new BizException(ErrorCode.WECHAT_API_ERROR, "微信未返回 applyment_id");
            }
            applyment.setApplymentId(applymentId);
            applyment.setApplymentState("submitted");
            applyment.setWxState("ACCEPTED");
            applyment.setLastError(null);
            applyment.setRetryCount(0);
            applymentRepository.save(applyment);
            log.info("[进件] 商家 {} 提交进件成功 applyment_id={}", applyment.getMerchantId(), applymentId);
        } catch (BizException e) {
            applyment.setLastError(e.getMessage());
            applymentRepository.save(applyment);
            throw e;
        } catch (Exception e) {
            log.error("[进件] 提交微信失败 merchantId={}", applyment.getMerchantId(), e);
            applyment.setLastError(e.getMessage());
            applymentRepository.save(applyment);
            throw new BizException(ErrorCode.WECHAT_API_ERROR, "提交进件失败: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApplymentDetailResponse getApplyment(String merchantId) {
        getMerchantForCurrentUser(merchantId);
        MerchantApplyment applyment = applymentRepository.findByMerchantId(merchantId).orElse(null);
        if (applyment == null) {
            return toResponse(newApplyment(merchantId));
        }
        return toResponse(applyment);
    }

    /**
     * 解析当前登录商家（merchant/activity_leader/technician）的商家ID。
     * 供「我的进件」类接口（不带 merchantId 参数）使用。
     */
    @Transactional(readOnly = true)
    public String resolveCurrentMerchantId() {
        return resolveOwnedMerchant().getId();
    }

    // ---------------------------------------------------------------------
    // 状态推进（调度 / 管理端查询复用）
    // ---------------------------------------------------------------------

    /**
     * 查询微信侧进件状态并推进本地状态机。成功回填 sub_mchid；驳回记录原因。
     * 供 {@code ApplymentStatusScheduler} 与运营后台手动查询共用。
     */
    @Transactional
    public void refreshApplymentStatus(MerchantApplyment applyment) {
        if (!StringUtils.hasText(applyment.getApplymentId())) {
            return;
        }
        try {
            JSONObject wx = applymentApiService.queryApplyment(applyment.getApplymentId());
            String wxState = wx.getStr("applyment_state");
            if (!StringUtils.hasText(wxState)) {
                applyment.setLastError("微信未返回 applyment_state");
                applyment.setRetryCount(inc(applyment.getRetryCount()));
                applymentRepository.save(applyment);
                return;
            }
            applyment.setWxState(wxState);
            applyment.setLegalValidationUrl(emptyToNull(wx.getStr("legal_validation_url")));
            applyment.setSignUrl(emptyToNull(wx.getStr("sign_url")));
            applyment.setRejectReason(emptyToNull(wx.getStr("reject_reason")));
            applyment.setRetryCount(0);
            applyment.setLastError(null);

            String local = WX_STATE_MAP.getOrDefault(wxState, applyment.getApplymentState());
            applyment.setApplymentState(local);

            if ("finished".equals(local)) {
                String subMchid = emptyToNull(wx.getStr("sub_mchid"));
                if (!StringUtils.hasText(subMchid)) {
                    subMchid = applyment.getSubMchid();
                }
                applyment.setSubMchid(subMchid);
                backfillWechatAccount(applyment, subMchid);
            }
            applymentRepository.save(applyment);
            log.info("[进件] 商家 {} 状态推进 wxState={} local={}", applyment.getMerchantId(), wxState, local);
        } catch (Exception e) {
            log.warn("[进件] 查询微信状态失败 merchantId={} applymentId={}: {}",
                    applyment.getMerchantId(), applyment.getApplymentId(), e.getMessage());
            applyment.setLastError(e.getMessage());
            applyment.setRetryCount(inc(applyment.getRetryCount()));
            applymentRepository.save(applyment);
        }
    }

    // ---------------------------------------------------------------------
    // 私有方法
    // ---------------------------------------------------------------------

    private void backfillWechatAccount(MerchantApplyment applyment, String subMchid) {
        if (!StringUtils.hasText(subMchid)) {
            return;
        }
        MerchantWechatAccount account = wechatAccountRepository.findByMerchantId(applyment.getMerchantId())
                .orElseGet(() -> {
                    MerchantWechatAccount a = new MerchantWechatAccount();
                    a.setId(idGenerator.generateId("mwa"));
                    a.setMerchantId(applyment.getMerchantId());
                    return a;
                });
        account.setSubMchid(subMchid);
        account.setMerchantName(applyment.getMerchantShortname());
        account.setStatus("active");
        wechatAccountRepository.save(account);
    }

    private EcommerceApplymentApiService.SubmitPayload buildPayload(MerchantApplyment a, Merchant merchant,
                                                                    String serialNo) {
        return EcommerceApplymentApiService.SubmitPayload.builder()
                .outRequestNo(a.getId())
                .certSerialNo(serialNo)
                .subjectType(subjectType(a.getOrganizationType()))
                .contactNameEnc(a.getIdCardNameEnc())
                .contactIdNumberEnc(a.getIdCardNumberEnc())
                .contactMobile(a.getContactMobile())
                .contactEmail(a.getContactEmail())
                .licenseMediaId(emptyToNull(a.getBusinessLicenseMedia()))
                .licenseNumberEnc(a.getIdCardNumberEnc())
                .merchantNameEnc(a.getIdCardNameEnc())
                .legalPersonEnc(a.getIdCardNameEnc())
                .idCardCopyMedia(a.getIdCardCopyMedia())
                .idCardNationalMedia(a.getIdCardNationalMedia())
                .idCardNameEnc(a.getIdCardNameEnc())
                .idCardNumberEnc(a.getIdCardNumberEnc())
                .owner(true)
                .bankAccountType(a.getBankAccountType())
                .accountBank("") // 占位：微信银行编号（account_bank）需按微信最新银行列表联调对齐
                .bankName(a.getBankName())
                .accountNameEnc(a.getBankAccountNameEnc())
                .accountNumberEnc(a.getBankAccountNumberEnc())
                .build();
    }

    /** 主体类型枚举 → 微信 subject_type。 */
    private static String subjectType(String organizationType) {
        if ("ENTERPRISE".equals(organizationType)) {
            return "SUBJECT_TYPE_ENTERPRISE";
        }
        if ("MICRO".equals(organizationType)) {
            return "SUBJECT_TYPE_MICRO";
        }
        return "SUBJECT_TYPE_INDIVIDUAL";
    }

    private void validateRequest(ApplymentSubmitRequest request) {
        if (!Set.of("ENTERPRISE", "INDIVIDUAL", "MICRO").contains(request.getOrganizationType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "主体类型必须为 ENTERPRISE/INDIVIDUAL/MICRO");
        }
        if (!Set.of("BANK_ACCOUNT_TYPE_CORPORATE", "BANK_ACCOUNT_TYPE_PERSONAL")
                .contains(request.getBankAccountType())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "结算账户类型必须为 BANK_ACCOUNT_TYPE_CORPORATE/BANK_ACCOUNT_TYPE_PERSONAL");
        }
        // 企业/个体户必须有营业执照
        if (!"MICRO".equals(request.getOrganizationType())
                && !StringUtils.hasText(request.getBusinessLicenseMedia())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "营业执照图片不能为空");
        }
    }

    private MerchantApplyment getApplymentEntity(String merchantId) {
        return applymentRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.APPLYMENT_NOT_FOUND, "请先提交进件资料"));
    }

    private MerchantApplyment newApplyment(String merchantId) {
        MerchantApplyment a = new MerchantApplyment();
        a.setMerchantId(merchantId);
        a.setApplymentState("init");
        return a;
    }

    private Merchant getMerchantForCurrentUser(String merchantId) {
        String role = UserContext.currentRole();
        if ("merchant".equals(role) || "activity_leader".equals(role) || "technician".equals(role)) {
            Merchant owned = resolveOwnedMerchant();
            if (!owned.getId().equals(merchantId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
            return owned;
        }
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private Merchant resolveOwnedMerchant() {
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private String encrypt(String plain, String serialNo) {
        PublicKey cert = certManager.getCertificate(serialNo);
        if (cert == null) {
            throw new BizException(ErrorCode.WECHAT_CERT_NOT_READY);
        }
        return WechatCrypto.encryptOaep(cert, plain);
    }

    /** 取平台证书序列号；无证书（占位未配置）时抛业务异常，fail-closed。 */
    private String requireCertSerial() {
        requireEcommerceEnabled();
        String serial = certManager.anySerial();
        if (!StringUtils.hasText(serial)) {
            throw new BizException(ErrorCode.WECHAT_CERT_NOT_READY,
                    "微信平台证书未就绪（请确认 wechat.pay 平台证书配置），无法加密进件资料");
        }
        return serial;
    }

    private void requireEcommerceEnabled() {
        if (!ecommerceProps.isEnabled() || !ecommerceProps.isConfigured(wechatPayProperties)) {
            throw new BizException(ErrorCode.WECHAT_ECOMMERCE_NOT_CONFIGURED);
        }
    }

    private static int inc(Integer n) {
        return (n == null ? 0 : n) + 1;
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    private static ApplymentDetailResponse toResponse(MerchantApplyment a) {
        return ApplymentDetailResponse.builder()
                .merchantId(a.getMerchantId())
                .applymentState(a.getApplymentState())
                .wxState(a.getWxState())
                .organizationType(a.getOrganizationType())
                .merchantShortname(a.getMerchantShortname())
                .merchantCategory(a.getMerchantCategory())
                .contactMobile(maskMobile(a.getContactMobile()))
                .bankAccountType(a.getBankAccountType())
                .bankName(a.getBankName())
                .submitted(a.getApplymentId() != null)
                .legalValidationUrl(a.getLegalValidationUrl())
                .signUrl(a.getSignUrl())
                .rejectReason(a.getRejectReason())
                .subMchid(a.getSubMchid())
                .submitMode(a.getSubmitMode())
                .lastError(a.getLastError())
                .build();
    }

    private static String maskMobile(String mobile) {
        if (!StringUtils.hasText(mobile) || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
