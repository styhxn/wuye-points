package com.wuye.points.controller;

import com.wuye.points.dto.auth.AuthLoginResponse;
import com.wuye.points.dto.auth.BindPhoneRequest;
import com.wuye.points.dto.auth.BindPhoneResponse;
import com.wuye.points.dto.auth.ProfileResponse;
import com.wuye.points.dto.auth.RefreshTokenRequest;
import com.wuye.points.dto.auth.TokenResponse;
import com.wuye.points.dto.auth.WechatLoginRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证模块
 * Auth: Optional - wechat-login / refresh-token
 * Auth: Required - bind-phone / profile
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 微信小程序登录
     * Auth: Optional
     */
    @PostMapping("/wechat-login")
    public ApiResponse<AuthLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(authService.wechatLogin(request));
    }

    /**
     * 刷新 Token（滚动续期）
     * Auth: Optional
     */
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    /**
     * 绑定手机号
     * Auth: Required
     * Roles: resident
     */
    @PostMapping("/bind-phone")
    public ApiResponse<BindPhoneResponse> bindPhone(@Valid @RequestBody BindPhoneRequest request) {
        return ApiResponse.success(authService.bindPhone(request));
    }

    /**
     * 获取当前用户信息
     * Auth: Required
     * Roles: resident, property_admin, platform_admin
     */
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.success(authService.getProfile());
    }
}
