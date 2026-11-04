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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信官方「服务商分账 / profit sharing」API 封装（/{@code v3/profitsharing/*}）。
 *
 * <p>与已废弃的 CBK / 扫呗（LCSW）聚合支付无关——这里只调微信官方分账接口（普通服务商文档族
 * {@code /v3/profitsharing/*}，勿与电商收付通的 {@code /v3/ecommerce/profitsharing/*}
 * 及品牌服务商的 {@code /v3/brand/profitsharing/*} 混用）：
 * 请求分账 ({@code /orders})、解冻剩余资金 ({@code /orders/unfreeze})、
 * 分账回退 ({@code /return-orders})、查询分账结果 ({@code /orders/{out_order_no}})、
 * 查询剩余待分金额 ({@code /transactions/{transaction_id}/amounts})、
 * 添加分账接收方 ({@code /receivers/add})。接收方类型为 MERCHANT_ID / PERSONAL_OPENID，
 * 不使用电商收付通的 sub_mchid 进件模式。</p>
 *
 * <p><b>2026-10-17 按微信官方文档（合作伙伴文档中心·分账）校准的关键约定</b>：</p>
 * <ul>
 *   <li>资金冻结在<b>特约商户（子商户）号</b>内，服务商通过 {@code sub_mchid} 指定分账出资方；
 *       请求分账 / 解冻剩余资金 / 回退 / 查单 / 添加接收方<b>均须携带 sub_mchid</b>；</li>
 *   <li>请求分账应答的微信分账单号字段是 {@code order_id}（历史实现误取 sub_trade_no，
 *       导致回退时拿不到微信分账单号）;</li>
 *   <li>{@code appid} 在含 PERSONAL_OPENID 接收方时必填；敏感字段 name 需平台证书加密，
 *       且请求头须带 {@code Wechatpay-Serial} 指明加密所用证书序列号;</li>
 *   <li>「完结」的官方端点是<b>解冻剩余资金</b> {@code POST /v3/profitsharing/orders/unfreeze}
 *       （与电商收付通的 finish-order 不同），body 为 sub_mchid + transaction_id + out_order_no + description;</li>
 *   <li>分账回退 body 为 sub_mchid + order_id(微信分账单号，与商户分账单号二选一) + out_order_no
 *       + out_return_no + <b>return_mchid(原分账接收方账号)</b> + amount + description，
 *       且<b>仅支持 MERCHANT_ID 类型接收方回退</b>，个人 openid 不可回退。</li>
 * </ul>
 *
 * <p>归集户余额查询 / 提现（applycash）为待对接占位，未接真实参数前安全返回 null / 抛异常由调用方兜底。</p>
 */
@Slf4j
@Service
public class WechatSplitApiService {

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;
    private final WechatCertManager certManager;

    /** 已添加成功的分账接收方（subMchid|account → 本进程内跳过重复添加，重启后重新添加幂等）。 */
    private final Set<String> addedReceivers = ConcurrentHashMap.newKeySet();

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
     * 请求分账：POST /v3/profitsharing/orders。
     *
     * <p>微信官方要求 transaction_id 必须为「微信支付订单号」（支付回调/查单返回的 transaction_id），
     * 商户订单号(out_trade_no)传入会被拒绝。服务商模式下 sub_mchid 为分账出资方（收款商家子商户号），
     * 本平台服务商分账必须携带该字段。</p>
     *
     * @param subMchid 特约商户号（分账出资方，必填）
     * @return 微信分账单号 order_id（解析失败返回 null）
     */
    public String freezeAllocate(String splitNo, String transactionId, String subMchid, String payeeAccount,
                                 BigDecimal freezeAmount, String description, String receiverName) {
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
        if (StringUtils.hasText(subMchid)) {
            body.put("sub_mchid", subMchid);
        }
        if (StringUtils.hasText(props.getAppId())) {
            // 含 PERSONAL_OPENID 接收方时 appid 必填；MERCHANT_ID 场景带上亦无害
            body.put("appid", props.getAppId());
        }
        body.put("transaction_id", transactionId);
        body.put("out_order_no", splitNo);
        // false：剩余资金不立即解冻，保留后续分账/完结能力（售后窗口内按需再分）
        body.put("unfreeze_unsplit", false);
        body.put("receivers", List.of(receiver));

        String resp = httpClient.post(props.getGateway() + "/v3/profitsharing/orders", body,
                serialHeaders(encName));
        return parseField(resp, "order_id");
    }

    /**
     * 解冻剩余资金（售后窗口到期完结划账）：POST /v3/profitsharing/orders/unfreeze。
     * 将剩余未分账资金解冻回分账方（商家特约商户号）。
     *
     * @param subMchid 特约商户号（分账出资方，必填）
     */
    public void finishOrder(String splitNo, String transactionId, String subMchid) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(subMchid)) {
            body.put("sub_mchid", subMchid);
        }
        body.put("transaction_id", transactionId);
        body.put("out_order_no", splitNo);
        body.put("description", "售后窗口到期，完结划账解冻剩余资金");
        httpClient.post(props.getGateway() + "/v3/profitsharing/orders/unfreeze", body);
    }

    /**
     * 分账回退（订单取消 / 退款时，已分资金从接收方退回分账方）：POST /v3/profitsharing/return-orders。
     *
     * <p>微信官方约束：order_id 为「微信分账单号」（请求分账应答的 order_id，即本地 wechatSubTradeNo），
     * 与商户分账单号 out_order_no 二选一；return_mchid 为原分账接收方账号；
     * <b>仅 MERCHANT_ID 类型接收方支持回退</b>，个人 openid 由调用方走垫付+追回台账。</p>
     *
     * @param subMchid        特约商户号（分账出资方，必填）
     * @param wechatOrderId   微信分账单号（可为空，届时仅凭商户分账单号回退）
     * @param receiverAccount 原分账接收方账号（return_mchid）
     * @return 微信回退单号 return_id
     */
    public String reverseRefund(String splitNo, String transactionId, String subMchid, String wechatOrderId,
                                String receiverAccount, BigDecimal amount) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(subMchid)) {
            body.put("sub_mchid", subMchid);
        }
        // 微信文档：「微信分账单号和商户分账单号二选一填写」，同时传两个属参数错误
        if (StringUtils.hasText(wechatOrderId)) {
            body.put("order_id", wechatOrderId);
        } else {
            body.put("out_order_no", splitNo);
        }
        body.put("out_return_no", splitNo + "_RET");
        body.put("return_mchid", receiverAccount);
        body.put("amount", Integer.parseInt(fen(amount)));
        body.put("description", "订单取消/退款，分账退回");
        String resp = httpClient.post(props.getGateway() + "/v3/profitsharing/return-orders", body);
        return parseField(resp, "return_id");
    }

    /**
     * 查询分账结果：GET /v3/profitsharing/orders/{out_order_no}?sub_mchid=xxx&amp;transaction_id=xxx。
     *
     * <p>微信<b>不推送</b>普通分账结果异步通知（请求体亦不含 notify_url），本地 frozen 记录
     * 只能靠本接口定期查单校核。应答状态字段为 {@code status}（PROCESSING/FINISHED），
     * 各接收方结果在 receivers[].result。由 {@code SplitReconcileScheduler} 定时调用。</p>
     *
     * @param subMchid 特约商户号（分账出资方，必填）
     * @return 微信原始响应 JSON（含 state / receivers）；查询失败返回 null
     */
    public JSONObject querySplitResult(String splitNo, String transactionId, String subMchid) {
        try {
            StringBuilder url = new StringBuilder(props.getGateway() + "/v3/profitsharing/orders/" + splitNo + "?");
            if (StringUtils.hasText(subMchid)) {
                url.append("sub_mchid=").append(subMchid).append("&");
            }
            url.append("transaction_id=").append(transactionId);
            return JSONUtil.parseObj(httpClient.get(url.toString()));
        } catch (Exception e) {
            log.warn("[微信分账] 查询分账结果失败 splitNo={}: {}", splitNo, e.getMessage());
            return null;
        }
    }

    /**
     * 查询订单剩余待分金额：GET /v3/profitsharing/transactions/{transaction_id}/amounts。
     *
     * <p>官方文档：请求分账前应先查剩余可分金额；分账总额不能超过「订单金额减去手续费的净额」，
     * 而 30% 比例校验的基数是订单金额，两者取小才是安全上限。应答字段
     * {@code unsplit_amount}（整数，单位分）。查询失败返回 null，由调用方退回按订单金额校验。</p>
     *
     * @param transactionId 微信支付订单号
     * @return 剩余待分金额（分）；查询失败返回 null
     */
    public Long queryUnsplitAmount(String transactionId) {
        if (!StringUtils.hasText(transactionId)) {
            return null;
        }
        try {
            String resp = httpClient.get(props.getGateway()
                    + "/v3/profitsharing/transactions/" + transactionId + "/amounts");
            JSONObject obj = JSONUtil.parseObj(resp);
            if (obj.containsKey("unsplit_amount")) {
                return obj.getLong("unsplit_amount");
            }
            log.warn("[微信分账] 查询剩余待分金额应答缺 unsplit_amount: {}", resp);
            return null;
        } catch (Exception e) {
            log.warn("[微信分账] 查询剩余待分金额失败 transactionId={}: {}", transactionId, e.getMessage());
            return null;
        }
    }

    /**
     * 添加分账接收方（分账前置）：POST /v3/profitsharing/receivers/add。
     *
     * <p>分账前必须先把接收方加入<b>该子商户</b>的分账接收方列表，否则请求分账报
     * 「分账接收方关系不存在」。MERCHANT_ID 的 name 为商户全称（必传），
     * PERSONAL_OPENID 的 name 为个人姓名（选传，传则校验实名一致性）。</p>
     *
     * <p>relation_type 官方可选取值：SERVICE_PROVIDER / STORE / STAFF / STORE_OWNER /
     * PARTNER / HEADQUARTER / BRAND / DISTRIBUTOR / USER / SUPPLIER / CUSTOM；
     * 取 CUSTOM 时 {@code custom_relation} 必填（最多 10 字），其余取值不得填写。</p>
     *
     * @param subMchid      特约商户号（分账出资方，必填）
     * @param account       接收方账号（商户号或 openid）
     * @param name          接收方全称/姓名（敏感字段，自动加密；可为空）
     * @param relationType  与分账方的关系类型
     * @param customRelation CUSTOM 关系时的自定义说明（其余类型传 null）
     */
    public void addReceiver(String subMchid, String account, String name, String relationType,
                            String customRelation) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        if (StringUtils.hasText(subMchid)) {
            body.put("sub_mchid", subMchid);
        }
        if (StringUtils.hasText(props.getAppId())) {
            body.put("appid", props.getAppId());
        }
        body.put("type", resolveReceiverType(account));
        body.put("account", account);
        String encName = encryptReceiverName(name);
        if (encName != null) {
            body.put("name", encName);
        }
        String type = StringUtils.hasText(relationType) ? relationType : "PARTNER";
        body.put("relation_type", type);
        if ("CUSTOM".equals(type) && StringUtils.hasText(customRelation)) {
            body.put("custom_relation", customRelation);
        }
        httpClient.post(props.getGateway() + "/v3/profitsharing/receivers/add", body, serialHeaders(encName));
    }

    /**
     * 确保接收方已添加（进程内缓存去重 + best-effort）。
     *
     * <p>添加失败不抛异常（仅告警）：真正的关系缺失会在随后的请求分账以
     * NO_RELATION / 「分账接收方关系不存在」暴露，落 freeze_failed 交由补偿调度重试，
     * 重试路径会再次调用本方法。</p>
     */
    public void ensureReceiverAdded(String subMchid, String account, String name, String relationType,
                                    String customRelation) {
        if (!StringUtils.hasText(account)) {
            return;
        }
        String key = (subMchid == null ? "" : subMchid) + "|" + account;
        if (!addedReceivers.add(key)) {
            return; // 本进程已成功添加过
        }
        try {
            addReceiver(subMchid, account, name, relationType, customRelation);
        } catch (Exception e) {
            addedReceivers.remove(key); // 失败移除，允许后续重试
            log.warn("[微信分账] 添加分账接收方失败 subMchid={} account={}: {}", subMchid, account, e.getMessage());
        }
    }

    /**
     * 归集户余额查询（HTML ③-b）。真实接口待对接，未接时返回 null（不误报）。
     */
    public BigDecimal queryCollectionBalance() {
        log.info("[微信监控] 归集户余额查询未对接（WechatSplitApiService.queryCollectionBalance 待实现），返回 null");
        return null;
    }

    /** 加密敏感字段时须在请求头指明加密证书序列号；未加密则不置头。 */
    private Map<String, String> serialHeaders(String encryptedName) {
        if (encryptedName == null) {
            return null;
        }
        try {
            String serial = certManager.anySerial();
            if (serial == null) {
                return null;
            }
            return Map.of("Wechatpay-Serial", serial);
        } catch (Exception e) {
            log.warn("[微信分账] 获取平台证书序列号失败，敏感字段可能无法解密: {}", e.getMessage());
            return null;
        }
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
            log.warn("[微信分账] receiver_name 加密失败: {}", e.getMessage());
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
