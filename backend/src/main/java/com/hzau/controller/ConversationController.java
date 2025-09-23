package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;
import com.hzau.dto.UserChatReq;
import com.hzau.entity.Character;
import com.hzau.entity.Conversation;
import com.hzau.entity.Message;
import com.hzau.service.AiRoleplayService;
import com.hzau.service.CharacterService;
import com.hzau.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final CharacterService characterService;
    private final ConversationService conversationService;



    /**
     * 获取角色开场白
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
            @RequestParam(required = false) String title) {
        try {
            log.info("创建新对话, userId: {}, characterId: {}, title: {}", userId, characterId, title);
            return aiRoleplayService.startNewConversation(userId, characterId, title)
                    .map(result -> {
                        Map<String, Object> response = Map.of(
                                "conversation", result.getConversation(),
                                "opening", result.getOpening()
                        );
                        return Result.success(response);
                    })
                    .onErrorReturn(Result.fail(ErrorCode.ERROR500.getCode(), "创建对话失败"));
        } catch (Exception e) {
            log.error("创建新对话失败, userId: {}, characterId: {}", userId, characterId, e);
            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "创建对话失败"));
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "发送消息", description = "在指定对话中发送消息并获取AI回复")
    public Mono<Result<String>> sendMessage(
            @Parameter(description = "对话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Integer userId,
            @Parameter(description = "用户消息", required = true)
            @RequestBody UserChatReq request) {
        try {
            log.info("发送消息, conversationId: {}, userId: {}", conversationId, userId);
            return aiRoleplayService.sendMessage(userId, conversationId, request.getMessage())
                    .map(Result::success)
                    .onErrorReturn(Result.fail(ErrorCode.ERROR500.getCode(), "发送消息失败"));
        } catch (Exception e) {
            log.error("发送消息失败, conversationId: {}", conversationId, e);
            return Mono.just(Result.fail(ErrorCode.ERROR500.getCode(), "发送消息失败"));
        }
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
}

