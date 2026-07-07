package com.wuye.points.interceptor;

import com.wuye.points.context.UserContext;
import com.wuye.points.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (!StringUtils.hasText(query)) {
            log.warn("WebSocket连接缺少token参数");
            return false;
        }

        String token = extractToken(query);
        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket连接token为空");
            return false;
        }

        try {
            Claims claims = jwtUtil.parseTokenClaims(token);
            jwtUtil.validateAccessToken(claims);
            String userId = claims.getSubject();
            attributes.put("userId", userId);
            attributes.put("role", claims.get("role", String.class));
            attributes.put("propertyCompanyId", claims.get("propertyCompanyId", String.class));
            attributes.put("familyId", claims.get("familyId", String.class));
            // 设置 Principal 以支持 /user/queue/notifications 用户路由
            attributes.put("user", (Principal) () -> userId);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket连接token校验失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(String query) {
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
