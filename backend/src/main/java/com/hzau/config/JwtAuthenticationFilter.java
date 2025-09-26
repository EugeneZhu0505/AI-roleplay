package com.hzau.config;

import com.hzau.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: JwtAuthenticationFilter
 * @author: zhuyuchen
 * @description: WebFlux JWT认证过滤器
 * @date: 2025/9/23 上午10:05
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.info("处理请求: {} {}", method, requestURI);

        // 检查是否是公开路径，如果是则直接放行
        if (isPublicPath(requestURI)) {
            log.info("公开路径，直接放行: {}", requestURI);
            return chain.filter(exchange);
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

                    if (username != null) {
                        // 创建认证对象，给用户基本的USER权限
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                    username, 
                                    null, 
                                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
                                );

                        // 将用户ID和用户名存储到exchange的attributes中，方便后续使用
                        exchange.getAttributes().put("userId", userId);
                        exchange.getAttributes().put("username", username);

                        log.info("用户 {} 认证成功，权限: {}", username, authentication.getAuthorities());
                        
                        // 在ReactiveSecurityContext中设置认证信息，并继续过滤链
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }
                } catch (Exception e) {
                    log.error("JWT认证失败: {}", e.getMessage(), e);
                    // 认证失败，继续过滤链但不设置认证上下文
                    return chain.filter(exchange);
                }
            } else {
                log.warn("Token验证失败");
            }
        } else {
            log.warn("请求中没有找到token");
        }

        // 没有有效token或token验证失败，继续过滤链
        return chain.filter(exchange);
    }

    /**
     * 判断是否是公开路径
     * @param requestURI
     * @return
     */
    private boolean isPublicPath(String requestURI) {
        // 允许公开访问的路径
        String[] publicPaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/swagger-ui", 
            "/swagger-ui.html",
            "/swagger-ui/",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/v3/api-docs.yaml",
            "/v3/api-docs/swagger-config",
            "/webjars/",
            "/swagger-resources",
            "/swagger-resources/",
            "/configuration/ui",
            "/configuration/security",
            "/actuator",
            "/error",
            "/files/"
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
     * @param request
     * @return
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        // 首先检查 Authorization 头
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 然后检查 accessToken 头
        String accessToken = request.getHeaders().getFirst("accessToken");
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }
        
        return null;
    }
}

