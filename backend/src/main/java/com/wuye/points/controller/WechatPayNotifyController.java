package com.wuye.points.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.payment.PaymentCompletionService;
import com.wuye.points.payment.WechatNotifyChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 微信支付结果通知入口（APIv3），按商户通道拆分为两个独立地址。
 *
 * <ul>
 *   <li>{@code /payment/notify/wechatpay} —— <b>服务商</b>通道（wechat.pay，商城订单）；</li>
 *   <li>{@code /payment/notify/wechatpay-direct} —— <b>直连商户</b>通道（wechat.direct，平台自营业务）。</li>
 * </ul>
 *
 * <p>两个地址必须分别配置到对应微信商户平台的「支付回调 / 退款回调」，不能混用：
 * 验签公钥、APIv3 解密密钥均按商户主体隔离。微信要求 5 秒内应答，故此处只做
 * 「验签 + 解密 + 标记支付 + 落 outbox + 发事件」，业务 finalize 异步进行。</p>
 *
 * <p>应答：成功 {@code {"code":"SUCCESS"}}，失败非 200 + {@code {"code":"FAIL"}}（微信按策略重推）。</p>
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class WechatPayNotifyController {

    private final PaymentCompletionService completionService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/notify/wechatpay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notifyPartner(@RequestBody String rawBody,
                                                             @RequestHeader HttpHeaders headers) {
        return ack(completionService.handleWechatNotify(rawBody, headers, WechatNotifyChannel.PARTNER));
    }

    @PostMapping(value = "/notify/wechatpay-direct", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notifyDirect(@RequestBody String rawBody,
                                                            @RequestHeader HttpHeaders headers) {
        return ack(completionService.handleWechatNotify(rawBody, headers, WechatNotifyChannel.DIRECT));
    }

    private ResponseEntity<Map<String, String>> ack(Map<String, String> ack) {
        if ("SUCCESS".equals(ack.get("code"))) {
            return ResponseEntity.ok(ack);
        }
        return ResponseEntity.status(500).body(ack);
    }
}
