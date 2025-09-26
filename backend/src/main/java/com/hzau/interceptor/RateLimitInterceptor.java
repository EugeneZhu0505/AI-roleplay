package com.hzau.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.interceptor
 * @className: RateLimitInterceptor
 * @author: zhuyuchen
 * @description: WebFlux 限流过滤器
 * @date: 2025/9/23 下午3:15
 */
@Slf4j
@Component
public class RateLimitInterceptor implements WebFilter {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();
        
        // 只对AI相关接口进行限流
        if (!shouldApplyRateLimit(requestURI)) {
            return chain.filter(exchange);
        }

        // 获取用户ID（从请求参数或exchange attributes中获取）
        Integer userId = getUserIdFromRequest(request, exchange);
        String clientIp = getClientIpAddress(request);

        boolean allowed = false;
        String limitType = "";

        // 优先使用用户ID进行限流
        if (userId != null) {
            allowed = rateLimitService.isAllowed(userId);
            limitType = "用户";
        } else {
            // 如果没有用户ID，使用IP地址限流
            allowed = rateLimitService.isAllowedByIp(clientIp);
            limitType = "IP";
        }

        if (!allowed) {
            log.warn("请求被限流拦截, {}限流, URI: {}, userId: {}, IP: {}",
                    limitType, requestURI, userId, clientIp);

            // 返回限流错误响应
            return writeRateLimitResponse(exchange.getResponse());
        }

        log.debug("请求通过限流检查, URI: {}, userId: {}, IP: {}", requestURI, userId, clientIp);
        return chain.filter(exchange);
    }

    /**
     * 判断是否需要对该请求进行限流
     * @param requestURI 请求URI
     * @return true表示需要限流，false表示不需要
     */
    private boolean shouldApplyRateLimit(String requestURI) {
        // 对AI相关接口进行限流
        return requestURI.startsWith("/api/roleplay/") ||
                requestURI.startsWith("/ai/chat/");
    }

    /**
     * 从请求中获取用户ID
     * @param request HTTP请求
     * @param exchange ServerWebExchange
     * @return 用户ID，如果获取不到返回null
     */
    private Integer getUserIdFromRequest(ServerHttpRequest request, ServerWebExchange exchange) {
        // 从请求参数中获取用户ID
        String userIdParam = request.getQueryParams().getFirst("userId");
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            try {
                return Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdParam);
            }
        }

        // 从exchange attributes中获取用户ID（由JwtAuthenticationFilter设置）
        Object userIdAttr = exchange.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return ((Long) userIdAttr).intValue();
        } else if (userIdAttr instanceof Integer) {
            return (Integer) userIdAttr;
        }

        return null;
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ? 
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 写入限流错误响应
     * @param response HTTP响应
     * @return Mono<Void>
     */
    private Mono<Void> writeRateLimitResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        try {
            Result<String> result = Result.fail(ErrorCode.ERROR100.getCode(), "请求过于频繁，请稍后再试");
            String jsonResponse = objectMapper.writeValueAsString(result);
            
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写入限流响应失败", e);
            return response.setComplete();
        }
    }
}

