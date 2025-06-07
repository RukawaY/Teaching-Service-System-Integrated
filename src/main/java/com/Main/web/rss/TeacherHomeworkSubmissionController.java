package com.Main.web.rss;

import com.Main.entity.rss.HomeworkSubmission;
import com.Main.service.rss.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
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
        Path path = Paths.get(sub.getFile_url());
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + sub.getFile_name() + "\"")
                .body(resource);
    }
}