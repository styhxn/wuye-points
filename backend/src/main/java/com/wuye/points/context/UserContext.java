package com.wuye.points.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文（从 JWT 解析后注入）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private String role;
    private String propertyCompanyId;
    private String familyId;

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static String currentUserId() {
        UserContext ctx = get();
        return ctx == null ? null : ctx.getUserId();
    }

    public static String currentPropertyCompanyId() {
        UserContext ctx = get();
        return ctx == null ? null : ctx.getPropertyCompanyId();
    }

    public static String currentRole() {
        UserContext ctx = get();
        return ctx == null ? null : ctx.getRole();
    }
}
