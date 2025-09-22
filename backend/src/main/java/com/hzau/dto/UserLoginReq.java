package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: UserLoginReq
 * @author: zhuyuchen
 * @description: 用户登录请求格式
 * @date: 2025/9/22 下午3:17
 */

@Schema(description = "用户登录请求")
@Data
public class UserLoginReq {

    @Schema(description = "用户名", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;
}

