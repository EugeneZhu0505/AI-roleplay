package com.hzau.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: PerformanceMonitoringService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午7:06
 */
@Service
@Slf4j
public class PerformanceMonitoringService {

    private final Executor monitoringExecutor;

    // API响应时间统计
    private final Map<String, List<Long>> apiResponseTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> apiCallCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> apiErrorCounts = new ConcurrentHashMap<>();

    // 并发用户统计
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, LocalDateTime> userLastActivity = new ConcurrentHashMap<>();

    // 系统资源监控
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // 历史数据存储（最近1小时的数据）
    private final Queue<MetricsSnapshot> metricsHistory = new LinkedList<>();
    private final int MAX_HISTORY_SIZE = 60; // 保存60个快照（每分钟一个）

    public PerformanceMonitoringService(@Qualifier("monitoringExecutor") Executor monitoringExecutor) {
        this.monitoringExecutor = monitoringExecutor;
    }

    /**
     * 记录API调用开始
     */
    public String startApiCall(String apiName, String userId) {
        String callId = UUID.randomUUID().toString();

        // 异步更新用户活跃状态
        monitoringExecutor.execute(() -> {
            activeUsers.add(userId);
            userLastActivity.put(userId, LocalDateTime.now());
        });

        return callId;
    }

    /**
     * 记录API调用结束
     */
    public void endApiCall(String apiName, String callId, long responseTime, boolean success) {
        monitoringExecutor.execute(() -> {
            // 记录响应时间
            apiResponseTimes.computeIfAbsent(apiName, k -> new ArrayList<>()).add(responseTime);

            // 记录调用次数
            apiCallCounts.computeIfAbsent(apiName, k -> new AtomicLong(0)).incrementAndGet();

            // 记录错误次数
            if (!success) {
                apiErrorCounts.computeIfAbsent(apiName, k -> new AtomicLong(0)).incrementAndGet();
            }

            // 清理过期的响应时间数据（保留最近1000条）
            List<Long> times = apiResponseTimes.get(apiName);
            if (times != null && times.size() > 1000) {
                times.subList(0, times.size() - 1000).clear();
            }
        });
    }

    /**
     * 获取API响应时间统计
     */
    public Map<String, ApiMetrics> getApiMetrics() {
        Map<String, ApiMetrics> result = new HashMap<>();

        for (String apiName : apiResponseTimes.keySet()) {
            List<Long> times = apiResponseTimes.get(apiName);
            if (times != null && !times.isEmpty()) {
                ApiMetrics metrics = calculateApiMetrics(apiName, times);
                result.put(apiName, metrics);
            }
        }

        return result;
    }

    /**
     * 获取当前并发用户数
     */
    public int getCurrentConcurrentUsers() {
        // 清理非活跃用户（5分钟内无活动）
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        userLastActivity.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        activeUsers.retainAll(userLastActivity.keySet());

        return activeUsers.size();
    }

    /**
     * 获取系统资源使用率
     */
    public SystemMetrics getSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();

        // 内存使用率
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        metrics.setMemoryUsagePercent((double) usedMemory / maxMemory * 100);

        // 线程数
        metrics.setThreadCount(threadBean.getThreadCount());

        // CPU使用率（简化版本，实际项目中可以使用更精确的方法）
        Runtime runtime = Runtime.getRuntime();
        metrics.setCpuCores(runtime.availableProcessors());

