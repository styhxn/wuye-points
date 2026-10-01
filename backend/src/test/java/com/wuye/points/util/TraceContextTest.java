package com.wuye.points.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 链路追踪上下文测试。
 *
 * <p>MDC 是线程绑定的，测试必须注意清理，否则会污染同线程执行的其它用例。
 */
class TraceContextTest {

    @BeforeEach
    @AfterEach
    void clear() {
        TraceContext.clear();
    }

    @Test
    @DisplayName("生成的 traceId 非空，且两次生成不重复")
    void generatesUniqueTraceId() {
        String first = TraceContext.newTraceId();
        String second = TraceContext.newTraceId();
        assertNotNull(first);
        assertNotEquals(first, second);
        assertTrue(first.length() >= 10, "traceId 应包含时间戳与随机后缀");
    }

    @Test
    @DisplayName("无上下文时返回占位符，保证日志列对齐")
    void returnsPlaceholderWithoutContext() {
        assertEquals("-", TraceContext.currentTraceId());
    }

    @Test
    @DisplayName("set 后可读回，clear 后恢复占位符")
    void setAndClear() {
        TraceContext.set("trace-123");
        assertEquals("trace-123", TraceContext.currentTraceId());
        TraceContext.clear();
        assertEquals("-", TraceContext.currentTraceId());
    }

    @Test
    @DisplayName("runWith 把 traceId 传递到其它线程，任务结束后清理该线程")
    void propagatesToOtherThread() throws Exception {
        TraceContext.set("trace-async");

        // 使用全新线程而非公共线程池：MDC 绑定在线程上，
        // 复用线程池会读到其它测试残留的值，导致用例不稳定。
        // 关键：runWith 必须在「调用线程（本例主线程，已 set traceId）」上调用以捕获 traceId，
        // 再把返回的 Runnable 交给 worker 线程执行，否则会在空 MDC 的工作线程中捕获到 null。
        String[] duringTask = new String[1];
        String[] afterTask = new String[1];
        Runnable task = TraceContext.runWith(() -> duringTask[0] = TraceContext.currentTraceId());
        Thread worker = new Thread(() -> {
            task.run();
            afterTask[0] = TraceContext.currentTraceId();
        });
        worker.start();
        worker.join(TimeUnit.SECONDS.toMillis(5));

        assertEquals("trace-async", duringTask[0], "异步线程应能读到父线程的 traceId");
        assertEquals("-", afterTask[0], "任务执行完毕后应清理该线程的 MDC");
    }

    @Test
    @DisplayName("runWith 无上下文时不报错")
    void runWithWithoutContextIsSafe() {
        TraceContext.clear();
        TraceContext.runWith(() -> assertEquals("-", TraceContext.currentTraceId())).run();
    }

    @Test
    @DisplayName("写入 MDC 的键与日志 pattern 中的占位符一致")
    void usesExpectedMdcKey() {
        TraceContext.set("abc");
        assertEquals("abc", MDC.get(TraceContext.TRACE_ID));
        assertEquals("traceId", TraceContext.TRACE_ID);
    }
}
