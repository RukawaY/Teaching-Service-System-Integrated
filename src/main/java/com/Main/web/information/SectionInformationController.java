package com.Main.web.information;

import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.ApiResponseListDTO;
import com.Main.dto.information.SectionSearchDTO;
import com.Main.dto.information.SectionSearchListDTO;
import com.Main.service.information.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/information/api/v1")
public class SectionInformationController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SectionService sectionService;

    /**
     * 获取开课列表
     * @param courseId 课程ID
     * @param semester  开课学期
     * @param sec_year 开课学年
     * @return 开课列表
     */
    @GetMapping("/courses/{course_id}/sections")
    public ResponseEntity<ApiResponseListDTO<SectionSearchDTO>> getSections(
            @PathVariable("course_id") Integer courseId,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "sec_year", required = false) Integer sec_year) {
        if(sec_year == null) sec_year = 0;
        try{
            List<SectionSearchDTO> sectionsearchlistDTO = sectionService.getSections(courseId, semester, sec_year);
            return ResponseEntity.ok(ApiResponseListDTO.success("获取成功",sectionsearchlistDTO));
        } catch (Exception e) {
            logger.error("获取开课列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseListDTO.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 获取开课详情
     * @param sectionId 开课ID
     * @return 开课详情
     */
    @GetMapping("/sections/{section_id}")
    public ResponseEntity<ApiResponseDTO<SectionSearchDTO>> getSection(
            @PathVariable("section_id") Integer sectionId
    ) {
        try{
            SectionSearchDTO sectionSearchDTO = sectionService.getSectionById(sectionId);
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功",sectionSearchDTO));
        } catch (Exception e) {
            logger.error("获取开课详情失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }
}
