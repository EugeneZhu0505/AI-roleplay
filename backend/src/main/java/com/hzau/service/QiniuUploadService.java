package com.hzau.service;

import com.hzau.config.QiniuKodoConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: QiniuUploadService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/25 下午4:50
 */
@Service
@Slf4j
public class QiniuUploadService {

    private final QiniuKodoConfig config;
    private final UploadManager uploadManager;
    private final Auth auth;

    @Autowired
    public QiniuUploadService(QiniuKodoConfig config) {
        this.config = config;

        // 初始化七牛云配置
        Configuration cfg = new Configuration(Region.autoRegion());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        this.uploadManager = new UploadManager(cfg);

        // 初始化认证信息
        this.auth = Auth.create(config.getAccessKey(), config.getSecretKey());

        log.info("七牛云上传服务初始化完成");
    }

    /**
     * 上传MultipartFile到七牛云
     * @param file MultipartFile文件
     * @return 七牛云公网访问URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (!config.isConfigValid()) {
            throw new RuntimeException("七牛云配置无效，请检查配置信息");
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        try {
            // 生成唯一的文件名
            String fileName = generateFileName(file.getOriginalFilename());

            // 生成上传凭证
            String upToken = auth.uploadToken(config.getBucket());

            // 执行上传 - 直接使用字节数组上传
            Response response = uploadManager.put(file.getBytes(), fileName, upToken);

            if (response.isOK()) {
                // 生成公网访问URL
                String publicUrl = generatePublicUrl(fileName);
                log.info("MultipartFile上传成功: {} -> {}", file.getOriginalFilename(), publicUrl);
                return publicUrl;
            } else {
                log.error("MultipartFile上传失败: {}, 响应: {}", file.getOriginalFilename(), response.bodyString());
                throw new RuntimeException("MultipartFile上传失败: " + response.bodyString());
            }

        } catch (QiniuException e) {
            log.error("七牛云上传异常: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("七牛云上传异常: " + e.getMessage(), e);
        }
    }

    /**
     * 上传本地文件到七牛云
     * @param localFilePath 本地文件路径
     * @return 七牛云公网访问URL
     */
    public String uploadFile(String localFilePath) {
        if (!config.isConfigValid()) {
            throw new RuntimeException("七牛云配置无效，请检查配置信息");
        }

        File file = new File(localFilePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在: " + localFilePath);
        }

        try {
            // 生成唯一的文件名
            String fileName = generateFileName(file.getName());

            // 生成上传凭证
            String upToken = auth.uploadToken(config.getBucket());

            // 执行上传
            Response response = uploadManager.put(file, fileName, upToken);

            if (response.isOK()) {
                // 生成公网访问URL
                String publicUrl = generatePublicUrl(fileName);
                log.info("文件上传成功: {} -> {}", localFilePath, publicUrl);
                return publicUrl;
            } else {
                log.error("文件上传失败: {}, 响应: {}", localFilePath, response.bodyString());
                throw new RuntimeException("文件上传失败: " + response.bodyString());
            }

        } catch (QiniuException e) {
            log.error("七牛云上传异常: {}", localFilePath, e);
            throw new RuntimeException("七牛云上传异常: " + e.getMessage(), e);
        }
    }

    /**
     * 上传字节数组到七牛云
     * @param data 文件数据
     * @param originalFileName 原始文件名
     * @return 七牛云公网访问URL
     */
    public String uploadBytes(byte[] data, String originalFileName) {
        if (!config.isConfigValid()) {
            throw new RuntimeException("七牛云配置无效，请检查配置信息");
        }

        if (data == null || data.length == 0) {
            throw new RuntimeException("文件数据为空");
        }

        try {
            // 生成唯一的文件名
            String fileName = generateFileName(originalFileName);

            // 生成上传凭证
            String upToken = auth.uploadToken(config.getBucket());

            // 执行上传
            Response response = uploadManager.put(data, fileName, upToken);

            if (response.isOK()) {
                // 生成公网访问URL
                String publicUrl = generatePublicUrl(fileName);
                log.info("字节数组上传成功: {} -> {}", originalFileName, publicUrl);
                return publicUrl;
            } else {
                log.error("字节数组上传失败: {}, 响应: {}", originalFileName, response.bodyString());
                throw new RuntimeException("字节数组上传失败: " + response.bodyString());
            }

        } catch (QiniuException e) {
            log.error("七牛云上传异常: {}", originalFileName, e);
            throw new RuntimeException("七牛云上传异常: " + e.getMessage(), e);
        }
    }

    /**
     * 上传临时文件并在上传后删除
     * @param tempFilePath 临时文件路径
     * @return 七牛云公网访问URL
     */
    public String uploadTempFile(String tempFilePath) {
        try {
            String publicUrl = uploadFile(tempFilePath);

            // 删除临时文件
            File tempFile = new File(tempFilePath);
            if (tempFile.exists() && tempFile.delete()) {
                log.info("临时文件删除成功: {}", tempFilePath);
            } else {
                log.warn("临时文件删除失败: {}", tempFilePath);
            }

            return publicUrl;
        } catch (Exception e) {
            // 即使上传失败也要尝试删除临时文件
            File tempFile = new File(tempFilePath);
            if (tempFile.exists() && tempFile.delete()) {
                log.info("临时文件删除成功: {}", tempFilePath);
            }
            throw e;
        }
    }

    /**
     * 生成唯一的文件名
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFileName) {
        // 获取文件扩展名
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 生成时间戳和UUID
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 组合文件名：前缀 + 时间戳 + UUID + 扩展名
        return config.getUploadPrefix() + timestamp + "_" + uuid + extension;
    }

    /**
     * 生成公网访问URL
     * @param fileName 文件名
     * @return 公网访问URL
     */
    private String generatePublicUrl(String fileName) {
        String domain = config.getDomain();
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "http://" + domain;  // 使用http协议而不是https
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        String publicUrl = domain + "/" + fileName;
        log.info("生成公网访问URL: {}", publicUrl);
        return publicUrl;
    }

    /**
     * 检查配置是否有效
     * @return 配置是否有效
     */
    public boolean isConfigValid() {
        return config.isConfigValid();
    }
}

