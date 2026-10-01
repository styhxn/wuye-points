package com.wuye.points.util;

import org.springframework.util.StringUtils;

/**
 * 权限码辅助：manage 权限隐含对应 read 权限
 */
public final class PermissionCodes {

    private PermissionCodes() {
    }

    public static String toManageCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode) || !permissionCode.endsWith(".read")) {
            return null;
        }
        return permissionCode.substring(0, permissionCode.length() - ".read".length()) + ".manage";
    }
}
