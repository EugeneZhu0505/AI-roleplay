package com.hzau.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: ChatMessage
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午9:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 消息角色：system, user, assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建用户消息
     */
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    /**
     * 创建助手消息
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }

    /**
     * 创建系统消息
     */
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }
}
