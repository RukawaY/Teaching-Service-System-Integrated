package com.Main.web.exam;

import com.Main.service.exam.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.Main.entity.exam.QuestionBank;
import com.Main.dto.exam.QuestionCreateDTO;

@RestController
@RequestMapping("/test/questions")
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @PostMapping("/addQuestion")
    public void addBank(@RequestBody QuestionCreateDTO dto) {
        QuestionBank questionBank = new QuestionBank();
        questionBank.setCourseId(dto.getCourseId());
        questionBank.setChapterId(dto.getChapterId());
        if (dto.getQuestionType() != null) {
            questionBank.setQuestionType(QuestionBank.QuestionType.valueOf(dto.getQuestionType()));
        }
        questionBank.setContent(dto.getContent());
        questionBank.setOptions(dto.getOptions());
        questionBank.setAnswer(dto.getAnswer());
        questionBank.setScore(dto.getScore());
        questionBank.setDifficulty(dto.getDifficulty());
        questionBankService.add_question(questionBank);
    }

    @PutMapping("/updateQuestion")
    public Map<String, Object> updateQuestion(@RequestBody QuestionCreateDTO dto) {
        if (dto.getQuestionId() == null || dto.getQuestionId() <= 0) {
            return Map.of("success", false, "message", "题目ID不能为空");
        }
        
        QuestionBank questionBank = new QuestionBank();
        questionBank.setQuestionId(dto.getQuestionId());
        questionBank.setCourseId(dto.getCourseId());
        questionBank.setChapterId(dto.getChapterId());
        if (dto.getQuestionType() != null) {
            questionBank.setQuestionType(QuestionBank.QuestionType.valueOf(dto.getQuestionType()));
        }
        questionBank.setContent(dto.getContent());
        questionBank.setOptions(dto.getOptions());
        questionBank.setAnswer(dto.getAnswer());
        questionBank.setScore(dto.getScore());
        questionBank.setDifficulty(dto.getDifficulty());
        
        int updated = questionBankService.update_question(questionBank);
        if (updated > 0) {
            return Map.of("success", true, "message", "题目更新成功");
        } else {
            return Map.of("success", false, "message", "题目不存在或更新失败");
        }
    }

    @DeleteMapping("/delQuestion")
    public void deleteBank(@RequestParam int question_id) {
        questionBankService.del_question(question_id);
    }

    @GetMapping("/course/{teacherId}")
    public List<Map<String, Object>> getCourses(@PathVariable int teacherId) {
        return questionBankService.get_course(teacherId);
    }

    @GetMapping("/getQuestionByCourse")
    public List<Map<String, Object>> getQuestionByCourse(
            @RequestParam int courseId,
            @RequestParam(required = false, defaultValue = "0") int chapter_id) {
        if (chapter_id == 0) {
            return questionBankService.get_question_by_course(courseId, 0);
        }
        return questionBankService.get_question_by_course(courseId, chapter_id);
    }

    @GetMapping("/searchQuestions")
    public List<Map<String, Object>> searchQuestions(
            @RequestParam int courseId,
            @RequestParam int bankId,
            @RequestParam(required = false) String queName,
            @RequestParam(required = false) String queType) {
        return questionBankService.search_que(courseId, bankId, queName, queType);
    }
}
