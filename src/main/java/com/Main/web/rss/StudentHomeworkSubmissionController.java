package com.Main.web.rss;

import com.Main.entity.rss.HomeworkSubmission;
import com.Main.service.rss.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
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

    // 从 application.properties 读取上传目录，默认 uploads
    @Value("${upload.base-dir:uploads}")
    private String uploadBaseDir;

    @PostMapping("/submit_homework")
    public Map<String, Object> submitHomework(
            @RequestParam Integer homework_id,
            @RequestParam Integer student_id,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 自动适配系统分隔符，存储到项目目录下的 uploads 目录
        String saveDir = Paths.get(System.getProperty("user.dir"), uploadBaseDir).toString();
        Files.createDirectories(Paths.get(saveDir));
        String savePath = Paths.get(saveDir, fileName).toString();

        file.transferTo(new java.io.File(savePath));

        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomework_id(homework_id);
        submission.setStudent_id(student_id);
        submission.setFile_name(fileName);
        submission.setFile_url(savePath);
        submissionService.submit(submission);

        return Map.of("code", "200", "message", "success");
    }

    @GetMapping("/get_submission")
    public Map<String, Object> getSubmission(
            @RequestParam Integer homework_id,
            @RequestParam Integer student_id) {
        HomeworkSubmission submission = submissionService.getByHomeworkIdAndStudentId(homework_id, student_id);
        if (submission != null) {
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
