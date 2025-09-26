package com.hzau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: FileStorageService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午5:20
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${file.upload.base-path:uploads}")
    private String baseUploadPath;

    @Value("${file.upload.base-url:http://localhost}")
    private String baseUrl;

    // 支持的音频格式
    private static final List<String> SUPPORTED_AUDIO_FORMATS = Arrays.asList(
            "mp3", "wav", "ogg", "m4a", "aac", "flac", "wma"
    );

    // 支持的图片格式
    private static final List<String> SUPPORTED_IMAGE_FORMATS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"
    );

    // 支持的视频格式
    private static final List<String> SUPPORTED_VIDEO_FORMATS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"
    );

    // 支持的文档格式
    private static final List<String> SUPPORTED_DOCUMENT_FORMATS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    /**
     * 通用文件上传方法
     */
    public String uploadFile(MultipartFile file, String category) throws IOException {
        // 验证文件
        validateFile(file);

        // 生成文件名和路径
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileName = generateUniqueFileName() + fileExtension;
        String uploadDir = createUploadDirectory(category);
        String filePath = uploadDir + File.separator + fileName;

        // 保存文件
        File targetFile = new File(filePath);
        file.transferTo(targetFile);

        // 生成访问URL
        String fileUrl = generateFileUrl(category, fileName);

        log.info("文件上传成功: {} -> {} (分类: {})", originalFilename, fileUrl, category);
        return fileUrl;
    }

    /**
     * 保存音频字节数组到本地
     */
    public String saveAudioBytes(byte[] audioBytes, String originalFilename) throws IOException {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new RuntimeException("音频数据为空");
        }
        
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("文件名为空");
        }

        // 生成文件名和路径
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileName = generateUniqueFileName() + fileExtension;
        String uploadDir = createUploadDirectory("audio");
        String filePath = uploadDir + File.separator + fileName;

        // 保存文件
        File targetFile = new File(filePath);
        Files.write(targetFile.toPath(), audioBytes);

        // 生成访问URL
        String fileUrl = generateFileUrl("audio", fileName);

        log.info("音频字节数组保存成功: {} -> {} (大小: {} bytes)", originalFilename, fileUrl, audioBytes.length);
        return fileUrl;
    }


    /**
     * 上传音频文件
     */
    public String uploadAudioFile(MultipartFile file) throws IOException {
        return uploadFile(file, "audio");
    }

    /**
     * 上传图片文件
     */
    public String uploadImageFile(MultipartFile file) throws IOException {
        return uploadFile(file, "image");
    }

    /**
     * 上传视频文件
     */
    public String uploadVideoFile(MultipartFile file) throws IOException {
        return uploadFile(file, "video");
    }

    /**
     * 上传文档文件
     */
    public String uploadDocumentFile(MultipartFile file) throws IOException {
        return uploadFile(file, "document");
    }

    /**
     * 保存Base64编码的音频文件
     */
    public String saveBase64AudioFile(String base64Data, String format) throws IOException {
        // 解码Base64数据
        byte[] audioData = Base64.getDecoder().decode(base64Data);

        // 生成文件名和路径
        String fileName = generateUniqueFileName() + "." + format;
        String uploadDir = createUploadDirectory("audio");
        String filePath = uploadDir + File.separator + fileName;

        // 保存文件
        Path path = Paths.get(filePath);
        Files.write(path, audioData);

        // 生成访问URL
        String fileUrl = generateFileUrl("audio", fileName);

        log.info("Base64音频文件保存成功: {} (格式: {})", fileUrl, format);
        return fileUrl;
    }

    /**
     * 保存Base64编码的文件
     */
    public String saveBase64File(String base64Data, String format, String category) throws IOException {
        // 解码Base64数据
        byte[] fileData = Base64.getDecoder().decode(base64Data);

        // 生成文件名和路径
        String fileName = generateUniqueFileName() + "." + format;
        String uploadDir = createUploadDirectory(category);
        String filePath = uploadDir + File.separator + fileName;

        // 保存文件
        Path path = Paths.get(filePath);
        Files.write(path, fileData);

        // 生成访问URL
        String fileUrl = generateFileUrl(category, fileName);

        log.info("Base64文件保存成功: {} (格式: {}, 分类: {})", fileUrl, format, category);
        return fileUrl;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String relativePath = extractRelativePathFromUrl(fileUrl);
            if (relativePath == null) {
                log.warn("无法从URL中提取文件路径: {}", fileUrl);
                return false;
            }

            String filePath = baseUploadPath + File.separator + relativePath;
            File file = new File(filePath);

            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                } else {
                    log.warn("文件删除失败: {}", filePath);
                }
                return deleted;
            } else {
                log.warn("文件不存在: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            log.error("删除文件时发生异常: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileUrl) {
        try {
            String relativePath = extractRelativePathFromUrl(fileUrl);
            if (relativePath == null) {
                return false;
            }

            String filePath = baseUploadPath + File.separator + relativePath;
            File file = new File(filePath);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            log.error("检查文件存在性时发生异常: {}", fileUrl, e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件大小（通用限制：100MB）
        long maxSize = 100 * 1024 * 1024L; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }
    }

    /**
     * 验证音频文件
     */
    private void validateAudioFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        if (!SUPPORTED_AUDIO_FORMATS.contains(fileExtension)) {
            throw new IllegalArgumentException("不支持的音频格式: " + fileExtension +
                    "，支持的格式: " + String.join(", ", SUPPORTED_AUDIO_FORMATS));
        }

        // 音频文件大小限制：10MB
        long maxAudioSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxAudioSize) {
            throw new IllegalArgumentException("音频文件大小不能超过10MB");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 创建上传目录
     */
    private String createUploadDirectory(String category) throws IOException {
        // 按日期和分类组织目录结构: uploads/category/2025/01/25
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uploadDir = baseUploadPath + File.separator + category + File.separator + dateStr;

        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            log.debug("创建上传目录: {}", uploadDir);
        }

        return uploadDir;
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String category, String fileName) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("%s:%s/files/%s/%s/%s",
                baseUrl, serverPort, category, dateStr, fileName);
    }

    /**
     * 从URL中提取相对路径
     */
    private String extractRelativePathFromUrl(String fileUrl) {
        try {
            // 假设URL格式为: http://localhost:8080/files/category/yyyy/MM/dd/filename
            String prefix = "/files/";
            int prefixIndex = fileUrl.indexOf(prefix);
            if (prefixIndex == -1) {
                return null;
            }

            return fileUrl.substring(prefixIndex + prefix.length());
        } catch (Exception e) {
            log.error("提取文件路径失败: {}", fileUrl, e);
            return null;
        }
    }
}

