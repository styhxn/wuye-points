package com.wuye.points.payment;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信电商收付通（平台收付通）分账 API 封装（/{@code v3/ecommerce/profitsharing/*}）。
 *
 * <p>与普通分账（/{@code v3/profitsharing/*}）不同，电商收付通的资金流为：
 * 用户付款直进「二级商户（sub_mchid，即订单商家）」→ 平台调本服务「请求分账」主动划走
 * 佣金（给平台商户号 MERCHANT_ID）与配送费（给快递员 PERSONAL_OPENID）→ 售后窗口到期
 * {@code finishSplit} 完结，剩余货款解冻给商家。全程资金不经过平台归集，避免二清。</p>
 *
 * <p>本封装仅负责 HTTP 报文与签名，业务状态机编排在 {@link WechatSplitService}。</p>
 */
@Slf4j
@Service
public class EcommerceSplitApiService {

    private final WechatHttpClient httpClient;
    private final WechatPayProperties props;
    private final EcommerceSplitProperties ecommerceProps;

    public EcommerceSplitApiService(WechatHttpClient httpClient, WechatPayProperties props,
                                    EcommerceSplitProperties ecommerceProps) {
        this.httpClient = httpClient;
        this.props = props;
        this.ecommerceProps = ecommerceProps;
    }

    /** 元 → 分（字符串，无小数）。 */
    public static String fen(BigDecimal yuan) {
        if (yuan == null) {
            return "0";
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 请求分账：POST /v3/ecommerce/profitsharing/orders。
     *
     * <p>一笔支付可分账给多个接收方（最多 50 个）；分账后的剩余资金在 {@link #finishSplit} 完结时
     * 解冻给二级商户（商家货款）。接收方类型：MERCHANT_ID（商户号，含平台/服务商）、
     * PERSONAL_OPENID / PERSONAL_SUB_OPENID（个人 openid，需预先授权关系）。</p>
     *
     * @param subMchid        分账方二级商户号（订单商家）
     * @param transactionId   微信支付订单号（transaction_id）
     * @param outOrderNo      平台分账单号（out_order_no，幂等键）
     * @param receivers       接收方列表（type/account/amount/description）
     * @return 微信分账订单号 order_id
     */
    public String requestSplit(String subMchid, String transactionId, String outOrderNo,
                               List<Map<String, Object>> receivers) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("sub_mchid", subMchid);
        body.put("transaction_id", transactionId);
        body.put("out_order_no", outOrderNo);
        body.put("receivers", receivers);

        String resp = httpClient.post(props.getGateway() + "/v3/ecommerce/profitsharing/orders", body);
        return parseField(resp, "order_id");
    }

    /**
     * 查询分账结果：GET /v3/ecommerce/profitsharing/orders/{out_order_no}。
     *
     * @return 微信分账订单号 order_id（不存在返回 null）
     */
    public String querySplit(String subMchid, String outOrderNo) {
        String url = props.getGateway() + "/v3/ecommerce/profitsharing/orders/" + outOrderNo
                + "?sub_mchid=" + subMchid;
        String resp = httpClient.get(url);
        return parseField(resp, "order_id");
    }

    /**
     * 完结分账：POST /v3/ecommerce/profitsharing/finish-order。
     * 将未分账的剩余资金解冻给二级商户（商家货款）。幂等：已完结订单再次调用由微信侧拒绝，调用方需自行兜底。
     */
    public void finishSplit(String subMchid, String transactionId, String outOrderNo) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("sub_mchid", subMchid);
        body.put("transaction_id", transactionId);
        body.put("out_order_no", outOrderNo);
        body.put("description", "售后窗口到期，完结分账解冻商家货款");
        httpClient.post(props.getGateway() + "/v3/ecommerce/profitsharing/finish-order", body);
    }

    /**
     * 分账回退：POST /v3/ecommerce/profitsharing/returnorders。
     *
     * <p>仅支持 MERCHANT_ID 类型接收方回退；PERSONAL_OPENID（个人）类型不可回退，
     * 个人类型已分资金需业务侧另行处理（平台补款 / 对账核销）。</p>
     *
     * @param returnMchid 回退方商户号（原接收方，如平台商户号）
     * @return 微信回退单号 out_return_no
     */
    public String returnSplit(String subMchid, String orderId, String outOrderNo, String outReturnNo,
                              String returnMchid, BigDecimal amount) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("sub_mchid", subMchid);
        body.put("order_id", orderId);
        body.put("out_order_no", outOrderNo);
        body.put("out_return_no", outReturnNo);
        body.put("return_mchid", returnMchid);
        body.put("amount", Integer.parseInt(fen(amount)));
        body.put("description", "订单取消/退款，分账回退");
        String resp = httpClient.post(props.getGateway() + "/v3/ecommerce/profitsharing/returnorders", body);
        return parseField(resp, "out_return_no");
    }

    /**
     * 添加分账接收方：POST /v3/ecommerce/profitsharing/receivers/add。
     *
     * <p>给「个人 openid」分账前必须添加接收方并完成授权关系；给平台 / 服务商自身商户号分账无需添加。
     * 当前作为对外能力占位：进件阶段由运营在商户平台配置，代码侧仅在需要时调用。</p>
     */
    public String addReceiver(String type, String account) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("appid", props.getAppId());
        body.put("type", type);
        body.put("account", account);
        body.put("relation_type", ecommerceProps.getReceiverRelationType());
        String resp = httpClient.post(props.getGateway() + "/v3/ecommerce/profitsharing/receivers/add", body);
        return resp;
    }

    /** 构建单个分账接收方（type/account/amount/description）。 */
    public static Map<String, Object> receiver(String type, String account, BigDecimal amount, String description) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", type);
        r.put("account", account);
        r.put("amount", Integer.parseInt(fen(amount)));
        if (StringUtils.hasText(description)) {
            r.put("description", description);
        }
        return r;
    }

    /** 接收方列表（可空集合 → 空列表）。 */
    public static List<Map<String, Object>> receiversOf(Map<String, Object>... items) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> item : items) {
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
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
