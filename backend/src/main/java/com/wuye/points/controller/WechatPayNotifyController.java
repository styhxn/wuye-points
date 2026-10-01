package com.wuye.points.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.payment.PaymentCompletionService;
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
 * 微信支付结果通知入口（APIv3）。
 *
 * <p>对应 application.yml 中 {@code wechat.pay.notify-url} 配置的地址。微信要求 5 秒内应答，
 * 故此处只做「验签 + 解密 + 标记支付 + 落 outbox + 发事件」，业务 finalize 异步进行。
 *
 * <p>应答：成功 {@code {"code":"SUCCESS"}}，失败非 200 + {@code {"code":"FAIL"}}（微信按策略重推）。
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class WechatPayNotifyController {

    private final PaymentCompletionService completionService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/notify/wechatpay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notify(@RequestBody String rawBody,
                                                       @RequestHeader HttpHeaders headers) {
        Map<String, String> ack = completionService.handleWechatNotify(rawBody, headers);
        if ("SUCCESS".equals(ack.get("code"))) {
            return ResponseEntity.ok(ack);
        }
        return ResponseEntity.status(500).body(ack);
    }
}
