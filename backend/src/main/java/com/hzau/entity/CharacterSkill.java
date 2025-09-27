package com.hzau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.entity
 * @className: CharacterSkill
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/26 下午3:28
 */
@Data
@TableName("character_skills")
public class CharacterSkill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long characterId;

    private String skillName;

    private String skillDescription;

    private String triggerPrompt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean deleted;
}

