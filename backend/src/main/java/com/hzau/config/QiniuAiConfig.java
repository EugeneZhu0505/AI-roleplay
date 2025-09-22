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
     * API密钥 - 请在application.yml中配置
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
}

