package com.wuye.points.service;

import com.wuye.points.util.PermissionGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PermissionGuard 权限工具测试")
class PermissionGuardTest {

    @Test
    @DisplayName("normalizeCode - 冒号转点号")
    void normalizeCode_colonToDot() {
        assertEquals("merchant.audit", PermissionGuard.normalizeCode("merchant:audit"));
    }

    @Test
    @DisplayName("normalizeCode - 空值处理")
    void normalizeCode_nullOrEmpty() {
        assertNull(PermissionGuard.normalizeCode(null));
        assertEquals("", PermissionGuard.normalizeCode(""));
    }

    @Test
    @DisplayName("normalizeCode - 正常权限码")
    void normalizeCode_normalCode() {
        assertEquals("permission.read", PermissionGuard.normalizeCode("permission:read"));
        assertEquals("pool.adjust", PermissionGuard.normalizeCode("pool:adjust"));
    }
}
