package com.hzau.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: ScheduleConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午8:21
 */
@Slf4j
@Configuration
@EnableScheduling
public class ScheduleConfig {

    /**
     * 配置定时任务专用线程池
     * 支持全局@Scheduled注解的执行
     */
    @Bean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 设置线程池大小
        scheduler.setPoolSize(10);

        // 设置线程名前缀，便于调试和监控
        scheduler.setThreadNamePrefix("scheduled-task-");

        // 设置线程池关闭时等待所有任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // 设置等待时间，超时则强制关闭
        scheduler.setAwaitTerminationSeconds(60);

        // 设置拒绝策略：由调用线程执行
        scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        scheduler.initialize();

        log.info("定时任务线程池初始化完成，线程池大小: {}", scheduler.getPoolSize());

        return scheduler;
    }
}

