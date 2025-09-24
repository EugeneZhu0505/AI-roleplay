package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: AudioAsrReq
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:15
 */
@Data
@Schema(description = "语音识别请求")
public class AudioAsrReq {

    @Schema(description = "模型名称", required = true, example = "asr")
    private String model = "asr";

    @Schema(description = "音频参数", required = true)
    private AudioParam audio;

    @Data
    @Schema(description = "音频参数")
    public static class AudioParam {
        @Schema(description = "音频格式", required = true, example = "mp3")
        private String format;

        @Schema(description = "音频文件的公网URL", required = true, example = "https://example.com/audio.mp3")
        private String url;
    }
}
