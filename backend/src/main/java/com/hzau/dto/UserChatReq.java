package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: UserChatReq
 * @author: zhuyuchen
 * @description: 用户聊天请求DTO
 * @date: 2025/9/23 上午10:22
 */
@Data
@Schema(description = "用户聊天请求")
public class UserChatReq {

    @Schema(description = "用户消息", required = false, example = "你好，请介绍一下自己")
    private String message;
    
    @Schema(description = "响应类型", required = false, example = "text", allowableValues = {"text", "voice"})
    private String responseType = "text"; // 默认为text类型（流式输出）

    @Schema(description = "输入类型", required = false, example = "text", allowableValues = {"text", "voice"})
    private String inputType = "text"; // 默认为text类型

    @Schema(description = "语音文件URL", required = false, example = "https://example.com/audio.mp3")
    private String audioUrl;

    @Schema(description = "音频格式", required = false, example = "mp3", allowableValues = {"mp3", "wav", "ogg", "raw"})
    private String audioFormat;
}
