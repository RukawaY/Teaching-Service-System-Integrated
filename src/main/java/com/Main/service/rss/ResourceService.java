package com.Main.service.rss;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {
    /**
     * 上传文件
     *
     * @param file         上传的文件
     * @param directoryId  目录ID
     * @param courseId     课程ID
     * @param ownerId      所有者ID
     * @param resource_Name 资源名称（可选）
     * @param description  文件描述（可选）
     * @return 上传结果
     */
    ResponseEntity<?> uploadFile(
            MultipartFile file,
            Long directoryId,
            Long courseId,
            Long ownerId,
            String resource_Name,
            String description
    );

    /**
     * 下载资源文件
     *
     * @param resourceId 资源ID
     * @return 文件下载响应
     */
    ResponseEntity<?> downloadFile(Long resourceId);

    /**
     * 获取课程目录下的所有资源
     *
     * @param courseId 课程ID
     * @return 资源列表
     */
    ResponseEntity<?> getDirectory(String courseId);
}
