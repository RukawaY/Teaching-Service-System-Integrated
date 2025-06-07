package com.Main.entity.rss;


import java.time.LocalDateTime;

public class Resource {
    private long resourceId;
    private long uploaderId;
    private long courseId;
    private String resourceName;
    private String resourceType;
    private LocalDateTime uploadTime;
    private String filePath;
    private String description;
    private String keywords;
    private long directoryId;
    // 目录ID，用于标识资源所属目录

    // 无参构造函数
    public Resource() {}

    // Getter 方法
    public long getResourceId() {
        return resourceId;
    }

    public long getUploaderId() {
        return uploaderId;
    }

    public long getCourseId() {
        return courseId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDescription() {
        return description;
    }

    public String getKeywords() {
        return keywords;
    }

    public long getDirectoryId() {
        return directoryId;
    }

    // Setter 方法
    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public void setUploaderId(long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setDirectoryId(long directoryId) {
        this.directoryId = directoryId;
    }
}
