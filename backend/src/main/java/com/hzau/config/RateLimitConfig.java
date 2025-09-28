package com.hzau.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: RateLimitConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:13
 */
@Configuration
public class RateLimitConfig {

    /**
     * 内存限流器，用于简单的限流控制
     * Key: 用户ID或IP地址
     * Value: 限流信息
     */
    @Bean
    public ConcurrentMap<String, RateLimitInfo> rateLimitMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * 限流信息类
     */
    public static class RateLimitInfo {
        private long lastRequestTime;
        private int requestCount;
        private long windowStartTime;

        public RateLimitInfo() {
            long currentTime = System.currentTimeMillis();
            // 将lastRequestTime初始化为足够早的时间，避免第一次请求被误判为间隔过短
            this.lastRequestTime = currentTime - 10000; // 10秒前
            this.requestCount = 0; // 初始计数为0，在incrementCount中会增加到1
            this.windowStartTime = currentTime;
        }

        public long getLastRequestTime() {
            return lastRequestTime;
        }

        public void setLastRequestTime(long lastRequestTime) {
            this.lastRequestTime = lastRequestTime;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(int requestCount) {
            this.requestCount = requestCount;
        }

        public long getWindowStartTime() {
            return windowStartTime;
        }

        public void setWindowStartTime(long windowStartTime) {
            this.windowStartTime = windowStartTime;
        }

        public void incrementCount() {
            this.requestCount++;
            this.lastRequestTime = System.currentTimeMillis();
        }

        public void resetWindow() {
            long currentTime = System.currentTimeMillis();
            this.requestCount = 0; // 重置为0，在incrementCount中会增加到1
            this.windowStartTime = currentTime;
            // 重置lastRequestTime为足够早的时间，避免重置后立即请求被误判为间隔过短
            this.lastRequestTime = currentTime - 10000; // 10秒前
        }
    }
}
