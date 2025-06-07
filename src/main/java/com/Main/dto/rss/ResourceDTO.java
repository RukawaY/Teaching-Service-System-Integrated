package com.Main.dto.rss;

import java.time.LocalDateTime;

public class ResourceDTO {
    private Long resourceId;
    private Long directoryId;
    private Long courseId;
    private String resourceName;
    private String resourceType;
    private LocalDateTime uploadTime;
    private String filePath;
    private String description;
    private Long uploaderId;
    private String keywords;

    // 无参构造函数
    public ResourceDTO() {}

    // 所有参数的构造函数
    public ResourceDTO(Long resourceId, Long directoryId, Long courseId, String resourceName,
                       String resourceType, LocalDateTime uploadTime, String filePath,
                       String description, Long uploaderId, String keywords) {
        this.resourceId = resourceId;
        this.directoryId = directoryId;
        this.courseId = courseId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.uploadTime = uploadTime;
        this.filePath = filePath;
        this.description = description;
        this.uploaderId = uploaderId;
        this.keywords = keywords;
    }

    // Getters and Setters
    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(Long directoryId) {
        this.directoryId = directoryId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
