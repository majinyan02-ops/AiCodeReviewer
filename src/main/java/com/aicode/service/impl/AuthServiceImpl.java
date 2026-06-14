package com.aicode.service.impl;

import com.aicode.common.ResultCode;
import com.aicode.dto.LoginRequest;
import com.aicode.dto.LoginResponse;
import com.aicode.dto.RegisterRequest;
import com.aicode.dto.UserInfoResponse;
import com.aicode.entity.User;
import com.aicode.exception.BusinessException;
import com.aicode.mapper.UserMapper;
import com.aicode.security.utils.JwtUtils;
import com.aicode.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOGIN_USER_KEY = "login:user:";

    @Override
    public void register(RegisterRequest request) {
        // 校验用户名唯一
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXISTS.getCode(), "用户名已存在");
        }

        // 校验邮箱唯一
        User existEmail = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
        );
        if (existEmail != null) {
            throw new BusinessException(ResultCode.USER_EXISTS.getCode(), "邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role("DEVELOPER")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        long expireTime = jwtUtils.getExpireSeconds();

        // 写入Redis
        String redisKey = LOGIN_USER_KEY + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, expireTime, TimeUnit.SECONDS);

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .expireTime(expireTime)
                .build();
    }

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
