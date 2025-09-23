package com.hzau.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.entity
 * @className: Message
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午2:55
 */
@Schema(description = "消息实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("messages")
public class Message {

    @Schema(description = "消息ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "对话ID", example = "1")
    @TableField("conversation_id")
    private Long conversationId;

    @Schema(description = "发送者类型", example = "user")
    @TableField("sender_type")
    private String senderType;

    @Schema(description = "内容类型", example = "text")
    @TableField("content_type")
    private String contentType;

    @Schema(description = "文本内容", example = "你好，哈利波特！")
    @TableField("text_content")
    private String textContent;

    @Schema(description = "音频文件URL", example = "https://example.com/audio/message1.mp3")
    @TableField("audio_url")
    private String audioUrl;

    @Schema(description = "音频时长(秒)", example = "30")
    @TableField("audio_duration")
    private Integer audioDuration;

    @Schema(description = "创建时间", example = "2025-01-23T10:30:00")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "删除标记", hidden = true)
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}