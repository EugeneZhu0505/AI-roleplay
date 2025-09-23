package com.hzau.service;

import com.hzau.entity.Character;
import com.hzau.entity.Conversation;
import com.hzau.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: AiRoleplayService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:06
 */
@Slf4j
@Service
public class AiRoleplayService {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private QiniuAiService qiniuAiService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String OPENING_CACHE_KEY = "ai:opening:";
    private static final String CONTEXT_CACHE_KEY = "ai:context:";
    private static final int OPENING_CACHE_EXPIRE = 24 * 60 * 60; // 24小时
    private static final int CONTEXT_CACHE_EXPIRE = 30 * 60; // 30分钟

    /**
     * 获取角色开场白
     * @param characterId 角色ID
     * @return 开场白内容
     */
    public Mono<String> getCharacterOpening(Long characterId) {
        log.info("获取角色开场白, characterId: {}", characterId);

        // 先从缓存获取
        String cacheKey = OPENING_CACHE_KEY + characterId;
        String cachedOpening = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedOpening != null) {
            log.info("从缓存获取角色开场白, characterId: {}", characterId);
            return Mono.just(cachedOpening);
        }

        // 缓存未命中，生成开场白
        return generateCharacterOpening(characterId)
                .doOnSuccess(opening -> {
                    // 缓存开场白
                    redisTemplate.opsForValue().set(cacheKey, opening, OPENING_CACHE_EXPIRE, TimeUnit.SECONDS);
                    log.info("角色开场白已缓存, characterId: {}", characterId);
                });
    }

    /**
     * 生成角色开场白
     * @param characterId 角色ID
     * @return 开场白内容
     */
    private Mono<String> generateCharacterOpening(Long characterId) {
        log.info("生成角色开场白, characterId: {}", characterId);

        Character character = characterService.getById(characterId);
        if (character == null) {
            return Mono.error(new RuntimeException("角色不存在"));
        }

        // 构建开场白生成提示
        String prompt = buildOpeningPrompt(character);

        return qiniuAiService.singleChat(prompt)
                .doOnSuccess(opening -> log.info("角色开场白生成成功, characterId: {}", characterId))
                .doOnError(error -> log.error("角色开场白生成失败, characterId: {}", characterId, error));
    }

    /**
     * 构建开场白生成提示
     * @param character 角色信息
     * @return 提示内容
     */
    private String buildOpeningPrompt(Character character) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你现在要扮演").append(character.getName()).append("。\n");
        prompt.append("角色描述：").append(character.getDescription()).append("\n");
        prompt.append("性格特点：").append(character.getPersonality()).append("\n");
        prompt.append("背景故事：").append(character.getBackgroundStory()).append("\n");
        prompt.append("系统提示：").append(character.getSystemPrompt()).append("\n\n");
        prompt.append("请以").append(character.getName()).append("的身份，用第一人称生成一段简短的开场白，");
        prompt.append("介绍自己并欢迎用户开始对话。开场白应该体现角色的性格特点，长度控制在100字以内。");

        return prompt.toString();
    }

    /**
     * 开始新对话
     * @param userId 用户ID
     * @param characterId 角色ID
     * @param title 对话标题
     * @return 对话信息和开场白
     */
    public Mono<ConversationWithOpening> startNewConversation(Integer userId, Long characterId, String title) {
        log.info("开始新对话, userId: {}, characterId: {}, title: {}", userId, characterId, title);

        // 检查角色是否存在且激活
        Character character = characterService.getById(characterId);
        if (character == null || !character.getIsActive()) {
            return Mono.error(new RuntimeException("角色不存在或未激活"));
        }

        // 创建对话
        Conversation conversation = conversationService.createConversation(userId, characterId, title);

        // 获取开场白
        return getCharacterOpening(characterId)
                .map(opening -> {
                    // 保存开场白消息
                    messageService.saveCharacterMessage(conversation.getId(), opening);
                    return new ConversationWithOpening(conversation, opening);
                })
                .doOnSuccess(result -> log.info("新对话创建成功, conversationId: {}", conversation.getId()))
                .doOnError(error -> log.error("新对话创建失败, userId: {}, characterId: {}", userId, characterId, error));
    }

    /**
     * 发送消息并获取AI回复
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @return AI回复
     */
    public Mono<String> sendMessage(Integer userId, Long conversationId, String userMessage) {
        log.info("发送消息, userId: {}, conversationId: {}, message: {}", userId, conversationId, userMessage);

        // 验证对话是否属于用户
        if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
            return Mono.error(new RuntimeException("无权访问该对话"));
        }

        // 获取对话信息
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null) {
            return Mono.error(new RuntimeException("对话不存在"));
        }

        // 获取角色信息
        Character character = characterService.getById(conversation.getCharacterId());
        if (character == null) {
            return Mono.error(new RuntimeException("角色不存在"));
        }

        // 保存用户消息
        messageService.saveUserMessage(conversationId, userMessage);

        // 构建对话上下文
        return buildConversationContext(conversationId, character, userMessage)
                .flatMap(context -> {
                    // 调用AI API获取回复
                    String contextKey = CONTEXT_CACHE_KEY + conversationId;
                    return qiniuAiService.multiTurnChat(contextKey, context);
                })
                .doOnSuccess(aiReply -> {
                    // 保存AI回复
                    messageService.saveCharacterMessage(conversationId, aiReply);
                    log.info("消息发送成功, conversationId: {}", conversationId);
                })
                .doOnError(error -> log.error("消息发送失败, conversationId: {}", conversationId, error));
    }

    /**
     * 构建对话上下文
     * @param conversationId 对话ID
     * @param character 角色信息
     * @param currentMessage 当前用户消息
     * @return 上下文内容
     */
    private Mono<String> buildConversationContext(Long conversationId, Character character, String currentMessage) {
        log.info("构建对话上下文, conversationId: {}", conversationId);

        // 获取最近的对话历史
        List<Message> recentMessages = messageService.getRecentConversationMessages(conversationId, 10);

        StringBuilder context = new StringBuilder();

        // 添加角色设定
        context.append("你现在要扮演").append(character.getName()).append("。\n");
        context.append("角色描述：").append(character.getDescription()).append("\n");
        context.append("性格特点：").append(character.getPersonality()).append("\n");
        context.append("背景故事：").append(character.getBackgroundStory()).append("\n");
        context.append("系统提示：").append(character.getSystemPrompt()).append("\n\n");

        // 添加对话历史
        if (!recentMessages.isEmpty()) {
            context.append("以下是之前的对话历史：\n");
            for (Message message : recentMessages) {
                if ("user".equals(message.getSenderType())) {
                    context.append("用户：").append(message.getTextContent()).append("\n");
                } else {
                    context.append(character.getName()).append("：").append(message.getTextContent()).append("\n");
                }
            }
            context.append("\n");
        }

        // 添加当前用户消息
        context.append("用户：").append(currentMessage).append("\n");
        context.append("请以").append(character.getName()).append("的身份回复用户，保持角色一致性。");

        return Mono.just(context.toString());
    }

    /**
     * 获取对话历史
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @return 消息列表
     */
    public List<Message> getConversationHistory(Integer userId, Long conversationId) {
        log.info("获取对话历史, userId: {}, conversationId: {}", userId, conversationId);

        // 验证对话是否属于用户
        if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
            throw new RuntimeException("无权访问该对话");
        }

        return messageService.getConversationMessages(conversationId);
    }

    /**
     * 对话信息和开场白的包装类
     */
    public static class ConversationWithOpening {
        private final Conversation conversation;
        private final String opening;

        public ConversationWithOpening(Conversation conversation, String opening) {
            this.conversation = conversation;
            this.opening = opening;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public String getOpening() {
            return opening;
        }
    }
}

