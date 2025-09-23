package com.hzau.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.entity
 * @className: Character
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 上午11:55
 */
@Schema(description = "AI角色实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("characters")
@Alias("AiCharacter")
public class Character {

    @Schema(description = "角色ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色名称", example = "哈利波特")
    @TableField("name")
    private String name;

    @Schema(description = "角色描述", example = "勇敢的魔法师，霍格沃茨学生")
    @TableField("description")
    private String description;

    @Schema(description = "角色头像URL", example = "https://example.com/harry-potter.jpg")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "角色性格特点", example = "勇敢、善良、有正义感")
    @TableField("personality")
    private String personality;

    @Schema(description = "角色背景故事", example = "生活在魔法世界的年轻巫师...")
    @TableField("background_story")
    private String backgroundStory;

    @Schema(description = "系统提示词", example = "你是哈利波特，一个勇敢的魔法师...")
    @TableField("system_prompt")
    private String systemPrompt;

    @Schema(description = "语音配置JSON", example = "{\"voice_id\": \"harry\", \"speed\": 1.0}")
    @TableField("voice_config")
    private String voiceConfig;

    @Schema(description = "角色标签", example = "魔法,冒险,勇敢")
    @TableField("tags")
    private String tags;

    @Schema(description = "是否激活", example = "true")
    @TableField("is_active")
    private Boolean isActive;

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
