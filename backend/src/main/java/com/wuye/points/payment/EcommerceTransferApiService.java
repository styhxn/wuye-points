package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信「商家转账（新版）」API 封装（/{@code v3/fund-app/mch-transfer/*}，免确认授权模式）。
 *
 * <p>四层分账树（物业/统筹/板块/个体）在平台内部钱包结算后，通过本服务向角色
 * 「微信零钱 / 银行卡」出款。场景报备为「佣金报酬」（transfer_scene_id=1005），
 * 需在微信商户平台开通商家转账产品并报备场景后方可调用。</p>
 *
 * <p>零钱渠道：{@code pre-transfer-with-authorization}（发起转账并完成免确认收款授权），
 * 首次向某 openid 转账前需先 {@code user-confirm-authorization} 取得用户免确认授权；
 * 银行卡渠道：{@code to-bank}（bank_type/账号/户名，户名与账号需平台证书 OAEP 加密）。
 * 两渠道查询单共用 {@code out-bill-no/{out_bill_no}}。字段以微信官方 2026 文档联调对齐（占位）。</p>
 */
@Slf4j
@Service
public class EcommerceTransferApiService {

    /** 佣金报酬场景 ID（需报备）。 */
    public static final String SCENE_COMMISSION = "1005";

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatCertManager certManager;

    public EcommerceTransferApiService(WechatHttpClient httpClient, WechatPayProperties props,
                                       EcommerceSplitProperties ecommerceProps, WechatCertManager certManager) {
        this.httpClient = httpClient;
        this.props = props;
        this.ecommerceProps = ecommerceProps;
        this.certManager = certManager;
    }

    /**
     * 发起免确认收款授权：POST /v3/fund-app/mch-transfer/user-confirm-authorization。
     *
     * <p>返回授权单号 authorization_id；若微信侧返回「无需授权/已授权」则返回 null，
     * 由调用方直接继续转账。</p>
     */
    public String requestUserAuthorization(String openid, String outRequestNo) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("out_request_no", outRequestNo);
        body.put("openid", openid);
        body.put("transfer_scene_id", SCENE_COMMISSION);
        body.put("transfer_scene_report_infos", sceneReport("佣金报酬"));
        String resp = httpClient.post(props.getGateway() + "/v3/fund-app/mch-transfer/user-confirm-authorization", body);
        return parseField(resp, "authorization_id");
    }

    /**
     * 发起零钱转账（含免确认收款授权）：POST /v3/fund-app/mch-transfer/transfer-bills/pre-transfer-with-authorization。
     *
     * @return 微信转账单号 transfer_bill_id
     */
    public String transferToZero(String outBillNo, String openid, int amountFen, String remark,
                                 String userNameEnc) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("out_bill_no", outBillNo);
        body.put("transfer_scene_id", SCENE_COMMISSION);
        body.put("openid", openid);
        body.put("transfer_scene_report_infos", sceneReport(remark));
        if (StringUtils.hasText(userNameEnc)) {
            body.put("user_name", userNameEnc); // 实名校验（可选，需加密）
        }
        body.put("amount", amount(amountFen));
        body.put("transfer_remark", truncate(remark, 32));
        if (StringUtils.hasText(ecommerceProps.getTransferNotifyUrl())) {
            body.put("notify_url", ecommerceProps.getTransferNotifyUrl());
        }
        String resp = httpClient.post(props.getGateway()
                + "/v3/fund-app/mch-transfer/transfer-bills/pre-transfer-with-authorization", body);
        return parseField(resp, "transfer_bill_id");
    }

    /**
     * 发起银行卡转账：POST /v3/fund-app/mch-transfer/transfer-bills/to-bank。
     * 账号/户名必须用平台证书 OAEP 加密（字段以微信官方文档联调对齐）。
     *
     * @param serialNo        加密所用平台证书序列号（对应 {@code Wechatpay-Serial} 请求头，须与 accountNumberEnc/accountNameEnc 加密时一致）
     * @return 微信转账单号 transfer_bill_id
     */
    public String transferToBank(String outBillNo, String serialNo, String bankType, String accountNumberEnc,
                                 String accountNameEnc, int amountFen, String remark) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("out_bill_no", outBillNo);
        body.put("transfer_scene_id", SCENE_COMMISSION);
        body.put("bank_type", bankType);
        body.put("account_number", accountNumberEnc);
        body.put("account_name", accountNameEnc);
        body.put("transfer_scene_report_infos", sceneReport(remark));
        body.put("amount", amount(amountFen));
        body.put("transfer_remark", truncate(remark, 32));
        if (StringUtils.hasText(ecommerceProps.getTransferNotifyUrl())) {
            body.put("notify_url", ecommerceProps.getTransferNotifyUrl());
        }
        // 敏感字段用平台证书加密，须携带加密所用证书序列号头（证书轮换后旧密文不可用）
        Map<String, String> headers = new LinkedHashMap<>();
        if (StringUtils.hasText(serialNo)) {
            headers.put("Wechatpay-Serial", serialNo);
        } else {
            headers.put("Wechatpay-Serial", certManager.anySerial());
        }
        String resp = httpClient.post(props.getGateway()
                + "/v3/fund-app/mch-transfer/transfer-bills/to-bank", body, headers);
        return parseField(resp, "transfer_bill_id");
    }

    /**
     * 查询转账单：GET /v3/fund-app/mch-transfer/transfer-bills/out-bill-no/{out_bill_no}。
     *
     * <p>返回 state（WAIT_PAY/SENDING/SUCCESS/FAIL/CANCELING 等）、fail_reason、payer_total_amount。</p>
     */
    public JSONObject queryTransfer(String outBillNo) {
        String resp = httpClient.get(props.getGateway()
                + "/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/" + outBillNo);
        return JSONUtil.parseObj(resp);
    }

    private static Map<String, Object> amount(int fen) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total_amount", fen);
        m.put("currency", "CNY");
        return m;
    }

    private static List<Map<String, Object>> sceneReport(String infoContent) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("info_type", "佣金");
        info.put("info_content", truncate(infoContent, 64));
        return List.of(info);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static String parseField(String resp, String key) {
        try {
            JSONObject obj = JSONUtil.parseObj(resp);
            return obj.getStr(key);
        } catch (Exception e) {
            return null;
        }
    }
}
