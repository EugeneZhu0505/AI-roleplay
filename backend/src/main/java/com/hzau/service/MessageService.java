package com.hzau.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzau.entity.Message;
import com.hzau.mapper.MessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: MessageService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:04
 */
@Slf4j
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    @Autowired
    private ConversationCacheService conversationCacheService;

    /**
     * 保存用户消息
     * @param conversationId 对话ID
     * @param textContent 文本内容
     * @return 保存的消息
     */
    public Message saveUserMessage(Long conversationId, String textContent) {
        log.info("保存用户消息, conversationId: {}, textContent: {}", conversationId, textContent);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("user");
        message.setContentType("text");
        message.setTextContent(textContent);

        this.save(message);
        
        // 更新缓存
        conversationCacheService.addMessageToCache(conversationId, message);
        
        log.info("用户消息保存成功, messageId: {}", message.getId());
        return message;
    }

    /**
     * 保存角色消息
     * @param conversationId 对话ID
     * @param textContent 文本内容
     * @return 保存的消息
     */
    public Message saveCharacterMessage(Long conversationId, String textContent) {
        log.info("保存角色消息, conversationId: {}, textContent: {}", conversationId, textContent);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("character");
        message.setContentType("text");
        message.setTextContent(textContent);

        this.save(message);
        
        // 更新缓存
        conversationCacheService.addMessageToCache(conversationId, message);
        
        log.info("角色消息保存成功, messageId: {}", message.getId());
        return message;
    }

    /**
     * 保存音频消息
     * @param conversationId 对话ID
     * @param senderType 发送者类型
     * @param audioUrl 音频URL
     * @param audioDuration 音频时长
     * @param textContent 转录的文本内容（可选）
     * @return 保存的消息
     */
    public Message saveAudioMessage(Long conversationId, String senderType, String audioUrl,
                                    Integer audioDuration, String textContent) {
        log.info("保存音频消息, conversationId: {}, senderType: {}, audioUrl: {}",
                conversationId, senderType, audioUrl);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType(senderType);
        message.setContentType("audio");
        message.setAudioUrl(audioUrl);
        message.setAudioDuration(audioDuration);
        message.setTextContent(textContent);

        this.save(message);
        log.info("音频消息保存成功, messageId: {}", message.getId());
        return message;
    }

    /**
     * 保存用户语音消息
     * @param conversationId 对话ID
     * @param textContent 转录的文本内容
     * @param audioUrl 音频URL
     * @param audioDuration 音频时长
     * @return 保存的消息
     */
    public Message saveUserVoiceMessage(Long conversationId, String textContent, String audioUrl, Integer audioDuration) {
        log.info("保存用户语音消息, conversationId: {}, textContent: {}, audioUrl: {}", 
                conversationId, textContent, audioUrl);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("user");
        message.setContentType("voice");
        message.setTextContent(textContent);
        message.setAudioUrl(audioUrl);
        message.setAudioDuration(audioDuration);

        this.save(message);
        
        // 更新缓存
        conversationCacheService.addMessageToCache(conversationId, message);
        
        log.info("用户语音消息保存成功, messageId: {}", message.getId());
        return message;
    }

    /**
     * 保存角色语音消息
     * @param conversationId 对话ID
     * @param textContent 文本内容
     * @param audioUrl 音频URL
     * @param audioDuration 音频时长
     * @return 保存的消息
     */
    public Message saveCharacterVoiceMessage(Long conversationId, String textContent, String audioUrl, Integer audioDuration) {
        log.info("保存角色语音消息, conversationId: {}, textContent: {}, audioUrl: {}", 
                conversationId, textContent, audioUrl);

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType("character");
        message.setContentType("voice");
        message.setTextContent(textContent);
        message.setAudioUrl(audioUrl);
        message.setAudioDuration(audioDuration);

        this.save(message);
        
        // 更新缓存
        conversationCacheService.addMessageToCache(conversationId, message);
        
        log.info("角色语音消息保存成功, messageId: {}", message.getId());
        return message;
    }

    /**
     * 获取对话的消息列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    public List<Message> getConversationMessages(Long conversationId) {
        log.info("获取对话消息列表, conversationId: {}", conversationId);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .orderBy(true, true, "created_at");
        return this.list(queryWrapper);
    }

    /**
     * 获取对话的最近N条消息
     * @param conversationId 对话ID
     * @param limit 消息数量限制
     * @return 最近的消息列表
     */
    public List<Message> getRecentConversationMessages(Long conversationId, int limit) {
        log.info("获取对话最近消息, conversationId: {}, limit: {}", conversationId, limit);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .orderBy(true, false, "created_at")
                .last("LIMIT " + limit);
        List<Message> messages = this.list(queryWrapper);
        // 反转列表，使其按时间正序排列
        messages.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));
        return messages;
    }

    /**
     * 获取对话的消息数量
     * @param conversationId 对话ID
     * @return 消息数量
     */
    public long getConversationMessageCount(Long conversationId) {
        log.info("获取对话消息数量, conversationId: {}", conversationId);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        return this.count(queryWrapper);
    }

    /**
     * 删除对话的所有消息
     * @param conversationId 对话ID
     * @return 是否删除成功
     */
    public boolean deleteConversationMessages(Long conversationId) {
        log.info("删除对话所有消息, conversationId: {}", conversationId);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        return this.remove(queryWrapper);
    }
}

