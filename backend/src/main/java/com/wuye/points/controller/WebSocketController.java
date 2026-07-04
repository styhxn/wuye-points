package com.wuye.points.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/heartbeat.pong", Map.of(
                "type", "heartbeat.pong",
                "timestamp", Instant.now().toString()
        ));
    }
}
