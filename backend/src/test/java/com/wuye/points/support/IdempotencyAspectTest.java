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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * C1 幂等切面回归：放行条件、抢占冲突的四种走向、等待与超时、业务失败回滚、缓存降级。
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

    private void tryBeginFailsWithDuplicate() {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
    }

    private IdempotencyRecord record(String id, String status, LocalDateTime expiresAt, String responseBody) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setId(id);
        record.setStatus(status);
        record.setExpiresAt(expiresAt);
        record.setResponseBody(responseBody);
        return record;
    }

    private String cachedBody(String orderId) throws Exception {
        return objectMapper.writeValueAsString(ApiResponse.success(Map.of("orderId", orderId)));
    }

    @Test
    @DisplayName("首次请求：执行并缓存响应")
    void firstRequest_executesAndCaches() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
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
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString()))
                .thenReturn(record("idem_1", "completed", LocalDateTime.now().plusHours(1), cachedBody("ord_existing")));

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

    @Test
    @DisplayName("无请求上下文（定时任务等内部调用）：直接放行")
    void noRequestContext_proceeds() throws Throwable {
        RequestContextHolder.resetRequestAttributes();
        ApiResponse<Void> response = ApiResponse.success(null);
        when(joinPoint.proceed()).thenReturn(response);

        assertSame(response, aspect.around(joinPoint));

        verifyNoInteractions(idempotencyService);
    }

    @Test
    @DisplayName("未登录（无 userId）：直接放行，不落幂等记录")
    void noUserId_proceeds() throws Throwable {
        UserContext.set(UserContext.builder().role("resident").build());
        ApiResponse<Void> response = ApiResponse.success(null);
        when(joinPoint.proceed()).thenReturn(response);

        assertSame(response, aspect.around(joinPoint));

        verify(idempotencyService, never()).tryBegin(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("抢占冲突但查不到记录：重试一次仍冲突，返回请求处理中")
    void duplicateRecordNotFound_retriesThenConflicts() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString())).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        verify(idempotencyService, times(2))
                .tryBegin(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("已有记录已过期：删除后第二次抢占成功并正常执行")
    void expiredRecord_isDeletedAndRetried() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate"))
                .thenReturn(record("idem_new", "processing", LocalDateTime.now().plusMinutes(2), null));
        when(idempotencyService.find(anyString(), anyString()))
                .thenReturn(record("idem_old", "processing", LocalDateTime.now().minusMinutes(1), null));
        ApiResponse<Void> response = ApiResponse.success(null);
        when(joinPoint.proceed()).thenReturn(response);

        assertSame(response, aspect.around(joinPoint));

        verify(idempotencyService).delete("idem_old");
        verify(idempotencyService).complete(eq("idem_new"), eq(200), anyString());
    }

    @Test
    @DisplayName("已有记录处理中：等待期间转完成后回放，不重复执行业务")
    void processingRecord_replaysAfterWait() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString())).thenReturn(
                record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null),
                record("idem_1", "completed", LocalDateTime.now().plusHours(1), cachedBody("ord_waited")));

        Object result = aspect.around(joinPoint);

        ApiResponse<?> apiResponse = assertInstanceOf(ApiResponse.class, result);
        assertEquals("ord_waited", assertInstanceOf(Map.class, apiResponse.getData()).get("orderId"));
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("等待期间记录被清理：返回请求处理中")
    void waitingRecordVanishes_conflict() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString())).thenReturn(
                record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null), null);

        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("等待超时仍处理中：返回请求处理中，不无限阻塞")
    void waitingTimesOut_conflict() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));

        long start = System.currentTimeMillis();
        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        assertEquals(2000L, System.currentTimeMillis() - start, 800L);
    }

    @Test
    @DisplayName("回放空响应体：提示已处理，不重复执行")
    void replayBlankBody_conflict() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString()))
                .thenReturn(record("idem_1", "completed", LocalDateTime.now().plusHours(1), null));

        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("回放响应体解析失败：提示已处理而非抛序列化异常")
    void replayUnparseableBody_conflict() throws Throwable {
        tryBeginFailsWithDuplicate();
        when(idempotencyService.find(anyString(), anyString()))
                .thenReturn(record("idem_1", "completed", LocalDateTime.now().plusHours(1), "{不是 JSON"));

        BizException ex = assertThrows(BizException.class, () -> aspect.around(joinPoint));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("业务异常：删除 processing 记录并原样抛出，允许同 requestId 重试")
    void businessError_failsRecordAndRethrowsOriginal() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
        BizException business = new BizException(ErrorCode.ORDER_CONFLICT, "库存不足");
        when(joinPoint.proceed()).thenThrow(business);

        assertSame(business, assertThrows(BizException.class, () -> aspect.around(joinPoint)));

        verify(idempotencyService).fail("idem_1");
        verify(idempotencyService, never()).complete(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("业务异常且删除记录也失败：仍抛业务异常，不被基础设施异常顶替")
    void businessError_failAlsoThrows_stillRethrowsOriginal() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
        BizException business = new BizException(ErrorCode.ORDER_CONFLICT, "库存不足");
        when(joinPoint.proceed()).thenThrow(business);
        doThrow(new RuntimeException("库连不上")).when(idempotencyService).fail("idem_1");

        assertSame(business, assertThrows(BizException.class, () -> aspect.around(joinPoint)));
    }

    @Test
    @DisplayName("响应过大：只标记完成不缓存响应体")
    void oversizedResponse_completesWithoutBody() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
        when(joinPoint.proceed()).thenReturn(ApiResponse.success(Map.of("blob", "x".repeat(1_100_000))));

        aspect.around(joinPoint);

        verify(idempotencyService).complete("idem_1", 200, null);
    }

    @Test
    @DisplayName("响应无法序列化：业务结果照常返回，标记完成不带体")
    void unserializableResponse_stillReturnsResult() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
        Object businessResult = new Object();
        when(joinPoint.proceed()).thenReturn(businessResult);

        assertSame(businessResult, aspect.around(joinPoint));

        verify(idempotencyService).complete("idem_1", 200, null);
    }

    @Test
    @DisplayName("响应无法序列化且标记完成也失败：仍返回业务成功结果")
    void completeAlsoFails_stillReturnsResult() throws Throwable {
        when(idempotencyService.tryBegin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(record("idem_1", "processing", LocalDateTime.now().plusMinutes(2), null));
        Object businessResult = new Object();
        when(joinPoint.proceed()).thenReturn(businessResult);
        doThrow(new RuntimeException("库连不上"))
                .when(idempotencyService).complete(anyString(), anyInt(), isNull());

        assertSame(businessResult, aspect.around(joinPoint));
    }
}
