package com.mind.assistant.security;

/**
 * 用户上下文：登录解析后的用户信息保存在 ThreadLocal 中，
 * 供各 Service / Controller 直接获取当前登录用户
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    /** 当前登录用户 ID */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 当前登录用户角色：USER / ADMIN */
    public static String getRole() {
        return ROLE.get();
    }

    /** 请求结束后清理，防止线程复用导致内存泄漏 / 数据串号 */
    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
