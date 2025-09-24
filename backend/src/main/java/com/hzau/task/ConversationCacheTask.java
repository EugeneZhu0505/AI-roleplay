package com.hzau.task;

import com.hzau.service.ConversationCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.task
 * @className: ConversationCacheTask
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午8:22
 */
@Slf4j
@Component
public class ConversationCacheTask {

    @Autowired
    private ConversationCacheService conversationCacheService;

    /**
     * 清理过期的激活对话缓存
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void cleanupExpiredActiveConversations() {
        log.info("开始执行定时任务：清理过期的激活对话缓存");
        try {
            conversationCacheService.cleanupExpiredActiveConversations();
            log.info("定时任务执行完成：清理过期的激活对话缓存");
        } catch (Exception e) {
            log.error("定时任务执行失败：清理过期的激活对话缓存", e);
        }
    }

    /**
     * 定期统计缓存使用情况
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1小时
    public void reportCacheStatistics() {
        log.info("开始执行定时任务：统计缓存使用情况");
        try {
            // 这里可以添加缓存统计逻辑
            // 比如统计活跃对话数量、缓存命中率等
            log.info("定时任务执行完成：统计缓存使用情况");
        } catch (Exception e) {
            log.error("定时任务执行失败：统计缓存使用情况", e);
        }
    }
}

