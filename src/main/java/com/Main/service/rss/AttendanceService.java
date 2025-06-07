package com.Main.service.rss;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Main.entity.course_selection.Course; // 注意导入正确的Course实体类
import com.Main.entity.information.GradeComponent;

@Service
public class AttendanceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取教师所授课程列表（从Course表查询）
     * @param teacherName 教师姓名
     * @return 课程名称列表
     */

    /**
     * 完整考勤成绩处理（修改：接收studentId代替studentName）
     */
    @Transactional
    public boolean processAttendance(Integer studentId, String courseName, int attendanceScore, int attendanceRatio) {  // 修改参数类型
        // 1. 通过课程名获取course_id（保持不变）
        Integer courseId = getCourseIdByCourseName(courseName);
        if (courseId == null) {
            throw new RuntimeException("未找到对应课程: " + courseName);
        }

        // 3. 通过course_id和student_id获取grade_id（保持逻辑，参数调整为直接使用studentId）
        Integer gradeId = getGradeIdByCourseAndStudent(courseId, studentId);
        if (gradeId == null) {
            throw new RuntimeException("未找到该学生对应课程的成绩记录");
        }

        // 4. 更新GradeBase的score（原score + 考勤得分*考勤比例/100）
        updateGradeBaseScore(gradeId, attendanceScore, attendanceRatio);

        // 5. 插入新的GradeComponent记录
        insertGradeComponent(gradeId, attendanceScore, attendanceRatio);

        return true;
    }

    // 辅助方法：通过课程名获取course_id
    private Integer getCourseIdByCourseName(String courseName) {
        String sql = "SELECT course_id FROM Course WHERE course_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, courseName);
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：通过course_id和student_id获取grade_id
    private Integer getGradeIdByCourseAndStudent(int courseId, int studentId) {
        String sql = "SELECT grade_id FROM GradeBase WHERE course_id = ? AND student_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, courseId, studentId);
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：更新GradeBase的score
    private void updateGradeBaseScore(int gradeId, int attendanceScore, int attendanceRatio) {
        // 获取当前总得分
        String currentScoreSql = "SELECT score FROM GradeBase WHERE grade_id = ?";
        Integer currentScore = jdbcTemplate.queryForObject(currentScoreSql, Integer.class, gradeId);
        
        // 计算新增分数（考勤得分 * 比例%）
        int addedScore = (int) (attendanceScore * (attendanceRatio / 100.0));
        int newScore = currentScore + addedScore;

        // 更新总得分
        String updateSql = "UPDATE GradeBase SET score = ? WHERE grade_id = ?";
        jdbcTemplate.update(updateSql, newScore, gradeId);
    }

    // 辅助方法：插入或更新GradeComponent记录
    private void insertGradeComponent(int gradeId, int attendanceScore, int attendanceRatio) {

        // 1. 查询是否存在相同gradeId、考勤类型的成绩组件
        String selectSql = "SELECT component_id FROM GradeComponent WHERE grade_id = ? AND component_name = ? AND component_type = '0'";
        List<Integer> componentIds = jdbcTemplate.query(
            selectSql,
            new Object[]{gradeId, "考勤成绩"},
            (rs, rowNum) -> rs.getInt("component_id")
        );

        if (!componentIds.isEmpty()) {
            // 2. 存在记录则更新ratio和score
            Integer componentId = componentIds.get(0);
            String updateSql = "UPDATE GradeComponent SET ratio = ?, score = ? WHERE component_id = ?";
            jdbcTemplate.update(updateSql, attendanceRatio, attendanceScore, componentId);
        } else {
            // 3. 不存在记录则插入新记录
            GradeComponent component = new GradeComponent();
            component.setGradeId(gradeId);
            component.setComponentName("考勤成绩");

            component.setRatio(attendanceRatio);
            component.setScore(attendanceScore);

            String insertSql = "INSERT INTO GradeComponent (grade_id, component_name, component_type, ratio, score) VALUES (?, ?, '0', ?, ?)";
            jdbcTemplate.update(insertSql,
                    component.getGradeId(),
                    component.getComponentName(),

                    component.getRatio(),
                    component.getScore());
        }

    }

    /**
     * 新增：通过教师ID查询所授课程列表（从Course表查询）
     * @param teacherId 教师ID
     * @return 课程实体列表
     */
    public List<Course> getCoursesByTeacherId(Integer teacherId) {
        String sql = "SELECT * FROM Course WHERE teacher_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Course course = new Course();
            course.setCourseId(rs.getInt("course_id"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseDescription(rs.getString("course_description"));
            course.setTeacherId(rs.getInt("teacher_id"));
            
            return course;
        }, teacherId);
    }
}