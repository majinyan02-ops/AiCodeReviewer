package com.aicode.security.utils;

import com.aicode.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 上下文工具类
 * 所有方法都是静态方法，直接通过类名调用
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        // ① 从安全上下文获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // ② 检查认证是否存在，且主体是 LoginUser 类型
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        // ③ 如果认证不存在或主体不是 LoginUser 类型，返回 null
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }
}
