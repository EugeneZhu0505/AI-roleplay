package com.hzau.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.entity
 * @className: Conversation
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午2:54
 */
@Schema(description = "对话实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("conversations")
public class Conversation {

    @Schema(description = "对话ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID", example = "1")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "角色ID", example = "1")
    @TableField("character_id")
    private Long characterId;

    @Schema(description = "对话标题", example = "与哈利波特的对话")
    @TableField("title")
    private String title;

    @Schema(description = "对话状态", example = "active")
    @TableField("status")
    private String status;

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

