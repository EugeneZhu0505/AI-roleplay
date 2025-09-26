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
        Mono<Boolean> permitMono = concurrentControlService.acquirePermit("system", "llm");

        List<MessageContent> messages = List.of(MessageContent.user(message));
        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        Mono<String> resultMono = sendChatReq(request)
                .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                .map(this::extractMessageContent)
                .doOnSuccess(res -> concurrentControlService.releasePermit("system", "llm"))
                .doOnError(error -> {
                    log.error("单次对话失败", error);
                    concurrentControlService.releasePermit("system", "llm");
                });

        Mono<String> finalMono = permitMono.flatMap(permit -> resultMono)
                .onErrorResume(error -> {
                    log.error("获取并发许可失败", error);
                    return Mono.error(new RuntimeException("系统繁忙，请稍后重试"));
                });

        return finalMono;
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
        Mono<String> finalMono = errorHandlerMono.onErrorResume(error -> {
            log.error("获取并发许可失败", error);
            return Mono.error(new RuntimeException("系统繁忙，请稍后重试"));
        });
        
        return finalMono;
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

        String originalContent = choice.getMessage().getContent();
        return filterActionAndThoughts(originalContent);
    }

    /**
     * 过滤AI回复中的内心戏和动作描述
     * @param content 原始内容
     * @return 过滤后的纯对话内容
     */
    private String filterActionAndThoughts(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        // 移除括号内的内容（包括中文括号和英文括号）
        String filtered = content.replaceAll("\\([^)]*\\)", "")  // 英文括号
                                .replaceAll("（[^）]*）", "")      // 中文括号
                                .replaceAll("\\[[^\\]]*\\]", "")  // 方括号
                                .replaceAll("【[^】]*】", "");     // 中文方括号
        
        // 移除多余的空白字符和换行
        filtered = filtered.replaceAll("\\s+", " ")  // 多个空格替换为单个空格
                          .replaceAll("\\n\\s*\\n", "\n")  // 多个换行替换为单个换行
                          .trim();
        
        // 如果过滤后内容为空，返回原内容（避免完全没有回复）
        if (filtered.trim().isEmpty()) {
            log.warn("过滤后内容为空，返回原内容");
            return content;
        }
        
        return filtered;
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
        // 使用局部变量分步构建，避免在 return 中出现长链式调用
        Mono<Boolean> permitMono = concurrentControlService.acquirePermit("system", "streaming");

        List<MessageContent> messages = List.of(MessageContent.user(message));
        LlmChatReq request = LlmChatReq.builder()
                .model(model)
                .messages(messages)
                .stream(true) // 启用流式输出
                .temperature(0.7)
                .maxTokens(2000)
                .build();

        Flux<String> streamFlux = permitMono.flatMapMany(permit ->
                sendChatStreamReq(request)
                        .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                        .doOnComplete(() -> {
                            concurrentControlService.releasePermit("system", "streaming");
                        })
                        .doOnError(error -> {
                            log.error("流式单次对话失败", error);
                            concurrentControlService.releasePermit("system", "streaming");
                        })
        ).onErrorResume(error -> {
            log.error("获取流式并发许可失败", error);
            return Flux.error(new RuntimeException("系统繁忙，请稍后重试"));
        });

        return streamFlux;
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
        Flux<String> resultFlux = historyMono.flatMapMany(history -> {
            LlmChatReq request = LlmChatReq.builder()
                    .model(model)
                    .messages(history)
                    .stream(true) // 启用流式输出
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            StringBuilder responseBuilder = new StringBuilder();
            
            // 发送流式请求，现在直接返回内容
            return sendChatStreamReq(request)
                    .subscribeOn(Schedulers.fromExecutor(llmRequestExecutor))
                    .doOnNext(content -> {
                        // 累积响应内容用于保存到历史
                        responseBuilder.append(content);
                    })
                    .doOnComplete(() -> {
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
                    })
                    .doOnError(error -> {
                        log.error("流式多轮对话失败", error);
                    });
        });

        return resultFlux;
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
                // 提取每个chunk中的content内容
                .map(this::extractContentFromStreamChunk)
                .filter(content -> content != null && !content.isEmpty())
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
            if (chunk != null && chunk.contains("\"content\":")) {
                int start = chunk.indexOf("\"content\":\"") + 11;
                int end = chunk.indexOf("\"", start);
                if (start > 10 && end > start) {
                    String content = chunk.substring(start, end);
                    // 处理转义字符
                    String unescapedContent = content.replace("\\n", "\n")
                                 .replace("\\t", "\t")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\");
                    
                    // 对流式内容也进行过滤
                    return filterActionAndThoughts(unescapedContent);
                }
            }
        } catch (Exception e) {
            log.warn("提取流式内容失败: {}", chunk, e);
        }
        // 返回空字符串而不是null，避免NullPointerException
        return "";
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


