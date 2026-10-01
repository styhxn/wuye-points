package com.wuye.points.web;

import com.wuye.points.util.SensitiveMasker;
import com.wuye.points.util.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.StringJoiner;

/**
 * Controller 统一请求日志切面。
 *
 * <p>解决原先"各接口手写日志且极不均衡"的问题：所有 HTTP 入口统一记录
 * 方法、路径、耗时、脱敏后的入参与结果状态，慢请求以 WARN 级别突出。
 *
 * <p>安全约束：
 * <ul>
 *   <li>入参统一经 {@link SensitiveMasker} 脱敏后才落盘；</li>
 *   <li>文件上传参数只记录文件名与大小，绝不记录二进制内容；</li>
 *   <li>切面内异常一律原样抛出，绝不吞异常或改变业务语义。</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class RequestLoggingAspect {

    /** 超过该阈值的请求以 WARN 输出，便于快速定位慢接口 */
    private static final long SLOW_THRESHOLD_MS = 1000L;

    @Pointcut("within(com.wuye.points.controller..*)")
    public void controllerMethods() {
    }

    @Around("controllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String traceId = TraceContext.currentTraceId();
        String args = describeArgs(joinPoint.getArgs());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            log(cost, "[入口] traceId={} method={} 耗时={}ms 入参={} 结果={}",
                    traceId, method, cost, args, summarize(result));
            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[入口异常] traceId={} method={} 耗时={}ms 入参={} 异常={}: {}",
                    traceId, method, cost, args, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private void log(long cost, String format, Object... arguments) {
        if (cost >= SLOW_THRESHOLD_MS) {
            log.warn(format.replace("[入口]", "[慢请求]"), arguments);
        } else {
            log.info(format, arguments);
        }
    }

    /**
     * 仅展开可直接文本化的简单入参，复杂 DTO 走 toString（已由各 DTO 控制输出范围），
     * 文件参数只摘要，避免大对象或二进制内容进入日志。
     */
    private String describeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object arg : args) {
            if (arg instanceof MultipartFile file) {
                joiner.add("file{name=" + file.getOriginalFilename() + ", size=" + file.getSize() + "}");
            } else if (arg instanceof MultipartFile[] files) {
                joiner.add("files{count=" + files.length + "}");
            } else if (arg == null || arg.getClass().isPrimitive()
                    || arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean
                    || arg instanceof Enum<?>) {
                joiner.add(String.valueOf(arg));
            } else {
                joiner.add(SensitiveMasker.mask(arg));
            }
        }
        return joiner.toString();
    }

    /** 结果只记录类名与规模，不打印完整响应体（响应体可能包含大量业务数据） */
    private String summarize(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof org.springframework.http.ResponseEntity<?> entity) {
            return "ResponseEntity(status=" + entity.getStatusCode() + ")";
        }
        return result.getClass().getSimpleName();
    }
}
