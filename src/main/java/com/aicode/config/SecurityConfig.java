package com.aicode.config;

import com.aicode.security.filter.JwtAuthenticationFilter;
import com.aicode.security.handler.AccessDeniedHandlerImpl;
import com.aicode.security.handler.AuthenticationEntryPointImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * JWT 无状态认证架构
 */
@Configuration
@EnableWebSecurity  // 启用 Spring Security 的 Web 安全支持
@EnableMethodSecurity   // 启用方法级别的安全控制（如 @PreAuthorize）
@RequiredArgsConstructor    // Lombok 注解，为 final 字段生成构造函数
public class SecurityConfig {

    //拦截请求，验证 JWT Token
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    //户未登录时返回 401
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    //用户权限不足时返回 403
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    /**
     * 放行路径（无需认证）
     */
    private static final String[] PERMIT_ALL_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（JWT 无状态，无需 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)

                // 无状态会话
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 请求授权
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated()
                )

                // JWT 过滤器（在 UsernamePasswordAuthenticationFilter 之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
