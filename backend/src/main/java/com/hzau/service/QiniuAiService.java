package com.hzau.service;

import com.hzau.config.QiniuAiConfig;
import com.hzau.dto.LlmChatReq;
import com.hzau.dto.LlmChatRes;
import com.hzau.dto.MessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    
    /**
     * 存储多轮对话的会话历史
     * Key: 会话ID, Value: 消息历史列表
     */
    private final ConcurrentMap<String, List<MessageContent>> conversationHistory = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，初始化WebClient
     */
    public QiniuAiService(QiniuAiConfig config) {
        this.config = config;
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
        List<MessageContent> messages = List.of(MessageContent.user(message));

        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        return sendChatReq(request)
                .map(this::extractMessageContent);
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
        // 获取或创建会话历史
        List<MessageContent> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());

        // 添加用户消息到历史
        history.add(MessageContent.user(message));

        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(new ArrayList<>(history)) // 创建副本避免并发修改
                .stream(false)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        return sendChatReq(request)
                .map(this::extractMessageContent)
                .doOnSuccess(response -> {
                    // 将AI回复添加到历史
                    if (response != null) {
                        history.add(MessageContent.assistant(response));
                        // 限制历史长度，避免token过多
                        if (history.size() > 20) {
                            history.subList(0, history.size() - 20).clear();
                        }
                    }
                });
    }

    /**
     * 清除会话历史
     * @param conversationId 会话ID
     */
    public void clearConversation(String conversationId) {
        conversationHistory.remove(conversationId);
        log.info("已清除会话历史: {}", conversationId);
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
        List<MessageContent> messages = List.of(MessageContent.user(message));

        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(messages)
                .stream(true) // 启用流式输出
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        return sendChatStreamReq(request);
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
        // 获取或创建会话历史
        List<MessageContent> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());

        // 添加用户消息到历史
        history.add(MessageContent.user(message));

        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(new ArrayList<>(history)) // 创建副本避免并发修改
                .stream(true) // 启用流式输出
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        StringBuilder responseBuilder = new StringBuilder();
        
        return sendChatStreamReq(request)
                .doOnNext(chunk -> {
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
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，将完整回复添加到历史
                    String fullResponse = responseBuilder.toString();
                    if (!fullResponse.isEmpty()) {
                        history.add(MessageContent.assistant(fullResponse));
                        // 限制历史长度，避免token过多
                        if (history.size() > 20) {
                            history.subList(0, history.size() - 20).clear();
                        }
                    }
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

