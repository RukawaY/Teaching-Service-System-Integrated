package com.Main.service.course_selection.impl;

import com.Main.RowMapper.course_selection.CourseDao;
import com.Main.RowMapper.course_selection.CourseSelectionDao;
import com.Main.RowMapper.course_selection.CourseSupplementDao;
import com.Main.RowMapper.course_selection.GradeBaseDao;
import com.Main.RowMapper.course_selection.StudentDao;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.SupplementListDTO;
import com.Main.entity.course_selection.Course;
import com.Main.entity.course_selection.CourseSelection;
import com.Main.entity.course_selection.CourseSupplementApplication;
import com.Main.entity.course_selection.Section;
import com.Main.entity.course_selection.SelectionTime;
import com.Main.entity.course_selection.Student;
import com.Main.service.course_selection.ManagerService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Main.course_selection.config.ConcurrentSelectionConfig;
import com.Main.RowMapper.course_selection.CurriculumDao;
import com.Main.RowMapper.course_selection.SelectionTimeDao;
import com.Main.RowMapper.course_selection.SectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理员服务实现类
 */
@Service
public class ManagerServiceImpl implements ManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private CourseSupplementDao courseSupplementDao;

    @Autowired private CourseDao courseDao;

    @Autowired private StudentDao studentDao;

    @Autowired private CourseSelectionDao courseSelectionDao;

    @Autowired private CurriculumDao curriculumDao;  // 添加这一行

    @Autowired private ObjectMapper objectMapper;  // 添加这一行

    @Autowired private SelectionTimeDao selectionTimeDao;

    @Autowired private SectionDao sectionDao;
    
    @Autowired private GradeBaseDao gradeBaseDao;
    
    @Autowired private ConcurrentSelectionConfig concurrentSelectionConfig;

    @Override
    @Transactional
    public boolean updateSelectionTime(SelectionTime selectionTime) {
        // 检查参数是否合法
        if (selectionTime == null || selectionTime.getMaxNumber() == null) {
            return false;
        }
        
        // 调用DAO层更新选课时间配置
        boolean success = selectionTimeDao.updateSelectionTime(selectionTime);
        
        // 如果更新成功，刷新选课信号量以立即应用新的并发限制
        if (success) {
            logger.info("Selection time updated, refreshing selection semaphore with max concurrent selections: {}", selectionTime.getMaxNumber());
            concurrentSelectionConfig.refreshSelectionSemaphore();
        }
        
        return success;
    }

    @Override
    @Transactional
    public String setCurriculum(CurriculumDTO curriculumDTO) {
        if (curriculumDTO.getMajorName() == null || curriculumDTO.getSections() == null) {
            return "培养方案信息不完整"; // 培养方案信息不完整
        }
        
        // 2. 检查专业是否存在
        if (!curriculumDao.checkMajorExists(curriculumDTO.getMajorName())) {
            return "专业不存在"; // 专业不存在
        }
        
        // 3. 将培养方案对象转换为JSON字符串
        String curriculumJson;
        try {
            curriculumJson = objectMapper.writeValueAsString(curriculumDTO.getSections());
        } catch (Exception e) {
            e.printStackTrace();
            return "JSON转换失败";
        }
        
        // 4. 删除原有培养方案
        try {
            curriculumDao.deleteCurriculumByMajorName(curriculumDTO.getMajorName());
        } catch (Exception e) {
            e.printStackTrace();
            return "删除原有培养方案失败";
        }
        
        // 5. 保存新的培养方案
        if(curriculumDao.saveCurriculum(curriculumDTO.getMajorName(), curriculumJson)){
            return "success"; // 保存培养方案成功
        } else {
            return "保存培养方案失败"; // 保存培养方案失败
        }
    }

    @Override
    public SelectionTime getSelectionTime() {
        // 调用DAO层获取选课系统时间配置
        return selectionTimeDao.getSelectionTime();
    }

    @Override
    @Transactional
    public String chooseCourseForStudent(Integer studentId, Integer courseId) {
        // 实现管理员为学生选课的逻辑
        // 注意：这里的courseId实际上是sectionId
        
        // 1. 检查学生是否存在
        if(!studentDao.existsById(studentId)) {
            return "学生不存在"; // 学生不存在
        }
        
        // 2. 检查开课信息是否存在
        if(!sectionDao.existsById(courseId)) {
            return "开课信息不存在"; // 开课信息不存在
        }
        
        // 3. 检查学生是否已选该开课
        if(courseSelectionDao.isStudentSelectedCourse(studentId, courseId)) {
            return "学生已选该开课"; // 学生已选该开课
        }
        
        // 4. 获取真正的课程ID
        Section section = sectionDao.findById(courseId);
        if (section == null) {
            return "开课信息不存在";
        }
        Integer realCourseId = section.getCourseId();
        
        // 5. 插入选课记录
        courseSelectionDao.insertSelection(studentId, courseId);
        
        // 6. 插入初始成绩记录到GradeBase表
        gradeBaseDao.insertInitialGrade(studentId, realCourseId, courseId);
        
        // 7. 更新开课可用容量
        sectionDao.decreaseAvailableCapacity(courseId);
        
        return "success"; // 选课成功
    }

    @Override
    public SupplementListDTO getSupplementApplications() {
        // 不带过滤条件，获取所有补选申请
        return getSupplementApplications(null);
    }

    @Override
    public SupplementListDTO getSupplementApplications(Integer courseId) {
        // 获取补选申请列表，可选按课程ID筛选
        List<CourseSupplementApplication> supplements;

        if (courseId != null) {
            // 如果提供了课程ID，按课程ID筛选
            supplements = courseSupplementDao.findBySectionId(courseId);
        } else {
            // 获取所有补选申请
            supplements = courseSupplementDao.findAll();
        }

        // 转换成DTO对象
        SupplementListDTO supplementListDTO = new SupplementListDTO();
        List<SupplementListDTO.SupplementItemDTO> items = new ArrayList<>();

        for (CourseSupplementApplication supplement : supplements) {
            SupplementListDTO.SupplementItemDTO item = new SupplementListDTO.SupplementItemDTO();
            item.setSupplementId(supplement.getSupplementId());
            
            // 查询学生名称和课程名称
            try {
                // 查询学生名称
                String studentSql = "SELECT name FROM user WHERE user_id = ?";
                String studentName = jdbcTemplate.queryForObject(studentSql, String.class, supplement.getStudentId());
                item.setStudentName(studentName);
                
                // 查询课程名称
                String courseSql = "SELECT c.course_name FROM course c JOIN section s ON c.course_id = s.course_id WHERE s.section_id = ?";
                String courseName = jdbcTemplate.queryForObject(courseSql, String.class, supplement.getSectionId());
                item.setCourseName(courseName);
            } catch (Exception e) {
                // 如果查询失败，设置默认值
                e.printStackTrace();
                item.setStudentName("未知学生");
                item.setCourseName("未知课程");
            }
            
            items.add(item);
        }

        supplementListDTO.setSupplementList(items);
        return supplementListDTO;
    }

    @Override
    @Transactional
    public String processSupplementApplication(Integer supplementId, Boolean approved) {
        // 1. 检查补选申请是否存在
        CourseSupplementApplication application = courseSupplementDao.findById(supplementId);
        if(application == null) {
            return "补选申请不存在"; // 补选申请不存在
        }
        
        // 2. 更新补选申请状态
        Integer newStatus = approved ? 1 : 2; // 1-已同意, 2-已拒绝
        boolean statusUpdateSuccess = courseSupplementDao.updateStatus(supplementId, newStatus);
        
        if (!statusUpdateSuccess) {
            return "更新状态失败"; // 更新状态失败
        }
        
        // 3. 如果批准了申请，则为学生选课
        if (approved) {
            Integer sectionId = application.getSectionId();
            
            // 检查学生是否已选过该开课
            if (courseSelectionDao.isStudentSelectedCourse(application.getStudentId(), sectionId)) {
                return "success"; // 学生已经选过该开课，直接返回成功
            }
            
            // 获取真正的课程ID
            Section section = sectionDao.findById(sectionId);
            if (section == null) {
                return "开课信息不存在";
            }
            Integer realCourseId = section.getCourseId();
            
            // 为学生选课
            boolean courseSelectionSuccess = courseSelectionDao.saveSelection(application.getStudentId(), sectionId);
            if (!courseSelectionSuccess) {
                return "选课失败"; // 选课失败
            }
            
            // 插入初始成绩记录到GradeBase表
            gradeBaseDao.insertInitialGrade(application.getStudentId(), realCourseId, sectionId);

            // 使用sectionDao减少对应开课的可用容量
            if(sectionDao.decreaseAvailableCapacity(sectionId)) {
                return "success"; // 选课成功
            } else {
                return "更新开课可用容量失败"; // 更新开课可用容量失败
            }
        }
        
        return "success"; // 如果是拒绝申请，到这里就已经成功了
    }
}