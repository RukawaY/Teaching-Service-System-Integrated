package com.Main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class ResourceAppConfig {

    @Value("${file.upload.path:${user.home}/uploads/resources}")
    private String uploadPath;

    @Value("${file.upload.maxSize:100000}")
    private long maxUploadSize;

    // 获取上传路径
    public String getUploadPath() {
        return uploadPath;
    }

    // 获取文件大小限制
    public long getMaxUploadSize() {
        return maxUploadSize;
    }

    // 获取用户特定的上传路径
    public String getUserUploadPath(Long userId) {
        return uploadPath + "/" + userId.toString();
    }

    // 获取课程特定的上传路径
    public String getCourseUploadPath(Long courseId) {
        return uploadPath + "/courses/" + courseId.toString();
    }
}
