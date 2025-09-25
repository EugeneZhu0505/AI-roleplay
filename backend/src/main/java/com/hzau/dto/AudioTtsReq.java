package com.hzau.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: AudioTtsReq
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:17
 */
@Data
@Schema(description = "语音合成请求")
public class AudioTtsReq {

    @Schema(description = "音频参数", required = true)
    private AudioParam audio;

    @Schema(description = "请求参数", required = true)
    private RequestParam request;

    @Data
    @Schema(description = "音频参数")
    public static class AudioParam {
        @Schema(description = "音色类型", required = true, example = "qiniu_zh_female_wwxkjx")
        @JsonProperty("voice_type")
        private String voiceType;

        @Schema(description = "音频编码", required = true, example = "mp3")
        private String encoding;

        @Schema(description = "语速", example = "1.0")
        @JsonProperty("speed_ratio")
        private Float speedRatio = 1.0f;
    }

    @Data
    @Schema(description = "请求参数")
    public static class RequestParam {
        @Schema(description = "需要合成的文本", required = true, example = "你好，世界！")
        private String text;
    }
}

