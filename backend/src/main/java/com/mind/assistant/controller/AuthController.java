package com.mind.assistant.controller;
import com.mind.assistant.service.AuthService;
import com.mind.assistant.vo.UserVO;


import com.mind.assistant.security.UserContext;
import com.mind.assistant.common.Result;
import com.mind.assistant.dto.LoginDTO;
import com.mind.assistant.dto.RegisterDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 当前用户信息
 */
@Validated
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册：POST /api/auth/register
     */
    @PostMapping("/auth/register")
    public Result<AuthService.LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto.getUsername(), dto.getPassword(), dto.getNickname()));
    }

    /**
     * 登录：POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public Result<AuthService.LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto.getUsername(), dto.getPassword()));
    }

    /**
     * 当前登录用户信息：GET /api/user/me
     */
    @GetMapping("/user/me")
    public Result<UserVO> me() {
        return Result.success(authService.currentUser(UserContext.getUserId()));
    }
}
