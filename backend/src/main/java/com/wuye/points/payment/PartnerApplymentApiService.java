package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信支付「特约商户进件」API 封装（普通服务商，{@code /v3/applyment4sub/*}）。
 *
 * <p>官方文档：《提交申请单》{@code POST /v3/applyment4sub/applyment/}；
 * 《查询申请状态》{@code GET /v3/applyment4sub/applyment/business_code/{business_code}}；
 * 《文件上传》{@code POST /v3/merchant/media/upload}；
 * 银行下拉来自 {@code /v3/capital/capitallhh/banks/corporate-banking|personal-banking}
 * 与 {@code /v3/capital/capitallhh/banks/{bank_alias_code}/branches}。</p>
 *
 * <p>敏感字段（证件姓名/号码、开户名、银行账号、超管姓名/手机号/邮箱）必须用微信支付公钥或
 * 平台证书 RSA-OAEP 加密，并在请求头带 {@code Wechatpay-Serial}。本类只接收已加密的值，
 * 明文只在业务层短暂存在。</p>
 *
 * <p>注意：本类对应「普通服务商」文档族，不要与电商收付通 {@code /v3/ecommerce/applyments/}
 * 或品牌服务商 {@code /v3/brand/applyments} 混用。</p>
 */
@Slf4j
@Service
public class PartnerApplymentApiService {

    public static final String HEADER_WECHATPAY_SERIAL = "Wechatpay-Serial";

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;

    public PartnerApplymentApiService(WechatHttpClient httpClient, WechatPayProperties props) {
        this.httpClient = httpClient;
        this.props = props;
    }

    /**
     * 上传进件资料文件：POST /v3/merchant/media/upload（multipart）。
     *
     * @return media_id（官方说明有效期 3 天，需及时提交进件）
     */
    public String uploadMedia(byte[] fileBytes, String fileName) {
        String resp = httpClient.postMultipart(props.getGateway() + "/v3/merchant/media/upload",
                fileName, fileBytes);
        String mediaId = parseField(resp, "media_id");
        if (!StringUtils.hasText(mediaId)) {
            // 带上微信原文：否则上层只剩「未返回 media_id」，排障时无从判断微信到底回了什么
            throw new WechatPayException("MEDIA_ID_MISSING", "微信文件上传未返回 media_id", resp);
        }
        return mediaId;
    }

    /**
     * 提交申请单：POST /v3/applyment4sub/applyment/。
     *
     * @return 微信支付申请单号 applyment_id
     */
    public String submitApplyment(SubmitPayload payload) {
        Map<String, Object> body = buildBody(payload);
        String resp = httpClient.post(props.getGateway() + "/v3/applyment4sub/applyment/", body,
                Map.of(HEADER_WECHATPAY_SERIAL, payload.getCertSerialNo()));
        return parseField(resp, "applyment_id");
    }

    /**
     * 通过业务申请编号查询申请状态：GET /v3/applyment4sub/applyment/business_code/{business_code}。
     *
     * <p>应答含 applyment_id、sub_mchid（仅 FINISHED / TO_BE_SIGNED 时返回）、sign_url（超管签约链接）、
     * applyment_state、audit_detail（驳回明细）。</p>
     */
    public JSONObject queryApplyment(String businessCode) {
        String resp = httpClient.get(props.getGateway()
                + "/v3/applyment4sub/applyment/business_code/" + businessCode);
        return JSONUtil.parseObj(resp);
    }

    /**
     * 查询支持对公/个人业务的银行列表（进件「开户银行」下拉数据源）。
     *
     * @param bankAccountType BANK_ACCOUNT_TYPE_CORPORATE / BANK_ACCOUNT_TYPE_PERSONAL
     */
    public JSONObject listBanks(String bankAccountType) {
        String path = "BANK_ACCOUNT_TYPE_PERSONAL".equals(bankAccountType)
                ? "personal-banking" : "corporate-banking";
        String resp = httpClient.get(props.getGateway()
                + "/v3/capital/capitallhh/banks/" + path + "?offset=0&limit=100");
        return JSONUtil.parseObj(resp);
    }

    /**
     * 查询支行列表：GET /v3/capital/capitallhh/banks/{bank_alias_code}/branches。
     *
     * <p>官方要求 city_code 必填；支行较多（大城市可达数百条），必须支持 offset/limit 分页，
     * 否则前端只能拿到前 100 条，无法选择后面的支行。</p>
     *
     * @param bankAliasCode 银行别名编码（银行列表返回的 bank_alias_code）
     * @param cityCode      城市编码（省市对照表中的市一级编码）
     * @param offset        起始位置，从 0 开始
     * @param limit         本次最大返回条数
     */
    public JSONObject listBranches(String bankAliasCode, String cityCode, int offset, int limit) {
        String url = props.getGateway()
                + "/v3/capital/capitallhh/banks/" + bankAliasCode + "/branches"
                + "?city_code=" + cityCode
                + "&offset=" + offset
                + "&limit=" + limit;
        return JSONUtil.parseObj(httpClient.get(url));
    }

    private Map<String, Object> buildBody(SubmitPayload p) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("business_code", p.getBusinessCode());

        LinkedHashMap<String, Object> subject = new LinkedHashMap<>();
        subject.put("subject_type", p.getSubjectType());
        subject.put("finance_institution", p.isFinanceInstitution());

        LinkedHashMap<String, Object> license = new LinkedHashMap<>();
        license.put("license_copy", p.getLicenseCopyMedia());
        license.put("license_number", p.getLicenseNumber());
        license.put("merchant_name", p.getMerchantName());
        license.put("legal_person", p.getLegalPerson());
        putIfText(license, "license_address", p.getLicenseAddress());
        putIfText(license, "period_begin", p.getLicensePeriodBegin());
        putIfText(license, "period_end", p.getLicensePeriodEnd());
        subject.put("business_license_info", license);

        LinkedHashMap<String, Object> idCard = new LinkedHashMap<>();
        idCard.put("id_card_copy", p.getIdCardCopyMedia());
        idCard.put("id_card_national", p.getIdCardNationalMedia());
        idCard.put("id_card_name", p.getIdCardNameEnc());
        idCard.put("id_card_number", p.getIdCardNumberEnc());
        putIfText(idCard, "id_card_address", p.getIdCardAddressEnc());
        idCard.put("card_period_begin", p.getIdCardPeriodBegin());
        idCard.put("card_period_end", p.getIdCardPeriodEnd());
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("id_doc_type", "IDENTIFICATION_TYPE_IDCARD");
        identity.put("id_card_info", idCard);
        subject.put("identity_info", identity);

        if (p.getUboInfoList() != null && !p.getUboInfoList().isEmpty()) {
            subject.put("ubo_info_list", p.getUboInfoList());
        }
        body.put("subject_info", subject);

        LinkedHashMap<String, Object> business = new LinkedHashMap<>();
        business.put("merchant_shortname", p.getMerchantShortname());
        business.put("service_phone", p.getServicePhone());
        business.put("sales_info", buildSalesInfo(p));
        body.put("business_info", business);

        LinkedHashMap<String, Object> settlement = new LinkedHashMap<>();
        settlement.put("settlement_id", p.getSettlementId());
        settlement.put("qualification_type", p.getQualificationType());
        if (p.getQualifications() != null && !p.getQualifications().isEmpty()) {
            settlement.put("qualifications", p.getQualifications());
        }
        body.put("settlement_info", settlement);

        LinkedHashMap<String, Object> bank = new LinkedHashMap<>();
        bank.put("bank_account_type", p.getBankAccountType());
        bank.put("account_name", p.getAccountNameEnc());
        bank.put("account_bank", p.getAccountBank());
        putIfText(bank, "bank_branch_id", p.getBankBranchId());
        putIfText(bank, "bank_name", p.getBankName());
        bank.put("account_number", p.getAccountNumberEnc());
        body.put("bank_account_info", bank);

        LinkedHashMap<String, Object> contact = new LinkedHashMap<>();
        contact.put("contact_type", p.getContactType());
        contact.put("contact_name", p.getContactNameEnc());
        contact.put("mobile_phone", p.getContactMobileEnc());
        contact.put("contact_email", p.getContactEmailEnc());
        body.put("contact_info", contact);

        return body;
    }

    private Map<String, Object> buildSalesInfo(SubmitPayload p) {
        LinkedHashMap<String, Object> sales = new LinkedHashMap<>();
        sales.put("sales_scenes_type", List.of(p.getSalesScenesType()));
        if ("SALES_SCENES_STORE".equals(p.getSalesScenesType())) {
            LinkedHashMap<String, Object> store = new LinkedHashMap<>();
            store.put("biz_store_name", p.getBizStoreName());
            store.put("biz_address_code", p.getBizAddressCode());
            store.put("biz_store_address", p.getBizStoreAddress());
            store.put("store_entrance_pic", nullSafe(p.getStoreEntrancePics()));
            store.put("indoor_pic", nullSafe(p.getStoreIndoorPics()));
            putIfText(store, "biz_sub_appid", p.getSalesSubAppId());
            sales.put("biz_store_info", store);
        } else if ("SALES_SCENES_MINI_PROGRAM".equals(p.getSalesScenesType())) {
            LinkedHashMap<String, Object> mini = new LinkedHashMap<>();
            putIfText(mini, "mini_program_sub_appid", p.getSalesSubAppId());
            putIfText(mini, "mini_program_appid", p.getSalesAppId());
            if (p.getSalesPics() != null && !p.getSalesPics().isEmpty()) {
                mini.put("mini_program_pics", p.getSalesPics());
            }
            sales.put("mini_program_info", mini);
        } else if ("SALES_SCENES_MP".equals(p.getSalesScenesType())) {
            LinkedHashMap<String, Object> mp = new LinkedHashMap<>();
            putIfText(mp, "mp_sub_appid", p.getSalesSubAppId());
            putIfText(mp, "mp_appid", p.getSalesAppId());
            mp.put("mp_pics", nullSafe(p.getSalesPics()));
            sales.put("mp_info", mp);
        }
        return sales;
    }

    private static List<String> nullSafe(List<String> list) {
        return list == null ? List.of() : list;
    }

    private static void putIfText(Map<String, Object> map, String key, String value) {
        if (StringUtils.hasText(value)) {
            map.put(key, value);
        }
    }

    private static String parseField(String resp, String key) {
        try {
            JSONObject obj = JSONUtil.parseObj(resp);
            return obj.getStr(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提交申请单报文。敏感字段均为业务层加密后的密文（{@code *Enc}），本类不接触明文。
     */
    @Data
    @Builder
    public static class SubmitPayload {

        /** 业务申请编号（每次重新提交必须更换，微信侧同编号视为同一申请单） */
        private String businessCode;
        /** 加密所用公钥 ID / 平台证书序列号 */
        private String certSerialNo;

        /** SUBJECT_TYPE_ENTERPRISE / SUBJECT_TYPE_INDIVIDUAL */
        private String subjectType;
        private boolean financeInstitution;

        private String licenseCopyMedia;
        private String licenseNumber;
        private String merchantName;
        private String legalPerson;
        private String licenseAddress;
        private String licensePeriodBegin;
        private String licensePeriodEnd;

        private String idCardCopyMedia;
        private String idCardNationalMedia;
        private String idCardNameEnc;
        private String idCardNumberEnc;
        /** 身份证居住地址（已加密；官方《提交申请单》要求该字段 RSA 加密） */
        private String idCardAddressEnc;
        private String idCardPeriodBegin;
        private String idCardPeriodEnd;

        /** 最终受益人列表（企业/社会组织必填，字段值已加密） */
        private List<Map<String, Object>> uboInfoList;

        private String merchantShortname;
        private String servicePhone;
        private String salesScenesType;
        private String salesAppId;
        private String salesSubAppId;
        private List<String> salesPics;
        private String bizStoreName;
        private String bizAddressCode;
        private String bizStoreAddress;
        private List<String> storeEntrancePics;
        private List<String> storeIndoorPics;

        private String settlementId;
        private String qualificationType;
        private List<String> qualifications;

        private String bankAccountType;
        private String accountNameEnc;
        private String accountBank;
        private String bankBranchId;
        private String bankName;
        private String accountNumberEnc;

        /** LEGAL(法人/经营者) — 当前版本仅支持法人本人作为超级管理员 */
        private String contactType;
        private String contactNameEnc;
        private String contactMobileEnc;
        private String contactEmailEnc;
    }
}
