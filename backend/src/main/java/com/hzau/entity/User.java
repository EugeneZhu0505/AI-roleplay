package com.hzau.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jdk.jfr.Description;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.entity
 * @className: User
 * @author: zhuyuchen
 * @description: 用户实体类, 对应数据库中的user表
 * @date: 2025/9/22 下午3:01
 */

@Schema(description = "用户实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User {

    @Schema(description = "用户ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户名", example = "testuser")
    @TableField("username")
    private String username;

    @Schema(description = "密码哈希值", hidden = true)
    @TableField("password_hash")
    private String passwordHash;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "创建时间", example = "2025-01-23T10:30:00")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-01-23T10:30:00")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "删除标记", hidden = true)
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

