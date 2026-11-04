package com.wuye.points.util;

/**
 * 管理端请求级授权标记。
 *
 * <p>{@code PermissionInterceptor} 在按路径映射完成权限码校验后设置该标记。
 * 业务层仍保留的历史角色门（如 {@code requireAnyRole("property_admin", "platform_admin")}）
 * 可据此放行已持有对应权限码的标签身份（活动组长/统筹/板块/个体负责人/社区管理员），
 * 避免“权限配置里能勾选、管理端接口却按角色一刀切”的不一致。</p>
 *
 * <p>标记只对当前线程（当前请求）生效，拦截器 afterCompletion 必须清理。</p>
 */
public final class AdminAuthorizationContext {

    private static final ThreadLocal<Boolean> GRANTED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private AdminAuthorizationContext() {
    }

    public static void markGranted() {
        GRANTED.set(Boolean.TRUE);
    }

    public static boolean isGranted() {
        return Boolean.TRUE.equals(GRANTED.get());
    }

    public static void clear() {
        GRANTED.remove();
    }
}
