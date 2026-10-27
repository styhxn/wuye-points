package com.wuye.points.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.entity.IdempotencyRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.service.IdempotencyService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * C1 幂等切面回归：首次执行、重复回放、无 requestId、非法 requestId。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyAspectTest {

    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private ProceedingJoinPoint joinPoint;

    private IdempotencyAspect aspect;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        aspect = new IdempotencyAspect(idempotencyService, objectMapper);
        UserContext.set(UserContext.builder().userId("res_1").role("resident").build());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        request.addHeader(IdempotencyAspect.HEADER, "req-12345678");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        UserContext.clear();
    }

    @Test
    @DisplayName("首次请求：执行并缓存响应")
    void firstRequest_executesAndCaches() throws Throwable {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setId("idem_1");
        record.setStatus("processing");
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record);
        ApiResponse<Map<String, String>> response = ApiResponse.success(Map.of("orderId", "ord_1"));
        when(joinPoint.proceed()).thenReturn(response);

        Object result = aspect.around(joinPoint);

        assertSame(response, result);
        verify(joinPoint).proceed();
        verify(idempotencyService).complete(eq("idem_1"), eq(200), contains("\"orderId\":\"ord_1\""));
    }

    @Test
    @DisplayName("重复请求：直接回放首次响应，不再执行业务")
    void duplicateRequest_replaysCachedResponse() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        IdempotencyRecord existing = new IdempotencyRecord();
        existing.setId("idem_1");
        existing.setStatus("completed");
        existing.setExpiresAt(LocalDateTime.now().plusHours(1));
        existing.setResponseBody(objectMapper.writeValueAsString(
                ApiResponse.success(Map.of("orderId", "ord_existing"))));
        when(idempotencyService.find(anyString(), anyString())).thenReturn(existing);

        Object result = aspect.around(joinPoint);

        ApiResponse<?> apiResponse = assertInstanceOf(ApiResponse.class, result);
        Map<?, ?> data = assertInstanceOf(Map.class, apiResponse.getData());
        assertEquals("ord_existing", data.get("orderId"));
        verify(joinPoint, never()).proceed();
        verify(idempotencyService, never()).complete(anyString(), eq(200), anyString());
    }

    @Test
    @DisplayName("无 X-Request-Id：直接放行，不走幂等")
    void noHeader_proceedsWithoutIdempotency() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ApiResponse<Void> response = ApiResponse.success(null);
        when(joinPoint.proceed()).thenReturn(response);

        assertSame(response, aspect.around(joinPoint));

        verify(idempotencyService, never()).tryBegin(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("非法 X-Request-Id：返回参数错误")
    void invalidRequestId_paramInvalid() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/orders");
        request.addHeader(IdempotencyAspect.HEADER, "bad");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }
}
