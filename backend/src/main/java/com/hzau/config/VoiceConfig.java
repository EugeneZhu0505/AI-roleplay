package com.hzau.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: VoiceConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/26 下午7:56
 */
@Data
@Schema(description = "角色音色配置")
public class VoiceConfig {

    @Schema(description = "音色类型", example = "qiniu_zh_female_wwxkjx")
    @JsonProperty("voice_type")
    private String voiceType;

    @Schema(description = "音色名称", example = "温婉小静")
    @JsonProperty("voice_name")
    private String voiceName;

    @Schema(description = "语速比例", example = "1.0")
    @JsonProperty("speed_ratio")
    private Float speedRatio;

    /**
     * 获取默认音色配置
     */
    public static VoiceConfig getDefault() {
        VoiceConfig config = new VoiceConfig();
        config.setVoiceType("qiniu_zh_female_wwxkjx");
        config.setVoiceName("温婉小静");
        config.setSpeedRatio(1.0f);
        return config;
    }
}
