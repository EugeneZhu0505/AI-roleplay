package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.service.ConcurrentControlService;
import com.hzau.service.PerformanceMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: MonitoringController
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午7:09
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "性能监控", description = "系统性能监控相关接口")
public class MonitoringController {

    private final PerformanceMonitoringService monitoringService;
    private final ConcurrentControlService concurrencyControlService;

    /**
     * 获取完整的监控报告
     */
    @GetMapping("/report")
    @Operation(summary = "获取完整监控报告", description = "获取包含API指标、并发用户数、系统资源使用率等的完整监控报告")
    public Result<PerformanceMonitoringService.MonitoringReport> getMonitoringReport() {
        try {
            PerformanceMonitoringService.MonitoringReport report = monitoringService.getMonitoringReport();
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取监控报告失败", e);
            return Result.fail(500, "获取监控报告失败");
        }
    }

    /**
     * 获取API性能指标
     */
    @GetMapping("/api-metrics")
    @Operation(summary = "获取API性能指标", description = "获取所有API的响应时间、调用次数、错误率等指标")
    public Result<Map<String, PerformanceMonitoringService.ApiMetrics>> getApiMetrics() {
        try {
            Map<String, PerformanceMonitoringService.ApiMetrics> metrics = monitoringService.getApiMetrics();
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取API指标失败", e);
            return Result.fail(500, "获取API指标失败");
        }
    }

    /**
     * 获取当前并发用户数
     */
    @GetMapping("/concurrent-users")
    @Operation(summary = "获取当前并发用户数", description = "获取当前系统中活跃的并发用户数量")
    public Result<Integer> getConcurrentUsers() {
        try {
            int concurrentUsers = monitoringService.getCurrentConcurrentUsers();
            return Result.success(concurrentUsers);
        } catch (Exception e) {
            log.error("获取并发用户数失败", e);
            return Result.fail(500, "获取并发用户数失败");
        }
    }

    /**
     * 获取系统资源使用率
     */
    @GetMapping("/system-metrics")
    @Operation(summary = "获取系统资源指标", description = "获取内存使用率、线程数、CPU核心数等系统资源指标")
    public Result<PerformanceMonitoringService.SystemMetrics> getSystemMetrics() {
        try {
            PerformanceMonitoringService.SystemMetrics metrics = monitoringService.getSystemMetrics();
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取系统指标失败", e);
            return Result.fail(500, "获取系统指标失败");
        }
    }

    /**
     * 获取错误率统计
     */
    @GetMapping("/error-rates")
    @Operation(summary = "获取错误率统计", description = "获取各个API的错误率统计信息")
    public Result<Map<String, Double>> getErrorRates() {
        try {
            Map<String, Double> errorRates = monitoringService.getErrorRates();
            return Result.success(errorRates);
        } catch (Exception e) {
            log.error("获取错误率统计失败", e);
            return Result.fail(500, "获取错误率统计失败");
        }
    }

    /**
     * 获取历史监控数据
     */
    @GetMapping("/history")
    @Operation(summary = "获取历史监控数据", description = "获取最近1小时的监控数据历史记录")
    public Result<List<PerformanceMonitoringService.MetricsSnapshot>> getMetricsHistory() {
        try {
            List<PerformanceMonitoringService.MetricsSnapshot> history = monitoringService.getMetricsHistory();
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取历史监控数据失败", e);
            return Result.fail(500, "获取历史监控数据失败");
        }
    }

