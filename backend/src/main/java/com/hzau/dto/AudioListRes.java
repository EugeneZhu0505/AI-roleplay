package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: AudioListRes
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:19
 */
@Data
@Schema(description = "音色列表响应")
public class AudioListRes {

    @Schema(description = "音色列表")
    private List<VoiceInfo> voices;

    @Data
    @Schema(description = "音色信息")
    public static class VoiceInfo {
        @Schema(description = "音色名称")
        private String voiceName;

        @Schema(description = "音色类型")
        private String voiceType;

        @Schema(description = "试听音频链接")
        private String url;

        @Schema(description = "音色分类")
        private String category;

        @Schema(description = "更新时间（毫秒）")
        private Long updatetime;
    }
}

