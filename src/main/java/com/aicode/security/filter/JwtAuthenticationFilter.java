package com.aicode.security.filter;

import com.aicode.security.utils.JwtUtils;
import com.aicode.security.model.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 - 每个请求校验Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);

            // 构建LoginUser放入SecurityContext
            LoginUser loginUser = new LoginUser(
                    com.aicode.entity.User.builder()
                            .id(userId)
                            .username(username)
                            .password("")
                            .role(jwtUtils.parseToken(token).get("role", String.class))
                            .build()
            );
            // 5. 放入SecurityContext（后续Controller可以通过SecurityUtils获取）
            // ① Principal: 用户身份信息
            // ② Credentials: 凭证（认证后设为null）
            // ③ Authorities: 权限列表
            //UsernamePasswordAuthenticationToken 是 Spring Security 的认证令牌，代表"当前用户已经通过认证"。
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

            //Spring Security 的全局安全上下文，类似于一个"保险箱"
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
