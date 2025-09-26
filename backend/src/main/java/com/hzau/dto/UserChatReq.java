package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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

    @Schema(description = "输入类型", required = true, example = "text", allowableValues = {"text", "audio"})
    private String inputType;

    @Schema(description = "音频文件", required = false)
    private MultipartFile audioFile;

    @Schema(description = "音频格式", required = false, example = "mp3", allowableValues = {"mp3", "wav", "ogg", "raw"})
    private String audioFormat;
}
