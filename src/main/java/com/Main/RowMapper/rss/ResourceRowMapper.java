package com.Main.RowMapper.rss;

import com.Main.entity.rss.Resource;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceRowMapper implements RowMapper<Resource> {
    @Override
    public Resource mapRow(ResultSet rs, int rowNum) throws SQLException {
        Resource resource = new Resource();
        resource.setResourceId(rs.getLong("resource_id"));
        resource.setUploaderId(rs.getLong("uploader_id"));
        resource.setCourseId(rs.getLong("course_id"));
        resource.setResourceName(rs.getString("resource_name"));
        resource.setResourceType(rs.getString("resource_type"));
        resource.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
        resource.setFilePath(rs.getString("file_path"));
        resource.setDescription(rs.getString("description"));
        resource.setKeywords(rs.getString("keywords"));
        resource.setDirectoryId(rs.getLong("directory_id"));
        return resource;
    }
}
