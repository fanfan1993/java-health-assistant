package com.mind.assistant.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mind.assistant.exception.BizException;
import com.mind.assistant.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具类：HS256 签名，过期 7 天
 * Claims：userId、username、role
 */
@Component
public class JwtUtil {

    /** 签名密钥（从配置读取，生产环境务必替换） */
    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（毫秒），默认 7 天 */
    @Value("${jwt.expire-millis:604800000}")
    private long expireMillis;

    /**
     * 生成 token
     */
    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + expireMillis))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 校验并解析 token，失败抛出 401 业务异常
     */
    public DecodedJWT verify(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        } catch (JWTVerificationException e) {
            throw new BizException(ResultCode.UNAUTHORIZED.getCode(), "登录已过期，请重新登录");
        }
    }
}
