package com.Main.util.rss;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ResourceUtils {
    // 允许的文件类型
    private static final Set<String> ALLOWED_FILE_TYPES = new HashSet<>(Arrays.asList(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "jpg", "jpeg", "png", "gif"
    ));

    // 最大文件大小（500MB）
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;

    /**
     * 验证文件类型
     */
    public static boolean isValidFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return ALLOWED_FILE_TYPES.contains(extension);
    }

    /**
     * 验证文件大小
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }

    /**
     * 生成唯一的文件名
     */
    public static String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return timestamp + "_" + System.nanoTime() + extension;
    }

    /**
     * 确保目录存在，如果不存在则创建
     */
    public static void ensureDirectoryExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * 保存文件到指定路径
     */
    public static String saveFile(MultipartFile file, String directoryPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        if (!isValidFileType(file.getOriginalFilename())) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        if (!isValidFileSize(file.getSize())) {
            throw new IllegalArgumentException("文件大小超出限制");
        }

        ensureDirectoryExists(directoryPath);

        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
        Path destinationPath = Paths.get(directoryPath, uniqueFileName);

        file.transferTo(destinationPath.toFile());
        return uniqueFileName;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
