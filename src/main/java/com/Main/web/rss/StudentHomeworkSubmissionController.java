package com.Main.web.rss;

import com.Main.entity.rss.HomeworkSubmission;
import com.Main.service.rss.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentHomeworkSubmissionController {
    @Autowired
    private HomeworkSubmissionService submissionService;
    
    // 作业文件存储的基础路径，默认为项目根目录下的homework_uploads文件夹
    @Value("${homework.upload.path:src/main/webapp/homework_uploads}")
    private String homeworkUploadPath;

    @PostMapping("/submit_homework")
    public Map<String, Object> submitHomework(
            @RequestParam Integer homework_id,
            @RequestParam Integer student_id,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }
        
        try {
            // 确保目录存在
            File uploadDir = new File(homeworkUploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    throw new RuntimeException("创建作业存储目录失败");
                }
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            
            // 构建完整文件路径
            Path filePath = Paths.get(homeworkUploadPath, fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 数据库中存储相对路径
            String savePath = "/homework_uploads/" + fileName;
            System.out.println("文件已保存到: " + filePath.toAbsolutePath());
            
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setHomework_id(homework_id);
            submission.setStudent_id(student_id);
            submission.setFile_name(fileName);
            submission.setFile_url(savePath);
            submissionService.submit(submission);
            return Map.of("code", "200", "message", "success");
        } catch (Exception e) {
            System.err.println("文件保存失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/get_submission")
    public Map<String, Object> getSubmission(
            @RequestParam Integer homework_id,
            @RequestParam Integer student_id) {
        HomeworkSubmission submission = submissionService.getByHomeworkIdAndStudentId(homework_id, student_id);
        if (submission != null) {
            // 构建data
            Map<String, Object> data = new HashMap<>();
            data.put("submission_id", submission.getSubmission_id());
            data.put("homework_id", submission.getHomework_id());
            data.put("student_id", submission.getStudent_id());
            data.put("submit_time", submission.getSubmit_time());
            data.put("file_name", submission.getFile_name());
            data.put("file_url", submission.getFile_url());
            data.put("score", submission.getScore());
            data.put("comment", submission.getComment());

            Map<String, Object> result = new HashMap<>();
            result.put("code", "200");
            result.put("message", "success");
            result.put("data", data);
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "404");
            result.put("message", "未找到提交");
            return result;
        }
    }
}