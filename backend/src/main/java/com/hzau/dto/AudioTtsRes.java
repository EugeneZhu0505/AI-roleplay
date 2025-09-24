package com.hzau.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.dto
 * @className: AudioTtsRes
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午9:18
 */
@Data
@Schema(description = "语音合成响应")
public class AudioTtsRes {

    @Schema(description = "请求唯一标识")
    private String reqid;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "序列号")
    private Integer sequence;

    @Schema(description = "合成的base64编码音频数据")
    private String data;

    @Schema(description = "附加信息")
    private TtsAddition addition;

    @Data
    @Schema(description = "附加信息")
    public static class TtsAddition {
        @Schema(description = "音频时长（毫秒）")
        private String duration;
    }
}

