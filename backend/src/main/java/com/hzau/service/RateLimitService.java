package com.hzau.service;

import com.hzau.config.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: RateLimitService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:14
 */
@Slf4j
@Service
public class RateLimitService {

    @Autowired
    private ConcurrentMap<String, RateLimitConfig.RateLimitInfo> rateLimitMap;

    // 限流配置
    private static final int MAX_REQUESTS_PER_MINUTE = 30; // 每分钟最大请求数
    private static final int MAX_REQUESTS_PER_HOUR = 300;  // 每小时最大请求数
    private static final long MINUTE_IN_MILLIS = 60 * 1000L;
    private static final long HOUR_IN_MILLIS = 60 * 60 * 1000L;

    /**
     * 检查用户是否被限流
     * @param userId 用户ID
     * @return true表示允许请求，false表示被限流
     */
    public boolean isAllowed(Integer userId) {
        return isAllowed("user:" + userId);
    }

    /**
     * 检查IP是否被限流
     * @param ipAddress IP地址
     * @return true表示允许请求，false表示被限流
     */
    public boolean isAllowedByIp(String ipAddress) {
        return isAllowed("ip:" + ipAddress);
    }

    /**
     * 通用限流检查方法
     * @param key 限流键（用户ID或IP地址）
     * @return true表示允许请求，false表示被限流
     */
    private boolean isAllowed(String key) {
        long currentTime = System.currentTimeMillis();

        RateLimitConfig.RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(key,
                k -> new RateLimitConfig.RateLimitInfo());

        synchronized (rateLimitInfo) {
            // 检查是否需要重置时间窗口（每分钟重置）
            if (currentTime - rateLimitInfo.getWindowStartTime() >= MINUTE_IN_MILLIS) {
                rateLimitInfo.resetWindow();
                log.debug("重置限流窗口, key: {}", key);
                // 重置后允许请求，并更新时间
                rateLimitInfo.incrementCount();
                return true;
            }

            // 检查当前分钟内的请求数
            if (rateLimitInfo.getRequestCount() >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("用户请求过于频繁，触发分钟级限流, key: {}, count: {}",
                        key, rateLimitInfo.getRequestCount());
                return false;
            }

            // 检查秒级限流（基于请求间隔）- 在更新时间戳之前检查
            long timeSinceLastRequest = currentTime - rateLimitInfo.getLastRequestTime();
            if (timeSinceLastRequest < 2000) { // 2秒内不能连续请求
                log.warn("用户请求间隔过短，触发秒级限流, key: {}, interval: {}ms",
                        key, timeSinceLastRequest);
                return false;
            }

            // 允许请求，更新计数和时间
            rateLimitInfo.incrementCount();
            log.debug("允许请求, key: {}, count: {}, 上次请求间隔: {}ms", 
                    key, rateLimitInfo.getRequestCount(), timeSinceLastRequest);
            return true;
        }
    }

    /**
     * 获取用户剩余请求次数
     * @param userId 用户ID
     * @return 剩余请求次数
     */
    public int getRemainingRequests(Integer userId) {
        return getRemainingRequests("user:" + userId);
    }

    /**
     * 获取IP剩余请求次数
     * @param ipAddress IP地址
     * @return 剩余请求次数
     */
    public int getRemainingRequestsByIp(String ipAddress) {
        return getRemainingRequests("ip:" + ipAddress);
    }

    /**
     * 通用获取剩余请求次数方法
     * @param key 限流键
     * @return 剩余请求次数
     */
    private int getRemainingRequests(String key) {
        RateLimitConfig.RateLimitInfo rateLimitInfo = rateLimitMap.get(key);
        if (rateLimitInfo == null) {
            return MAX_REQUESTS_PER_MINUTE;
        }

        long currentTime = System.currentTimeMillis();

        synchronized (rateLimitInfo) {
            // 如果时间窗口已过期，返回最大请求数
            if (currentTime - rateLimitInfo.getWindowStartTime() >= MINUTE_IN_MILLIS) {
                return MAX_REQUESTS_PER_MINUTE;
            }

            return Math.max(0, MAX_REQUESTS_PER_MINUTE - rateLimitInfo.getRequestCount());
        }
    }

    /**
     * 清理过期的限流记录
     */
    public void cleanupExpiredRecords() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;

        for (String key : rateLimitMap.keySet()) {
            RateLimitConfig.RateLimitInfo rateLimitInfo = rateLimitMap.get(key);
            if (rateLimitInfo != null) {
                synchronized (rateLimitInfo) {
                    // 清理1小时前的记录
                    if (currentTime - rateLimitInfo.getLastRequestTime() > HOUR_IN_MILLIS) {
                        rateLimitMap.remove(key);
                        cleanedCount++;
                    }
                }
            }
        }

        if (cleanedCount > 0) {
            log.info("清理过期限流记录, 清理数量: {}", cleanedCount);
        }
    }

    /**
     * 获取限流统计信息
     * @return 统计信息
     */
    public String getRateLimitStats() {
        return String.format("当前限流记录数: %d, 每分钟限制: %d次, 每小时限制: %d次",
                rateLimitMap.size(), MAX_REQUESTS_PER_MINUTE, MAX_REQUESTS_PER_HOUR);
    }
}
