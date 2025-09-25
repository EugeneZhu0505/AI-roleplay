package com.hzau.service;

import com.hzau.config.QiniuAiConfig;
import com.hzau.dto.LlmChatReq;
import com.hzau.dto.LlmChatRes;
import com.hzau.dto.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: QiniuAiService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午9:07
 */
@Service
@Slf4j
public class QiniuAiService {
    
    private final QiniuAiConfig config;
    private final WebClient webClient;
    private final ConcurrentControlService concurrentControlService;
    private final Executor llmRequestExecutor;
    private final Executor messageProcessingExecutor;
    private final Executor sessionManagementExecutor;
    
    /**
     * 存储多轮对话的会话历史
     * Key: 会话ID, Value: 消息历史列表
     */
    private final ConcurrentMap<String, List<MessageContent>> conversationHistory = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，初始化WebClient和线程池
     */
    public QiniuAiService(QiniuAiConfig config, 
                         ConcurrentControlService concurrentControlService,
                         @Qualifier("llmRequestExecutor") Executor llmRequestExecutor,
                         @Qualifier("messageProcessingExecutor") Executor messageProcessingExecutor,
                         @Qualifier("sessionManagementExecutor") Executor sessionManagementExecutor) {
        this.config = config;
        this.concurrentControlService = concurrentControlService;
        this.llmRequestExecutor = llmRequestExecutor;
        this.messageProcessingExecutor = messageProcessingExecutor;
        this.sessionManagementExecutor = sessionManagementExecutor;
        this.webClient = WebClient.builder()
                .baseUrl(config.getPrimaryEndpoint())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 单次对话
     * @param message 用户消息
     * @return AI回复
     */
    public Mono<String> singleChat(String message) {
        return singleChat(message, config.getDefaultModel());
    }

    /**
     * 单次对话（指定模型）
     * @param message 用户消息
     * @param model 模型名称
     * @return AI回复
     */
    public Mono<String> singleChat(String message, String model) {
        // 获取并发控制许可
        return concurrentControlService.acquirePermit("system", "llm")
                .flatMap(permit -> {
                    List<MessageContent> messages = List.of(MessageContent.user(message));

                    LlmChatReq request = LlmChatReq.builder()
                            .model(model)
                            .messages(messages)
                            .stream(false)
                            .temperature(0.7)
                            .maxTokens(2000)
                            .build();

                    return sendChatReq(request)
                            .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                            .map(this::extractMessageContent)
                            .doOnSuccess(result -> {
                                concurrentControlService.releasePermit("system", "llm");
                            })
                            .doOnError(error -> {
                                log.error("单次对话失败", error);
                                concurrentControlService.releasePermit("system", "llm");
                            });
                })
                .onErrorResume(error -> {
                    log.error("获取并发许可失败", error);
                    return Mono.error(new RuntimeException("系统繁忙，请稍后重试"));
                });
    }

    /**
     * 多轮对话
     * @param conversationId 会话ID
     * @param message 用户消息
     * @return AI回复
     */
    public Mono<String> multiTurnChat(String conversationId, String message) {
        return multiTurnChat(conversationId, message, config.getDefaultModel());
    }

    /**
     * 多轮对话（指定模型）
     * @param conversationId 会话ID
     * @param message 用户消息
     * @param model 模型名称
     * @return AI回复
     */
    public Mono<String> multiTurnChat(String conversationId, String message, String model) {
        // 获取并发控制许可
        Mono<Boolean> permitMono = concurrentControlService.acquirePermit("system", "llm");
        
        // 处理会话历史
        Mono<List<MessageContent>> historyMono = permitMono.flatMap(permit -> {
            return Mono.fromCallable(() -> {
                // 在会话管理线程池中处理会话历史
                List<MessageContent> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());
                history.add(MessageContent.user(message));
                return new ArrayList<>(history); // 创建副本避免并发修改
            }).subscribeOn(Schedulers.fromExecutor(sessionManagementExecutor));
        });
        
        // 发送聊天请求并处理响应
        Mono<String> chatResponseMono = historyMono.flatMap(history -> {
            LlmChatReq request = LlmChatReq.builder()
                    .model(model)
                    .messages(history)
                    .stream(false)
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            return sendChatReq(request)
                    .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                    .map(this::extractMessageContent);
        });
        
        // 处理成功响应
        Mono<String> successHandlerMono = chatResponseMono.doOnSuccess(response -> {
            // 在消息处理线程池中更新会话历史
            Mono.fromRunnable(() -> {
                if (response != null) {
                    List<MessageContent> currentHistory = conversationHistory.get(conversationId);
                    if (currentHistory != null) {
                        currentHistory.add(MessageContent.assistant(response));
                        // 限制历史长度，避免token过多
                        if (currentHistory.size() > 20) {
                            currentHistory.subList(0, currentHistory.size() - 20).clear();
                        }
                    }
                }
            })
            .subscribeOn(Schedulers.fromExecutor(messageProcessingExecutor))
            .subscribe();
            
            concurrentControlService.releasePermit("system", "llm");
        });
        
        // 处理错误响应
        Mono<String> errorHandlerMono = successHandlerMono.doOnError(error -> {
            log.error("多轮对话失败", error);
            concurrentControlService.releasePermit("system", "llm");
        });
        
        // 处理许可获取失败
        return errorHandlerMono.onErrorResume(error -> {
            log.error("获取并发许可失败", error);
            return Mono.error(new RuntimeException("系统繁忙，请稍后重试"));
        });
    }

