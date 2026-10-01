package com.wuye.points.util;

import org.slf4j.MDC;

import java.security.SecureRandom;

/**
 * 链路追踪上下文。
 *
 * <p>基于 SLF4J MDC 实现，配合 {@code TraceIdFilter} 在请求入口写入、出口清理，
 * 日志 pattern 中以 {@code %X{traceId}} 输出，从而把同一次请求散落在各 Service 的
 * 日志串联起来。异步线程需通过 {@link #runWith(Runnable)} 显式传递。
 */
public final class TraceContext {

    public static final String TRACE_ID = "traceId";

    /** 无追踪上下文时的占位符，保证日志列对齐 */
    private static final String NONE = "-";

    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private TraceContext() {
    }

    /**
     * 生成短 traceId：13 位时间戳（十六进制）+ 6 位随机串，兼顾可读性与碰撞概率。
     */
    public static String newTraceId() {
        return Long.toString(System.currentTimeMillis(), 36) + randomSuffix(6);
    }

    /** 当前线程的 traceId，未设置时返回 {@value #NONE} */
    public static String currentTraceId() {
        String value = MDC.get(TRACE_ID);
        return value == null || value.isEmpty() ? NONE : value;
    }

    public static void set(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 在异步线程中延续当前 traceId。
     *
     * <p>{@code @Async}、线程池、MQ 消费者等场景默认丢失 MDC，必须显式包装，
     * 否则异步任务的日志无法与触发它的请求关联。
     */
    public static Runnable runWith(Runnable task) {
        String traceId = MDC.get(TRACE_ID);
        return () -> {
            if (traceId != null) {
                MDC.put(TRACE_ID, traceId);
            }
            try {
                task.run();
            } finally {
                MDC.remove(TRACE_ID);
            }
        };
    }

    private static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
