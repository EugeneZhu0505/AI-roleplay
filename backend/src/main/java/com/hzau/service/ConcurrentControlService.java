package com.hzau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: ConcurrentControlService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午7:12
 */
@Service
@Slf4j
public class ConcurrentControlService {

    private final PerformanceMonitoringService monitoringService;
    private final Executor monitoringExecutor;

    // 全局并发控制
    private final Semaphore globalConcurrencyLimit;
    private final Semaphore llmRequestLimit;
    private final Semaphore streamingRequestLimit;

    // 用户级别并发控制
    private final ConcurrentMap<String, UserConcurrencyControl> userConcurrencyMap = new ConcurrentHashMap<>();

    // 动态限流参数
    private volatile int maxGlobalConcurrency = 100;
    private volatile int maxLlmConcurrency = 50;
    private volatile int maxStreamingConcurrency = 30;
    private volatile int maxUserConcurrency = 5;

    // 系统负载监控
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong rejectedRequests = new AtomicLong(0);
    private final AtomicInteger currentActiveRequests = new AtomicInteger(0);

    public ConcurrentControlService(PerformanceMonitoringService monitoringService,
                                    @Qualifier("monitoringExecutor") Executor monitoringExecutor) {
        this.monitoringService = monitoringService;
        this.monitoringExecutor = monitoringExecutor;

        // 初始化信号量
        this.globalConcurrencyLimit = new Semaphore(maxGlobalConcurrency, true);
        this.llmRequestLimit = new Semaphore(maxLlmConcurrency, true);
        this.streamingRequestLimit = new Semaphore(maxStreamingConcurrency, true);

        // 启动动态调整任务
        startDynamicAdjustmentTask();
    }

    /**
     * 用户并发控制数据结构
     */
    private static class UserConcurrencyControl {
        private final Semaphore userLimit;
        private final AtomicInteger activeRequests;
        private final AtomicLong lastRequestTime;
        private final AtomicLong totalUserRequests;

        public UserConcurrencyControl(int maxConcurrency) {
            this.userLimit = new Semaphore(maxConcurrency, true);
            this.activeRequests = new AtomicInteger(0);
            this.lastRequestTime = new AtomicLong(System.currentTimeMillis());
            this.totalUserRequests = new AtomicLong(0);
        }

        public Semaphore getUserLimit() { return userLimit; }
        public AtomicInteger getActiveRequests() { return activeRequests; }
        public AtomicLong getLastRequestTime() { return lastRequestTime; }
        public AtomicLong getTotalUserRequests() { return totalUserRequests; }
    }

    /**
     * 请求许可证（全局 + 用户级别）
     * @param userId 用户ID
     * @param requestType 请求类型（llm, streaming, normal）
     * @return 是否获得许可
     */
    public Mono<Boolean> acquirePermit(String userId, String requestType) {
        return Mono.fromCallable(() -> {
            totalRequests.incrementAndGet();

            // 1. 检查全局并发限制
            if (!globalConcurrencyLimit.tryAcquire()) {
                rejectedRequests.incrementAndGet();
                log.warn("全局并发限制达到上限，拒绝请求 - 用户: {}, 类型: {}", userId, requestType);
                return false;
            }

            // 2. 检查特定类型的并发限制
            Semaphore typeLimit = getTypeLimitSemaphore(requestType);
            if (typeLimit != null && !typeLimit.tryAcquire()) {
                globalConcurrencyLimit.release();
                rejectedRequests.incrementAndGet();
                log.warn("{}请求并发限制达到上限，拒绝请求 - 用户: {}", requestType, userId);
                return false;
            }

            // 3. 检查用户级别并发限制
            UserConcurrencyControl userControl = getUserConcurrencyControl(userId);
            if (!userControl.getUserLimit().tryAcquire()) {
                if (typeLimit != null) typeLimit.release();
                globalConcurrencyLimit.release();
                rejectedRequests.incrementAndGet();
                log.warn("用户并发限制达到上限，拒绝请求 - 用户: {}, 类型: {}", userId, requestType);
                return false;
            }

            // 4. 更新统计信息
            currentActiveRequests.incrementAndGet();
            userControl.getActiveRequests().incrementAndGet();
            userControl.getLastRequestTime().set(System.currentTimeMillis());
            userControl.getTotalUserRequests().incrementAndGet();

            log.debug("成功获取请求许可 - 用户: {}, 类型: {}, 当前活跃请求: {}",
                    userId, requestType, currentActiveRequests.get());
            return true;
        });
    }

