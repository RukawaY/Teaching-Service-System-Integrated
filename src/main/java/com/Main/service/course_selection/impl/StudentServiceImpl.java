package com.Main.service.course_selection.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Main.RowMapper.course_selection.CourseDao;
import com.Main.RowMapper.course_selection.CourseSelectionDao;
import com.Main.RowMapper.course_selection.CourseSupplementDao;
import com.Main.RowMapper.course_selection.GradeBaseDao;
import com.Main.RowMapper.course_selection.SectionDao;
import com.Main.RowMapper.course_selection.StudentDao;
import com.Main.RowMapper.course_selection.CurriculumDao;
import com.Main.dto.course_selection.CourseListDTO;
import com.Main.dto.course_selection.CourseTableDTO;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.SupplementResultListDTO;
import com.Main.entity.course_selection.Course;
import com.Main.entity.course_selection.CourseSelection;
import com.Main.entity.course_selection.Section;
import com.Main.service.course_selection.SelectionTimeService;
import com.Main.service.course_selection.StudentService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 学生服务实现类
 */
@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private CourseSupplementDao courseSupplementDao;

    @Autowired private CourseSelectionDao courseSelectionDao;

    @Autowired private CourseDao courseDao;

    @Autowired private StudentDao studentDao;

    @Autowired private SectionDao sectionDao;

    @Autowired private CurriculumDao curriculumDao;
    
    @Autowired private SelectionTimeService selectionTimeService;
    
    @Autowired private GradeBaseDao gradeBaseDao;

    @Override
    public CourseListDTO searchCourse(String courseName, String teacherName, Integer courseId, Integer studentId, Boolean needAvailable) {
        // 实现课程搜索逻辑
        // 根据不同的参数组合构建SQL查询
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.course_id, c.course_name, c.course_description, c.teacher_id, c.credit, " +
                   "u.name as teacher_name, s.section_id, s.classroom_id, s.sec_time, s.available_capacity, s.capacity as total_capacity, " +
                   "cl.location as classroom " +
                   "FROM course c " +
                   "JOIN user u ON c.teacher_id = u.user_id " +
                   "JOIN section s ON c.course_id = s.course_id " +
                   "JOIN classroom cl ON s.classroom_id = cl.classroom_id " +
                   "WHERE u.role = 't'");

        List<Object> params = new ArrayList<>();
        
        // 如果提供了studentId，先检查学生的个人培养方案是否存在
        Set<String> personalCurriculumCourses = new HashSet<>();
        if (studentId != null) {
            try {
                // 获取学生的个人培养方案
                CurriculumDTO personalCurriculum = getPersonalCurriculum(studentId);
                
                // 检查个人培养方案是否存在（sections不为null且不为空）
                if (personalCurriculum == null || personalCurriculum.getSections() == null || personalCurriculum.getSections().isEmpty()) {
                    logger.warn("Personal curriculum not found for student ID: {}", studentId);
                    return null; // 返回null表示个人培养方案不存在
                }
                
                    // 提取所有课程名
                    for (CurriculumDTO.SectionDTO section : personalCurriculum.getSections()) {
                        if (section.getCourseList() != null) {
                            for (CurriculumDTO.CourseCreditDTO course : section.getCourseList()) {
                                if (course.getCourseName() != null) {
                                    personalCurriculumCourses.add(course.getCourseName());
                            }
                        }
                    }
                }
                
                if (!personalCurriculumCourses.isEmpty()) {
                    logger.info("Found {} courses in student's personal curriculum", personalCurriculumCourses.size());
                    // 添加条件：课程名在个人培养方案列表中
                    sql.append(" AND c.course_name IN (");
                    for (int i = 0; i < personalCurriculumCourses.size(); i++) {
                        sql.append(i > 0 ? ", ?" : "?");
                    }
                    sql.append(")");
                    params.addAll(personalCurriculumCourses);
                } else {
                    logger.warn("No courses found in student's personal curriculum for student ID: {}", studentId);
                    return null; // 个人培养方案为空，返回null
                }
            } catch (Exception e) {
                logger.error("Error processing personal curriculum for student {}: {}", studentId, e.getMessage());
                return null; // 获取个人培养方案时出错，返回null
            }
        }
        
        if (courseName != null && !courseName.isEmpty()) {
            sql.append(" AND c.course_name LIKE ?");
            params.add("%" + courseName + "%");
        }
        if (teacherName != null && !teacherName.isEmpty()) {
            sql.append(" AND u.name LIKE ?");
            params.add("%" + teacherName + "%");
        }
        if (courseId != null) {
            sql.append(" AND c.course_id = ?");
            params.add(courseId);
        }
        if (needAvailable != null && needAvailable) {
            sql.append(" AND s.available_capacity > 0");
        }

        logger.info("Executing search course query: {}", sql.toString());
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        logger.info("Found {} course sections matching criteria", rows.size());

        List<com.Main.dto.course_selection.CourseListDTO.CourseDTO> courseDTOList = new ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            com.Main.dto.course_selection.CourseListDTO.CourseDTO dto = new com.Main.dto.course_selection.CourseListDTO.CourseDTO();
            // 正确设置courseId和sectionId
            dto.setCourseId(row.get("course_id") != null ? ((Number)row.get("course_id")).intValue() : null);
            dto.setSectionId(row.get("section_id") != null ? ((Number)row.get("section_id")).intValue() : null);
            dto.setCourseName((String)row.get("course_name"));
            dto.setCourseDescription((String)row.get("course_description"));
            dto.setTeacherName((String)row.get("teacher_name"));
            dto.setClassTime(row.get("sec_time") != null ? row.get("sec_time").toString() : null);
            dto.setClassroom((String)row.get("classroom"));
            dto.setAvailableCapacity(row.get("available_capacity") != null ? ((Number)row.get("available_capacity")).intValue() : null);
            
            // 添加总容量
            dto.setTotalCapacity(row.get("total_capacity") != null ? ((Number)row.get("total_capacity")).intValue() : null);
            
            // 添加学分
            dto.setCredit(row.get("credit") != null ? ((Number)row.get("credit")).doubleValue() : null);
            
            courseDTOList.add(dto);
        }
        CourseListDTO courseListDTO = new CourseListDTO();
        courseListDTO.setCourseList(courseDTOList);
        return courseListDTO;
    }

    @Override
    public CourseTableDTO searchCourseTable(Integer courseId, String courseName, String category) {
        logger.info("Searching course table with filters - courseId: {}, courseName: {}, category: {}", 
                   courseId, courseName, category);
        
        try {
            // Call DAO to search course table
            List<Course> courses = courseDao.searchCourseTable(courseId, courseName, category);
            logger.info("Found {} courses matching the criteria", courses.size());
            
            // Convert Course entities to CourseInfoDTO
            List<CourseTableDTO.CourseInfoDTO> courseInfoList = new ArrayList<>();
            for (Course course : courses) {
                CourseTableDTO.CourseInfoDTO courseInfo = new CourseTableDTO.CourseInfoDTO();
                courseInfo.setCourseId(course.getCourseId());
                courseInfo.setCourseName(course.getCourseName());
                courseInfo.setCourseDescription(course.getCourseDescription());
                
                // Keep credit as Double to support decimal places
                courseInfo.setCredit(course.getCredit() != null ? course.getCredit() : 0.0);
                courseInfo.setCategory(course.getCategory());
                
                courseInfoList.add(courseInfo);
                logger.debug("Added course: id={}, name={}, category={}, credit={}", 
                           course.getCourseId(), course.getCourseName(), course.getCategory(), course.getCredit());
            }
            
            CourseTableDTO result = new CourseTableDTO(courseInfoList);
            logger.info("Successfully created course table response with {} courses", courseInfoList.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error searching course table: {}", e.getMessage(), e);
            // Return empty result in case of error
            return new CourseTableDTO(new ArrayList<>());
        }
    }

    @Override
    @Transactional
    public String chooseCourse(Integer studentId, Integer courseId) {
        // 检查当前时间是否在正选时间段内
        if (!selectionTimeService.isInFirstSelectionTime()) {
            logger.warn("Cannot choose course outside the first selection time period for student: {}", studentId);
            return "当前非选课时间段";
        }
        
        // 1. 检查学生是否存在
        if (!studentDao.existsById(studentId)) {
            return "学生不存在";
        }
        // 2. 检查开课信息是否存在
        if (!sectionDao.existsById(courseId)) {
            return "课程不存在";
        }
        
        // 3. 获取section对应的courseId（真正的课程ID）
        Section section = sectionDao.findById(courseId);
        if (section == null) {
            return "课程不存在";
        }
        Integer realCourseId = section.getCourseId();
        
        // 4. 检查学生是否已选该课程的任何开课
        if (courseSelectionDao.hasStudentSelectedCourse(studentId, realCourseId)) {
            return "你已经选过该课程";
        }
        // 5. 检查开课容量是否已满
        if (sectionDao.isFull(courseId)) {
            return "课程已满";
        }
        // 6. 检查时间冲突（简单实现：查出学生已选开课的sec_time，与当前开课比对）
        List<CourseSelection> selectedCourses = courseSelectionDao.findByStudentId(studentId);
        // 直接使用courseId（实际是sectionId）获取目标课程时间
        String targetTime = sectionDao.getSectionTime(courseId);
        if (targetTime == null) {
            return "课程不存在";
        }
        for (CourseSelection selection : selectedCourses) {
            String sectionTime = sectionDao.getSectionTime(selection.getSectionId());
            if (sectionTime != null && sectionTime.equals(targetTime)) {
                // 时间冲突
                return "时间冲突";
            }
        }
        // 7. 插入选课记录
        boolean insertSuccess = courseSelectionDao.insertSelection(studentId, courseId);
        if (!insertSuccess) {
            return "插入选课记录失败";
        }
        
        // 8. 插入初始成绩记录到GradeBase表
        gradeBaseDao.insertInitialGrade(studentId, realCourseId, courseId);
        
        // 9. 更新开课可用容量
        boolean updateSuccess = sectionDao.decreaseAvailableCapacity(courseId);
        return updateSuccess ? "success" : "容量更新失败";
    }

    @Override
    @Transactional
    public boolean dropCourse(Integer studentId, Integer courseId) {
        // 检查当前时间是否在退课时间段内
        if (!selectionTimeService.isInDropTime()) {
            logger.warn("Cannot drop course outside the drop time period for student: {}", studentId);
            return false;
        }
        
        // 实现退课逻辑
        // 根据学生ID和课程ID删除选课记录
        // 在新结构中，courseId 实际上是 sectionId
        if (courseSelectionDao.deleteSelection(studentId, courseId)) {
            // 退课成功后，增加开课可用容量
            return sectionDao.increaseAvailableCapacity(courseId);
        }
        return false;
    }

    @Override
    public CourseListDTO getSelectedCourses(Integer studentId) {
        // logger.info("Starting to fetch selected courses for student {}", studentId);
        
        // 验证用户是否为学生（通过数据库查询）
        try {
            String sql = "SELECT role FROM user WHERE user_id = ?";
            String userRole = jdbcTemplate.queryForObject(sql, String.class, studentId);
            if (!"s".equals(userRole)) {
                logger.warn("User {} is not a student, role: {}", studentId, userRole);
                return null; // 返回null表示错误
            }
        } catch (Exception e) {
            logger.error("Failed to verify user role for user_id: {}, Error: {}", studentId, e.getMessage());
            return null; // 用户不存在或查询失败，返回null
        }
        
        // 实现查询学生已选课程逻辑
        // 根据学生ID查询所有已选课程
        List<CourseSelection> selections = courseSelectionDao.findByStudentId(studentId);
        // logger.info("Student {} has selected {} courses", studentId, selections.size());

        if (CollectionUtils.isEmpty(selections)) {
            // logger.info("Student {} has not selected any courses", studentId);
            return new CourseListDTO();
        }

        List<CourseListDTO.CourseDTO> courseDTOList = new ArrayList<>();
        for (CourseSelection selection : selections) {
            // logger.info("Processing selection record: selection_id={}, section_id={}", selection.getSelectionId(), selection.getSectionId());
            
            // 获取section对应的course
            Section section = sectionDao.findById(selection.getSectionId());
            if (section == null) {
                logger.warn("Section not found for section_id={}", selection.getSectionId());
                continue;
            }
            logger.info("Found section info: section_id={}, course_id={}, classroom_id={}, sec_time={}",
                      section.getSectionId(), section.getCourseId(), section.getClassroomId(), section.getSecTime());
            
            com.Main.entity.course_selection.Course course = courseDao.findById(section.getCourseId());
            if (course != null) {
                logger.info("Found course info: course_id={}, course_name={}, teacher_id={}",
                          course.getCourseId(), course.getCourseName(), course.getTeacherId());
                
                CourseListDTO.CourseDTO dto = new CourseListDTO.CourseDTO();
                // 设置courseId字段为sectionId（字段名保持courseId，但内容实际是sectionId）
                dto.setCourseId(selection.getSectionId()); // section_id
                dto.setSectionId(selection.getSectionId()); // section_id
                dto.setCourseName(course.getCourseName());
                dto.setCourseDescription(course.getCourseDescription());
                
                // 从user表获取教师名称
                String teacherName = getTeacherNameById(course.getTeacherId());
                // logger.info("Teacher name for ID {}: {}", course.getTeacherId(), teacherName);
                dto.setTeacherName(teacherName);
                
                // 使用section的时间信息
                dto.setClassTime(section.getSecTime());
                
                // 获取教室信息
                String classroom = getClassroomLocation(section.getClassroomId());
                logger.info("Classroom location for ID {}: {}", section.getClassroomId(), classroom);
                dto.setClassroom(classroom);
                
                dto.setAvailableCapacity(section.getAvailableCapacity());
                
                // 添加总容量字段
                dto.setTotalCapacity(section.getCapacity());
                
                // 添加学分字段
                dto.setCredit(course.getCredit());
                
                courseDTOList.add(dto);
                logger.info("Successfully added course DTO: {}", dto.getCourseName());
            } else {
                logger.warn("Course not found for course_id={}. Creating minimal course info.", section.getCourseId());
                // 即使找不到课程信息，也创建一个最小的课程DTO，以便前端显示
                CourseListDTO.CourseDTO dto = new CourseListDTO.CourseDTO();
                dto.setCourseId(selection.getSectionId()); // section_id（字段名保持courseId，但内容实际是sectionId）
                dto.setSectionId(selection.getSectionId()); // section_id
                dto.setCourseName("Unknown Course (ID: " + section.getCourseId() + ")");
                dto.setCourseDescription("Course information unavailable");
                dto.setTeacherName("Unknown");
                dto.setClassTime(section.getSecTime());
                
                // 获取教室信息
                String classroom = getClassroomLocation(section.getClassroomId());
                dto.setClassroom(classroom);
                
                dto.setAvailableCapacity(section.getAvailableCapacity());
                
                // 添加总容量字段
                dto.setTotalCapacity(section.getCapacity());
                
                // 添加默认学分字段
                dto.setCredit(0.0);
                
                courseDTOList.add(dto);
                logger.info("Added minimal course info for missing course_id={}", section.getCourseId());
            }
        }
        
        CourseListDTO courseListDTO = new CourseListDTO();
        courseListDTO.setCourseList(courseDTOList);
        logger.info("Completed fetching selected courses for student {}, returned {} courses", studentId, courseDTOList.size());
        return courseListDTO;
    }
    
    /**
     * 根据教师ID获取教师姓名
     */
    private String getTeacherNameById(Integer teacherId) {
        try {
            String sql = "SELECT name FROM user WHERE user_id = ? AND role = 't'";
            String name = jdbcTemplate.queryForObject(sql, String.class, teacherId);
            // logger.info("Teacher name query: ID={}, Name={}", teacherId, name);
            return name;
        } catch (Exception e) {
            logger.error("Failed to get teacher name: ID={}, Error={}", teacherId, e.getMessage());
            return String.valueOf(teacherId); // 查询失败时返回ID
        }
    }
    
    /**
     * 根据教室ID获取教室位置
     */
    private String getClassroomLocation(Integer classroomId) {
        try {
            String sql = "SELECT location FROM classroom WHERE classroom_id = ?";
            String location = jdbcTemplate.queryForObject(sql, String.class, classroomId);
            logger.info("Classroom location query: ID={}, Location={}", classroomId, location);
            return location;
        } catch (Exception e) {
            logger.error("Failed to get classroom location: ID={}, Error={}", classroomId, e.getMessage());
            return String.valueOf(classroomId); // 查询失败时返回ID
        }
    }

    @Override
    public CurriculumDTO getCurriculum(String majorName) {
        logger.info("Fetching curriculum for major: {}", majorName);
        CurriculumDTO curriculumDTO = new CurriculumDTO();

        try {
            // 首先检查专业是否存在
            if (!curriculumDao.checkMajorExists(majorName)) {
                logger.warn("Major not found: {}", majorName);
                return null; // 返回空对象
            }
            
            // 获取专业ID
            String sql = "SELECT major_id FROM major WHERE major_name = ?";
            Integer majorId = jdbcTemplate.queryForObject(sql, Integer.class, majorName);
            
            if (majorId == null) {
                logger.warn("Could not find major_id for major_name: {}", majorName);
                return curriculumDTO;
            }
            
            // 设置专业名称（无论是否有培养方案都要设置）
            curriculumDTO.setMajorName(majorName);
            
            // 查询专业培养方案 - 使用queryForList来避免EmptyResultDataAccessException
            String curriculumSql = "SELECT curriculum_json FROM curriculum WHERE major_id = ?";
            List<String> curriculumResults = jdbcTemplate.queryForList(curriculumSql, String.class, majorId);
            
            if (!curriculumResults.isEmpty()) {
                String curriculumJson = curriculumResults.get(0);
                if (curriculumJson != null && !curriculumJson.trim().isEmpty()) {
                    // 反序列化JSON到CurriculumDTO对象
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        // 数据库中的JSON是数组格式，先尝试解析为Section列表
                        List<CurriculumDTO.SectionDTO> sections = objectMapper.readValue(
                            curriculumJson,
                            objectMapper.getTypeFactory().constructCollectionType(
                                List.class, CurriculumDTO.SectionDTO.class)
                        );
                        curriculumDTO.setSections(sections);
                        logger.info("Successfully fetched curriculum for major: {}", majorName);
                    } catch (Exception e) {
                        logger.error("Failed to parse curriculum JSON: {}", e.getMessage());
                        // JSON解析失败时，保持sections为null，但仍返回包含专业名称的对象
                    }
                } else {
                    logger.info("Curriculum JSON is empty for major: {}", majorName);
                }
            } else {
                logger.info("No curriculum data found for major: {}, returning curriculum with major name only", majorName);
                // 没有培养方案数据，但专业存在，返回只包含专业名称的对象
            }
        } catch (Exception e) {
            logger.error("Exception in getCurriculum: {}", e.getMessage());
            // 发生异常时，如果专业名称已设置，仍返回包含专业名称的对象
            if (curriculumDTO.getMajorName() == null) {
                curriculumDTO.setMajorName(majorName);
            }
        }

        return curriculumDTO;
    }

    @Override
    public CurriculumDTO getPersonalCurriculum(Integer studentId) {
        // 实现获取个人培养方案逻辑
        // 根据学生ID查询个人培养方案
        try {
            String sql = "SELECT curriculum_json FROM personal_curriculum WHERE student_id = ?";
            String curriculumJson = jdbcTemplate.queryForObject(sql, String.class, studentId);
            if (curriculumJson != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(curriculumJson, CurriculumDTO.class);
            }
        } catch (Exception e) {
            // 查询不到或反序列化失败时返回空对象
            return new CurriculumDTO();
        }
        return new CurriculumDTO();
    }

    @Override
    @Transactional
    public String setPersonalCurriculum(Integer studentId, CurriculumDTO curriculumDTO) {
        // 首先检查学生是否存在
        if (!studentDao.existsById(studentId)) {
            return "学生不存在";
        }
        
        // 实现设置个人培养方案逻辑
        // 根据学生ID和培养方案DTO进行个人培养方案的设置
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String curriculumJson = objectMapper.writeValueAsString(curriculumDTO);
            String sql = "INSERT INTO personal_curriculum (student_id, curriculum_json) VALUES (?, ?) ON DUPLICATE KEY UPDATE curriculum_json = VALUES(curriculum_json)";
            int rows = jdbcTemplate.update(sql, studentId, curriculumJson);
            return rows > 0 ? "success" : "插入表格失败";
        } catch (Exception e) {
            e.printStackTrace();
            return "数据库处理发生未知错误";
        }
    }

    @Override
    @Transactional
    public String applySupplementCourse(Integer studentId, Integer sectionId) {
        // 检查当前时间是否在补选时间段内
        if (!selectionTimeService.isInSecondSelectionTime()) {
            // logger.warn("Cannot apply for supplement course outside the second selection time period for student: {}", studentId);
            return "不在补选时间段内";
        }

        // 检查该学生是否存在
        if (!studentDao.existsById(studentId)) {
            logger.warn("Student not found with ID: {}", studentId);
            return "学生不存在";
        }
        
        // 检查课程是否存在
        if (!sectionDao.existsById(sectionId)) {
            logger.warn("Section not found with ID: {}", sectionId);
            return "课程不存在";
        }

        // 获取section对应的courseId（真正的课程ID）
        Section section = sectionDao.findById(sectionId);
        if (section == null) {
            logger.warn("Section not found with ID: {}", sectionId);
            return "课程不存在";
        }
        Integer realCourseId = section.getCourseId();
        
        // 检查学生是否已经选了该课程的任何开课
        if (courseSelectionDao.hasStudentSelectedCourse(studentId, realCourseId)) {
            logger.warn("Student {} has already selected course {}", studentId, realCourseId);
            return "你已经选了该课程，不需要申请补选";
        }

        // 检查学生是否已经申请过该课程
        try {
            String checkSql = "SELECT COUNT(1) FROM course_supplement WHERE student_id = ? AND section_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, sectionId);
            if (count != null && count > 0) {
                logger.warn("Student {} has already applied for supplement of section {}", studentId, sectionId);
                return "你已经申请过该课程的补选";
            }
        } catch (Exception e) {
            logger.error("Error checking existing supplement application: {}", e.getMessage());
        }
        
        try {
            // 保存补选申请记录
            boolean result = courseSupplementDao.saveSupplementApplication(studentId, sectionId);
            if (result) {
                logger.info("Successfully saved supplement application for student {} and section {}", studentId, sectionId);
                return "success";
            } else {
                logger.warn("Failed to save supplement application");
                return "保存补选申请失败";
            }
        } catch (Exception e) {
            // 处理异常情况
            logger.error("Error saving supplement application: {}", e.getMessage());
            return "保存补选申请记录异常: " + e.getMessage();
        }
    }

    @Override
    public SupplementResultListDTO getSupplementResult(Integer studentId) {
        // 实现获取学生补选结果的逻辑
        SupplementResultListDTO resultDTO = new SupplementResultListDTO();
        List<SupplementResultListDTO.ResultItem> resultList = new ArrayList<>();
        
        // 更新SQL语句，使用section表连接
        String sql = "SELECT cs.section_id, c.course_name, u.name as teacher_name, s.sec_time, cl.location, c.credit, cs.status "
                   + "FROM course_supplement cs "
                   + "JOIN section s ON cs.section_id = s.section_id "
                   + "JOIN course c ON s.course_id = c.course_id "
                   + "JOIN user u ON c.teacher_id = u.user_id "
                   + "JOIN classroom cl ON s.classroom_id = cl.classroom_id "
                   + "WHERE cs.student_id = ?";
                   
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(sql, studentId);
        
        for (java.util.Map<String, Object> row : rows) {
            SupplementResultListDTO.ResultItem item = new SupplementResultListDTO.ResultItem();
            item.setCourseId(row.get("section_id") != null ? ((Number)row.get("section_id")).intValue() : null);
            item.setCourseName((String)row.get("course_name"));
            item.setTeacherName((String)row.get("teacher_name"));
            item.setClassTime(row.get("sec_time") != null ? row.get("sec_time").toString() : null);
            item.setClassroom((String)row.get("location"));
            item.setCredit(row.get("credit") != null ? ((Number)row.get("credit")).doubleValue() : null);
            item.setResult(row.get("status") != null ? row.get("status").toString() : null);
            resultList.add(item);
        }
        
        resultDTO.setResultList(resultList);
        return resultDTO;
    }
}