        return metrics;
    }

    /**
     * 获取错误率统计
     */
    public Map<String, Double> getErrorRates() {
        Map<String, Double> errorRates = new HashMap<>();

        for (String apiName : apiCallCounts.keySet()) {
            long totalCalls = apiCallCounts.get(apiName).get();
            long errorCalls = apiErrorCounts.getOrDefault(apiName, new AtomicLong(0)).get();

            if (totalCalls > 0) {
                double errorRate = (double) errorCalls / totalCalls * 100;
                errorRates.put(apiName, errorRate);
            }
        }

        return errorRates;
    }

    /**
     * 获取完整的监控报告
     */
    public MonitoringReport getMonitoringReport() {
        MonitoringReport report = new MonitoringReport();
        report.setTimestamp(LocalDateTime.now());
        report.setApiMetrics(getApiMetrics());
        report.setConcurrentUsers(getCurrentConcurrentUsers());
        report.setSystemMetrics(getSystemMetrics());
        report.setErrorRates(getErrorRates());
        return report;
    }

    /**
     * 定时任务：每分钟收集一次监控数据快照
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Async("monitoringExecutor")
    public void collectMetricsSnapshot() {
        try {
            MetricsSnapshot snapshot = new MetricsSnapshot();
            snapshot.setTimestamp(LocalDateTime.now());
            snapshot.setConcurrentUsers(getCurrentConcurrentUsers());
            snapshot.setSystemMetrics(getSystemMetrics());
            snapshot.setErrorRates(getErrorRates());

            // 添加到历史记录
            metricsHistory.offer(snapshot);

            // 保持历史记录大小
            while (metricsHistory.size() > MAX_HISTORY_SIZE) {
                metricsHistory.poll();
            }

            log.debug("监控数据快照收集完成，当前并发用户数: {}", snapshot.getConcurrentUsers());
        } catch (Exception e) {
            log.error("收集监控数据快照时发生错误", e);
        }
    }

    /**
     * 获取历史监控数据
     */
    public List<MetricsSnapshot> getMetricsHistory() {
        return new ArrayList<>(metricsHistory);
    }

    /**
     * 计算API指标
     */
    private ApiMetrics calculateApiMetrics(String apiName, List<Long> times) {
        ApiMetrics metrics = new ApiMetrics();
        metrics.setApiName(apiName);
        metrics.setTotalCalls(apiCallCounts.getOrDefault(apiName, new AtomicLong(0)).get());
        metrics.setErrorCount(apiErrorCounts.getOrDefault(apiName, new AtomicLong(0)).get());

        if (!times.isEmpty()) {
            // 计算平均响应时间
            double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
            metrics.setAverageResponseTime(avgTime);

            // 计算最小和最大响应时间
            metrics.setMinResponseTime(times.stream().mapToLong(Long::longValue).min().orElse(0));
            metrics.setMaxResponseTime(times.stream().mapToLong(Long::longValue).max().orElse(0));

            // 计算P95响应时间
            List<Long> sortedTimes = new ArrayList<>(times);
            sortedTimes.sort(Long::compareTo);
            int p95Index = (int) Math.ceil(sortedTimes.size() * 0.95) - 1;
            metrics.setP95ResponseTime(sortedTimes.get(Math.max(0, p95Index)));
        }

        return metrics;
    }

    /**
     * API指标数据类
     */
    @Data
    public static class ApiMetrics {
        private String apiName;
        private long totalCalls;
        private long errorCount;
        private double averageResponseTime;
        private long minResponseTime;
        private long maxResponseTime;
        private long p95ResponseTime;

        public double getSuccessRate() {
            return totalCalls > 0 ? (double) (totalCalls - errorCount) / totalCalls * 100 : 0.0;
        }
    }

    /**
     * 系统指标数据类
     */
    @Data
    public static class SystemMetrics {
        private double memoryUsagePercent;
        private int threadCount;
        private int cpuCores;
        private long usedMemoryMB;
        private long maxMemoryMB;

        public void setMemoryUsagePercent(double memoryUsagePercent) {
            this.memoryUsagePercent = memoryUsagePercent;
            // 同时设置内存使用量（MB）
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            this.usedMemoryMB = memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024;
            this.maxMemoryMB = memoryBean.getHeapMemoryUsage().getMax() / 1024 / 1024;
        }
    }

    /**
     * 监控报告数据类
     */
    @Data
    public static class MonitoringReport {
        private LocalDateTime timestamp;
        private Map<String, ApiMetrics> apiMetrics;
        private int concurrentUsers;
        private SystemMetrics systemMetrics;
        private Map<String, Double> errorRates;
    }

    /**
     * 监控数据快照
     */
    @Data
    public static class MetricsSnapshot {
        private LocalDateTime timestamp;
        private int concurrentUsers;
        private SystemMetrics systemMetrics;
        private Map<String, Double> errorRates;
    }
}

