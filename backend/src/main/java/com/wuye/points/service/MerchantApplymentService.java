package com.wuye.points.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.dto.merchant.ApplymentSubmitRequest;
import com.wuye.points.dto.merchant.MediaUploadResponse;
import com.wuye.points.dto.merchant.UboInfoRequest;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantApplyment;
import com.wuye.points.entity.MerchantWechatAccount;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PartnerApplymentApiService;
import com.wuye.points.payment.PartnerApplymentProperties;
import com.wuye.points.payment.PartnerApplymentRules;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商家微信进件（普通服务商「特约商户进件」）。
 *
 * <p>流程：商家填资料（init）→ 平台提交 {@code /v3/applyment4sub/applyment/}（submitted）
 * → 微信审核（auditing）→ 待账户验证（legal_validating）→ 待签约（signing，返回超管签约链接）
 * → 完成（finished，回填 sub_mchid 并激活商家收款）/ 驳回（rejected，可改资料重提）。</p>
 *
 * <p>官方约束：一个商家同一时间只能有一笔流程中的申请单；重新提交必须更换业务申请编号
 * （相同编号微信视为同一申请单，不会重新审核）。本类每次提交都生成新的 business_code。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantApplymentService {

    /** 非终态（可被调度推进）的本地状态。 */
    public static final Set<String> ACTIVE_STATES =
            Set.of("submitted", "auditing", "legal_validating", "signing");

    /** 微信 applyment_state → 本地状态。 */
    private static final Map<String, String> WX_STATE_MAP = Map.ofEntries(
            Map.entry("APPLYMENT_STATE_EDITTING", "init"),
            Map.entry("APPLYMENT_STATE_UNSUBMITTED", "init"),
            Map.entry("APPLYMENT_STATE_AUDITING", "auditing"),
            Map.entry("APPLYMENT_STATE_REJECTED", "rejected"),
            Map.entry("APPLYMENT_STATE_TO_BE_CONFIRMED", "legal_validating"),
            Map.entry("APPLYMENT_STATE_TO_BE_SIGNED", "signing"),
            // 开通权限中：继续保持 signing，前端展示等待；完成后转 finished
            Map.entry("APPLYMENT_STATE_SIGNING", "signing"),
            Map.entry("APPLYMENT_STATE_FINISHED", "finished"),
            Map.entry("APPLYMENT_STATE_CANCELED", "rejected"));

    private static final Set<String> SUBJECT_TYPES = Set.of("ENTERPRISE", "INDIVIDUAL");
    private static final Set<String> CONTACT_TYPES = Set.of("LEGAL");

    /** 当前支持资料结构的经营场景；APP/网站/企业微信无对应字段，提交必被微信驳回。 */
    private static final Set<String> SUPPORTED_SALES_SCENES =
            Set.of("SALES_SCENES_STORE", "SALES_SCENES_MINI_PROGRAM", "SALES_SCENES_MP");

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp");

    /** 前端轮询时最多每 15 秒触发一次微信状态查询，避免把查询接口打成限频。 */
    private static final long APPLYMENT_REFRESH_INTERVAL_MS = 15_000L;

    private final Map<String, Long> lastRefreshAt = new ConcurrentHashMap<>();

    private final MerchantApplymentRepository applymentRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantWechatAccountRepository wechatAccountRepository;
    private final ResidentRepository residentRepository;
    private final PartnerApplymentApiService applymentApiService;
    private final PartnerApplymentProperties applymentProps;
    private final WechatPayProperties wechatPayProperties;
    private final WechatCertManager certManager;
    private final SnowflakeIdGenerator idGenerator;
    private final com.wuye.points.util.RoleGuard roleGuard;

    // ---------------------------------------------------------------------
    // 商家端
    // ---------------------------------------------------------------------

    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file) {
        resolveCurrentMerchantId();
        requireApplymentReady();
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "上传文件不能为空");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BizException(ErrorCode.PARAM_INVALID, "进件图片不能超过5MB");
        }
        String fileName = file.getOriginalFilename();
        if (!hasImageExtension(fileName)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "进件图片仅支持 JPG/JPEG/PNG/BMP");
        }
        try {
            String mediaId = applymentApiService.uploadMedia(file.getBytes(), fileName);
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

    /** 银行列表（进件表单「开户银行」下拉数据源）。 */
    public JSONObject listBanks(String bankAccountType) {
        resolveCurrentMerchantId();
        requireApplymentReady();
        return applymentApiService.listBanks(bankAccountType);
    }

    /** 支行列表（银行要求填写支行时使用）。官方要求 city_code 必填，支持分页。 */
    public JSONObject listBranches(String bankAliasCode, String cityCode, Integer offset, Integer limit) {
        resolveCurrentMerchantId();
        requireApplymentReady();
        if (!StringUtils.hasText(bankAliasCode)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "银行编码不能为空");
        }
        if (!StringUtils.hasText(cityCode)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "城市编码不能为空");
        }
        int safeOffset = offset == null || offset < 0 ? 0 : offset;
        int safeLimit = limit == null ? 100 : Math.max(1, Math.min(limit, 500));
        return applymentApiService.listBranches(bankAliasCode, cityCode, safeOffset, safeLimit);
    }

    /** 可选行业与结算规则（前端下拉数据源，避免与后端校验漂移）。 */
    public Map<String, Object> industryOptions() {
        resolveCurrentMerchantId();
        if (!applymentProps.isEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家自助进件功能未开启");
        }
        List<Map<String, Object>> industries = new ArrayList<>();
        for (String name : PartnerApplymentRules.SUPPORTED_INDUSTRIES) {
            LinkedHashMap<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("needsQualification", PartnerApplymentRules.NEEDS_QUALIFICATION.contains(name));
            industries.add(item);
        }
        List<String> special = new ArrayList<>(PartnerApplymentRules.SPECIAL_RULE_INDUSTRIES);
        special.sort(String::compareTo);
        LinkedHashMap<String, String> settlementRules = new LinkedHashMap<>();
        settlementRules.put("ENTERPRISE", effectiveSettlementId("ENTERPRISE"));
        settlementRules.put("INDIVIDUAL", effectiveSettlementId("INDIVIDUAL"));

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("defaultIndustry", effectiveDefaultIndustry());
        result.put("industries", industries);
        result.put("specialRuleIndustries", special);
        result.put("settlementRules", settlementRules);
        return result;
    }

    /**
     * 商家保存/更新进件草稿（init 或 rejected 可编辑）。敏感字段加密落库。
     */
    @Transactional
    public ApplymentDetailResponse saveApplyment(String merchantId, ApplymentSubmitRequest request) {
        getMerchantForCurrentUser(merchantId);
        requireApplymentReady();
        validateRequest(request);

        MerchantApplyment applyment = applymentRepository.findByMerchantId(merchantId).orElse(null);
        if (applyment != null && ACTIVE_STATES.contains(applyment.getApplymentState())) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID, "进件已在微信审核中，不可修改资料");
        }

        String serialNo = requireCertSerial();
        if (applyment == null) {
            applyment = new MerchantApplyment();
            applyment.setId(idGenerator.generateId("ma"));
            applyment.setMerchantId(merchantId);
            applyment.setRetryCount(0);
        } else {
            // 驳回重提：清空微信侧回执，重新走提交流程
            applyment.setApplymentId(null);
            applyment.setBusinessCode(null);
            applyment.setWxState(null);
            applyment.setRejectReason(null);
            applyment.setSignUrl(null);
            applyment.setRetryCount(0);
            applyment.setLastError(null);
        }
        applyment.setApplymentState("init");
        applyment.setSubmitMode(applymentProps.isAutoSubmit() ? "auto" : "manual");

        applyment.setOrganizationType(request.getOrganizationType());
        applyment.setFinanceInstitution(applymentProps.isFinanceInstitution());
        applyment.setMerchantShortname(request.getMerchantShortname());
        applyment.setServicePhone(request.getServicePhone());

        applyment.setBusinessLicenseMedia(request.getBusinessLicenseMedia());
        applyment.setBusinessLicenseNumber(request.getBusinessLicenseNumber());
        applyment.setMerchantName(request.getMerchantName());
        applyment.setLegalPerson(request.getLegalPerson());
        applyment.setLicenseAddress(request.getLicenseAddress());
        applyment.setLicensePeriodBegin(request.getLicensePeriodBegin());
        applyment.setLicensePeriodEnd(request.getLicensePeriodEnd());

        applyment.setIdCardNameEnc(encrypt(request.getIdCardName(), serialNo));
        applyment.setIdCardNumberEnc(encrypt(request.getIdCardNumber(), serialNo));
        applyment.setIdCardCopyMedia(request.getIdCardCopyMedia());
        applyment.setIdCardNationalMedia(request.getIdCardNationalMedia());
        applyment.setIdCardAddressEnc(StringUtils.hasText(request.getIdCardAddress())
                ? encrypt(request.getIdCardAddress(), serialNo) : null);
        applyment.setIdCardPeriodBegin(request.getIdCardPeriodBegin());
        applyment.setIdCardPeriodEnd(request.getIdCardPeriodEnd());
        applyment.setUboJson(encryptUboJson(request.getUboList(), serialNo));

        applyment.setSalesScenesType(request.getSalesScenesType());
        applyment.setSalesAppId(resolveSalesAppId(request));
        applyment.setSalesSubAppId(request.getSalesSubAppId());
        applyment.setSalesPics(csv(request.getSalesPics()));
        applyment.setBizStoreName(request.getBizStoreName());
        applyment.setBizAddressCode(request.getBizAddressCode());
        applyment.setBizStoreAddress(request.getBizStoreAddress());
        applyment.setStoreEntrancePics(csv(request.getStoreEntrancePics()));
        applyment.setStoreIndoorPics(csv(request.getStoreIndoorPics()));

        // 结算规则由平台控制，商家请求不允许指定，避免商户自选低费率规则
        applyment.setSettlementId(effectiveSettlementId(request.getOrganizationType()));
        applyment.setQualificationType(effectiveIndustry(request));
        applyment.setQualifications(csv(request.getQualifications()));

        applyment.setBankAccountType(request.getBankAccountType());
        applyment.setBankAccountNameEnc(encrypt(request.getBankAccountName(), serialNo));
        applyment.setBankAccountNumberEnc(encrypt(request.getBankAccountNumber(), serialNo));
        applyment.setAccountBank(request.getAccountBank());
        applyment.setBankAliasCode(request.getBankAliasCode());
        applyment.setBankBranchId(request.getBankBranchId());
        applyment.setBankName(request.getBankName());

        applyment.setContactType(request.getContactType());
        applyment.setContactName(request.getContactName());
        applyment.setContactMobile(request.getContactMobile());
        applyment.setContactEmailEnc(encrypt(request.getContactEmail(), serialNo));

        applymentRepository.save(applyment);
        log.info("[进件] 商家 {} 保存进件资料，状态 init", merchantId);

        if ("auto".equals(applyment.getSubmitMode())) {
            try {
                submitInternal(applyment);
            } catch (Exception e) {
                log.warn("[进件] 自动提交失败（资料已保存，可手动重试）merchantId={}: {}",
                        merchantId, e.getMessage());
            }
        }
        return toResponse(applyment, false);
    }

    /**
     * 提交进件到微信（商家端手动提交）。状态 init/rejected → submitted。
     */
    @Transactional
    public ApplymentDetailResponse submitApplyment(String merchantId) {
        return submitApplyment(merchantId, false);
    }

    /** 提交进件到微信（运营后台触发）：响应额外带 lastError/retryCount。 */
    @Transactional
    public ApplymentDetailResponse submitApplymentForAdmin(String merchantId) {
        return submitApplyment(merchantId, true);
    }

    private ApplymentDetailResponse submitApplyment(String merchantId, boolean withAdminDiagnostics) {
        getMerchantForCurrentUser(merchantId);
        requireApplymentReady();
        MerchantApplyment applyment = getApplymentEntity(merchantId);
        submitInternal(applyment);
        return toResponse(applyment, withAdminDiagnostics);
    }

    private void submitInternal(MerchantApplyment applyment) {
        if (ACTIVE_STATES.contains(applyment.getApplymentState())) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID, "该申请已提交微信，请等待审核");
        }
        if (!Set.of("init", "rejected").contains(applyment.getApplymentState())) {
            throw new BizException(ErrorCode.APPLYMENT_STATE_INVALID,
                    "当前状态（" + applyment.getApplymentState() + "）不可提交进件");
        }
        String serialNo = requireCertSerial();
        // 官方：重新提交必须更换业务申请编号，否则微信视为同一申请单不重新审核
        String businessCode = idGenerator.generateId("bc");
        PartnerApplymentApiService.SubmitPayload payload = buildPayload(applyment, businessCode, serialNo);
        try {
            String applymentId = applymentApiService.submitApplyment(payload);
            if (!StringUtils.hasText(applymentId)) {
                throw new BizException(ErrorCode.WECHAT_API_ERROR, "微信未返回 applyment_id");
            }
            applyment.setBusinessCode(businessCode);
            applyment.setApplymentId(applymentId);
            applyment.setApplymentState("submitted");
            applyment.setWxState("SUBMITTED");
            applyment.setLastError(null);
            applyment.setRetryCount(0);
            applymentRepository.save(applyment);
            log.info("[进件] 商家 {} 提交成功 applyment_id={} business_code={}",
                    applyment.getMerchantId(), applymentId, businessCode);
        } catch (BizException e) {
            applyment.setLastError(truncate(e.getMessage(), 500));
            applymentRepository.save(applyment);
            throw e;
        } catch (Exception e) {
            log.error("[进件] 提交微信失败 merchantId={}", applyment.getMerchantId(), e);
            applyment.setLastError(truncate(e.getMessage(), 500));
            applymentRepository.save(applyment);
            throw new BizException(ErrorCode.WECHAT_API_ERROR, "提交进件失败: " + e.getMessage());
        }
    }

    /**
     * 查询进件状态。审核中的申请单会顺带查询微信（15 秒节流），
     * 这样前端轮询 GET 就能及时拿到签约链接，而不是等每日兜底任务。
     */
    @Transactional
    public ApplymentDetailResponse getApplyment(String merchantId) {
        return getApplyment(merchantId, false);
    }

    /** 查询进件状态（运营后台）：响应额外带 lastError/retryCount。 */
    @Transactional
    public ApplymentDetailResponse getApplymentForAdmin(String merchantId) {
        return getApplyment(merchantId, true);
    }

    private ApplymentDetailResponse getApplyment(String merchantId, boolean withAdminDiagnostics) {
        getMerchantForCurrentUser(merchantId);
        MerchantApplyment applyment = applymentRepository.findByMerchantId(merchantId).orElse(null);
        if (applyment == null) {
            return toResponse(newApplyment(merchantId), withAdminDiagnostics);
        }
        refreshIfStale(applyment);
        return toResponse(applyment, withAdminDiagnostics);
    }

    private void refreshIfStale(MerchantApplyment applyment) {
        if (!ACTIVE_STATES.contains(applyment.getApplymentState())
                || !StringUtils.hasText(applyment.getBusinessCode())
                || !isApplymentReady()
                || applyment.getId() == null) {
            return;
        }
        Long last = lastRefreshAt.get(applyment.getId());
        long now = System.currentTimeMillis();
        if (last != null && now - last < APPLYMENT_REFRESH_INTERVAL_MS) {
            return;
        }
        refreshApplymentStatus(applyment);
    }

    /** 解析当前登录商家（merchant/activity_leader/technician/组长）的商家ID。 */
    @Transactional(readOnly = true)
    public String resolveCurrentMerchantId() {
        return resolveOwnedMerchant().getId();
    }

    // ---------------------------------------------------------------------
    // 状态推进
    // ---------------------------------------------------------------------

    /**
     * 查询微信侧进件状态并推进本地状态机。成功回填 sub_mchid；驳回记录原因。
     */
    @Transactional
    public void refreshApplymentStatus(MerchantApplyment applyment) {
        if (!StringUtils.hasText(applyment.getBusinessCode())) {
            return;
        }
        if (applyment.getId() != null) {
            lastRefreshAt.put(applyment.getId(), System.currentTimeMillis());
        }
        try {
            JSONObject wx = applymentApiService.queryApplyment(applyment.getBusinessCode());
            String wxState = wx.getStr("applyment_state");
            if (!StringUtils.hasText(wxState)) {
                applyment.setLastError("微信未返回 applyment_state");
                applyment.setRetryCount(inc(applyment.getRetryCount()));
                applymentRepository.save(applyment);
                return;
            }
            applyment.setWxState(wxState);
            if (StringUtils.hasText(wx.getStr("applyment_id"))) {
                applyment.setApplymentId(wx.getStr("applyment_id"));
            }
            applyment.setSignUrl(emptyToNull(wx.getStr("sign_url")));
            applyment.setRejectReason(emptyToNull(extractRejectReason(wx)));
            applyment.setRetryCount(0);
            applyment.setLastError(null);

            String local = localStateOf(wxState, applyment.getApplymentState());
            applyment.setApplymentState(local);
            if (!ACTIVE_STATES.contains(local) && applyment.getId() != null) {
                lastRefreshAt.remove(applyment.getId());
            }

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
            log.warn("[进件] 查询微信状态失败 merchantId={} businessCode={}: {}",
                    applyment.getMerchantId(), applyment.getBusinessCode(), e.getMessage());
            applyment.setLastError(truncate(e.getMessage(), 500));
            applyment.setRetryCount(inc(applyment.getRetryCount()));
            applymentRepository.save(applyment);
        }
    }

    // ---------------------------------------------------------------------
    // 私有方法
    // ---------------------------------------------------------------------

    /** 微信申请状态 → 本地状态（未知状态保持原值）。 */
    static String localStateOf(String wxState, String fallback) {
        return WX_STATE_MAP.getOrDefault(wxState, fallback);
    }

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

    private PartnerApplymentApiService.SubmitPayload buildPayload(MerchantApplyment a, String businessCode,
                                                                  String serialNo) {
        return PartnerApplymentApiService.SubmitPayload.builder()
                .businessCode(businessCode)
                .certSerialNo(serialNo)
                .subjectType(subjectType(a.getOrganizationType()))
                .financeInstitution(Boolean.TRUE.equals(a.getFinanceInstitution()))
                .licenseCopyMedia(a.getBusinessLicenseMedia())
                .licenseNumber(a.getBusinessLicenseNumber())
                .merchantName(a.getMerchantName())
                .legalPerson(a.getLegalPerson())
                .licenseAddress(a.getLicenseAddress())
                .licensePeriodBegin(a.getLicensePeriodBegin())
                .licensePeriodEnd(a.getLicensePeriodEnd())
                .idCardCopyMedia(a.getIdCardCopyMedia())
                .idCardNationalMedia(a.getIdCardNationalMedia())
                .idCardNameEnc(a.getIdCardNameEnc())
                .idCardNumberEnc(a.getIdCardNumberEnc())
                .idCardAddressEnc(a.getIdCardAddressEnc())
                .idCardPeriodBegin(a.getIdCardPeriodBegin())
                .idCardPeriodEnd(a.getIdCardPeriodEnd())
                .uboInfoList(parseUboJson(a.getUboJson()))
                .merchantShortname(a.getMerchantShortname())
                .servicePhone(a.getServicePhone())
                .salesScenesType(a.getSalesScenesType())
                .salesAppId(a.getSalesAppId())
                .salesSubAppId(a.getSalesSubAppId())
                .salesPics(splitCsv(a.getSalesPics()))
                .bizStoreName(a.getBizStoreName())
                .bizAddressCode(a.getBizAddressCode())
                .bizStoreAddress(a.getBizStoreAddress())
                .storeEntrancePics(splitCsv(a.getStoreEntrancePics()))
                .storeIndoorPics(splitCsv(a.getStoreIndoorPics()))
                .settlementId(StringUtils.hasText(a.getSettlementId())
                        ? a.getSettlementId() : effectiveSettlementId(a.getOrganizationType()))
                .qualificationType(StringUtils.hasText(a.getQualificationType())
                        ? a.getQualificationType() : effectiveDefaultIndustry())
                .qualifications(splitCsv(a.getQualifications()))
                .bankAccountType(a.getBankAccountType())
                .accountNameEnc(a.getBankAccountNameEnc())
                .accountBank(a.getAccountBank())
                .bankBranchId(a.getBankBranchId())
                .bankName(a.getBankName())
                .accountNumberEnc(a.getBankAccountNumberEnc())
                .contactType(a.getContactType())
                .contactNameEnc(encrypt(a.getContactName(), serialNo))
                .contactMobileEnc(encrypt(a.getContactMobile(), serialNo))
                .contactEmailEnc(a.getContactEmailEnc())
                .build();
    }

    private static String subjectType(String organizationType) {
        return "INDIVIDUAL".equals(organizationType)
                ? "SUBJECT_TYPE_INDIVIDUAL" : "SUBJECT_TYPE_ENTERPRISE";
    }

    void validateRequest(ApplymentSubmitRequest request) {
        if (applymentProps.isFinanceInstitution()) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "本平台暂不受理金融机构进件（缺少金融机构类型与许可证字段），请将 wechat.applyment.finance-institution 置为 false");
        }
        if (!SUBJECT_TYPES.contains(request.getOrganizationType())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "主体类型仅支持 ENTERPRISE(企业)/INDIVIDUAL(个体工商户)；小微进件需单独开通权限");
        }
        if (!CONTACT_TYPES.contains(request.getContactType())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "当前版本仅支持法定代表人本人作为超级管理员（contactType=LEGAL）");
        }
        if (!SUPPORTED_SALES_SCENES.contains(request.getSalesScenesType())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "经营场景仅支持线下门店(SALES_SCENES_STORE)/小程序(SALES_SCENES_MINI_PROGRAM)/公众号(SALES_SCENES_MP)");
        }
        if (!Set.of("BANK_ACCOUNT_TYPE_CORPORATE", "BANK_ACCOUNT_TYPE_PERSONAL")
                .contains(request.getBankAccountType())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "结算账户类型必须为 BANK_ACCOUNT_TYPE_CORPORATE/BANK_ACCOUNT_TYPE_PERSONAL");
        }
        boolean corporateAccount = "BANK_ACCOUNT_TYPE_CORPORATE".equals(request.getBankAccountType());
        if ("ENTERPRISE".equals(request.getOrganizationType()) && !corporateAccount) {
            throw new BizException(ErrorCode.PARAM_INVALID, "企业主体只能使用对公银行账户");
        }
        // 官方《提交申请单》：对公开户名必须与执照商户名称一致；个人卡必须与经营者/法人姓名一致
        String expectedAccountName = corporateAccount ? request.getMerchantName() : request.getLegalPerson();
        if (!sameName(request.getBankAccountName(), expectedAccountName)) {
            throw new BizException(ErrorCode.PARAM_INVALID, corporateAccount
                    ? "对公账户开户名必须与营业执照上的商户名称一致"
                    : "个人银行卡开户名必须与经营者/法定代表人姓名一致");
        }
        if ("LEGAL".equals(request.getContactType())
                && !sameName(request.getContactName(), request.getLegalPerson())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "超级管理员为法定代表人时，姓名必须与营业执照法人姓名一致");
        }
        if ("ENTERPRISE".equals(request.getOrganizationType())
                && !StringUtils.hasText(request.getIdCardAddress())) {
            throw new BizException(ErrorCode.PARAM_MISSING, "企业主体必须填写身份证居住地址");
        }
        // 官方：企业若仅有法人一名最终受益人可留空（微信自动回填）；填写多条时必须完整
        if (request.getUboList() != null && !request.getUboList().isEmpty()) {
            if (request.getUboList().size() > 4) {
                throw new BizException(ErrorCode.PARAM_INVALID, "最终受益人最多填写4人");
            }
            for (UboInfoRequest ubo : request.getUboList()) {
                if (ubo == null
                        || !StringUtils.hasText(ubo.getIdDocCopy())
                        || !StringUtils.hasText(ubo.getName())
                        || !StringUtils.hasText(ubo.getNumber())
                        || !StringUtils.hasText(ubo.getAddress())
                        || !StringUtils.hasText(ubo.getPeriodBegin())
                        || !StringUtils.hasText(ubo.getPeriodEnd())) {
                    throw new BizException(ErrorCode.PARAM_MISSING,
                            "最终受益人需填写证件正面照片、姓名、证件号、居住地址与有效期");
                }
                if (StringUtils.hasText(ubo.getIdDocType())
                        && !"IDENTIFICATION_TYPE_IDCARD".equals(ubo.getIdDocType())) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "当前仅支持身份证类型的最终受益人");
                }
            }
        }
        if ("SALES_SCENES_STORE".equals(request.getSalesScenesType())) {
            if (!StringUtils.hasText(request.getBizStoreName())
                    || !StringUtils.hasText(request.getBizAddressCode())
                    || !StringUtils.hasText(request.getBizStoreAddress())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "门店场景必须填写门店名称、省市编码与详细地址");
            }
            if (isBlank(request.getStoreEntrancePics()) || isBlank(request.getStoreIndoorPics())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "门店场景必须上传门头照片与店内照片");
            }
        } else if ("SALES_SCENES_MINI_PROGRAM".equals(request.getSalesScenesType())) {
            // 商家通常没有自己的小程序；平台小程序 AppID（wechat.pay.app-id）可直接作为服务商 AppID
            if (!StringUtils.hasText(request.getSalesAppId())
                    && !StringUtils.hasText(request.getSalesSubAppId())
                    && !StringUtils.hasText(wechatPayProperties.getAppId())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "小程序场景必须填写 AppID");
            }
        } else if ("SALES_SCENES_MP".equals(request.getSalesScenesType())) {
            if (!StringUtils.hasText(request.getSalesAppId())
                    && !StringUtils.hasText(request.getSalesSubAppId())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "公众号场景必须填写 AppID");
            }
            if (isBlank(request.getSalesPics())) {
                throw new BizException(ErrorCode.PARAM_MISSING, "公众号场景必须上传页面截图");
            }
        }
        String industry = effectiveIndustry(request);
        if (PartnerApplymentRules.SPECIAL_RULE_INDUSTRIES.contains(industry)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "行业「" + industry + "」适用 0.3% 专项结算规则，本平台不代受理，请联系运营");
        }
        if (!PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains(industry)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "暂不支持行业「" + industry + "」，可选：" + PartnerApplymentRules.SUPPORTED_INDUSTRIES);
        }
        if (PartnerApplymentRules.NEEDS_QUALIFICATION.contains(industry)
                && isBlank(request.getQualifications())) {
            throw new BizException(ErrorCode.PARAM_MISSING,
                    "行业「" + industry + "」需上传收费授权证明等特殊资质");
        }
    }

    /** 入驻结算规则 ID：平台配置 → 按主体类型自动取 716/719（商家不可指定）。 */
    String effectiveSettlementId(String organizationType) {
        if (StringUtils.hasText(applymentProps.getSettlementId())) {
            return applymentProps.getSettlementId();
        }
        return PartnerApplymentRules.settlementIdFor(organizationType);
    }

    private String effectiveDefaultIndustry() {
        return StringUtils.hasText(applymentProps.getQualificationType())
                ? applymentProps.getQualificationType() : PartnerApplymentRules.DEFAULT_INDUSTRY;
    }

    /** 所属行业：请求 → 平台配置 → 默认「居民生活服务」。 */
    private String effectiveIndustry(ApplymentSubmitRequest request) {
        return StringUtils.hasText(request.getQualificationType())
                ? request.getQualificationType() : effectiveDefaultIndustry();
    }

    /** 小程序场景未填 AppID 时，默认使用平台小程序 AppID（服务商侧）。 */
    String resolveSalesAppId(ApplymentSubmitRequest request) {
        if (StringUtils.hasText(request.getSalesAppId())) {
            return request.getSalesAppId();
        }
        if ("SALES_SCENES_MINI_PROGRAM".equals(request.getSalesScenesType())
                && !StringUtils.hasText(request.getSalesSubAppId())
                && StringUtils.hasText(wechatPayProperties.getAppId())) {
            return wechatPayProperties.getAppId();
        }
        return null;
    }

    private static boolean sameName(String a, String b) {
        return a != null && b != null && a.trim().equals(b.trim());
    }

    private static boolean hasImageExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return false;
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return IMAGE_EXTENSIONS.contains(ext);
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
        // 组长身份由 activity_leader_identities 表判定（role 归一为 resident）
        String role = roleGuard.normalizeRole(UserContext.currentRole());
        boolean activityLeader = "resident".equals(role) && roleGuard.isActivityLeader();
        if ("merchant".equals(role) || "activity_leader".equals(role)
                || "technician".equals(role) || activityLeader) {
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

    /** UBO 列表加密为 JSON 存储：姓名/号码/地址为密文，其余为明文。 */
    private String encryptUboJson(List<UboInfoRequest> uboList, String serialNo) {
        if (uboList == null || uboList.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (UboInfoRequest ubo : uboList) {
            LinkedHashMap<String, Object> item = new LinkedHashMap<>();
            item.put("ubo_id_doc_type", StringUtils.hasText(ubo.getIdDocType())
                    ? ubo.getIdDocType() : "IDENTIFICATION_TYPE_IDCARD");
            item.put("ubo_id_doc_copy", ubo.getIdDocCopy());
            if (StringUtils.hasText(ubo.getIdDocCopyBack())) {
                item.put("ubo_id_doc_copy_back", ubo.getIdDocCopyBack());
            }
            item.put("ubo_id_doc_name", encrypt(ubo.getName(), serialNo));
            item.put("ubo_id_doc_number", encrypt(ubo.getNumber(), serialNo));
            item.put("ubo_id_doc_address", encrypt(ubo.getAddress(), serialNo));
            item.put("ubo_period_begin", ubo.getPeriodBegin());
            item.put("ubo_period_end", ubo.getPeriodEnd());
            items.add(item);
        }
        return JSONUtil.toJsonStr(items);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> parseUboJson(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        JSONArray array = JSONUtil.parseArray(json);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object item : array) {
            list.add((Map<String, Object>) item);
        }
        return list;
    }

    private String encrypt(String plain, String serialNo) {
        if (!StringUtils.hasText(plain)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "进件必填的敏感字段不能为空");
        }
        PublicKey cert = certManager.getCertificate(serialNo);
        if (cert == null) {
            throw new BizException(ErrorCode.WECHAT_CERT_NOT_READY);
        }
        return WechatCrypto.encryptOaep(cert, plain);
    }

    /**
     * 取加密公钥标识；未配置时 fail-closed。
     */
    private String requireCertSerial() {
        String serial = certManager.anySerial();
        if (!StringUtils.hasText(serial)) {
            throw new BizException(ErrorCode.WECHAT_CERT_NOT_READY,
                    "微信支付公钥/平台证书未就绪（请确认 wechat.pay 公钥配置），无法加密进件资料");
        }
        return serial;
    }

    /**
     * 进件可用性：只校验进件实际需要的凭据（服务商号 + 私钥 + 证书序列号 + 开关）。
     * 不要求 apiV3Key/notifyUrl——它们用于支付回调，进件阶段配置不全不应拦截商家填资料。
     */
    public boolean isApplymentReady() {
        if (!applymentProps.isEnabled()
                || !wechatPayProperties.isEnabled()
                || !StringUtils.hasText(wechatPayProperties.getMchId())
                || !(StringUtils.hasText(wechatPayProperties.getPrivateKeyBase64())
                    || StringUtils.hasText(wechatPayProperties.getPrivateKeyPath()))
                || !StringUtils.hasText(wechatPayProperties.getMerchantSerialNumber())) {
            return false;
        }
        // 公钥模式用本地公钥加密；平台证书模式必须先能解密 /v3/certificates，故要求 apiV3Key
        return wechatPayProperties.isPublicKeyMode()
                || (StringUtils.hasText(wechatPayProperties.getApiV3Key())
                    && wechatPayProperties.getApiV3Key().length() == 32);
    }

    private void requireApplymentReady() {
        if (!applymentProps.isEnabled()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商家自助进件功能未开启");
        }
        if (!isApplymentReady()) {
            throw new BizException(ErrorCode.WECHAT_APPLYMENT_NOT_CONFIGURED);
        }
    }

    private static String extractRejectReason(JSONObject wx) {
        if (StringUtils.hasText(wx.getStr("reject_reason"))) {
            return wx.getStr("reject_reason");
        }
        JSONArray detail = wx.getJSONArray("audit_detail");
        if (detail == null || detail.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object item : detail) {
            JSONObject d = (JSONObject) item;
            // 官方《查询申请状态》audit_detail 字段名为 field/field_name；
            // param_name 是旧版/其他产品的叫法，保留兜底。
            String field = d.getStr("field_name");
            if (!StringUtils.hasText(field)) {
                field = d.getStr("field");
            }
            if (!StringUtils.hasText(field)) {
                field = d.getStr("param_name");
            }
            String reason = d.getStr("reject_reason");
            sb.append(field == null ? "" : field)
                    .append(':').append(reason == null ? "" : reason).append(';');
        }
        return sb.length() > 500 ? sb.substring(0, 500) : sb.toString();
    }

    private static String csv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }

    private static List<String> splitCsv(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of();
        }
        return List.of(csv.split(","));
    }

    private static boolean isBlank(List<String> values) {
        return values == null || values.isEmpty();
    }

    private static int inc(Integer n) {
        return (n == null ? 0 : n) + 1;
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    private static ApplymentDetailResponse toResponse(MerchantApplyment a, boolean withAdminDiagnostics) {
        ApplymentDetailResponse.ApplymentDetailResponseBuilder builder = ApplymentDetailResponse.builder()
                .merchantId(a.getMerchantId())
                .applymentId(a.getApplymentId())
                .applymentState(a.getApplymentState())
                .wxState(a.getWxState())
                .organizationType(a.getOrganizationType())
                .merchantShortname(a.getMerchantShortname())
                .servicePhone(a.getServicePhone())
                .qualificationType(a.getQualificationType())
                .salesScenesType(a.getSalesScenesType())
                .salesAppId(a.getSalesAppId())
                .salesSubAppId(a.getSalesSubAppId())
                .salesPics(splitCsv(a.getSalesPics()))
                .bizStoreName(a.getBizStoreName())
                .bizAddressCode(a.getBizAddressCode())
                .bizStoreAddress(a.getBizStoreAddress())
                .storeEntrancePics(splitCsv(a.getStoreEntrancePics()))
                .storeIndoorPics(splitCsv(a.getStoreIndoorPics()))
                .businessLicenseNumber(a.getBusinessLicenseNumber())
                .merchantName(a.getMerchantName())
                .legalPerson(a.getLegalPerson())
                .licenseAddress(a.getLicenseAddress())
                .licensePeriodBegin(a.getLicensePeriodBegin())
                .licensePeriodEnd(a.getLicensePeriodEnd())
                .idCardPeriodBegin(a.getIdCardPeriodBegin())
                .idCardPeriodEnd(a.getIdCardPeriodEnd())
                .qualifications(splitCsv(a.getQualifications()))
                .contactMobile(maskMobile(a.getContactMobile()))
                .contactType(a.getContactType())
                .contactName(a.getContactName())
                .bankAccountType(a.getBankAccountType())
                .accountBank(a.getAccountBank())
                .bankAliasCode(a.getBankAliasCode())
                .bankBranchId(a.getBankBranchId())
                .bankName(a.getBankName())
                .submitted(a.getApplymentId() != null)
                .signUrl(a.getSignUrl())
                .rejectReason(a.getRejectReason())
                .subMchid(a.getSubMchid())
                .submitMode(a.getSubmitMode());
        // 微信原始报错含商户号/接口细节，只给运营后台；商家侧看 rejectReason 就够了。
        if (withAdminDiagnostics) {
            builder.lastError(a.getLastError()).retryCount(a.getRetryCount());
        }
        return builder.build();
    }

    private static String maskMobile(String mobile) {
        if (!StringUtils.hasText(mobile) || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
