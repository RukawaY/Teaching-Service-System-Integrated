package com.Main.web.exam;

import com.Main.service.exam.TestPublishService;
import com.Main.entity.exam.TestPublish;
import com.Main.dto.exam.TestPublishCreateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test/testPublish")
public class TestPublishController {

    @Autowired
    private TestPublishService testPublishService;

    @GetMapping("/getTestByCourseId")
    public List<Map<String, Object>> getTestByCourseId(@RequestParam int courseId) {
        return testPublishService.getTestByCourseId(courseId);
    }

    @GetMapping("/getQuestionsByTestId")
    public List<Map<String, Object>> getQuestionsByTestId(
            @RequestParam int testId,
            @RequestParam boolean isTeacher) {
        return testPublishService.getQuestionsByTestId(testId, isTeacher);
    }

    @GetMapping("/getQuestionsByTestIdIfAnswered")
    public List<Map<String, Object>> getQuestionsByTestIdIfAnswered(
            @RequestParam int testId,
            @RequestParam int studentId) {
        return testPublishService.getQuestionsByTestIdIfAnswered(testId, studentId);
    }

    @GetMapping("/getScoresByTestId")
    public List<Map<String, Object>> getScoresByTestId(@RequestParam int testId) {
        return testPublishService.getScoresByTestId(testId);
    }

    @GetMapping("/getTestForStudent")
    public List<Map<String, Object>> getTestForStudent(
            @RequestParam int studentId,
            @RequestParam int courseId) {
        return testPublishService.getTestForStudent(studentId, courseId);
    }

    @GetMapping("/getStudentCourses")
    public List<Map<String, Object>> getStudentCourses(@RequestParam int studentId) {
        return testPublishService.getStudentCourses(studentId);
    }

    @PostMapping("/generate")
    public int generate(@RequestBody TestPublishCreateDTO dto) {
        TestPublish testPublish = new TestPublish();
        testPublish.setTeacherId(dto.getTeacherId());
        testPublish.setCourseId(dto.getCourseId());
        testPublish.setTestName(dto.getTestName());
        testPublish.setPublishTime(dto.getPublishTime());
        testPublish.setDeadline(dto.getDeadline());
        testPublish.setQuestionCount(dto.getQuestionCount());
        testPublish.setRandom(dto.getRandom());
        testPublish.setQuestionIds(dto.getQuestionIds());
        testPublish.setRatio(dto.getRatio());

        return testPublishService.createTest(testPublish);
    }

    @PostMapping("/publish")
    public int publish(@RequestParam int testId,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                       LocalDateTime publishTime) {
        return testPublishService.publishTest(testId, publishTime);
    }

    @GetMapping("/getTestByTestId")
    public List<Map<String, Object>> getTestByTestId(@RequestParam int testId) {
        return testPublishService.getTestByTestId(testId);
    }
}
