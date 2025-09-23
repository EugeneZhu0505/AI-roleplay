package com.hzau.config;

import com.hzau.common.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: JwtAuthenticationFilter
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 上午10:05
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("处理请求: {} {}", request.getMethod(), requestURI);

        // 检查是否是公开路径，如果是则直接放行
        if (isPublicPath(requestURI)) {
            log.info("公开路径，直接放行: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        log.info("从请求中获取到的token: {}", token != null ? "存在" : "不存在");

        if (StringUtils.hasText(token)) {
            boolean isValid = jwtUtils.validateToken(token);
            log.info("Token验证结果: {}", isValid);
            
            if (isValid) {
                try {
                    String username = jwtUtils.getUsernameFromToken(token);
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    log.info("从token中解析出用户: {}, ID: {}", username, userId);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 创建认证对象，给用户基本的USER权限
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                    username, 
                                    null, 
                                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 将用户ID存储到请求属性中，方便后续使用
                        request.setAttribute("userId", userId);
                        request.setAttribute("username", username);

                        // 设置认证上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("用户 {} 认证成功，权限: {}", username, authentication.getAuthorities());
                        log.info("SecurityContext中的认证信息: {}", SecurityContextHolder.getContext().getAuthentication());
                    }
                } catch (Exception e) {
                    log.error("JWT认证失败: {}", e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("Token验证失败，清除SecurityContext");
                SecurityContextHolder.clearContext();
            }
        } else {
            log.info("请求中没有找到token，清除SecurityContext");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否是公开路径
     */
    private boolean isPublicPath(String requestURI) {
        String[] publicPaths = {
            "/auth/login", 
            "/auth/register", 
            "/swagger-ui", 
            "/v3/api-docs",
            "/actuator",
            "/error"
        };
        
        for (String path : publicPaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中获取JWT token
     * 支持两种格式：
     * 1. Authorization: Bearer <token>
     * 2. accessToken: <token>
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 首先检查 Authorization 头
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 然后检查 accessToken 头
        String accessToken = request.getHeader("accessToken");
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }
        
        return null;
    }
}

