package com.wuye.points.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要幂等保护的写接口。
 *
 * <p>前端在请求头携带 {@code X-Request-Id}（8~128 位字母/数字/._:-）；
 * 同一用户 + 同一路径 + 同一 requestId 的重复请求会回放首次成功响应。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}
