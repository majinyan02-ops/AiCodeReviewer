package com.aicode.security.filter;

import com.aicode.security.utils.JwtUtils;
import com.aicode.security.model.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 - 每个请求校验 Token 签名 + Redis 会话
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String LOGIN_USER_KEY = "login:user:";

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);

            // Redis 会话校验：Token 必须在 Redis 中存在且匹配
            String redisKey = LOGIN_USER_KEY + userId;
            String cachedToken = (String) redisTemplate.opsForValue().get(redisKey);

            if (cachedToken != null && cachedToken.equals(token)) {
                String username = jwtUtils.getUsernameFromToken(token);

                LoginUser loginUser = new LoginUser(
                        com.aicode.entity.User.builder()
                                .id(userId)
                                .username(username)
                                .password("")
                                .role(jwtUtils.parseToken(token).get("role", String.class))
                                .build()
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Redis 会话不存在或 Token 不匹配: userId={}", userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取Token
     * Authorization: Bearer <token>
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
