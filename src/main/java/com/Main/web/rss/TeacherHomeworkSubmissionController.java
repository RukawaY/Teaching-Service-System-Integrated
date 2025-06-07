package com.Main.web.rss;

import com.Main.entity.rss.HomeworkSubmission;
import com.Main.service.rss.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherHomeworkSubmissionController {
    @Autowired
    private HomeworkSubmissionService submissionService;
    
    // 配置项目根目录，与上传路径保持一致
    @Value("${homework.upload.path:src/main/webapp/homework_uploads}")
    private String homeworkUploadPath;

    @GetMapping("/homework/{homework_id}/submissions")
    public Map<String, Object> getSubmissions(@PathVariable Integer homework_id) {
        List<HomeworkSubmission> list = submissionService.getSubmissions(homework_id);
        return Map.of("code", "200", "message", "success", "data", list);
    }

    @PostMapping("/submit_grading")
    public Map<String, Object> grading(@RequestBody Map<String, Object> req) {
        Integer student_id = (Integer) req.get("student_id");
        Integer submission_id = (Integer) req.get("submission_id");
        Double score = Double.valueOf(req.get("score").toString());
        String comment = (String) req.get("comment");
        submissionService.grade(submission_id, score, comment);
        return Map.of("code", "200", "message", "success");
    }

    @GetMapping("/submission/{submission_id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer submission_id) throws Exception {
        HomeworkSubmission sub = submissionService.getById(submission_id);
        try {
            // 数据库中存储的是相对路径如/homework_uploads/filename
            // 需要去掉开头的/以匹配项目目录结构
            String relativePath = sub.getFile_url();
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            
            // 构建文件的完整路径
            Path filePath = Paths.get("src/main/webapp", relativePath);
            System.out.println("尝试访问文件: " + filePath.toAbsolutePath());
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + sub.getFile_name() + "\"")
                    .body(resource);
            } else {
                throw new RuntimeException("文件不存在或无法读取: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("文件下载错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}