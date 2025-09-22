package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.dto.ChatMessage;
import com.hzau.service.QiniuAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: AiChatController
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午9:09
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI聊天", description = "AI聊天相关接口")
public class AiChatController {

    private final QiniuAiService qiniuAiService;

    /**
     * 检查AI服务配置状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查AI服务状态", description = "检查API密钥配置是否正确")
    public Result<Map<String, Object>> checkStatus() {
        boolean isValid = qiniuAiService.isConfigValid();
        return Result.success(Map.of(
                "configured", isValid,
                "message", isValid ? "AI服务配置正常" : "请配置正确的API密钥"
        ));
    }

    /**
     * 单次对话
     */
    @PostMapping("/chat/single")
    @Operation(summary = "单次对话", description = "发送单条消息给AI并获取回复")
    public Mono<Result<String>> singleChat(
            @Parameter(description = "用户消息", required = true)
            @RequestBody Map<String, String> request) {

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "消息内容不能为空"));
        }

        if (!qiniuAiService.isConfigValid()) {
            return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "AI服务未正确配置，请检查API密钥"));
        }

        log.info("单次对话请求: {}", message);

        return qiniuAiService.singleChat(message.trim())
                .map(response -> {
                    log.info("单次对话响应: {}", response);
                    return Result.success(response);
                })
                .onErrorReturn(Result.fail(ErrorCode.ERROR400.getCode(), "AI服务调用失败，请稍后重试"));
    }

    /**
     * 多轮对话
     */
    @PostMapping("/chat/multi/{conversationId}")
    @Operation(summary = "多轮对话", description = "在指定会话中发送消息")
    public Mono<Result<String>> multiTurnChat(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId,
            @Parameter(description = "用户消息", required = true)
            @RequestBody Map<String, String> request) {

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "消息内容不能为空"));
        }

        if (!qiniuAiService.isConfigValid()) {
            return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "AI服务未正确配置，请检查API密钥"));
        }

        log.info("多轮对话请求 [{}]: {}", conversationId, message);

        return qiniuAiService.multiTurnChat(conversationId, message.trim())
                .map(response -> {
                    log.info("多轮对话响应 [{}]: {}", conversationId, response);
                    return Result.success(response);
                })
                .onErrorReturn(Result.fail(ErrorCode.ERROR500.getCode(), "AI服务调用失败，请稍后重试"));
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/chat/history/{conversationId}")
    @Operation(summary = "获取会话历史", description = "获取指定会话的消息历史")
    public Result<List<ChatMessage>> getConversationHistory(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId) {

        List<ChatMessage> history = qiniuAiService.getConversationHistory(conversationId);
        return Result.success(history);
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/chat/history/{conversationId}")
    @Operation(summary = "清除会话历史", description = "清除指定会话的所有消息历史")
    public Result<String> clearConversationHistory(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String conversationId) {

        qiniuAiService.clearConversation(conversationId);
        return Result.success("会话历史已清除");
    }

    /**
     * 测试接口 - 快速验证功能
     */
    @GetMapping("/test")
    @Operation(summary = "快速测试", description = "发送测试消息验证AI服务是否正常")
    public Mono<Result<String>> quickTest() {
        if (!qiniuAiService.isConfigValid()) {
            return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "AI服务未正确配置，请在application.yml中设置正确的API密钥"));
        }

        String testMessage = "你现在是哈利波特, 请介绍下自己的个人情况和能力.";
        log.info("执行快速测试: {}", testMessage);

        return qiniuAiService.singleChat(testMessage)
                .map(response -> {
                    log.info("快速测试成功: {}", response);
                    return Result.success("测试成功！AI回复: " + response);
                })
                .onErrorResume(error -> {
                    log.error("快速测试失败", error);
                    return Mono.just(Result.fail(ErrorCode.ERROR400.getCode(), "测试失败: " + error.getMessage()));
                });
    }
}

