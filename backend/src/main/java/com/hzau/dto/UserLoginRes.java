package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: UserLoginRes
 * @author: zhuyuchen
 * @description: 用户登录返回结果
 * @date: 2025/9/22 下午4:02
 */

@Schema(description = "用户登录响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRes {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "令牌类型", example = "Bearer", defaultValue = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "用户名", example = "testuser")
    private String username;

    @Schema(description = "头像", example = "http://122.205.70.147:8080/files/image/2025/09/24/993353aae463424bb3492e67e4448ee1.jpg")
    private String avatarUrl;

    public UserLoginRes(String accessToken, Long userId, String username, String avatarUrl) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}

