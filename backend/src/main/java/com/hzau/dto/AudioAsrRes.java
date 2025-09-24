package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: AudioAsrRes
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:16
 */
@Data
@Schema(description = "语音识别响应")
public class AudioAsrRes {

    @Schema(description = "请求唯一标识")
    private String reqid;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "识别结果数据")
    private AsrData data;

    @Data
    @Schema(description = "识别结果数据")
    public static class AsrData {
        @Schema(description = "音频信息")
        private AudioInfo audioInfo;

        @Schema(description = "识别结果")
        private AsrResult result;
    }

    @Data
    @Schema(description = "音频信息")
    public static class AudioInfo {
        @Schema(description = "音频时长（毫秒）")
        private Integer duration;
    }

    @Data
    @Schema(description = "识别结果")
    public static class AsrResult {
        @Schema(description = "附加信息")
        private AsrAdditions additions;

        @Schema(description = "识别出的文本")
        private String text;
    }

    @Data
    @Schema(description = "附加信息")
    public static class AsrAdditions {
        @Schema(description = "音频时长（字符串）")
        private String duration;
    }
}
