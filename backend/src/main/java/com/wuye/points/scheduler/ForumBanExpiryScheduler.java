package com.wuye.points.scheduler;

import com.wuye.points.service.ForumBanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 论坛禁言到期自动解除调度器。
 *
 * <p>设置了 expiresAt 的禁言到期后自动恢复：解除禁言记录、恢复历史内容可见、
 * 回补评论计数（与管理员手动解除同链路）。每 10 分钟扫描一次，多实例部署下由
 * ShedLock 保证仅一个实例执行。</p>
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ForumBanExpiryScheduler {

    private final ForumBanService forumBanService;

    @Scheduled(fixedRate = 600000) // 10 分钟
    @SchedulerLock(name = "forum_ban_expiry_lift", lockAtMostFor = "PT9M", lockAtLeastFor = "PT0S")
    public void liftExpiredBans() {
        try {
            int lifted = forumBanService.liftExpiredBans();
            if (lifted > 0) {
                log.info("[论坛禁言] 到期自动解除 {} 条", lifted);
            }
        } catch (Exception e) {
            log.error("[论坛禁言] 到期自动解除调度异常", e);
        }
    }
}
