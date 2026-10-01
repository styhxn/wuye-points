package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信电商收付通「商家进件」API 封装（/{@code v3/ecommerce/applyments/*} 与媒体上传）。
 *
 * <p>商家（订单收款方）在平台提交资料后，由平台（服务商）调用本服务向微信申请「二级商户号」：
 * 营业执照/身份证照片先经 {@code /v3/merchant/media/upload} 转 media_id，随后提交进件申请，
 * 微信审核 → 法人扫码核验 → 签约后返回 sub_mchid。敏感字段（证件号/账号/姓名）须以平台证书
 * RSA-OAEP 加密（密文与 {@code Wechatpay-Serial} 头同传），详见 {@link WechatCrypto#encryptOaep}。</p>
 *
 * <p>本封装仅负责 HTTP 报文与签名；资料校验、状态机流转、sub_mchid 回填在
 * {@code EcommerceApplymentService}。经营类目/特殊资质等字段以微信官方最新文档联调对齐（占位）。</p>
 */
@Slf4j
@Service
public class EcommerceApplymentApiService {

    /** 进件敏感字段加密时使用的 HTTP 头：标识所用平台证书序列号。 */
    public static final String HEADER_WECHATPAY_SERIAL = "Wechatpay-Serial";

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;

    public EcommerceApplymentApiService(WechatHttpClient httpClient, WechatPayProperties props) {
        this.httpClient = httpClient;
        this.props = props;
    }

    /**
     * 上传进件资料图片：POST /v3/merchant/media/upload（multipart）。
     *
     * @return 微信返回的 media_id（有效期 3 天，需及时提交进件）
     */
    public String uploadMedia(byte[] fileBytes, String fileName) {
        String resp = httpClient.postMultipart(props.getGateway() + "/v3/merchant/media/upload", fileName, fileBytes);
        return parseField(resp, "media_id");
    }

    /**
     * 提交进件申请：POST /v3/ecommerce/applyments/。
     *
     * @param payload 已加密的进件资料（敏感字段在业务层用平台证书加密后传入）
     * @return 微信进件申请单号 applyment_id
     */
    public String submitApplyment(SubmitPayload payload) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("out_request_no", payload.getOutRequestNo());
        body.put("appid", props.getAppId());
        body.put("contact", contact(payload));
        body.put("subject_info", subjectInfo(payload));

        String resp = httpClient.post(props.getGateway() + "/v3/ecommerce/applyments/", body,
                Map.of(HEADER_WECHATPAY_SERIAL, payload.getCertSerialNo()));
        return parseField(resp, "applyment_id");
    }

    /**
     * 查询进件申请状态：GET /v3/ecommerce/applyments/{applyment_id}。
     *
     * <p>返回 JSON 含 applyment_state（微信侧状态）、sub_mchid、reject_reason、
     * legal_validation_url / sign_url（法人验证/签约链接）等。</p>
     */
    public JSONObject queryApplyment(String applymentId) {
        String resp = httpClient.get(props.getGateway() + "/v3/ecommerce/applyments/" + applymentId);
        return JSONUtil.parseObj(resp);
    }

    private Map<String, Object> contact(SubmitPayload p) {
        LinkedHashMap<String, Object> c = new LinkedHashMap<>();
        c.put("contact_name", p.getContactNameEnc());
        c.put("contact_id_number", p.getContactIdNumberEnc());
        c.put("mobile_phone", p.getContactMobile());
        if (StringUtils.hasText(p.getContactEmail())) {
            c.put("contact_email", p.getContactEmail());
        }
        return c;
    }

    private Map<String, Object> subjectInfo(SubmitPayload p) {
        LinkedHashMap<String, Object> info = new LinkedHashMap<>();
        info.put("subject_type", p.getSubjectType());

        // 营业执照主体（企业/个体户）：小微商户无营业执照，走辅助证明材料
        if (StringUtils.hasText(p.getLicenseMediaId())) {
            LinkedHashMap<String, Object> license = new LinkedHashMap<>();
            license.put("license_copy", p.getLicenseMediaId());
            license.put("license_number", p.getLicenseNumberEnc());
            license.put("merchant_name", p.getMerchantNameEnc());
            license.put("legal_person", p.getLegalPersonEnc());
            if (StringUtils.hasText(p.getLicenseValidDate())) {
                license.put("license_period_type", "LICENSE_PERIOD_VALID_TO");
                license.put("license_valid_date", p.getLicenseValidDate());
            } else {
                license.put("license_period_type", "LICENSE_PERIOD_LONG_TERM");
            }
            info.put("business_license_info", license);
        }

        // 经营者/法人身份信息
        LinkedHashMap<String, Object> idCard = new LinkedHashMap<>();
        idCard.put("id_card_copy", p.getIdCardCopyMedia());
        idCard.put("id_card_national", p.getIdCardNationalMedia());
        idCard.put("id_card_name", p.getIdCardNameEnc());
        idCard.put("id_card_number", p.getIdCardNumberEnc());
        idCard.put("card_period_type", "CARD_PERIOD_LONG_TERM");
        LinkedHashMap<String, Object> identity = new LinkedHashMap<>();
        identity.put("id_doc_type", "IDENTIFICATION_TYPE_IDCARD");
        identity.put("id_card_info", idCard);
        identity.put("owner", p.isOwner());
        info.put("identity_info", identity);

        // 结算账户
        LinkedHashMap<String, Object> settlement = new LinkedHashMap<>();
        settlement.put("settlement_bank_type", p.getBankAccountType());
        settlement.put("account_bank", p.getAccountBank());
        settlement.put("bank_name", p.getBankName());
        settlement.put("account_name", p.getAccountNameEnc());
        settlement.put("account_number", p.getAccountNumberEnc());
        info.put("settlement_info", settlement);

        // 小微商户辅助证明材料（无营业执照时，门店/流动经营/线上交易三选一）
        if (p.getBusinessAdditionMedia() != null && !p.getBusinessAdditionMedia().isEmpty()) {
            LinkedHashMap<String, Object> addition = new LinkedHashMap<>();
            addition.put("business_addition_media", p.getBusinessAdditionMedia());
            info.put("addition_info", addition);
        }

        return info;
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
     * 进件提交报文（敏感字段均为业务层加密后的密文，本类不再接触明文）。
     */
    @Data
    @Builder
    public static class SubmitPayload {

        /** 平台业务单号（= merchant_applyments.id，幂等键） */
        private String outRequestNo;

        /** 加密所用平台证书序列号（Wechatpay-Serial 头） */
        private String certSerialNo;

        /** 主体类型：SUBJECT_TYPE_ENTERPRISE / SUBJECT_TYPE_INDIVIDUAL / SUBJECT_TYPE_MICRO */
        private String subjectType;

        private String contactNameEnc;
        private String contactIdNumberEnc;
        private String contactMobile;
        private String contactEmail;

        /** 营业执照 media_id（小微为空，走辅助证明） */
        private String licenseMediaId;
        private String licenseNumberEnc;
        private String merchantNameEnc;
        private String legalPersonEnc;
        /** 营业执照有效期截止（如 2026-12-31）；空则视为长期有效 */
        private String licenseValidDate;

        private String idCardCopyMedia;
        private String idCardNationalMedia;
        private String idCardNameEnc;
        private String idCardNumberEnc;
        /** 经营者/法人是否同为申请人（一般 true） */
        private boolean owner;

        private String bankAccountType;
        private String accountBank;
        private String bankName;
        private String accountNameEnc;
        private String accountNumberEnc;

        /** 小微商户辅助证明材料 media_id 列表（门店照片等，非小微为空） */
        private java.util.List<String> businessAdditionMedia;
    }
}
