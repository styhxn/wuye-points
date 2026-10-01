package com.wuye.points.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 支付 finalize 异步线程池。
 *
 * <p>拒绝策略使用“记录日志并丢弃”：当队列已满时，任务交由 {@code PaymentSettleScheduler} 兜底扫描重试，
 * 绝不让回调请求线程被阻塞（否则会拖慢回调应答，触发微信 3 秒超时重推）。</p>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("settleTaskExecutor")
    public Executor settleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("pay-settle-");
        executor.setRejectedExecutionHandler((r, e) ->
                log.warn("支付 finalize 线程池已满，任务丢弃，将由调度器兜底处理"));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
