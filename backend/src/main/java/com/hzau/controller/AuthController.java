package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.common.utils.JwtUtils;
import com.hzau.dto.UserLoginReq;
import com.hzau.dto.UserLoginRes;
import com.hzau.dto.UserRegisterReq;
import com.hzau.entity.User;
import com.hzau.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: AuthController
 * @author: zhuyuchen
 * @description: 认证控制器
 * @date: 2025/9/22 下午4:25
 */

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     * @param userRegisterReq
     * @return
     */
    @PostMapping("/register")
    public Result<UserLoginRes> register(@Valid @RequestBody UserRegisterReq userRegisterReq) {
        try {
            // 注册用户
            User user = userService.registerUser(
                    userRegisterReq.getUsername(),
                    userRegisterReq.getPassword()
            );

            // 生成JWT token
            String token = jwtUtils.generateToken(user.getUsername(), user.getId().longValue());

            // 构建响应
            UserLoginRes response = new UserLoginRes(
                    token,
                    user.getId().longValue(),
                    user.getUsername(),
                    user.getAvatarUrl()
            );

            log.info("用户 {} 注册成功", user.getUsername());
            return Result.success(response, "注册成功");
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.fail(ErrorCode.ERROR400.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.fail(ErrorCode.ERROR500.getCode(), "注册失败");
        }
    }

    /**
     * 登录
     * @param userLoginReq
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginRes> login(@Valid @RequestBody UserLoginReq userLoginReq) {
        try {
            // 查找用户 - 支持用户名或邮箱登录
            User user = userService.getUserByUsername(userLoginReq.getUsername());
            
            if (user == null) {
                return Result.fail(ErrorCode.ERROR400.getCode(), "用户名或密码错误");
            }

            // 验证密码
            if (!userService.validatePassword(userLoginReq.getPassword(), user.getPasswordHash())) {
                return Result.fail(ErrorCode.ERROR400.getCode(), "用户名或密码错误");
            }

            // 生成JWT token
            String token = jwtUtils.generateToken(user.getUsername(), user.getId().longValue());

            // 构建响应
            UserLoginRes response = new UserLoginRes(
                    token,
                    user.getId().longValue(),
                    user.getUsername(),
                    user.getAvatarUrl()
            );

            log.info("用户 {} 登录成功", user.getUsername());
            return Result.success(response, "登录成功");
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.fail(ErrorCode.ERROR500.getCode(), "登录失败");
        }
    }

    /**
     * 获取当前用户
     * @return
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // 提取token
            String token = authHeader.replace("Bearer ", "");

            // 验证token
            if (!jwtUtils.validateToken(token)) {
                return Result.fail(ErrorCode.ERROR400.getCode(), "token无效");
            }

            // 获取用户信息
            String username = jwtUtils.getUsernameFromToken(token);
            User user = userService.getUserByUsername(username);

            if (user == null) {
                return Result.fail(ErrorCode.ERROR400.getCode(), "用户不存在");
            }

            // 清除敏感信息
            user.setPasswordHash(null);

            return Result.success(user);

        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取用户信息失败");
        }
    }
}

