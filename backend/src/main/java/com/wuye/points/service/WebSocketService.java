package com.wuye.points.service;

import com.wuye.points.dto.websocket.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String userId, String type, Object payload) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .payload(payload)
                .timestamp(Instant.now().toString())
                .messageId("ws_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .build();
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
    }

    public void broadcast(String type, Object payload) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .payload(payload)
                .timestamp(Instant.now().toString())
                .messageId("ws_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .build();
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }

    public void sendOrderUpdate(String userId, String orderId, String status) {
        sendToUser(userId, "order.status_update", Map.of(
                "orderId", orderId,
                "orderStatus", status,
                "updatedAt", Instant.now().toString()
        ));
    }

    public void sendDeliveryResult(String courierId, String orderId, boolean success) {
        sendToUser(courierId, "delivery.grab_result", Map.of(
                "orderId", orderId,
                "courierId", courierId,
                "result", success
        ));
    }

    public void sendBalanceUpdate(String residentId, String type, Object balance) {
        sendToUser(residentId, "point.balance_update", Map.of(
                "residentId", residentId,
                "pointType", type,
                "balance", balance
        ));
    }

    public void sendNotification(String userId, String announcementId, String announcementType, String title) {
        sendToUser(userId, "notification.new", Map.of(
                "announcementId", announcementId != null ? announcementId : "",
                "announcementType", announcementType != null ? announcementType : "",
                "title", title
        ));
    }
}
