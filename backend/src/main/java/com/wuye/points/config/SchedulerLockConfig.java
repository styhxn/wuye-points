package com.wuye.points.config;

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 分布式定时任务锁（ShedLock）。
 *
 * <p>背景：本系统 15 个 {@code @Scheduled} 任务原先没有任何互斥机制。单实例时无碍，
 * 一旦多副本部署（滚动发布、扩容、K8s 多副本），每个实例都会各跑一遍，
 * 积分清零 / 物业币过期这类"生成扣减流水"的任务会重复出账，属确定性资金事故。
 *
 * <p>实现：以数据库 {@code shedlock} 表作为锁存储（不引入 Redis 等新中间件），
 * 任务执行前尝试抢占命名锁，抢不到则跳过本次执行。
 *
 * <p>锁参数语义：
 * <ul>
 *   <li>{@code lockAtMostFor}：持锁上限。进程崩溃未释放时，锁在该时长后自动过期，
 *       避免任务被永久卡死。必须大于任务的最坏执行耗时。</li>
 *   <li>{@code lockAtLeastFor}：最短持锁时间。防止多实例时钟漂移导致同一任务被
 *       两个实例在极短时间内各执行一次。</li>
 * </ul>
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class SchedulerLockConfig {

    @Bean
    public net.javacrumbs.shedlock.core.LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                // 以数据库时间为准，避免应用服务器时钟漂移导致锁提前/延后释放
                .usingDbTime()
                .build());
    }

    /**
     * 供定时任务使用的编程式事务模板。
     *
     * <p>批处理任务（积分清零、物业币过期）必须避免在方法级 {@code @Transactional} 中
     * 遍历全表：那样单个事务会持有 thousands 行锁直到方法结束，极易锁等待超时甚至锁表。
     * 正确做法是外层无事务、每个处理单元一个独立短事务，由本模板实现。
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
