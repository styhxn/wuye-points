package com.wuye.points.service;

import com.wuye.points.dto.resident.ResidentListItemResponse;
import com.wuye.points.entity.Resident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 注销终审列表要能显示冷静期到期时间。
 *
 * <p>整改清单条目9/10 后，冷静期到期时间承载在独立列 {@code deletion_expires_at} 上，
 * {@code deleted_at} 回归「真正注销才写」的语义；只有 pending_deletion 才把到期时间暴露出去。</p>
 */
@DisplayName("住户列表项的注销到期时间字段")
class ResidentConverterDeletionExpiryTest {

    private final ResidentConverter converter = new ResidentConverter(null, null, null, null);

    private Resident resident(String status, LocalDateTime deletedAt, LocalDateTime deletionExpiresAt) {
        Resident resident = new Resident();
        resident.setId("res_1");
        resident.setName("张三");
        resident.setPhone("13800138000");
        resident.setRole("resident");
        resident.setStatus(status);
        resident.setDeletedAt(deletedAt);
        resident.setDeletionExpiresAt(deletionExpiresAt);
        return resident;
    }

    @Test
    @DisplayName("pending_deletion 返回冷静期到期时间")
    void pendingDeletion_exposesExpiry() {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 11, 19, 21, 16, 41);

        ResidentListItemResponse item = converter.toListItem(
                resident("pending_deletion", null, expiresAt), Map.of());

        assertEquals("2026-11-19 21:16:41", item.getDeletionExpiresAt());
    }

    @Test
    @DisplayName("active 不返回到期时间")
    void active_hasNoExpiry() {
        ResidentListItemResponse item = converter.toListItem(
                resident("active", null, LocalDateTime.of(2026, 11, 19, 21, 16, 41)), Map.of());

        assertNull(item.getDeletionExpiresAt());
    }

    @Test
    @DisplayName("deleted_at 不再被当成冷静期时间：已注销账号不输出到期时间")
    void deletedAtIsNotCoolingPeriod() {
        ResidentListItemResponse item = converter.toListItem(
                resident("deleted", LocalDateTime.of(2026, 11, 5, 10, 0, 0), null), Map.of());

        assertNull(item.getDeletionExpiresAt());
    }
}
