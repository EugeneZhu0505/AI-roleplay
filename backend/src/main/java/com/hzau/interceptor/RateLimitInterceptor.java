package com.hzau.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.interceptor
 * @className: RateLimitIntercepter
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:15
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只对AI相关接口进行限流
        String requestURI = request.getRequestURI();
        if (!shouldApplyRateLimit(requestURI)) {
            return true;
        }

        // 获取用户ID（从请求参数或JWT token中获取）
        Integer userId = getUserIdFromRequest(request);
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
            writeRateLimitResponse(response);
            return false;
        }

        log.debug("请求通过限流检查, URI: {}, userId: {}, IP: {}", requestURI, userId, clientIp);
        return true;
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
     * @return 用户ID，如果获取不到返回null
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        // 从请求参数中获取用户ID
        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            try {
                return Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdParam);
            }
        }

        // TODO: 从JWT token中获取用户ID
        // 这里可以添加从JWT token中解析用户ID的逻辑

        return null;
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 写入限流错误响应
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        response.setContentType("application/json;charset=UTF-8");

        Result<String> result = Result.fail(ErrorCode.ERROR100.getCode(), "请求过于频繁，请稍后再试");
        String jsonResponse = objectMapper.writeValueAsString(result);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}

