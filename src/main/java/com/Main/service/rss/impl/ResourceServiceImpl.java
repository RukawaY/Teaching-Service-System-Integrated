package com.Main.service.rss.impl;

import com.Main.config.ResourceAppConfig;
import com.Main.service.rss.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceServiceImpl implements ResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Autowired
    private ResourceAppConfig resourceAppConfig;

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, Long directoryId, Long courseId,
                                      Long ownerId, String resourceName, String description) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件为空");
            }

            // 生成文件存储路径
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = timestamp + "_" + originalFilename;

            // 确保目录存在
            String uploadDir = resourceAppConfig.getUploadPath() + File.separator + courseId;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath.toString());
            response.put("fileName", filename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> downloadFile(Long resourceId) {
        try {
            // TODO: 根据resourceId从数据库获取文件路径
            String filePath = "临时测试路径"; // 这里需要替换为实际的文件路径获取逻辑

            Path path = Paths.get(filePath);
            byte[] content = Files.readAllBytes(path);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", path.getFileName().toString());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);

        } catch (IOException e) {
            logger.error("文件下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getDirectory(String courseId) {
        try {
            // TODO: 实现目录获取逻辑
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "目录获取成功");
            // 这里需要添加实际的目录数据

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取目录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取目录失败: " + e.getMessage());
        }
    }
}
