package com.wuye.points.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.entity.IdempotencyRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * {@link Idempotent} 切面：按用户 + 方法 + 路径 + X-Request-Id 做幂等。
 *
 * <p>首次请求落 processing；成功后缓存响应体，重复请求回放同一响应。
 * 同一请求并发到达时，后到请求最多等待 2 秒，仍未完成返回 409“请求处理中”。</p>
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class IdempotencyAspect {

    public static final String HEADER = "X-Request-Id";
    private static final int MAX_CACHE_BODY = 1_000_000;
    private static final int WAIT_LOOPS = 20;
    private static final long WAIT_INTERVAL_MS = 100L;

    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.wuye.points.support.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String requestId = request.getHeader(HEADER);
        if (!StringUtils.hasText(requestId)) {
            return joinPoint.proceed();
        }
        requestId = requestId.trim();
        if (!requestId.matches("[A-Za-z0-9._:-]{8,128}")) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "X-Request-Id格式不正确（8-128位字母/数字/._:-）");
        }
        String userId = UserContext.currentUserId();
        if (!StringUtils.hasText(userId)) {
            return joinPoint.proceed();
        }

        String method = request.getMethod();
        String path = request.getRequestURI();
        String requestKey = sha256(method + ":" + path + ":" + requestId);

        IdempotencyRecord record = null;
        for (int attempt = 0; attempt < 2 && record == null; attempt++) {
            try {
                record = idempotencyService.tryBegin(userId, requestKey, requestId, method, path);
            } catch (DataIntegrityViolationException duplicate) {
                IdempotencyRecord existing = idempotencyService.find(userId, requestKey);
                if (existing == null) {
                    continue;
                }
                if (existing.getExpiresAt() != null
                        && existing.getExpiresAt().isBefore(LocalDateTime.now())) {
                    idempotencyService.delete(existing.getId());
                    continue;
                }
                if ("completed".equals(existing.getStatus())) {
                    return replay(existing);
                }
                return waitForCompletion(userId, requestKey);
            }
        }
        if (record == null) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "请求处理中，请稍后重试");
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable businessError) {
            // 业务失败不缓存，允许用户用同一 requestId 重试
            safeFail(record.getId());
            throw businessError;
        }

        // 业务已成功：缓存失败只记日志，绝不能把成功结果改写成错误返回。
        try {
            String body = objectMapper.writeValueAsString(result);
            if (body.length() > MAX_CACHE_BODY) {
                log.warn("[幂等] 响应过大，只标记完成不缓存 path={} size={}", path, body.length());
                idempotencyService.complete(record.getId(), 200, null);
            } else {
                idempotencyService.complete(record.getId(), 200, body);
            }
        } catch (Exception cacheError) {
            log.error("[幂等] 响应缓存失败，业务已成功 path={}", path, cacheError);
            try {
                // 标记 completed 但不带响应体：重复请求会提示“已处理，请刷新查看”，避免重复执行。
                idempotencyService.complete(record.getId(), 200, null);
            } catch (Exception ignored) {
                log.error("[幂等] 标记 completed 也失败，记录将按 processing TTL 过期 id={}", record.getId(), ignored);
            }
        }
        return result;
    }

    private void safeFail(String id) {
        try {
            idempotencyService.fail(id);
        } catch (Exception e) {
            log.error("[幂等] 删除 processing 记录失败 id={}", id, e);
        }
    }

    private Object waitForCompletion(String userId, String requestKey) throws InterruptedException {
        for (int i = 0; i < WAIT_LOOPS; i++) {
            Thread.sleep(WAIT_INTERVAL_MS);
            IdempotencyRecord latest = idempotencyService.find(userId, requestKey);
            if (latest == null) {
                throw new BizException(ErrorCode.ORDER_CONFLICT, "请求处理中，请稍后重试");
            }
            if ("completed".equals(latest.getStatus())) {
                return replay(latest);
            }
        }
        throw new BizException(ErrorCode.ORDER_CONFLICT, "请求处理中，请稍后重试");
    }

    private Object replay(IdempotencyRecord record) {
        if (!StringUtils.hasText(record.getResponseBody())) {
            throw new BizException(ErrorCode.ORDER_CONFLICT, "该请求已处理，请刷新查看结果");
        }
        try {
            return objectMapper.readValue(record.getResponseBody(), ApiResponse.class);
        } catch (Exception e) {
            log.error("[幂等] 回放响应解析失败 id={}", record.getId(), e);
            throw new BizException(ErrorCode.ORDER_CONFLICT, "该请求已处理，请刷新查看结果");
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
