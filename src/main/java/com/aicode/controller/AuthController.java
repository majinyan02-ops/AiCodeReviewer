package com.aicode.controller;

import com.aicode.common.Result;
import com.aicode.dto.LoginRequest;
import com.aicode.dto.LoginResponse;
import com.aicode.dto.RegisterRequest;
import com.aicode.dto.UserInfoResponse;
import com.aicode.security.utils.SecurityUtils;
import com.aicode.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 获取当前用户信息（测试JWT鉴权）
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> me() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserInfoResponse userInfo = authService.getCurrentUser(userId);
        return Result.success(userInfo);
    }
}
