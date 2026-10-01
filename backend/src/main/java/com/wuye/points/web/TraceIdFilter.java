package com.wuye.points.web;

import com.wuye.points.util.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 链路追踪入口过滤器。
 *
 * <p>职责：
 * <ul>
 *   <li>优先沿用上游网关注入的 {@code X-Trace-Id}，否则生成新的 traceId；</li>
 *   <li>写入 MDC，使本次请求产生的所有日志自动携带 traceId；</li>
 *   <li>响应头回写 {@code X-Trace-Id}，便于前端报错时提供排查凭据；</li>
 *   <li>请求结束时清理 MDC，避免线程复用导致 traceId 串号。</li>
 * </ul>
 *
 * <p>排序在安全过滤器之后、业务处理之前；异常处理由 {@code GlobalExceptionHandler} 负责，
 * 此过滤器不吞异常。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        TraceContext.set(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceContext.clear();
        }
    }

    /**
     * 仅接受来自上游可信头的 traceId，并做长度与字符集限制，
     * 防止外部构造超长或含控制字符的值污染日志文件。
     */
    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(TRACE_ID_HEADER);
        if (incoming != null && !incoming.isBlank()
                && incoming.length() <= 64
                && incoming.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '-' || c == '_')) {
            return incoming;
        }
        return TraceContext.newTraceId();
    }
}
