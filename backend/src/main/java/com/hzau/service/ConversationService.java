package com.hzau.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzau.entity.Conversation;
import com.hzau.mapper.ConversationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: ConversationService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:01
 */
@Slf4j
@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    /**
     * 创建新对话
     * @param userId 用户ID
     * @param characterId 角色ID
     * @param title 对话标题
     * @return 创建的对话
     */
    public Conversation createConversation(Integer userId, Long characterId, String title) {
        log.info("创建新对话, userId: {}, characterId: {}, title: {}", userId, characterId, title);

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setCharacterId(characterId);
        conversation.setTitle(title);
        conversation.setStatus("active");

        this.save(conversation);
        log.info("对话创建成功, conversationId: {}", conversation.getId());
        return conversation;
    }

    /**
     * 获取用户的对话列表
     * @param userId 用户ID
     * @return 对话列表
     */
    public List<Conversation> getUserConversations(Integer userId) {
        log.info("获取用户对话列表, userId: {}", userId);
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderBy(true, false, "updated_at");
        return this.list(queryWrapper);
    }

    /**
     * 获取用户与特定角色的对话列表
     * @param userId 用户ID
     * @param characterId 角色ID
     * @return 对话列表
     */
    public List<Conversation> getUserConversationsByCharacter(Integer userId, Long characterId) {
        log.info("获取用户与特定角色的对话列表, userId: {}, characterId: {}", userId, characterId);
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("character_id", characterId)
                .orderBy(true, false, "updated_at");
        return this.list(queryWrapper);
    }

    /**
     * 根据ID获取对话详情
     * @param conversationId 对话ID
     * @return 对话详情
     */
    public Conversation getConversationById(Long conversationId) {
        log.info("获取对话详情, conversationId: {}", conversationId);
        return this.getById(conversationId);
    }

    /**
     * 检查对话是否属于指定用户
     * @param conversationId 对话ID
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    public boolean isConversationOwnedByUser(Long conversationId, Integer userId) {
        log.info("检查对话是否属于指定用户, conversationId: {}, userId: {}", conversationId, userId);
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", conversationId)
                .eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 更新对话状态
     * @param conversationId 对话ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateConversationStatus(Long conversationId, String status) {
        log.info("更新对话状态, conversationId: {}, status: {}", conversationId, status);
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setStatus(status);
        return this.updateById(conversation);
    }

    /**
     * 更新对话标题
     * @param conversationId 对话ID
     * @param title 新标题
     * @return 是否更新成功
     */
    public boolean updateConversationTitle(Long conversationId, String title) {
        log.info("更新对话标题, conversationId: {}, title: {}", conversationId, title);
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setTitle(title);
        return this.updateById(conversation);
    }

    /**
     * 删除对话
     * @param conversationId 对话ID
     * @return 是否删除成功
     */
    public boolean deleteConversation(Long conversationId) {
        log.info("删除对话, conversationId: {}", conversationId);
        return this.removeById(conversationId);
    }
}

