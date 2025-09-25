package com.hzau.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: QiniuAiConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午9:02
 */
@Configuration
@ConfigurationProperties(prefix = "qiniu.ai")
@Data
public class QiniuAiConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 主接入点
     */
    private String primaryEndpoint = "https://openai.qiniu.com/v1";

    /**
     * 备用接入点
     */
    private String backupEndpoint = "https://api.qnaigc.com/v1";

    /**
     * 默认模型
     */
    private String defaultModel = "deepseek-v3";

    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * ASR语音识别配置
     */
    private AsrConfig asr = new AsrConfig();

    /**
     * TTS语音合成配置
     */
    private TtsConfig tts = new TtsConfig();

    @Data
    public static class AsrConfig {
        /**
         * ASR模型名称
         */
        private String model = "asr";

        /**
         * 支持的音频格式
         */
        private String[] supportedFormats = {"mp3", "wav", "ogg", "raw"};
        
        /**
         * ASR请求超时时间（秒）
         */
        private int timeout = 60;
    }

    @Data
    public static class TtsConfig {
        /**
         * 默认音色类型
         */
        private String defaultVoiceType = "qiniu_zh_female_wwxkjx";

        /**
         * 默认音频编码
         */
        private String defaultEncoding = "mp3";

        /**
         * 默认语速
         */
        private float defaultSpeedRatio = 1.0f;

        /**
         * 最大文本长度
         */
        private int maxTextLength = 1000;
    }
}