    /**
     * 清除会话历史
     * @param conversationId 会话ID
     */
    public void clearConversation(String conversationId) {
        Mono.fromRunnable(() -> {
            conversationHistory.remove(conversationId);
            log.info("已清除会话历史: {}", conversationId);
        })
        .subscribeOn(Schedulers.fromExecutor(sessionManagementExecutor))
        .subscribe();
    }

    /**
     * 获取会话历史
     * @param conversationId 会话ID
     * @return 消息历史列表
     */
    public List<MessageContent> getConversationHistory(String conversationId) {
        return conversationHistory.getOrDefault(conversationId, new ArrayList<>());
    }

    /**
     * 发送聊天请求
     * @param request 请求对象
     * @return 响应对象
     */
    private Mono<LlmChatRes> sendChatReq(LlmChatReq request) {
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LlmChatRes.class)
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .retryWhen(Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest)))
                .doOnError(error -> log.error("调用七牛云AI API失败", error))
                .onErrorMap(error -> new RuntimeException("AI服务调用失败: " + error.getMessage(), error));
    }

    /**
     * 从响应中提取消息内容
     * @param response 响应对象
     * @return 消息内容
     */
    private String extractMessageContent(LlmChatRes response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("AI响应格式错误");
        }

        LlmChatRes.Choice choice = response.getChoices().get(0);
        if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw new RuntimeException("AI响应内容为空");
        }

        return choice.getMessage().getContent();
    }

    /**
     * 流式单次对话
     * @param message 用户消息
     * @return AI回复流
     */
    public Flux<String> singleChatStream(String message) {
        return singleChatStream(message, config.getDefaultModel());
    }

    /**
     * 流式单次对话（指定模型）
     * @param message 用户消息
     * @param model 模型名称
     * @return AI回复流
     */
    public Flux<String> singleChatStream(String message, String model) {
        // 获取流式并发控制许可
        return concurrentControlService.acquirePermit("system", "streaming")
                .flatMapMany(permit -> {
                    List<MessageContent> messages = List.of(MessageContent.user(message));

                    LlmChatReq request = LlmChatReq.builder()
                            .model(model)
                            .messages(messages)
                            .stream(true) // 启用流式输出
                            .temperature(0.7)
                            .maxTokens(2000)
                            .build();

                    return sendChatStreamReq(request)
                            .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                            .doOnComplete(() -> {
                                concurrentControlService.releasePermit("system", "streaming");
                            })
                            .doOnError(error -> {
                                log.error("流式单次对话失败", error);
                                concurrentControlService.releasePermit("system", "streaming");
                            });
                })
                .onErrorResume(error -> {
                    log.error("获取流式并发许可失败", error);
                    return Flux.error(new RuntimeException("系统繁忙，请稍后重试"));
                });
    }

    /**
     * 流式多轮对话
     * @param conversationId 会话ID
     * @param message 用户消息
     * @return AI回复流
     */
    public Flux<String> multiTurnChatStream(String conversationId, String message) {
        return multiTurnChatStream(conversationId, message, config.getDefaultModel());
    }

    /**
     * 流式多轮对话（指定模型）
     * @param conversationId 会话ID
     * @param message 用户消息
     * @param model 模型名称
     * @return AI回复流
     */
    public Flux<String> multiTurnChatStream(String conversationId, String message, String model) {
        // 处理会话历史
        Mono<List<MessageContent>> historyMono = Mono.fromCallable(() -> {
            // 在会话管理线程池中处理会话历史
            List<MessageContent> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());
            history.add(MessageContent.user(message));
            return (List<MessageContent>) new ArrayList<>(history); // 创建副本避免并发修改
        }).subscribeOn(Schedulers.fromExecutor(sessionManagementExecutor));
        
        // 发送流式聊天请求并处理响应
        return historyMono.flatMapMany(history -> {
            LlmChatReq request = LlmChatReq.builder()
                    .model(model)
                    .messages(history)
                    .stream(true) // 启用流式输出
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            StringBuilder responseBuilder = new StringBuilder();
            
            // 发送流式请求
            Flux<String> streamRequestFlux = sendChatStreamReq(request)
                    .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor));
            
            // 处理每个数据块
            Flux<String> processedStreamFlux = streamRequestFlux.doOnNext(chunk -> {
                // 累积响应内容
                try {
                    // 解析流式响应中的内容
                    if (chunk.startsWith("{") && chunk.contains("\"content\"")) {
                        // 简单的JSON解析，实际项目中建议使用Jackson
                        String content = extractContentFromStreamChunk(chunk);
                        if (content != null && !content.isEmpty()) {
                            responseBuilder.append(content);
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析流式响应块失败: {}", chunk, e);
                }
            });
            
            // 处理流式响应完成事件
            Flux<String> streamWithCompleteHandler = processedStreamFlux.doOnComplete(() -> {
                // 流式响应完成后，在消息处理线程池中将完整回复添加到历史
                Mono.fromRunnable(() -> {
                    String fullResponse = responseBuilder.toString();
                    if (!fullResponse.isEmpty()) {
                        List<MessageContent> currentHistory = conversationHistory.get(conversationId);
                        if (currentHistory != null) {
                            currentHistory.add(MessageContent.assistant(fullResponse));
                            // 限制历史长度，避免token过多
                            if (currentHistory.size() > 20) {
                                currentHistory.subList(0, currentHistory.size() - 20).clear();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.fromExecutor(messageProcessingExecutor))
                .subscribe();
            });
            
            // 处理流式响应错误事件
            return streamWithCompleteHandler.doOnError(error -> {
                log.error("流式多轮对话失败", error);
            });
        });
    }

    /**
     * 发送流式聊天请求
     * @param request 请求对象
     * @return 响应流
     */
    private Flux<String> sendChatStreamReq(LlmChatReq request) {
        return webClient.post()
                .uri("/chat/completions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(config.getTimeout()))
                .retryWhen(Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest)))
                // 过滤空行并规范化为仅包含SSE的data内容
                .filter(line -> line != null && !line.trim().isEmpty())
                .map(line -> {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("data:")) {
                        // 去掉前缀 data: 或 data:  (带空格)
                        int idx = trimmed.indexOf(':');
                        String data = trimmed.substring(idx + 1).trim();
                        return data;
                    }
                    return trimmed;
                })
                .takeUntil(data -> "[DONE]".equals(data) || "data: [DONE]".equals(data))
                .filter(data -> !"[DONE]".equals(data))
                .doOnError(error -> log.error("调用七牛云AI流式API失败", error))
                .onErrorMap(error -> new RuntimeException("AI流式服务调用失败: " + error.getMessage(), error));
    }

    /**
     * 从流式响应块中提取内容
     * @param chunk 响应块
     * @return 提取的内容
     */
    private String extractContentFromStreamChunk(String chunk) {
        try {
            // 简单的内容提取，实际项目中建议使用Jackson
            if (chunk.contains("\"content\":")) {
                int start = chunk.indexOf("\"content\":\"") + 11;
                int end = chunk.indexOf("\"", start);
                if (start > 10 && end > start) {
                    return chunk.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.warn("提取流式内容失败: {}", chunk, e);
        }
        return null;
    }

    /**
     * 检查API配置是否有效
     * @return 是否有效
     */
    public boolean isConfigValid() {
        return config.getApiKey() != null &&
                !config.getApiKey().isEmpty() &&
                !"YOUR_API_KEY_HERE".equals(config.getApiKey());
    }
}


