package com.hzau.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: ChatReq
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午9:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReq {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<ChatMessage> messages;

    /**
     * 是否流式输出
     */
    private Boolean stream;

    /**
     * 温度参数，控制随机性
     */
    private Double temperature;

    /**
     * 最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * top_p参数
     */
    @JsonProperty("top_p")
    private Double topP;
}
