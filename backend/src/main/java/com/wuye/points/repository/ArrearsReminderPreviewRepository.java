package com.wuye.points.repository;

import com.wuye.points.entity.ArrearsReminderPreview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ArrearsReminderPreviewRepository extends JpaRepository<ArrearsReminderPreview, String> {

    /** 清理已过期的预览快照 */
    long deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
