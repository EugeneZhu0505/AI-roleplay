package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: UserChatReq
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 上午10:22
 */
@Data
@Schema(description = "用户聊天请求")
public class UserChatReq {

    @Schema(description = "用户消息", required = true, example = "你好，请介绍一下自己")
    private String message;
}
