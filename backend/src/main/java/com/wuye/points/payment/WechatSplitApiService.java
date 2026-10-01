package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信官方「普通分账 / profit sharing」API 封装（/{@code v3/profitsharing/*}）。
 *
 * <p>与已废弃的 CBK / 扫呗（LCSW）聚合支付无关——这里只调微信官方分账接口：
 * 6.2 冻结分账 (/{@code profitsharing/orders})、6.12 完结解冻 (/{@code profitsharing/orders/unfreeze})、
 * 6.9 分账退回 (/{@code profitsharing/return-orders})。接收方类型为 MERCHANT_ID / PERSONAL_OPENID，
 * 不使用电商收付通的 sub_mchid 进件模式。
 *
 * <p>归集户余额查询 / 提现（applycash）为待对接占位，未接真实参数前安全返回 null / 抛异常由调用方兜底。</p>
 */
@Slf4j
@Service
public class WechatSplitApiService {

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;
    private final WechatCertManager certManager;

    public WechatSplitApiService(WechatHttpClient httpClient, WechatPayProperties props, WechatCertManager certManager) {
        this.httpClient = httpClient;
        this.props = props;
        this.certManager = certManager;
    }

    /** 是否已具备发起真实微信分账调用的最低配置。 */
    public boolean isEnabled() {
        return props.isConfigured();
    }

    /** 元 → 分（字符串，无小数）。 */
    public static String fen(BigDecimal yuan) {
        if (yuan == null) {
            return "0";
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 6.2 冻结分账：POST /v3/profitsharing/orders。
     * <p>微信官方要求 transaction_id 必须为「微信支付订单号」（支付回调/查单返回的 transaction_id），
     * 商户订单号(out_trade_no)传入会被拒绝。</p>
     *
     * @return 微信分账交易号 sub_trade_no（解析失败返回 null）
     */
    public String freezeAllocate(String splitNo, String transactionId, String payerAccount,
                                 String payeeAccount, BigDecimal freezeAmount, String description, String receiverName) {
        LinkedHashMap<String, Object> receiver = new LinkedHashMap<>();
        receiver.put("type", resolveReceiverType(payeeAccount));
        receiver.put("account", payeeAccount);
        String encName = encryptReceiverName(receiverName);
        if (encName != null) {
            receiver.put("name", encName);
        }
        receiver.put("amount", Integer.parseInt(fen(freezeAmount)));
        receiver.put("description", description);

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("transaction_id", transactionId);
        body.put("out_order_no", splitNo);
        body.put("unfreeze_unsplit", false);
        body.put("receivers", List.of(receiver));

        String resp = httpClient.post(props.getGateway() + "/v3/profitsharing/orders", body);
        return parseField(resp, "sub_trade_no");
    }

    /**
     * 6.12 完结解冻（将未分账资金解冻给原商户）：POST /v3/profitsharing/orders/unfreeze。
     * transactionId 同 {@link #freezeAllocate}，必须为微信支付订单号。
     */
    public void finishOrder(String splitNo, String transactionId, String subTradeNo) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("transaction_id", transactionId);
        body.put("out_order_no", splitNo);
        body.put("description", "售后窗口到期，完结划账解冻剩余资金");
        httpClient.post(props.getGateway() + "/v3/profitsharing/orders/unfreeze", body);
    }

    /**
     * 6.9 分账退回（订单取消 / 退款时，物业分成从物业退回给商户）：POST /v3/profitsharing/return-orders。
     * <p>微信官方要求 order_id 必须为「微信支付订单号」，同 {@link #freezeAllocate}。</p>
     */
    public String reverseRefund(String splitNo, String transactionId, String subTradeNo,
                               String receiverAccount, BigDecimal amount) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("order_id", transactionId);
        body.put("out_order_no", splitNo);
        body.put("out_return_no", splitNo + "_RET");
        body.put("return_account_type", resolveReceiverType(receiverAccount));
        body.put("amount", Integer.parseInt(fen(amount)));
        body.put("description", "订单取消/退款，物业分成退回商户");
        String resp = httpClient.post(props.getGateway() + "/v3/profitsharing/return-orders", body);
        return parseField(resp, "out_return_no");
    }

    /**
     * 归集户余额查询（HTML ③-b）。真实接口待对接，未接时返回 null（不误报）。
     */
    public BigDecimal queryCollectionBalance() {
        log.info("[微信监控] 归集户余额查询未对接（WechatSplitApiService.queryCollectionBalance 待实现），返回 null");
        return null;
    }

    /**
     * 微信提现（applycash）。真实字段（接收方结算卡 / 微信账户、cash_type 等）待微信提供参数与测试环境后补全，
     * 当前抛异常由 {@code AutoWithdrawalService} 捕获并回退人工凭证流程。
     */
    public Map<String, Object> applyCash(Map<String, Object> biz) {
        throw new WechatPayException("WECHAT_APICASH_NOT_IMPL",
                "微信提现(applycash)待对接真实参数，暂不可用", JSONUtil.toJsonStr(biz));
    }

    private static String resolveReceiverType(String account) {
        if (account == null) {
            return "PERSONAL_OPENID";
        }
        String a = account.trim();
        // 纯数字且长度近似商户号 → 商户号；否则按 openid 处理
        if (a.matches("\\d{8,20}")) {
            return "MERCHANT_ID";
        }
        return "PERSONAL_OPENID";
    }

    private String encryptReceiverName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        try {
            String serial = certManager.anySerial();
            if (serial == null) {
                return null;
            }
            PublicKey cert = certManager.getCertificate(serial);
            if (cert == null) {
                return null;
            }
            return WechatCrypto.encryptOaep(cert, name);
        } catch (Exception e) {
            log.warn("[微信分账] receiver_name 加密失败（占位阶段可忽略）: {}", e.getMessage());
            return null;
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
}
