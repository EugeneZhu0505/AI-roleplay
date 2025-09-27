package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.entity.Conversation;
import com.hzau.entity.Message;
import com.hzau.service.AiRoleplayService;
import com.hzau.service.ConversationService;
import com.hzau.service.ConversationCacheService;
import com.hzau.service.FileStorageService;
import com.hzau.service.PerformanceMonitoringService;
import com.hzau.service.QiniuUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: ConversationController
 * @author: zhuyuchen
 * @description: 对话管理控制器，负责AI角色对话的创建、消息发送和对话历史管理
 * @date: 2025/9/23 下午3:09
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "对话管理", description = "AI角色对话相关接口")
public class ConversationController {

    private final AiRoleplayService aiRoleplayService;
    private final ConversationService conversationService;
    private final ConversationCacheService conversationCacheService;
    private final PerformanceMonitoringService monitoringService;
    private final FileStorageService fileStorageService;
    private final QiniuUploadService qiniuUploadService;


    /**
     * 获取角色开场白
     * @param characterId
     * @return
     */
    @GetMapping("/characters/{characterId}/opening")
    @Operation(summary = "获取角色开场白", description = "获取指定角色的开场白")
    public Mono<Result<String>> getCharacterOpening(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long characterId) {
        try {
            log.info("获取角色开场白, characterId: {}", characterId);
            return aiRoleplayService.getCharacterOpening(characterId)
                    .map(Result::success)
                    .onErrorReturn(Result.fail(ErrorCode.ERROR500.getCode(), "获取角色开场白失败"));
        } catch (Exception e) {
            log.error("获取角色开场白失败, characterId: {}", characterId, e);
            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "获取角色开场白失败"));
        }
    }

    /**
     * 创建新对话
     */
    @PostMapping
    @Operation(summary = "创建新对话", description = "与指定角色创建新的对话")
    public Mono<Result<Map<String, Object>>> createConversation(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId,
            @Parameter(description = "角色ID", required = true)
            @RequestParam Long characterId,
            @Parameter(description = "对话标题")
            @RequestParam(required = true) String title) {
        
        String callId = monitoringService.startApiCall("createConversation", userId.toString());
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("创建新对话, userId: {}, characterId: {}, title: {}", userId, characterId, title);
            
            // 调用服务创建新对话
            Mono<AiRoleplayService.ConversationWithOpening> conversationMono =
                    aiRoleplayService.startNewConversation(userId, characterId, title);
            
            // 处理成功响应
            Mono<Result<Map<String, Object>>> successHandler = conversationMono.map(result -> {
                // 构建响应数据
                Map<String, Object> response = Map.of(
                        "conversation", result.getConversation(),
                        "opening", result.getOpening()
                );
                
                // 记录成功的监控信息
                long responseTime = System.currentTimeMillis() - startTime;
                monitoringService.endApiCall("createConversation", callId, responseTime, true);
                
                return Result.success(response);
            });
            
            // 处理错误响应

            return successHandler.onErrorResume(error -> {
                log.error("创建对话服务调用失败", error);

                // 记录失败的监控信息
                long responseTime = System.currentTimeMillis() - startTime;
                monitoringService.endApiCall("createConversation", callId, responseTime, false);

                return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "创建对话失败"));
            });
            
        } catch (Exception e) {
            log.error("创建新对话失败, userId: {}, characterId: {}", userId, characterId, e);
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.endApiCall("createConversation", callId, responseTime, false);
            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "创建对话失败"));
        }
    }


    /**
     * 文本格式进行聊天
     * @param conversationId
     * @param userId
     * @param message
     * @param skill
     * @return
     */
    @PostMapping(value = "/{conversationId}/text-messages", 
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE},
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送文本消息", description = "发送文本消息并获取AI流式回复")
    public Flux<String> sendTextMessage(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId,
            @Parameter(description = "用户消息", required = true)
            @RequestParam String message,
            @Parameter(description = "技能触发标识", required = false)
            @RequestParam(value = "skill", required = false) String skill) {
        
        String callId = monitoringService.startApiCall("sendTextMessage", userId.toString());
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("发送文本消息, conversationId: {}, userId: {}, message: {}, skill: {}",
                    conversationId, userId, message, skill);
            
            // 验证消息不能为空
            if (message == null || message.trim().isEmpty()) {
                return Flux.just("data: " + "{\"error\":\"消息内容不能为空\"}\n\n");
            }
            
            // 调用流式消息服务
            Flux<String> messageStreamFlux = aiRoleplayService.sendMessageStream(userId, conversationId, message, skill);
            
            // 格式化流式数据
            Flux<String> formattedStreamFlux = messageStreamFlux.map(chunk -> "data: " + chunk + "\n\n");
            
            // 添加结束标记
            Flux<String> streamWithEndMarker = formattedStreamFlux.concatWith(Flux.just("data: [DONE]\n\n"));
            
            // 处理流式响应完成事件
            Flux<String> streamWithCompleteHandler = streamWithEndMarker.doOnComplete(() -> {
                // 记录成功的监控信息
                long responseTime = System.currentTimeMillis() - startTime;
                monitoringService.endApiCall("sendTextMessage", callId, responseTime, true);
            });
            
            // 处理流式响应错误事件
            return streamWithCompleteHandler.onErrorResume(error -> {
                log.error("流式文本消息发送失败", error);
                
                // 记录失败的监控信息
                long responseTime = System.currentTimeMillis() - startTime;
                monitoringService.endApiCall("sendTextMessage", callId, responseTime, false);
                
                return Flux.just("data: " + "{\"error\":\"发送消息失败\"}\n\n");
            });
            
        } catch (Exception e) {
            log.error("发送文本消息失败, conversationId: {}", conversationId, e);
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.endApiCall("sendTextMessage", callId, responseTime, false);
            
            return Flux.just("data: " + "{\"error\":\"发送消息失败\"}\n\n");
        }
    }


    /**
     * 语音格式进行聊天
     * @param conversationId
     * @param userId
     * @param audioFileMono
     * @param audioFormat
     * @param skill
     * @return
     */
    @PostMapping(value = "/{conversationId}/audio-messages",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "发送语音消息", description = "上传语音文件并获取AI回复")
    public Mono<Result<Object>> sendAudioMessage(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestPart("userId") String userId,
            @Parameter(description = "语音文件", required = true)
            @RequestPart("audioFile") Mono<FilePart> audioFileMono,
            @Parameter(description = "音频格式", required = false)
            @RequestPart(value = "audioFormat", required = false) String audioFormat,
            @Parameter(description = "技能触发标识", required = false)
            @RequestPart(value = "skill", required = false) String skill) {
        
        String callId = monitoringService.startApiCall("sendVoiceMessage", userId.toString());
        long startTime = System.currentTimeMillis();

        return audioFileMono.flatMap(audioFile -> {
            try {
                log.info("发送语音消息, conversationId: {}, userId: {}, audioFormat: {}, skill: {}",
                        conversationId, userId, audioFormat, skill);
                
                // 验证语音文件
                if (audioFile == null) {
                    return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "语音文件不能为空"));
                }
                
                // 创建临时文件来保存上传的文件内容
                Path tempFile = Files.createTempFile("audio_", "_" + audioFile.filename());
                
                // 将FilePart内容写入临时文件并获取字节数组
                return audioFile.transferTo(tempFile)
                    .then(Mono.fromCallable(() -> {
                        byte[] audioBytes = Files.readAllBytes(tempFile);
                        String originalFilename = audioFile.filename();
                        
                        // 清理临时文件
                        Files.deleteIfExists(tempFile);
                        
                        return new Object[]{audioBytes, originalFilename};
                    }))
                    .flatMap(fileData -> {
                        byte[] audioBytes = (byte[]) ((Object[]) fileData)[0];
                        String originalFilename = (String) ((Object[]) fileData)[1];
                        
                        try {
                            // 1. 上传到七牛云OSS，获取url2（用于语音转文本API调用）
                            String ossAudioUrl = qiniuUploadService.uploadBytes(audioBytes, originalFilename);
                            log.info("语音文件上传到OSS成功, ossUrl: {}", ossAudioUrl);
                            
                            // 2. 保存到本地服务器，获取url1（用于数据库存储）
                            String localAudioUrl = fileStorageService.saveAudioBytes(audioBytes, originalFilename);
                            log.info("语音文件保存到本地成功, localUrl: {}", localAudioUrl);
                            
                            // 3. 调用语音消息服务，传入本地URL和OSS URL
                            Integer userIdInt = Integer.parseInt(userId);
                            Mono<AiRoleplayService.VoiceChatResponse> voiceMessageMono = 
                                aiRoleplayService.sendVoiceMessage(userIdInt, conversationId, localAudioUrl, ossAudioUrl, audioFormat, skill);
                            
                            // 处理语音消息成功响应
                            Mono<Result<Object>> voiceSuccessHandler = voiceMessageMono.map(response -> {
                                // 记录成功的监控信息
                                long responseTime = System.currentTimeMillis() - startTime;
                                monitoringService.endApiCall("sendVoiceMessage", callId, responseTime, true);
                                
                                return Result.success(response);
                            });
                            
                            // 处理语音消息错误响应
                            return voiceSuccessHandler.onErrorResume(error -> {
                                log.error("语音消息处理失败", error);
                                
                                // 记录失败的监控信息
                                long responseTime = System.currentTimeMillis() - startTime;
                                monitoringService.endApiCall("sendVoiceMessage", callId, responseTime, false);
                                
                                return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "语音消息处理失败"));
                            });
                            
                        } catch (Exception e) {
                            log.error("语音文件处理失败", e);
                            long responseTime = System.currentTimeMillis() - startTime;
                            monitoringService.endApiCall("sendVoiceMessage", callId, responseTime, false);
                            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "语音文件处理失败: " + e.getMessage()));
                        }
                    });
                    
            } catch (IOException e) {
                log.error("创建临时文件失败", e);
                long responseTime = System.currentTimeMillis() - startTime;
                monitoringService.endApiCall("sendVoiceMessage", callId, responseTime, false);
                return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "文件处理失败: " + e.getMessage()));
            }
        }).onErrorResume(error -> {
            log.error("语音文件上传处理失败", error);
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.endApiCall("sendVoiceMessage", callId, responseTime, false);
            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "语音文件上传失败"));
        });
    }

    /**
     * 获取用户对话列表
     */
    @GetMapping
    @Operation(summary = "获取对话列表", description = "获取指定用户的所有对话")
    public Result<List<Conversation>> getUserConversations(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("获取用户对话列表, userId: {}", userId);
            List<Conversation> conversations = conversationService.getUserConversations(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取用户对话列表失败, userId: {}", userId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取对话列表失败");
        }
    }

    /**
     * 获取与特定角色的对话列表
     */
    @GetMapping("/character/{characterId}")
    @Operation(summary = "获取与特定角色的对话列表", description = "获取用户与指定角色的所有对话")
    public Result<List<Conversation>> getUserConversationsByCharacter(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId,
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long characterId) {
        try {
            log.info("获取与特定角色的对话列表, userId: {}, characterId: {}", userId, characterId);
            List<Conversation> conversations = conversationService.getUserConversationsByCharacter(userId, characterId);
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取与特定角色的对话列表失败, userId: {}, characterId: {}", userId, characterId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取对话列表失败");
        }
    }

    /**
     * 获取对话详情
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "获取对话详情", description = "获取指定对话的详细信息")
    public Result<Conversation> getConversationById(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("获取对话详情, conversationId: {}, userId: {}", conversationId, userId);

            // 验证对话是否属于用户
            if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
                return Result.fail(ErrorCode.ERROR403.getCode(), "无权访问该对话");
            }

            Conversation conversation = conversationService.getConversationById(conversationId);
            if (conversation == null) {
                return Result.fail(ErrorCode.ERROR404.getCode(), "对话不存在");
            }

            return Result.success(conversation);
        } catch (Exception e) {
            log.error("获取对话详情失败, conversationId: {}", conversationId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取对话详情失败");
        }
    }

    /**
     * 获取对话历史消息
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "获取对话历史", description = "获取指定对话的所有历史消息")
    public Result<List<Message>> getConversationMessages(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("获取对话历史, conversationId: {}, userId: {}", conversationId, userId);
            List<Message> messages = aiRoleplayService.getConversationHistory(userId, conversationId);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取对话历史失败, conversationId: {}", conversationId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取对话历史失败");
        }
    }

    /**
     * 更新对话标题
     */
    @PutMapping("/{conversationId}/title")
    @Operation(summary = "更新对话标题", description = "更新指定对话的标题")
    public Result<String> updateConversationTitle(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId,
            @Parameter(description = "新标题", required = true)
            @RequestParam String title) {
        try {
            log.info("更新对话标题, conversationId: {}, userId: {}, title: {}", conversationId, userId, title);

            // 验证对话是否属于用户
            if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
                return Result.fail(ErrorCode.ERROR403.getCode(), "无权访问该对话");
            }

            boolean success = conversationService.updateConversationTitle(conversationId, title);
            if (success) {
                return Result.success("对话标题更新成功");
            } else {
                return Result.fail(ErrorCode.ERROR500.getCode(), "对话标题更新失败");
            }
        } catch (Exception e) {
            log.error("更新对话标题失败, conversationId: {}", conversationId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "更新对话标题失败");
        }
    }

    /**
     * 激活对话
     * @param conversationId
     * @param userId
     * @return
     */
    @PostMapping("/{conversationId}/activate")
    @Operation(summary = "激活对话", description = "用户打开聊天窗口时激活对话缓存")
    public Result<String> activateConversation(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("激活对话, conversationId: {}, userId: {}", conversationId, userId);

            // 验证对话是否属于用户
            if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
                return Result.fail(ErrorCode.ERROR403.getCode(), "无权访问该对话");
            }

            conversationCacheService.activateConversation(userId, conversationId);
            return Result.success("对话激活成功");
        } catch (Exception e) {
            log.error("激活对话失败, conversationId: {}", conversationId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "激活对话失败");
        }
    }

    /**
     * 去激活对话 - 用户关闭聊天窗口时调用
     */
    @PostMapping("/{conversationId}/deactivate")
    @Operation(summary = "去激活对话", description = "用户关闭聊天窗口时去激活对话缓存")
    public Result<String> deactivateConversation(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("去激活对话, conversationId: {}, userId: {}", conversationId, userId);

            // 验证对话是否属于用户
            if (!conversationService.isConversationOwnedByUser(conversationId, userId)) {
                return Result.fail(ErrorCode.ERROR403.getCode(), "无权访问该对话");
            }

            conversationCacheService.deactivateConversation(userId, conversationId);
            return Result.success("对话去激活成功");
        } catch (Exception e) {
            log.error("去激活对话失败, conversationId: {}", conversationId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "去激活对话失败");
        }
    }

    /**
     * 获取用户激活的对话列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取激活对话列表", description = "获取用户当前激活的对话列表")
    public Result<Object> getUserActiveConversations(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId) {
        try {
            log.info("获取用户激活对话列表, userId: {}", userId);
            Object activeConversations = conversationCacheService.getUserActiveConversations(userId);
            return Result.success(activeConversations);
        } catch (Exception e) {
            log.error("获取用户激活对话列表失败, userId: {}", userId, e);
            return Result.fail(ErrorCode.ERROR500.getCode(), "获取激活对话列表失败");
        }
    }
}

