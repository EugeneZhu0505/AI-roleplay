package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: FileUploadController
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/24 下午5:18
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件上传接口", description = "通用文件上传和管理功能")
@Slf4j
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    // 支持的文件类型配置
    private static final Map<String, List<String>> SUPPORTED_FILE_TYPES = new HashMap<>();
    private static final Map<String, Long> MAX_FILE_SIZES = new HashMap<>();

    static {
        // 图片文件
        SUPPORTED_FILE_TYPES.put("image", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg"));
        MAX_FILE_SIZES.put("image", 20 * 1024 * 1024L); // 20MB

        // 音频文件
        SUPPORTED_FILE_TYPES.put("audio", Arrays.asList("mp3", "wav", "ogg", "m4a", "aac", "flac", "wma"));
        MAX_FILE_SIZES.put("audio", 30 * 1024 * 1024L); // 30MB

        // 视频文件
        SUPPORTED_FILE_TYPES.put("video", Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm"));
        MAX_FILE_SIZES.put("video", 100 * 1024 * 1024L); // 100MB

        // 文档文件
        SUPPORTED_FILE_TYPES.put("document", Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"));
        MAX_FILE_SIZES.put("document", 50 * 1024 * 1024L); // 50MB

        // 压缩文件
        SUPPORTED_FILE_TYPES.put("archive", Arrays.asList("zip", "rar", "7z", "tar", "gz"));
        MAX_FILE_SIZES.put("archive", 200 * 1024 * 1024L); // 200MB
    }

    /**
     * 通用文件上传接口
     * @param fileMono
     * @param category
     * @return
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "通用文件上传", description = "支持图片、音频、视频、文档等多种文件类型上传")
    public Mono<Result<Map<String, Object>>> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestPart("file") Mono<FilePart> fileMono,
            @Parameter(description = "文件类型分类", example = "image")
            @RequestPart(value = "category", required = false) String category) {

        return fileMono.flatMap(file -> {
            try {
                log.info("收到文件上传请求，文件名: {}, 分类: {}",
                        file.filename(), category);

                // 1. 基本验证
                 String originalFilename = file.filename();
                 if (originalFilename == null || originalFilename.trim().isEmpty()) {
                     return Mono.just(Result.<Map<String, Object>>fail(400, "文件名不能为空"));
                 }

                 // 2. 获取文件扩展名
                 String fileExtension = getFileExtension(originalFilename).toLowerCase();
                 if (fileExtension.isEmpty()) {
                     return Mono.just(Result.<Map<String, Object>>fail(400, "文件必须有扩展名"));
                 }

                 // 3. 自动检测文件类型（如果未指定分类）
                 String finalCategory = category;
                 if (finalCategory == null || finalCategory.trim().isEmpty()) {
                     finalCategory = detectFileCategory(fileExtension);
                     if (finalCategory == null) {
                         return Mono.just(Result.<Map<String, Object>>fail(400, "不支持的文件类型: " + fileExtension));
                     }
                 }

                // 创建临时文件来保存上传的文件内容
                Path tempFile = Files.createTempFile("upload_", "_" + originalFilename);
                String finalCategoryForLambda = finalCategory;
                
                // 将FilePart内容写入临时文件并获取字节数组
                return file.transferTo(tempFile)
                    .then(Mono.fromCallable(() -> {
                        byte[] fileBytes = Files.readAllBytes(tempFile);
                        long fileSize = fileBytes.length;
                        
                        // 清理临时文件
                        Files.deleteIfExists(tempFile);
                        
                        return new Object[]{fileBytes, fileSize, originalFilename};
                    }))
                    .flatMap(fileData -> {
                        byte[] fileBytes = (byte[]) ((Object[]) fileData)[0];
                        long fileSize = (long) ((Object[]) fileData)[1];
                        String fileName = (String) ((Object[]) fileData)[2];
                        
                        try {
                            log.info("处理文件上传，文件名: {}, 大小: {} bytes, 分类: {}",
                                    fileName, fileSize, finalCategoryForLambda);

                            // 4. 验证文件大小
                             Long maxSize = MAX_FILE_SIZES.get(finalCategoryForLambda);
                             if (maxSize != null && fileSize > maxSize) {
                                 return Mono.just(Result.<Map<String, Object>>fail(400, 
                                     String.format("文件大小超出限制，最大允许: %s，当前文件: %s", 
                                         formatFileSize(maxSize), formatFileSize(fileSize))));
                             }

                             // 5. 验证文件类型
                             List<String> supportedTypes = SUPPORTED_FILE_TYPES.get(finalCategoryForLambda);
                             if (supportedTypes == null || !supportedTypes.contains(fileExtension)) {
                                 return Mono.just(Result.<Map<String, Object>>fail(400, 
                                     String.format("不支持的文件类型: %s，支持的类型: %s", 
                                         fileExtension, supportedTypes)));
                             }

                            // 6. 保存文件
                            String fileUrl = fileStorageService.saveFileBytes(fileBytes, fileName, finalCategoryForLambda);

                            // 7. 构建响应
                            Map<String, Object> result = new HashMap<>();
                            result.put("fileUrl", fileUrl);
                            result.put("fileName", fileName);
                            result.put("fileSize", fileSize);
                            result.put("fileType", fileExtension);
                            result.put("category", finalCategoryForLambda);
                            result.put("uploadTime", System.currentTimeMillis());

                            log.info("文件上传成功，URL: {}", fileUrl);
                            return Mono.just(Result.success(result));

                        } catch (Exception e) {
                             log.error("文件保存失败", e);
                             return Mono.just(Result.<Map<String, Object>>fail(500, "文件保存失败: " + e.getMessage()));
                         }
                    });
                    
            } catch (IOException e) {
                log.error("创建临时文件失败", e);
                return Mono.just(Result.<Map<String, Object>>fail(500, "文件处理失败: " + e.getMessage()));
            }
        }).onErrorResume(error -> {
            log.error("文件上传处理失败", error);
            return Mono.just(Result.<Map<String, Object>>fail(500, "文件上传失败"));
        });
    }

    /**
     * 批量文件上传接口
     */
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量文件上传", description = "支持一次上传多个文件")
    public Result<Map<String, Object>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestPart("files") MultipartFile[] files,
            @Parameter(description = "文件类型分类")
            @RequestPart(value = "category", required = false) String category) {

        log.info("收到批量文件上传请求，文件数量: {}, 分类: {}", files.length, category);

        try {
            if (files.length == 0) {
                return Result.<Map<String, Object>>fail(400, "请选择要上传的文件");
            }

            if (files.length > 10) {
                return Result.<Map<String, Object>>fail(400, "单次最多上传10个文件");
            }

            List<Map<String, Object>> successList = new java.util.ArrayList<>();
            List<Map<String, Object>> failList = new java.util.ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || file.isEmpty()) {
                        Map<String, Object> failInfo = new HashMap<>();
                        failInfo.put("index", i);
                        failInfo.put("fileName", originalFilename);
                        failInfo.put("error", "文件为空或文件名无效");
                        failList.add(failInfo);
                        continue;
                    }

                    String fileExtension = getFileExtension(originalFilename).toLowerCase();
                    String fileCategory = category;
                    if (fileCategory == null || fileCategory.trim().isEmpty()) {
                        fileCategory = detectFileCategory(fileExtension);
                    }

                    if (fileCategory == null) {
                        Map<String, Object> failInfo = new HashMap<>();
                        failInfo.put("index", i);
                        failInfo.put("fileName", originalFilename);
                        failInfo.put("error", "不支持的文件类型: " + fileExtension);
                        failList.add(failInfo);
                        continue;
                    }

                    Result<String> validationResult = validateFile(file, fileExtension, fileCategory);
                    if (validationResult.getCode() != 0) {
                        Map<String, Object> failInfo = new HashMap<>();
                        failInfo.put("index", i);
                        failInfo.put("fileName", originalFilename);
                        failInfo.put("error", validationResult.getMessage());
                        failList.add(failInfo);
                        continue;
                    }

                    String fileUrl = fileStorageService.uploadFile(file, fileCategory);

                    Map<String, Object> successInfo = new HashMap<>();
                    successInfo.put("index", i);
                    successInfo.put("fileUrl", fileUrl);
                    successInfo.put("fileName", originalFilename);
                    successInfo.put("fileSize", file.getSize());
                    successInfo.put("fileType", fileExtension);
                    successInfo.put("category", fileCategory);
                    successList.add(successInfo);

                } catch (Exception e) {
                    Map<String, Object> failInfo = new HashMap<>();
                    failInfo.put("index", i);
                    failInfo.put("fileName", file.getOriginalFilename());
                    failInfo.put("error", "上传失败: " + e.getMessage());
                    failList.add(failInfo);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", files.length);
            result.put("successCount", successList.size());
            result.put("failCount", failList.size());
            result.put("successList", successList);
            result.put("failList", failList);
            result.put("uploadTime", System.currentTimeMillis());

            log.info("批量文件上传完成，成功: {}, 失败: {}", successList.size(), failList.size());
            return Result.success(result);

        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.<Map<String, Object>>fail(500, "批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的文件类型信息
     */
    @GetMapping("/supported-types")
    @Operation(summary = "获取支持的文件类型", description = "获取系统支持的所有文件类型和大小限制")
    public Result<Map<String, Object>> getSupportedFileTypes() {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : SUPPORTED_FILE_TYPES.entrySet()) {
            String category = entry.getKey();
            List<String> extensions = entry.getValue();
            Long maxSize = MAX_FILE_SIZES.get(category);

            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("extensions", extensions);
            categoryInfo.put("maxSize", maxSize);
            categoryInfo.put("maxSizeText", formatFileSize(maxSize));

            result.put(category, categoryInfo);
        }

        return Result.success(result);
    }

    /**
     * 删除文件接口
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据文件URL删除服务器上的文件")
    public Result<String> deleteFile(
            @Parameter(description = "文件URL", required = true)
            @RequestParam String fileUrl) {

        log.info("收到删除文件请求，URL: {}", fileUrl);

        try {
            boolean deleted = fileStorageService.deleteFile(fileUrl);
            if (deleted) {
                log.info("文件删除成功: {}", fileUrl);
                return Result.success("文件删除成功");
            } else {
                log.warn("文件删除失败，文件可能不存在: {}", fileUrl);
                return Result.fail(404, "文件不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.fail(500, "删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息接口
     */
    @GetMapping("/info")
    @Operation(summary = "获取文件信息", description = "根据文件URL获取文件的详细信息")
    public Result<Map<String, Object>> getFileInfo(
            @Parameter(description = "文件URL", required = true)
            @RequestParam String fileUrl) {

        log.info("获取文件信息请求，URL: {}", fileUrl);

        try {
            // 这里可以扩展为从数据库或文件系统获取文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileUrl", fileUrl);
            fileInfo.put("accessible", true); // 可以添加文件可访问性检查
            fileInfo.put("checkTime", System.currentTimeMillis());

            return Result.success(fileInfo);
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            return Result.fail(500, "获取文件信息失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 自动检测文件分类
     */
    private String detectFileCategory(String fileExtension) {
        for (Map.Entry<String, List<String>> entry : SUPPORTED_FILE_TYPES.entrySet()) {
            if (entry.getValue().contains(fileExtension)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 验证文件
     */
    private Result<String> validateFile(MultipartFile file, String fileExtension, String category) {
        // 检查文件类型
        List<String> allowedExtensions = SUPPORTED_FILE_TYPES.get(category);
        if (allowedExtensions == null || !allowedExtensions.contains(fileExtension)) {
            return Result.<String>fail(400, "不支持的文件类型: " + fileExtension + "，分类: " + category);
        }

        // 检查文件大小
        Long maxSize = MAX_FILE_SIZES.get(category);
        if (maxSize != null && file.getSize() > maxSize) {
            return Result.<String>fail(400, "文件大小超出限制，最大允许: " + formatFileSize(maxSize) +
                    "，当前文件: " + formatFileSize(file.getSize()));
        }

        return Result.success("验证通过");
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}

