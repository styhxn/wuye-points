package com.wuye.points.controller;

import com.wuye.points.service.MerchantTransferService;
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
 * 微信商家转账（新版）结果通知入口（APIv3）。
 *
 * <p>对应 application.yml 中 {@code wechat-ecommerce.transfer-notify-url} 配置的地址。
 * 处理逻辑（状态落地）见 {@link MerchantTransferService#handleTransferNotify}；本控制器只做透传与应答。</p>
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class MerchantTransferNotifyController {

    private final MerchantTransferService transferService;

    @PostMapping(value = "/ecommerce-transfer/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notify(@RequestBody String rawBody,
                                                      @RequestHeader HttpHeaders headers) {
        Map<String, String> ack = transferService.handleTransferNotify(rawBody, headers);
        if ("SUCCESS".equals(ack.get("code"))) {
            return ResponseEntity.ok(ack);
        }
        return ResponseEntity.status(500).body(ack);
    }
}