    /**
     * 释放许可证
     * @param userId 用户ID
     * @param requestType 请求类型
     */
    public void releasePermit(String userId, String requestType) {
        try {
            // 1. 释放用户级别许可
            UserConcurrencyControl userControl = userConcurrencyMap.get(userId);
            if (userControl != null) {
                // 检查用户活跃请求数，避免重复释放导致负数
                int currentUserRequests = userControl.getActiveRequests().get();
                if (currentUserRequests > 0) {
                    userControl.getUserLimit().release();
                    userControl.getActiveRequests().decrementAndGet();
                } else {
                    log.warn("用户 {} 的活跃请求数已为 {}，跳过释放用户许可", userId, currentUserRequests);
                }
            }

            // 2. 释放类型级别许可
            Semaphore typeLimit = getTypeLimitSemaphore(requestType);
            if (typeLimit != null) {
                typeLimit.release();
            }

            // 3. 释放全局许可
            globalConcurrencyLimit.release();

            // 4. 更新统计 - 检查全局活跃请求数，避免负数
            int currentGlobalRequests = currentActiveRequests.get();
            if (currentGlobalRequests > 0) {
                currentActiveRequests.decrementAndGet();
            } else {
                log.warn("全局活跃请求数已为 {}，跳过递减操作", currentGlobalRequests);
            }

            log.debug("释放请求许可 - 用户: {}, 类型: {}, 当前活跃请求: {}",
                    userId, requestType, currentActiveRequests.get());
        } catch (Exception e) {
            log.error("释放许可证时发生错误 - 用户: {}, 类型: {}", userId, requestType, e);
        }
    }

    /**
     * 获取用户并发控制对象
     */
    private UserConcurrencyControl getUserConcurrencyControl(String userId) {
        return userConcurrencyMap.computeIfAbsent(userId,
                k -> new UserConcurrencyControl(maxUserConcurrency));
    }

    /**
     * 根据请求类型获取对应的信号量
     */
    private Semaphore getTypeLimitSemaphore(String requestType) {
        switch (requestType.toLowerCase()) {
            case "llm":
            case "chat":
                return llmRequestLimit;
            case "streaming":
            case "stream":
                return streamingRequestLimit;
            default:
                return null; // 普通请求不需要特殊限制
        }
    }

    /**
     * 启动动态调整任务
     */
    private void startDynamicAdjustmentTask() {
        // 每30秒执行一次动态调整
        Mono.delay(Duration.ofSeconds(30))
                .repeat()
                .doOnNext(tick -> adjustConcurrencyLimits())
                .subscribe();
    }

    /**
     * 动态调整并发限制
     */
    private void adjustConcurrencyLimits() {
        try {
            // 获取系统指标
            PerformanceMonitoringService.SystemMetrics systemMetrics = monitoringService.getSystemMetrics();
            double memoryUsage = systemMetrics.getMemoryUsagePercent();
            int threadCount = systemMetrics.getThreadCount();

            // 计算拒绝率
            long total = totalRequests.get();
            long rejected = rejectedRequests.get();
            double rejectionRate = total > 0 ? (double) rejected / total : 0.0;

            log.info("系统负载监控 - 内存使用率: {}%, 线程数: {}, 拒绝率: {}%, 活跃请求: {}",
                    String.format("%.2f", memoryUsage), threadCount, String.format("%.2f", rejectionRate * 100), currentActiveRequests.get());

            // 根据系统负载动态调整
            if (memoryUsage > 85.0 || rejectionRate > 0.1) {
                // 系统负载过高，降低并发限制
                reducesConcurrencyLimits();
            } else if (memoryUsage < 60.0 && rejectionRate < 0.02) {
                // 系统负载较低，可以适当提高并发限制
                increaseConcurrencyLimits();
            }

            // 清理长时间未活跃的用户控制对象
            cleanupInactiveUsers();

        } catch (Exception e) {
            log.error("动态调整并发限制时发生错误", e);
        }
    }

    /**
     * 降低并发限制
     */
    private void reducesConcurrencyLimits() {
        int newGlobalLimit = Math.max(50, (int) (maxGlobalConcurrency * 0.8));
        int newLlmLimit = Math.max(25, (int) (maxLlmConcurrency * 0.8));
        int newStreamingLimit = Math.max(15, (int) (maxStreamingConcurrency * 0.8));
        int newUserLimit = Math.max(3, (int) (maxUserConcurrency * 0.8));

        updateConcurrencyLimits(newGlobalLimit, newLlmLimit, newStreamingLimit, newUserLimit);
        log.info("系统负载过高，降低并发限制 - 全局: {}, LLM: {}, 流式: {}, 用户: {}",
                newGlobalLimit, newLlmLimit, newStreamingLimit, newUserLimit);
    }

    /**
     * 提高并发限制
     */
    private void increaseConcurrencyLimits() {
        int newGlobalLimit = Math.min(200, (int) (maxGlobalConcurrency * 1.1));
        int newLlmLimit = Math.min(100, (int) (maxLlmConcurrency * 1.1));
        int newStreamingLimit = Math.min(60, (int) (maxStreamingConcurrency * 1.1));
        int newUserLimit = Math.min(10, (int) (maxUserConcurrency * 1.1));

        updateConcurrencyLimits(newGlobalLimit, newLlmLimit, newStreamingLimit, newUserLimit);
        log.info("系统负载较低，提高并发限制 - 全局: {}, LLM: {}, 流式: {}, 用户: {}",
                newGlobalLimit, newLlmLimit, newStreamingLimit, newUserLimit);
    }

