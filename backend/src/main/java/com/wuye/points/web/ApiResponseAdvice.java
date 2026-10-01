package com.wuye.points.web;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.TraceContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;

/**
 * 统一为 {@link ApiResponse} 注入 traceId 与响应时间戳。
 *
 * <p>两点收益：
 * <ol>
 *   <li>前端/App 报错时可直接从响应体拿到 traceId 反馈给后端，排查效率从"翻日志"变为"精确检索"；</li>
 *   <li>时间戳在序列化时计算，而非对象构造时生成，
 *       避免 ApiResponse 被缓存或复用后返回过期时间戳。</li>
 * </ol>
 *
 * <p>非 {@link ApiResponse} 的响应体（如支付回调的 Map、文件下载流）保持原样，不做包装。
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(body instanceof ApiResponse<?> apiResponse)) {
            return body;
        }
        apiResponse.setTraceId(TraceContext.currentTraceId());
        apiResponse.setTimestamp(DateTimeUtil.toLocalDateTimeString(LocalDateTime.now()));
        return apiResponse;
    }
}
