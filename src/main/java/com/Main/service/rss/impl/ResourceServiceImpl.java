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

import java.io.File;
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

    // 定义一个固定的上传目录，而不是从配置文件读取
    private static final String RESOURCE_UPLOAD_DIR = "resource_uploads";

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

            // 创建上传目录
            String uploadDir = RESOURCE_UPLOAD_DIR;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IOException("无法创建资源存储目录: " + uploadDir);
                }
            }

            // 保存文件
            Path filePath = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), filePath);
            
            logger.info("文件已保存到: " + filePath.toAbsolutePath());

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
            response.put("code", "200");
            response.put("message", "success");
            response.put("data", Map.of(
                "filePath", filePath.toString(),
                "fileName", filename
            ));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "500");
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
                        .body(Map.of("code", "404", "message", "找不到指定的资源文件"));
            }

            Path path = Paths.get(resource.getFilePath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", "404", "message", "文件不存在于服务器上"));
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
            errorResponse.put("code", "500");
            errorResponse.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> getDirectory(Long courseId) {
        try {
            // 查询该课程下的所有资源
            String sql = "SELECT * FROM resource WHERE course_id = ? ORDER BY directory_id, upload_time DESC";
            List<Resource> resources = jdbcTemplate.query(sql, new ResourceRowMapper(), courseId);

            if (resources.isEmpty()) {
                // 返回空目录
                Map<String, Object> response = new HashMap<>();
                response.put("code", "200");
                response.put("message", "success");
                response.put("data", new ArrayList<>());
                return ResponseEntity.ok(response);
            }

            // 构建树形结构的目录
            List<Map<String, Object>> directoryTree = new ArrayList<>();
            Map<Long, Map<String, Object>> directoryMap = new HashMap<>();

            // 查询目录名称（如果有目录表的话）
            // 这里假设目录信息存储在资源的description中，实际应根据数据库设计调整
            
            // 第一步：创建目录节点
            for (Resource resource : resources) {
                Long directoryId = resource.getDirectoryId();
                
                // 如果目录节点不存在，创建一个
                if (!directoryMap.containsKey(directoryId)) {
                    Map<String, Object> directoryNode = new HashMap<>();
                    directoryNode.put("directoryId", directoryId);
                    directoryNode.put("directoryName", "目录 " + directoryId); // 可根据实际情况设置目录名
                    directoryNode.put("children", new ArrayList<Map<String, Object>>());
                    
                    directoryMap.put(directoryId, directoryNode);
                    directoryTree.add(directoryNode);
                }
                
                // 第二步：为每个资源创建资源节点，并添加到对应目录的children中
                Map<String, Object> resourceNode = new HashMap<>();
                resourceNode.put("resource_id", resource.getResourceId());
                resourceNode.put("student_id", resource.getUploaderId());
                resourceNode.put("course_id", resource.getCourseId());
                resourceNode.put("resource_name", resource.getResourceName());
                resourceNode.put("resource_description", resource.getDescription());
                resourceNode.put("upload_time", resource.getUploadTime().toString());
                resourceNode.put("resource_type", resource.getResourceType());
                resourceNode.put("directory_id", resource.getDirectoryId());
                
                // 将资源节点添加到对应目录的children中
                List<Map<String, Object>> children = (List<Map<String, Object>>) directoryMap.get(directoryId).get("children");
                children.add(resourceNode);
            }

            // 如果目录为空，将每个资源直接放在根目录下
            if (directoryTree.isEmpty() && !resources.isEmpty()) {
                for (Resource resource : resources) {
                    Map<String, Object> resourceNode = new HashMap<>();
                    resourceNode.put("resource_id", resource.getResourceId());
                    resourceNode.put("student_id", resource.getUploaderId());
                    resourceNode.put("course_id", resource.getCourseId());
                    resourceNode.put("resource_name", resource.getResourceName());
                    resourceNode.put("resource_description", resource.getDescription());
                    resourceNode.put("upload_time", resource.getUploadTime().toString());
                    resourceNode.put("resource_type", resource.getResourceType());
                    directoryTree.add(resourceNode);
                }
            }

            // 打印日志，帮助调试
            logger.info("返回的目录结构: {}", directoryTree);

            // 构造返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("code", "200");
            response.put("message", "success");
            response.put("data", directoryTree);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取目录失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "500");
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
