package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: CharacterCreateReq
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/25 下午3:04
 */
@Schema(description = "创建AI角色请求")
@Data
public class CharacterCreateReq {

    @Schema(description = "角色名称", example = "小红帽", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    private String name;

    @Schema(description = "角色描述", example = "一个勇敢善良的小女孩，喜欢穿红色斗篷")
    @Size(max = 1000, message = "角色描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "角色头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    @Schema(description = "角色性格特点", example = "勇敢、善良、天真、乐观")
    @Size(max = 1000, message = "性格特点长度不能超过1000个字符")
    private String personality;

    @Schema(description = "角色背景故事", example = "小红帽生活在森林边的小村庄里，经常去看望住在森林深处的奶奶...")
    @Size(max = 2000, message = "背景故事长度不能超过2000个字符")
    private String backgroundStory;

    @Schema(description = "系统提示词", example = "你是小红帽，一个勇敢善良的小女孩。你总是乐于助人，对世界充满好奇...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "系统提示词不能为空")
    @Size(max = 2000, message = "系统提示词长度不能超过2000个字符")
    private String systemPrompt;

    @Schema(description = "语音配置JSON", example = "{\"voice_id\": \"female_sweet\", \"speed\": 1.0}")
    @Size(max = 1000, message = "语音配置长度不能超过1000个字符")
    private String voiceConfig;

    @Schema(description = "角色标签", example = "童话,勇敢,善良,冒险")
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;

    @Schema(description = "是否激活", example = "true")
    private Boolean isActive = true;
}

