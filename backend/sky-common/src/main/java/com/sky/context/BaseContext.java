package com.sky.context;

import com.sky.enumeration.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求上下文 —— 通过 ThreadLocal 存储当前登录用户信息
 */
public class BaseContext {

    private static final ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(LoginUser user) {
        threadLocal.set(user);
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        return threadLocal.get();
    }

    /**
     * 获取当前登录用户的 ID（兼容旧调用方）
     */
    public static Long getCurrentId() {
        LoginUser user = threadLocal.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户的类型
     */
    public static UserType getCurrentUserType() {
        LoginUser user = threadLocal.get();
        return user != null ? user.getUserType() : null;
    }

    /**
     * 移除当前线程的用户信息
     */
    public static void removeCurrentUser() {
        threadLocal.remove();
    }

    // ==================== 兼容旧 API ====================

    /**
     * @deprecated 请使用 {@link #setCurrentUser(LoginUser)}
     */
    @Deprecated
    public static void setCurrentId(Long id) {
        threadLocal.set(new LoginUser(id, null));
    }

    /**
     * @deprecated 请使用 {@link #removeCurrentUser()}
     */
    @Deprecated
    public static void removeCurrentId() {
        threadLocal.remove();
    }

    // ==================== 内部类 ====================

    @Data
    @AllArgsConstructor
    public static class LoginUser {
        private Long id;
        private UserType userType;
    }
}
