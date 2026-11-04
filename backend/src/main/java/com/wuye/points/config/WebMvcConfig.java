package com.wuye.points.config;

import com.wuye.points.interceptor.AuthInterceptor;
import com.wuye.points.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    @Value("${app.upload.path:/data/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/wechat-login",
                        "/auth/admin-login",
                        "/auth/refresh-token",
                        // 微信支付/分账/转账回调：由微信服务器直接调用，不带业务 Bearer。
                        // 控制器内部完成验签、解密与金额校验，不能在这里跳过这些校验。
                        "/payment/notify/wechatpay",
                        "/payment/notify/wechatpay-direct",
                        "/payment/ecommerce-split/notify",
                        "/payment/ecommerce-transfer/notify",
                        "/error",
                        "/actuator/**",
                        "/static/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                )
                .order(1);

        // 除 /admin/** 外，管理端还会调用公告/商家/报表的既有接口；
        // PermissionInterceptor 内部只对管理端工作台身份和显式映射的方法+路径校验，
        // 不会影响住户/商家移动端的同名接口。
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/admin/**", "/announcements/**", "/reports/**")
                .order(2);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + location);
    }
}
