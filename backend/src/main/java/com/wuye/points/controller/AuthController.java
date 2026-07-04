package com.wuye.points.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.wuye.points.dto.auth.AdminLoginRequest;
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
@Tag(name = "鉴权模块", description = "登录、Token刷新、手机号绑定、用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 微信小程序登录
     * Auth: Optional
     */
    @Operation(summary = "微信小程序登录", description = "微信小程序登录，无需鉴权")
    @PostMapping("/wechat-login")
    public ApiResponse<AuthLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success(authService.wechatLogin(request));
    }

    /**
     * 管理端登录（手机号+密码）
     * Auth: Optional
     */
    @Operation(summary = "管理端登录", description = "管理端登录（手机号+密码），无需鉴权")
    @PostMapping("/admin-login")
    public ApiResponse<AuthLoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(authService.adminLogin(request));
    }

    /**
     * 刷新 Token（滚动续期）
     * Auth: Optional
     */
    @Operation(summary = "刷新Token", description = "刷新Token（滚动续期），无需鉴权")
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    /**
     * 绑定手机号
     * Auth: Required
     * Roles: resident
     */
    @Operation(summary = "绑定手机号", description = "绑定手机号，需登录，角色：resident")
    @PostMapping("/bind-phone")
    public ApiResponse<BindPhoneResponse> bindPhone(@Valid @RequestBody BindPhoneRequest request) {
        return ApiResponse.success(authService.bindPhone(request));
    }

    /**
     * 获取当前用户信息
     * Auth: Required
     * Roles: resident, property_admin, platform_admin
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前用户信息，需登录，角色：resident, property_admin, platform_admin")
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.success(authService.getProfile());
    }
}
