package com.hzau.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: LlmChatRes
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 上午10:25
 */
@Data
public class LlmChatRes {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 对象类型
     */
    private String object;

    /**
     * 创建时间
     */
    private Long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 使用情况
     */
    private Usage usage;

    /**
     * 选择项
     */
    @Data
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息
         */
        private MessageContent message;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * 使用情况
     */
    @Data
    public static class Usage {
        /**
         * 提示token数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成token数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总token数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
