package com.hzau.service;

import com.hzau.entity.AiCharacter;
import com.hzau.entity.CharacterSkill;
import com.hzau.entity.Conversation;
import com.hzau.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

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

    @Autowired
    private ConversationCacheService conversationCacheService;

    @Autowired
    private ConcurrentControlService concurrentControlService;

    @Autowired
    private QiniuAudioService qiniuAudioService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private CharacterSkillService characterSkillService;

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
        Mono<String> openingMono = generateCharacterOpening(characterId)
                .doOnSuccess(opening -> {
                    // 缓存开场白
                    redisTemplate.opsForValue().set(cacheKey, opening, OPENING_CACHE_EXPIRE, TimeUnit.SECONDS);
                    log.info("角色开场白已缓存, characterId: {}", characterId);
                });
        return openingMono;
    }

    /**
     * 生成角色开场白
     * @param characterId 角色ID
     * @return 开场白内容
     */
    private Mono<String> generateCharacterOpening(Long characterId) {
        log.info("生成角色开场白, characterId: {}", characterId);

        AiCharacter character = characterService.getById(characterId);
        if (character == null) {
            return Mono.error(new RuntimeException("角色不存在"));
        }

        // 构建开场白生成提示
        String prompt = buildOpeningPrompt(character);

        // 获取并发控制许可
        Mono<Boolean> permitMono = concurrentControlService.acquirePermit("system", "llm");
        return permitMono.flatMap(permit -> qiniuAiService.singleChat(prompt)
                .doOnSuccess(opening -> {
                    log.info("角色开场白生成成功, characterId: {}", characterId);
                    concurrentControlService.releasePermit("system", "llm");
                })
                .doOnError(error -> {
                    log.error("角色开场白生成失败, characterId: {}", characterId, error);
                    concurrentControlService.releasePermit("system", "llm");
                }));
    }

    /**
     * 构建开场白生成提示
     * @param character 角色信息
     * @return 提示内容
     */
    private String buildOpeningPrompt(AiCharacter character) {
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
        AiCharacter character = characterService.getById(characterId);
        if (character == null || !character.getIsActive()) {
            return Mono.error(new RuntimeException("角色不存在或未激活"));
        }

        // 创建对话
        Conversation conversation = conversationService.createConversation(userId, characterId, title);

        // 获取开场白
        Mono<ConversationWithOpening> resultMono = getCharacterOpening(characterId)
                .map(opening -> {
                    // 保存开场白消息
                    messageService.saveCharacterMessage(conversation.getId(), opening);
                    return new ConversationWithOpening(conversation, opening);
                })
                .doOnSuccess(result -> log.info("新对话创建成功, conversationId: {}", conversation.getId()))
                .doOnError(error -> log.error("新对话创建失败, userId: {}, characterId: {}", userId, characterId, error));
        return resultMono;
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
        AiCharacter character = characterService.getById(conversation.getCharacterId());
        if (character == null) {
            return Mono.error(new RuntimeException("角色不存在"));
        }

        // 保存用户消息
        messageService.saveUserMessage(conversationId, userMessage);

        // 构建对话上下文
        Mono<String> contextMono = buildConversationContext(conversationId, character, userMessage, null);
        Mono<String> chatMono = contextMono.flatMap(context -> {
            // 获取并发控制许可
            Mono<Boolean> permitMono = concurrentControlService.acquirePermit(userId.toString(), "llm");
            return permitMono.flatMap(permit -> {
                // 调用AI API获取回复
                String contextKey = CONTEXT_CACHE_KEY + conversationId;
                return qiniuAiService.multiTurnChat(contextKey, context);
            });
        });
        Mono<String> resultMono = chatMono
                .doOnSuccess(aiReply -> {
                    // 保存AI回复
                    messageService.saveCharacterMessage(conversationId, aiReply);
                    log.info("消息发送成功, conversationId: {}", conversationId);
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                })
                .doOnError(error -> {
                    log.error("消息发送失败, conversationId: {}", conversationId, error);
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                });
        return resultMono;
    }

    /**
     * 发送消息并获取AI回复（流式输出）
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param skill 技能触发标识
     * @return AI回复流
     */
    public Flux<String> sendMessageStream(Integer userId, Long conversationId, String userMessage, String skill) {
        log.info("发送消息（流式）, userId: {}, conversationId: {}, message: {}, skill: {}", userId, conversationId, userMessage, skill);

        // 验证对话是否属于用户
        if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
            return Flux.error(new RuntimeException("无权访问该对话"));
        }

        // 获取对话信息
        Conversation conversation = conversationService.getConversationById(conversationId);
        if (conversation == null) {
            return Flux.error(new RuntimeException("对话不存在"));
        }

        // 获取角色信息
        AiCharacter character = characterService.getById(conversation.getCharacterId());
        if (character == null) {
            return Flux.error(new RuntimeException("角色不存在"));
        }

        // 保存用户消息
        messageService.saveUserMessage(conversationId, userMessage);

        // 构建对话上下文并进行流式对话
        Mono<String> contextMono = buildConversationContext(conversationId, character, userMessage, skill);
        
        // 处理上下文并获取流式回复
        Flux<String> contextProcessingFlux = contextMono.flatMapMany(context -> {
            // 获取并发控制许可
            Mono<Boolean> permitMono = concurrentControlService.acquirePermit(userId.toString(), "llm");
            
            // 处理许可获取后的流式对话
            return permitMono.flatMapMany(permit -> {
                // 调用AI API获取流式回复
                String contextKey = CONTEXT_CACHE_KEY + conversationId;
                StringBuilder responseBuilder = new StringBuilder();
                
                // 获取AI流式回复
                Flux<String> aiStreamFlux = qiniuAiService.multiTurnChatStream(contextKey, context);
                
                // 处理每个数据块
                // 累积响应内容
                Flux<String> chunkProcessingFlux = aiStreamFlux.doOnNext(responseBuilder::append);
                
                // 处理完成事件
                Flux<String> completionHandlerFlux = chunkProcessingFlux.doOnComplete(() -> {
                    // 流式输出完成后保存完整的AI回复
                    String fullResponse = responseBuilder.toString();
                    if (!fullResponse.trim().isEmpty()) {
                        messageService.saveCharacterMessage(conversationId, fullResponse);
                        log.info("流式消息发送成功, conversationId: {}", conversationId);
                    }
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                });
                
                // 处理错误事件
                return completionHandlerFlux.doOnError(error -> {
                    log.error("流式消息发送失败, conversationId: {}", conversationId, error);
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                });
            });
        });
        
        // 处理最终错误
        Flux<String> finalFlux = contextProcessingFlux.onErrorResume(error -> {
            log.error("流式消息处理失败, conversationId: {}", conversationId, error);
            return Flux.error(error);
        });
        return finalFlux;
    }

    /**
     * 构建对话上下文
     * @param conversationId 对话ID
     * @param character 角色信息
     * @param currentMessage 当前用户消息
     * @param skill 技能参数
     * @return 上下文内容
     */
    private Mono<String> buildConversationContext(Long conversationId, AiCharacter character, String currentMessage, String skill) {
        log.info("构建对话上下文, conversationId: {}, skill: {}", conversationId, skill);

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

        // 如果有技能参数，添加技能触发提示
        if (skill != null && !skill.trim().isEmpty()) {
            CharacterSkill characterSkill = characterSkillService.getSkillByCharacterIdAndName(character.getId(), skill);
            if (characterSkill != null) {
                context.append("技能触发：").append(characterSkill.getTriggerPrompt()).append("\n\n");
                log.info("技能触发成功, characterId: {}, skill: {}", character.getId(), skill);
            } else {
                log.warn("技能触发失败, characterId: {}, skill: {}, 未找到匹配的技能", character.getId(), skill);
            }
        }
        // 添加当前用户消息
        context.append("用户：").append(currentMessage).append("\n");
        context.append("请以").append(character.getName()).append("的身份回复用户，保持角色一致性。");

        return Mono.just(context.toString());
    }

    
    /**
     * 发送语音消息并获取AI回复
     * @param userId 用户ID
     * @param conversationId 对话ID
     * @param localAudioUrl 本地音频文件URL（用于数据库存储）
     * @param ossAudioUrl OSS音频文件URL（用于语音转文本API调用）
     * @param audioFormat 音频格式
     * @param skill 技能触发标识
     * @return AI回复（包含文本和语音URL）
     */
    public Mono<VoiceChatResponse> sendVoiceMessage(Integer userId, Long conversationId, String localAudioUrl, String ossAudioUrl, String audioFormat, String skill) {
        log.info("发送语音消息, userId: {}, conversationId: {}, localAudioUrl: {}, ossAudioUrl: {}, audioFormat: {}, skill: {}", 
                userId, conversationId, localAudioUrl, ossAudioUrl, audioFormat, skill);

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
        AiCharacter character = characterService.getById(conversation.getCharacterId());
        if (character == null) {
            return Mono.error(new RuntimeException("角色不存在"));
        }

        // 获取并发控制许可
        Mono<VoiceChatResponse> resultMono = concurrentControlService.acquirePermit(userId.toString(), "llm")
                .flatMap(permit -> {
                    // 1. 语音转文本 - 使用OSS URL进行语音转文本
                    Mono<String> asrMono = qiniuAudioService.speechToTextFromOssUrl(ossAudioUrl, audioFormat);
                    return asrMono.flatMap(asrText -> {
                        // 2. 保存用户消息（使用本地URL存储到数据库）
                        messageService.saveUserVoiceMessage(conversationId, asrText, localAudioUrl, null);

                        // 3. 构建对话上下文并获取AI回复
                        Mono<String> contextMono = buildConversationContext(conversationId, character, asrText, skill);
                        Mono<String> aiReplyMono = contextMono.flatMap(context -> {
                            String contextKey = CONTEXT_CACHE_KEY + conversationId;
                            return qiniuAiService.multiTurnChat(contextKey, context);
                        });

                        Mono<VoiceChatResponse> responseMono = aiReplyMono.flatMap(aiReplyText -> {
                            // 4. 文本转语音 - 使用角色配置的音色
                            Mono<String> ttsMono = qiniuAudioService.textToSpeechWithCharacter(aiReplyText, character);
                            return ttsMono.flatMap(ttsBase64Data -> {
                                try {
                                    // 5. 保存语音文件
                                    String audioFileUrl = fileStorageService.saveBase64AudioFile(
                                            ttsBase64Data, "mp3");

                                    // 6. 保存AI回复消息（包含文本和语音URL）
                                    messageService.saveCharacterVoiceMessage(conversationId,
                                            aiReplyText, audioFileUrl, null);

                                    // 7. 返回响应
                                    return Mono.just(new VoiceChatResponse(
                                            asrText, aiReplyText, audioFileUrl));
                                } catch (Exception e) {
                                    log.error("保存语音文件失败", e);
                                    return Mono.error(new RuntimeException("保存语音文件失败", e));
                                }
                            });
                        });

                        return responseMono;
                    });
                })
                .doOnSuccess(response -> {
                    log.info("语音消息处理成功, conversationId: {}", conversationId);
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                })
                .doOnError(error -> {
                    log.error("语音消息处理失败, conversationId: {}", conversationId, error);
                    // 释放并发控制许可
                    concurrentControlService.releasePermit(userId.toString(), "llm");
                });
        return resultMono;
    }

    /**
     * 语音聊天响应类
     */
    public static class VoiceChatResponse {
        private final String userText;
        private final String aiText;
        private final String aiAudioUrl;

        public VoiceChatResponse(String userText, String aiText, String aiAudioUrl) {
            this.userText = userText;
            this.aiText = aiText;
            this.aiAudioUrl = aiAudioUrl;
        }

        public String getUserText() {
            return userText;
        }

        public String getAiText() {
            return aiText;
        }

        public String getAiAudioUrl() {
            return aiAudioUrl;
        }
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

        // 先尝试从缓存获取
        List<Message> cachedMessages = conversationCacheService.getCachedMessages(conversationId);
        if (cachedMessages != null) {
            log.info("从缓存获取对话历史, conversationId: {}, 消息数量: {}", conversationId, cachedMessages.size());
            return cachedMessages;
        }

        // 缓存未命中，从数据库获取
        log.info("从数据库获取对话历史, conversationId: {}", conversationId);
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

