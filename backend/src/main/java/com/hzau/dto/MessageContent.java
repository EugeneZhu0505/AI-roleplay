package com.hzau.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: MessageContent
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 上午10:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageContent {

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
    public static MessageContent user(String content) {
        return new MessageContent("user", content);
    }

    /**
     * 创建助手消息
     */
    public static MessageContent assistant(String content) {
        return new MessageContent("assistant", content);
    }

    /**
     * 创建系统消息
     */
    public static MessageContent system(String content) {
        return new MessageContent("system", content);
    }
}
