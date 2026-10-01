package com.wuye.points.controller;

import com.wuye.points.payment.EcommerceSplitNotifyService;
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
 * 电商收付通分账结果通知入口（APIv3）。
 *
 * <p>对应 application.yml 中 {@code wechat-ecommerce.notify-url} 配置的地址。
 * 处理逻辑（幂等修正状态）见 {@link EcommerceSplitNotifyService}；本控制器只做透传与应答。</p>
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class EcommerceSplitNotifyController {

    private final EcommerceSplitNotifyService splitNotifyService;

    @PostMapping(value = "/ecommerce-split/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notify(@RequestBody String rawBody,
                                                      @RequestHeader HttpHeaders headers) {
        Map<String, String> ack = splitNotifyService.handleSplitNotify(rawBody, headers);
        if ("SUCCESS".equals(ack.get("code"))) {
            return ResponseEntity.ok(ack);
        }
        return ResponseEntity.status(500).body(ack);
    }
}
