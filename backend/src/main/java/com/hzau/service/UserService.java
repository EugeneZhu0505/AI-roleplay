package com.hzau.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzau.entity.User;
import com.hzau.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: UserService
 * @author: zhuyuchen
 * @description: 用户服务类
 * @date: 2025/9/22 下午4:12
 */

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    /**
     * 验证密码
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 加密密码
     * @param password
     * @return
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 注册用户
     * @param username
     * @param password
     * @return
     */
    public User registerUser(String username, String password) {
        // 检查用户名是否已存在
        if (getUserByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }


        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encodePassword(password));

        // 保存用户
        this.save(user);
        return user;
    }
}