    /**
     * 获取特定API的详细指标
     */
    @GetMapping("/api-metrics/{apiName}")
    @Operation(summary = "获取特定API指标", description = "获取指定API的详细性能指标")
    public Result<PerformanceMonitoringService.ApiMetrics> getApiMetrics(
            @Parameter(description = "API名称", required = true)
            @PathVariable String apiName) {
        try {
            Map<String, PerformanceMonitoringService.ApiMetrics> allMetrics = monitoringService.getApiMetrics();
            PerformanceMonitoringService.ApiMetrics apiMetrics = allMetrics.get(apiName);

            if (apiMetrics != null) {
                return Result.success(apiMetrics);
            } else {
                return Result.fail(404, "未找到指定API的监控数据");
            }
        } catch (Exception e) {
            log.error("获取API指标失败: {}", apiName, e);
            return Result.fail(500, "获取API指标失败");
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查监控服务是否正常运行")
    public Result<String> healthCheck() {
        try {
            // 简单的健康检查，确保监控服务可以正常获取数据
            monitoringService.getCurrentConcurrentUsers();
            monitoringService.getSystemMetrics();

            return Result.success("监控服务运行正常");
        } catch (Exception e) {
            log.error("监控服务健康检查失败", e);
            return Result.fail(500, "监控服务异常");
        }
    }

    /**
     * 获取监控统计摘要
     */
    @GetMapping("/summary")
    @Operation(summary = "获取监控摘要", description = "获取关键监控指标的摘要信息")
    public Result<MonitoringSummary> getMonitoringSummary() {
        try {
            MonitoringSummary summary = new MonitoringSummary();

            // 并发用户数
            summary.setConcurrentUsers(monitoringService.getCurrentConcurrentUsers());

            // 系统资源
            PerformanceMonitoringService.SystemMetrics systemMetrics = monitoringService.getSystemMetrics();
            summary.setMemoryUsagePercent(systemMetrics.getMemoryUsagePercent());
            summary.setThreadCount(systemMetrics.getThreadCount());

            // API统计
            Map<String, PerformanceMonitoringService.ApiMetrics> apiMetrics = monitoringService.getApiMetrics();
            summary.setTotalApiCalls(apiMetrics.values().stream()
                    .mapToLong(PerformanceMonitoringService.ApiMetrics::getTotalCalls)
                    .sum());

            // 平均错误率
            Map<String, Double> errorRates = monitoringService.getErrorRates();
            summary.setAverageErrorRate(errorRates.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0));

            // 并发控制统计
            ConcurrentControlService.ConcurrencyStats concurrencyStats = concurrencyControlService.getConcurrencyStats();
            summary.setActiveRequests(concurrencyStats.getCurrentActiveRequests());
            summary.setRejectedRequests(concurrencyStats.getRejectedRequests());
            summary.setAvailablePermits(concurrencyStats.getAvailableGlobalPermits());

            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取监控摘要失败", e);
            return Result.fail(500, "获取监控摘要失败");
        }
    }

    /**
     * 获取并发控制统计
     */
    @GetMapping("/concurrency-stats")
    @Operation(summary = "获取并发控制统计", description = "获取并发控制的详细统计信息")
    public Result<ConcurrentControlService.ConcurrencyStats> getConcurrencyStats() {
        try {
            ConcurrentControlService.ConcurrencyStats stats = concurrencyControlService.getConcurrencyStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取并发控制统计失败", e);
            return Result.fail(500, "获取并发控制统计失败");
        }
    }

    /**
     * 监控摘要数据类
     */
    public static class MonitoringSummary {
        private int concurrentUsers;
        private double memoryUsagePercent;
        private int threadCount;
        private long totalApiCalls;
        private double averageErrorRate;
        private int activeRequests;
        private long rejectedRequests;
        private int availablePermits;

        // Getters and Setters
        public int getConcurrentUsers() {
            return concurrentUsers;
        }

        public void setConcurrentUsers(int concurrentUsers) {
            this.concurrentUsers = concurrentUsers;
        }

        public double getMemoryUsagePercent() {
            return memoryUsagePercent;
        }

        public void setMemoryUsagePercent(double memoryUsagePercent) {
            this.memoryUsagePercent = memoryUsagePercent;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }

        public long getTotalApiCalls() {
            return totalApiCalls;
        }

        public void setTotalApiCalls(long totalApiCalls) {
            this.totalApiCalls = totalApiCalls;
        }

        public double getAverageErrorRate() {
            return averageErrorRate;
        }

        public void setAverageErrorRate(double averageErrorRate) {
            this.averageErrorRate = averageErrorRate;
        }

        public int getActiveRequests() {
            return activeRequests;
        }

        public void setActiveRequests(int activeRequests) {
            this.activeRequests = activeRequests;
        }

        public long getRejectedRequests() {
            return rejectedRequests;
        }

        public void setRejectedRequests(long rejectedRequests) {
            this.rejectedRequests = rejectedRequests;
        }

        public int getAvailablePermits() {
            return availablePermits;
        }

        public void setAvailablePermits(int availablePermits) {
            this.availablePermits = availablePermits;
        }
    }
}
