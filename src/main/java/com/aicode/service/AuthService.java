package com.aicode.service;

import com.aicode.dto.LoginRequest;
import com.aicode.dto.LoginResponse;
import com.aicode.dto.RegisterRequest;
import com.aicode.dto.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUser(Long userId);
}