    /**
     * 更新并发限制
     */
    private void updateConcurrencyLimits(int globalLimit, int llmLimit, int streamingLimit, int userLimit) {
        // 更新全局限制
        int globalDiff = globalLimit - maxGlobalConcurrency;
        if (globalDiff > 0) {
            globalConcurrencyLimit.release(globalDiff);
        } else if (globalDiff < 0) {
            globalConcurrencyLimit.tryAcquire(-globalDiff);
        }
        maxGlobalConcurrency = globalLimit;

        // 更新LLM限制
        int llmDiff = llmLimit - maxLlmConcurrency;
        if (llmDiff > 0) {
            llmRequestLimit.release(llmDiff);
        } else if (llmDiff < 0) {
            llmRequestLimit.tryAcquire(-llmDiff);
        }
        maxLlmConcurrency = llmLimit;

        // 更新流式限制
        int streamingDiff = streamingLimit - maxStreamingConcurrency;
        if (streamingDiff > 0) {
            streamingRequestLimit.release(streamingDiff);
        } else if (streamingDiff < 0) {
            streamingRequestLimit.tryAcquire(-streamingDiff);
        }
        maxStreamingConcurrency = streamingLimit;

        // 更新用户限制（对新用户生效）
        maxUserConcurrency = userLimit;
    }

    /**
     * 清理长时间未活跃的用户控制对象
     */
    private void cleanupInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        long inactiveThreshold = 30 * 60 * 1000; // 30分钟

        userConcurrencyMap.entrySet().removeIf(entry -> {
            UserConcurrencyControl control = entry.getValue();
            boolean isInactive = (currentTime - control.getLastRequestTime().get()) > inactiveThreshold
                    && control.getActiveRequests().get() == 0;

            if (isInactive) {
                log.debug("清理非活跃用户控制对象: {}", entry.getKey());
            }
            return isInactive;
        });
    }

    /**
     * 获取并发控制统计信息
     */
    public ConcurrencyStats getConcurrencyStats() {
        ConcurrencyStats stats = new ConcurrencyStats();
        stats.setMaxGlobalConcurrency(maxGlobalConcurrency);
        stats.setMaxLlmConcurrency(maxLlmConcurrency);
        stats.setMaxStreamingConcurrency(maxStreamingConcurrency);
        stats.setMaxUserConcurrency(maxUserConcurrency);
        stats.setCurrentActiveRequests(currentActiveRequests.get());
        stats.setTotalRequests(totalRequests.get());
        stats.setRejectedRequests(rejectedRequests.get());
        stats.setActiveUsers(userConcurrencyMap.size());
        stats.setAvailableGlobalPermits(globalConcurrencyLimit.availablePermits());
        stats.setAvailableLlmPermits(llmRequestLimit.availablePermits());
        stats.setAvailableStreamingPermits(streamingRequestLimit.availablePermits());

        return stats;
    }

    /**
     * 并发控制统计数据类
     */
    public static class ConcurrencyStats {
        private int maxGlobalConcurrency;
        private int maxLlmConcurrency;
        private int maxStreamingConcurrency;
        private int maxUserConcurrency;
        private int currentActiveRequests;
        private long totalRequests;
        private long rejectedRequests;
        private int activeUsers;
        private int availableGlobalPermits;
        private int availableLlmPermits;
        private int availableStreamingPermits;

        // Getters and Setters
        public int getMaxGlobalConcurrency() { return maxGlobalConcurrency; }
        public void setMaxGlobalConcurrency(int maxGlobalConcurrency) { this.maxGlobalConcurrency = maxGlobalConcurrency; }

        public int getMaxLlmConcurrency() { return maxLlmConcurrency; }
        public void setMaxLlmConcurrency(int maxLlmConcurrency) { this.maxLlmConcurrency = maxLlmConcurrency; }

        public int getMaxStreamingConcurrency() { return maxStreamingConcurrency; }
        public void setMaxStreamingConcurrency(int maxStreamingConcurrency) { this.maxStreamingConcurrency = maxStreamingConcurrency; }

        public int getMaxUserConcurrency() { return maxUserConcurrency; }
        public void setMaxUserConcurrency(int maxUserConcurrency) { this.maxUserConcurrency = maxUserConcurrency; }

        public int getCurrentActiveRequests() { return currentActiveRequests; }
        public void setCurrentActiveRequests(int currentActiveRequests) { this.currentActiveRequests = currentActiveRequests; }

        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getRejectedRequests() { return rejectedRequests; }
        public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }

        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

        public int getAvailableGlobalPermits() { return availableGlobalPermits; }
        public void setAvailableGlobalPermits(int availableGlobalPermits) { this.availableGlobalPermits = availableGlobalPermits; }

        public int getAvailableLlmPermits() { return availableLlmPermits; }
        public void setAvailableLlmPermits(int availableLlmPermits) { this.availableLlmPermits = availableLlmPermits; }

        public int getAvailableStreamingPermits() { return availableStreamingPermits; }
        public void setAvailableStreamingPermits(int availableStreamingPermits) { this.availableStreamingPermits = availableStreamingPermits; }
    }
}
