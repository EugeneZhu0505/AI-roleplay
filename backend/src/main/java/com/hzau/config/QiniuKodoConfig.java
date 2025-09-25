package com.hzau.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: QiniuKodoConfig
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/25 下午4:44
 */
@Configuration
@ConfigurationProperties(prefix = "qiniu.kodo")
@Data
public class QiniuKodoConfig {

    /**
     * 七牛云Access Key
     */
    private String accessKey;

    /**
     * 七牛云Secret Key
     */
    private String secretKey;

    /**
     * 存储空间名称
     */
    private String bucket;

    /**
     * CDN域名，用于生成公网访问URL
     */
    private String domain;

    /**
     * 上传文件的过期时间（秒），默认1小时
     */
    private long uploadTokenExpire = 3600;

    /**
     * 文件上传的前缀路径
     */
    private String uploadPrefix = "audio/";

    /**
     * 验证配置是否有效
     */
    public boolean isConfigValid() {
        return accessKey != null && !accessKey.trim().isEmpty() &&
                secretKey != null && !secretKey.trim().isEmpty() &&
                bucket != null && !bucket.trim().isEmpty() &&
                domain != null && !domain.trim().isEmpty();
    }
}

