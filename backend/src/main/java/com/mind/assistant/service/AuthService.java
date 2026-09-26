package com.mind.assistant.service;
import com.mind.assistant.entity.User;
import com.mind.assistant.mapper.UserMapper;
import com.mind.assistant.vo.UserVO;


import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mind.assistant.security.JwtUtil;
import com.mind.assistant.exception.BizException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证服务：注册 / 登录
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 注册：查重 -> BCrypt 加密 -> 插入 -> 生成 token
     */
    public LoginVO register(String username, String password, String nickname) {
        // 1. 用户名查重
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", username));
        if (count > 0) {
            throw new BizException("用户名已存在，请换一个试试");
        }

        // 2. BCrypt 加密密码并插入
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setRole("USER");
        userMapper.insert(user);

        // 3. 直接登录：生成 token
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(UserVO.from(user));
        return vo;
    }

    /**
     * 登录：查用户 -> BCrypt 校验 -> 生成 token
     */
    public LoginVO login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(UserVO.from(user));
        return vo;
    }

    /**
     * 当前用户信息
     */
    public UserVO currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return UserVO.from(user);
    }

    /**
     * 登录/注册返回结构：token + 用户信息
     */
    @Data
    public static class LoginVO {
        private String token;
        private UserVO user;
    }
}
