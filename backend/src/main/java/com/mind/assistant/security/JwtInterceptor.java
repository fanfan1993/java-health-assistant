package com.mind.assistant.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mind.assistant.exception.BizException;
import com.mind.assistant.common.Result;
import com.mind.assistant.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器：
 * 1. 优先从 Authorization: Bearer xxx 请求头解析 token
 * 2. 兼容 SSE 场景：支持 query 参数 ?token=xxx
 * 3. 解析成功后将 userId / role 写入 UserContext（ThreadLocal）
 * 4. /api/admin/** 路径要求 ADMIN 角色
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 1. 提取 token：请求头优先，其次 query 参数
        String token = null;
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            token = request.getParameter("token");
        }
        if (!StringUtils.hasText(token)) {
            return reject(response, "请先登录");
        }

        // 2. 校验并解析 token
        DecodedJWT jwt;
        try {
            jwt = jwtUtil.verify(token);
        } catch (BizException e) {
            return reject(response, e.getMessage());
        }

        // 3. 写入 ThreadLocal 上下文
        Long userId = jwt.getClaim("userId").asLong();
        String role = jwt.getClaim("role").asString();
        UserContext.set(userId, role);

        // 4. 管理端接口的角色校验
        if (request.getRequestURI().startsWith("/api/admin") && !"ADMIN".equals(role)) {
            response.setStatus(ResultCode.FORBIDDEN.getCode());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(ResultCode.FORBIDDEN)));
            return false;
        }
        return true;
    }

    /**
     * 请求结束后清理 ThreadLocal，防止线程池复用导致数据串号
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.clear();
    }

    /**
     * 未登录 / token 无效：返回 401 JSON
     */
    private boolean reject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(ResultCode.UNAUTHORIZED.getCode());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(ResultCode.UNAUTHORIZED.getCode(), message)));
        return false;
    }
}
