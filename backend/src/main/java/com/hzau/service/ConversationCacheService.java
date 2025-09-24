package com.hzau.service;

import com.hzau.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: ConversationCacheService
 * @author: zhuyuchen
 * @description: 对话缓存服务，负责管理活跃对话的Redis缓存
 * @date: 2025/9/23 下午8:13
 */
@Slf4j
@Service
public class ConversationCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    // Redis键前缀
    private static final String ACTIVE_CONVERSATIONS_KEY = "chat:active_conversations";
    private static final String CONVERSATION_MESSAGES_KEY = "chat:messages:";
    private static final String USER_ACTIVE_CONVERSATIONS_KEY = "chat:user_active:";

    // 缓存过期时间
    private static final int ACTIVE_CONVERSATION_EXPIRE = 2 * 60 * 60; // 2小时
    private static final int MESSAGE_CACHE_EXPIRE = 30 * 60; // 30分钟
    private static final int USER_ACTIVE_EXPIRE = 24 * 60 * 60; // 24小时

    /**
     * 激活对话 - 用户打开聊天窗口时调用
     * @param userId 用户ID
     * @param conversationId 对话ID
     */
    public void activateConversation(Integer userId, Long conversationId) {
        log.info("激活对话缓存, userId: {}, conversationId: {}", userId, conversationId);

        try {
            String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;
            String userActiveKey = USER_ACTIVE_CONVERSATIONS_KEY + userId;

            // 检查缓存中是否已有该对话的消息
            if (!redisTemplate.hasKey(conversationKey)) {
                // 延迟加载消息到缓存
                loadConversationToCache(conversationId);
            }

            // 将对话添加到全局激活列表
            redisTemplate.opsForSet().add(ACTIVE_CONVERSATIONS_KEY, conversationId);
            redisTemplate.expire(ACTIVE_CONVERSATIONS_KEY, ACTIVE_CONVERSATION_EXPIRE, TimeUnit.SECONDS);

            // 将对话添加到用户的激活列表
            redisTemplate.opsForSet().add(userActiveKey, conversationId);
            redisTemplate.expire(userActiveKey, USER_ACTIVE_EXPIRE, TimeUnit.SECONDS);

            log.info("对话激活成功, userId: {}, conversationId: {}", userId, conversationId);

        } catch (Exception e) {
            log.error("激活对话缓存失败, userId: {}, conversationId: {}", userId, conversationId, e);
        }
    }

    /**
     * 去激活对话 - 用户关闭聊天窗口时调用
     * @param userId 用户ID
     * @param conversationId 对话ID
     */
    public void deactivateConversation(Integer userId, Long conversationId) {
        log.info("去激活对话缓存, userId: {}, conversationId: {}", userId, conversationId);

        try {
            String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;
            String userActiveKey = USER_ACTIVE_CONVERSATIONS_KEY + userId;

            // 1. 从用户激活列表中移除
            redisTemplate.opsForSet().remove(userActiveKey, conversationId);

            // 2. 检查是否还有其他用户激活了这个对话
            boolean stillActive = isConversationActiveByOtherUsers(conversationId, userId);

            if (!stillActive) {
                // 3. 如果没有其他用户激活，从全局激活列表移除
                redisTemplate.opsForSet().remove(ACTIVE_CONVERSATIONS_KEY, conversationId);

                // 4. 删除消息缓存
                redisTemplate.delete(conversationKey);
                log.info("对话缓存已清理, conversationId: {}", conversationId);
            } else {
                log.info("对话仍被其他用户激活，保留缓存, conversationId: {}", conversationId);
            }

        } catch (Exception e) {
            log.error("去激活对话缓存失败, conversationId: {}", conversationId, e);
        }
    }

    /**
     * 获取缓存的对话消息
     * @param conversationId 对话ID
     * @return 消息列表，如果缓存中没有则返回null
     */
    @SuppressWarnings("unchecked")
    public List<Message> getCachedMessages(Long conversationId) {
        try {
            String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;
            Object cachedMessages = redisTemplate.opsForValue().get(conversationKey);

            if (cachedMessages != null) {
                log.debug("从缓存获取对话消息, conversationId: {}", conversationId);
                return (List<Message>) cachedMessages;
            }

            return null;
        } catch (Exception e) {
            log.error("获取缓存消息失败, conversationId: {}", conversationId, e);
            return null;
        }
    }

    /**
     * 添加新消息到缓存
     * @param conversationId 对话ID
     * @param message 新消息
     */
    public void addMessageToCache(Long conversationId, Message message) {
        try {
            String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;

            // 检查对话是否激活
            if (isConversationActive(conversationId)) {
                List<Message> messages = getCachedMessages(conversationId);
                if (messages != null) {
                    messages.add(message);
                    redisTemplate.opsForValue().set(conversationKey, messages, MESSAGE_CACHE_EXPIRE, TimeUnit.SECONDS);
                    log.debug("消息已添加到缓存, conversationId: {}", conversationId);
                } else {
                    // 如果缓存中没有消息，重新加载
                    loadConversationToCache(conversationId);
                }
            }
        } catch (Exception e) {
            log.error("添加消息到缓存失败, conversationId: {}", conversationId, e);
        }
    }

    /**
     * 检查对话是否激活
     * @param conversationId 对话ID
     * @return 是否激活
     */
    public boolean isConversationActive(Long conversationId) {
        try {
            return redisTemplate.opsForSet().isMember(ACTIVE_CONVERSATIONS_KEY, conversationId.toString());
        } catch (Exception e) {
            log.error("检查对话激活状态失败, conversationId: {}", conversationId, e);
            return false;
        }
    }

    /**
     * 获取用户的激活对话列表
     * @param userId 用户ID
     * @return 激活的对话ID列表
     */
    public Set<Object> getUserActiveConversations(Integer userId) {
        try {
            String userActiveKey = USER_ACTIVE_CONVERSATIONS_KEY + userId;
            return redisTemplate.opsForSet().members(userActiveKey);
        } catch (Exception e) {
            log.error("获取用户激活对话列表失败, userId: {}", userId, e);
            return Set.of();
        }
    }

    /**
     * 从数据库加载对话消息到缓存
     * @param conversationId 对话ID
     */
    /**
     * 从数据库加载对话消息到缓存（延迟加载，避免循环依赖）
     * @param conversationId 对话ID
     */
    private void loadConversationToCache(Long conversationId) {
        try {
            log.info("从数据库加载对话消息到缓存, conversationId: {}", conversationId);

            // 使用ApplicationContext延迟获取MessageService，避免循环依赖
            MessageService messageService = applicationContext.getBean(MessageService.class);
            List<Message> messages = messageService.getConversationMessages(conversationId);
            String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;

            redisTemplate.opsForValue().set(conversationKey, messages, MESSAGE_CACHE_EXPIRE, TimeUnit.SECONDS);
            log.info("对话消息已缓存, conversationId: {}, 消息数量: {}", conversationId, messages.size());

        } catch (Exception e) {
            log.error("加载对话消息到缓存失败, conversationId: {}", conversationId, e);
        }
    }

    /**
     * 检查对话是否被其他用户激活
     * @param conversationId 对话ID
     * @param excludeUserId 排除的用户ID
     * @return 是否被其他用户激活
     */
    private boolean isConversationActiveByOtherUsers(Long conversationId, Integer excludeUserId) {
        try {
            // 这里简化实现，实际可以遍历所有用户的激活列表
            // 为了性能考虑，我们假设如果对话在全局激活列表中，就可能被其他用户激活
            return isConversationActive(conversationId);
        } catch (Exception e) {
            log.error("检查对话是否被其他用户激活失败, conversationId: {}", conversationId, e);
            return false;
        }
    }

    /**
     * 清理过期的激活对话缓存
     * 建议通过定时任务调用
     */
    public void cleanupExpiredActiveConversations() {
        try {
            log.info("开始清理过期的激活对话缓存");

            Set<Object> activeConversations = redisTemplate.opsForSet().members(ACTIVE_CONVERSATIONS_KEY);
            if (activeConversations != null) {
                for (Object conversationId : activeConversations) {
                    String conversationKey = CONVERSATION_MESSAGES_KEY + conversationId;
                    if (!redisTemplate.hasKey(conversationKey)) {
                        // 如果消息缓存已过期，从激活列表中移除
                        redisTemplate.opsForSet().remove(ACTIVE_CONVERSATIONS_KEY, conversationId);
                        log.debug("清理过期的激活对话, conversationId: {}", conversationId);
                    }
                }
            }

            log.info("激活对话缓存清理完成");
        } catch (Exception e) {
            log.error("清理过期激活对话缓存失败", e);
        }
    }
}
