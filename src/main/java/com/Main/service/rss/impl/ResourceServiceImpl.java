package com.Main.service.rss.impl;

import com.Main.RowMapper.rss.ResourceRowMapper;
import com.Main.entity.rss.Resource;
import com.Main.service.rss.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceServiceImpl implements ResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${resource.upload.base.dir}")
    private String baseUploadDir;

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

            // 构建完整的上传路径：基础路径/课程ID/
            String uploadDir = Paths.get(baseUploadDir, courseId.toString()).toString();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 获取文件类型
            String resourceType = getFileType(originalFilename);

            // 如果没有提供资源名称，使用原始文件名
            if (resourceName == null || resourceName.trim().isEmpty()) {
                resourceName = originalFilename;
            }

            // 保存资源信息到数据库
            String sql = "INSERT INTO resource (uploader_id, course_id, resource_name, resource_type, " +
                    "upload_time, file_path, description, directory_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    ownerId,
                    courseId,
                    resourceName,
                    resourceType,
                    LocalDateTime.now(),
                    filePath.toString(),
                    description,
                    directoryId
            );

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("success", true);
            response.put("filePath", filePath.toString());
            response.put("fileName", filename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("success", false);
            errorResponse.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    private String getFileType(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public ResponseEntity<?> downloadFile(Long resourceId) {
        try {
            // 从数据库中查询资源信息
            String sql = "SELECT * FROM resource WHERE resource_id = ?";
            Resource resource = jdbcTemplate.queryForObject(sql, new ResourceRowMapper(), resourceId);

            if (resource == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("找不到指定的资源文件");
            }

            Path path = Paths.get(resource.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("文件不存在于服务器上");
            }

            byte[] content = Files.readAllBytes(path);

            HttpHeaders headers = new HttpHeaders();
            // 从实际文件路径获取文件类型
            String fileType = getFileType(path.getFileName().toString());
            MediaType mediaType = getMediaTypeForFileType(fileType);
            headers.setContentType(mediaType);

            // 使用原始文件名（包含后缀）作为下载文件名
            String filename = path.getFileName().toString();
            if (filename.contains("_")) {
                // 如果文件名包含时间戳前缀，去掉时间戳部分
                filename = filename.substring(filename.indexOf("_") + 1);
            }
            // 处理中文文件名的编码问题
            filename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
            headers.setContentDispositionFormData("attachment", filename);
            // 设置文件大小
            headers.setContentLength(content.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);

        } catch (Exception e) {
            logger.error("文件下载失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("success", false);
            errorResponse.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getDirectory(Long courseId) {
        try {
            // 查询该课程下的所有资源
            String sql = "SELECT * FROM resource WHERE course_id = ? ORDER BY directory_id ,upload_time DESC";
            List<Resource> resources = jdbcTemplate.query(sql, new ResourceRowMapper(), courseId);

            // 按目录ID分组
            Map<Long, List<Resource>> directoryMap = new HashMap<>();

            // 将资源按目录ID分组
            for (Resource resource : resources) {
                Long directoryId = resource.getDirectoryId();
                if (!directoryMap.containsKey(directoryId)) {
                    directoryMap.put(directoryId, new ArrayList<>());
                }
                directoryMap.get(directoryId).add(resource);
            }

            // 构造返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("success", true);
            response.put("directories", directoryMap);
            response.put("totalFiles", resources.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取目录失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("success", false);
            errorResponse.put("message", "获取目录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    private MediaType getMediaTypeForFileType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "html":
                return MediaType.TEXT_HTML;
            case "xml":
                return MediaType.APPLICATION_XML;
            case "json":
                return MediaType.APPLICATION_JSON;
            case "doc":
            case "docx":
                return MediaType.parseMediaType("application/msword");
            case "xls":
            case "xlsx":
                return MediaType.parseMediaType("application/vnd.ms-excel");
            case "ppt":
            case "pptx":
                return MediaType.parseMediaType("application/vnd.ms-powerpoint");
            case "zip":
                return MediaType.parseMediaType("application/zip");
            case "rar":
                return MediaType.parseMediaType("application/x-rar-compressed");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
