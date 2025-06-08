package com.Main.service.arrange;

import com.Main.entity.arrange.Section;
import com.Main.entity.arrange.LessonScheduleFilter;
import com.Main.entity.arrange.Course;
import com.Main.entity.arrange.Classroom;
import com.Main.RowMapper.arrange.SectionRowMapper;
import com.Main.dto.arrange.SectionDTO;
import com.Main.RowMapper.arrange.ClassroomRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.Main.RowMapper.arrange.CourseRowMapper;
@Component
public class LessonScheduler implements AutoManualScheduler, ClassroomManager {
    
    //logger
    private static final Logger logger = LoggerFactory.getLogger(LessonScheduler.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void deleteCurrentSchedule(String semester, int secYear) {
        String sql = "DELETE FROM section WHERE semester = ? AND sec_year = ?";
        jdbcTemplate.update(sql, semester, secYear);
    }

    private class Arrangement implements Comparable<Arrangement> {
        //周几
        public enum Week{
            MONDAY(1, "Monday"),
            TUESDAY(2, "Tuesday"),
            WEDNESDAY(3, "Wednesday"),
            THURSDAY(4, "Thursday"),
            FRIDAY(5, "Friday"),
            SATURDAY(6, "Saturday"),
            SUNDAY(7, "Sunday");

            private final int value;
            private final String name;

            Week(int value, String name) {
                this.value = value;
                this.name = name;
            }

            public int getValue() {
                return value;
            }

            public String getName() {
                return name;
            }

            public static Week fromInt(int value) {
                for (Week week : Week.values()) {
                    if (week.value == value) {
                        return week;
                    }
                }
                throw new IllegalArgumentException("Invalid week value: " + value);
            }

            public static Week fromString(String name) {
                for (Week week : Week.values()) {
                    if (week.name.equalsIgnoreCase(name)) {
                        return week;
                    }
                }
                throw new IllegalArgumentException("Invalid week name: " + name);
            }
        };
        //节数
        Week week;
        public String time;//"1,2,3"
        public double arrangedCount;//当前时间点已经安排的课程数(可能是三节课的平均值)
        Arrangement(Week week, String time, double arrangedCount){
            this.week = week;
            this.time = time;
            this.arrangedCount = arrangedCount;
        }
        Arrangement(int week, String time, double arrangedCount){
            this.week = Week.fromInt(week);
            this.time = time;
            this.arrangedCount = arrangedCount;
        }
        //从小到大排序
        @Override
        public int compareTo(Arrangement o) {
            return Double.compare(this.arrangedCount, o.arrangedCount);
        }
    }

    private void addPossibleTimeToHeap(PriorityQueue<Arrangement> heap,int hoursPerWeek, int[][] record, boolean avoidWeekend) {
        //根据连堂节数判断可以安排的时间
        //1.连堂三节，可以安排的时间为：
        //   - 上午：3 4 5
        //   - 下午：6 7 8
        //   - 晚上：11 12 13
        if(hoursPerWeek == 3){
            for(int i = 1; i <= 5; i++){
                heap.add(new Arrangement(i, "3,4,5", (record[i][3] + record[i][4] + record[i][5]) / 3.0));
                heap.add(new Arrangement(i, "6,7,8", (record[i][6] + record[i][7] + record[i][8]) / 3.0));
                heap.add(new Arrangement(i, "11,12,13", (record[i][11] + record[i][12] + record[i][13]) / 3.0));
            }
            if(!avoidWeekend){
                //周末降低优先级
                for(int i = 6; i <= 7; i++){
                    heap.add(new Arrangement(i, "3,4,5", (record[i][3] + record[i][4] + record[i][5]) / 3.0 * 10 + 10));
                    heap.add(new Arrangement(i, "6,7,8", (record[i][6] + record[i][7] + record[i][8]) / 3.0 * 10 + 10));
                    heap.add(new Arrangement(i, "11,12,13", (record[i][11] + record[i][12] + record[i][13]) / 3.0 * 10 + 10)); 
                }
            }
        }


        //2.连堂两节，可以安排的时间为：
        //   - 上午：1 2
        //   - 上午：3 4
        //   - 下午：6 7
        //   - 下午：7 8
        //   - 晚上：11 12
        if(hoursPerWeek == 2){
            for(int i = 1; i <= 5; i++){
                heap.add(new Arrangement(i, "1,2", (record[i][1] + record[i][2]) / 2.0));
                heap.add(new Arrangement(i, "3,4", (record[i][3] + record[i][4]) / 2.0));
                heap.add(new Arrangement(i, "6,7", (record[i][6] + record[i][7]) / 2.0));
                heap.add(new Arrangement(i, "7,8", (record[i][7] + record[i][8]) / 2.0));
                heap.add(new Arrangement(i, "11,12", (record[i][11] + record[i][12]) / 2.0));
            }
            if(!avoidWeekend){
                //周末降低优先级
                for(int i = 6; i <= 7; i++){
                    heap.add(new Arrangement(i, "1,2", (record[i][1] + record[i][2]) / 2.0 * 10 + 10));
                    heap.add(new Arrangement(i, "3,4", (record[i][3] + record[i][4]) / 2.0 * 10 + 10));
                    heap.add(new Arrangement(i, "6,7", (record[i][6] + record[i][7]) / 2.0 * 10 + 10));
                    heap.add(new Arrangement(i, "7,8", (record[i][7] + record[i][8]) / 2.0 * 10 + 10));
                    heap.add(new Arrangement(i, "11,12", (record[i][11] + record[i][12]) / 2.0 * 10 + 10));
                }
            }
        }
        //3.连堂一节，可以安排的时间为：
        //   1-13 均可
        if(hoursPerWeek == 1){
            for(int i = 1; i <= 5; i++){
                for(int j = 1; j <= 13; j++){
                    heap.add(new Arrangement(i, String.valueOf(j), (record[i][j]) / 1.0));
                }
            }
            for(int i = 6; i <= 7; i++){
                for(int j = 1; j <= 13; j++){
                    heap.add(new Arrangement(i, String.valueOf(j), (record[i][j]) / 1.0 * 10 + 10));
                }
            }
        }
        
        //4.连堂四节，可以安排的时间为：
        // 1234 6789
        if(hoursPerWeek == 4){
            for(int i = 1; i <= 5; i++){
                heap.add(new Arrangement(i, "1,2,3,4", (record[i][1] + record[i][2] + record[i][3] + record[i][4]) / 4.0));
                heap.add(new Arrangement(i, "6,7,8,9", (record[i][6] + record[i][7] + record[i][8] + record[i][9]) / 4.0));
            }
            if(!avoidWeekend){
                //周末降低优先级
                for(int i = 6; i <= 7; i++){
                    heap.add(new Arrangement(i, "1,2,3,4", (record[i][1] + record[i][2] + record[i][3] + record[i][4]) / 4.0 * 10 + 10));
                    heap.add(new Arrangement(i, "6,7,8,9", (record[i][6] + record[i][7] + record[i][8] + record[i][9]) / 4.0 * 10 + 10));
                }
            }
        }

        //5.连堂五节，可以安排的时间为：
        // 12345 678910
        if(hoursPerWeek == 5){
            for(int i = 1; i <= 5; i++){
                heap.add(new Arrangement(i, "1,2,3,4,5", (record[i][1] + record[i][2] + record[i][3] + record[i][4] + record[i][5]) / 5.0));
                heap.add(new Arrangement(i, "6,7,8,9,10", (record[i][6] + record[i][7] + record[i][8] + record[i][9] + record[i][10]) / 5.0));
            }
            if(!avoidWeekend){
                //周末降低优先级
                for(int i = 6; i <= 7; i++){
                    heap.add(new Arrangement(i, "1,2,3,4,5", (record[i][1] + record[i][2] + record[i][3] + record[i][4] + record[i][5]) / 5.0 * 10 + 10));
                    heap.add(new Arrangement(i, "6,7,8,9,10", (record[i][6] + record[i][7] + record[i][8] + record[i][9] + record[i][10]) / 5.0 * 10 + 10));
                }
            }
        }

        if(hoursPerWeek > 5){
            throw new RuntimeException("连堂节数超过5节");
        }
    }

    private List<Section> arrangeCourse(List<Course>courses, LessonScheduleFilter filter){
        var semester = filter.getSemester();
        var secYear = filter.getSecYear();
        boolean avoidConsecutive = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidConsecutive);
        boolean teacherGap = filter.getConstraints().contains(LessonScheduleFilter.Constraint.teacherGap);
        boolean classroomConflict = filter.getConstraints().contains(LessonScheduleFilter.Constraint.classroomConflict);
        boolean classroomGap = filter.getConstraints().contains(LessonScheduleFilter.Constraint.classroomGap);
        boolean avoidSingle = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidSingle);
        boolean avoidWeekend = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidWeekend);
        boolean teacherPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.teacher);
        boolean equipmentPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.equipment);
        boolean continuityPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.continuity);

        //按照hoursPerWeek排序courses
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course c1, Course c2) {
                return Integer.compare(c2.getHoursPerWeek(), c1.getHoursPerWeek());
            }
        });
        
        int[][] record = new int[8][20];
        Map<Integer, boolean[][]> teacherTimeMap = new HashMap<>();
        List<Section> sections = new ArrayList<>();
        
        for (Course course : courses) {
            int teacherId = course.getTeacherId();
            if (!teacherTimeMap.containsKey(teacherId)) {
                teacherTimeMap.put(teacherId, new boolean[8][20]);
                boolean[][] teacherTime = teacherTimeMap.get(teacherId);
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 20; j++) {
                        teacherTime[i][j] = false;
                    }
                }
            }
            var heap = new PriorityQueue<Arrangement>();
            addPossibleTimeToHeap(heap, course.getHoursPerWeek(), record, avoidWeekend);
            boolean successArrange = false;
            do {
                Arrangement times = heap.poll();
                var teacherTime = teacherTimeMap.get(teacherId);
                var timeList = times.time.split(",");
                boolean conflict = false;
                for(String time : timeList){
                    if(teacherTime[times.week.getValue()][Integer.parseInt(time)]){
                        conflict = true;
                        break;
                    }
                }
                if(!conflict){
                    String finalTime = "";
                    for(String time : timeList){
                        teacherTime[times.week.getValue()][Integer.parseInt(time)] = true;
                        record[times.week.getValue()][Integer.parseInt(time)] += 1;
                        finalTime += times.week.getName() + " " + time + "; ";
                    }
                    finalTime = finalTime.substring(0, finalTime.length() - 2);
                    sections.add(new Section(course.getId(), -1, -1, semester, secYear, finalTime, -1));
                    successArrange = true;
                }
            } while (!heap.isEmpty() && !successArrange);
            if(!successArrange){
                throw new RuntimeException("无法安排课程，课程id：" + course.getId() + "教师冲突");
            }
        }
        logger.info("安排教师完成");

        //安排教室 - 按课程分组，同一课程的所有section使用同一教室
        Map<Integer, boolean[][]> classroomTimeMap = new HashMap<>();
        List<Classroom> classrooms = queryClassrooms(new Classroom());
        Map<Integer, Integer> courseClassroomMap = new HashMap<>();  // 记录每个课程分配的教室
        
        // 按课程ID对sections进行分组
        Map<Integer, List<Section>> sectionsByCourse = new HashMap<>();
        for(Section section : sections){
            sectionsByCourse.computeIfAbsent(section.getCourseId(), k -> new ArrayList<>()).add(section);
        }
        
        // 为每个课程的所有section分配同一个教室
        for(Map.Entry<Integer, List<Section>> entry : sectionsByCourse.entrySet()){
            int courseId = entry.getKey();
            List<Section> courseSections = entry.getValue();
            boolean successArrange = false;
            String sectionCategory = getCourseCategory(courseId);
            
            // 尝试为当前课程的所有section找到合适的教室
            for(Classroom classroom : classrooms){
                if(!classroom.getCategory().equals(sectionCategory)){
                    continue;
                }
                
                if(!classroomTimeMap.containsKey(classroom.getId())){
                    classroomTimeMap.put(classroom.getId(), new boolean[8][20]);
                    boolean[][] classroomTime = classroomTimeMap.get(classroom.getId());
                    for(int i = 0; i < 8; i++){
                        for(int j = 0; j < 20; j++){
                            classroomTime[i][j] = false;
                        }
                    }
                }
                
                var classroomTime = classroomTimeMap.get(classroom.getId());
                boolean conflict = false;
                
                // 检查当前教室是否能容纳该课程的所有section
                for(Section section : courseSections){
                    var dayStringList = section.getSecTime().split("; ");
                    for(String dayString : dayStringList){
                        var day = Arrangement.Week.fromString(dayString.split(" ")[0]).getValue();
                        var timeList = dayString.split(" ")[1].split(",");
                        for(String time : timeList){
                            if(classroomTime[day][Integer.parseInt(time)]){
                                conflict = true;
                                break;
                            }
                        }
                        if(conflict) break;
                    }
                    if(conflict) break;
                }
                
                // 如果没有冲突，则为所有section分配该教室
                if(!conflict){
                    for(Section section : courseSections){
                        section.setClassroomId(classroom.getId());
                        section.setCapacity(classroom.getCapacity());
                        section.setAvailableCapacity(classroom.getCapacity());
                        
                        // 标记时间为已占用
                        var dayStringList = section.getSecTime().split("; ");
                        for(String dayString : dayStringList){
                            var day = Arrangement.Week.fromString(dayString.split(" ")[0]).getValue();
                            var timeList = dayString.split(" ")[1].split(",");
                            for(String time : timeList){
                                classroomTime[day][Integer.parseInt(time)] = true;
                            }
                        }
                    }
                    courseClassroomMap.put(courseId, classroom.getId());
                    successArrange = true;
                    break;
                }
            }
            
            if(!successArrange){
                throw new RuntimeException("无法安排课程，课程id：" + courseId + "教室冲突");
            }
        }
        logger.info("安排教室完成");
        
        // 合并相同课程号、相同开课学年和学期的 section
        Map<String, List<Section>> sectionGroups = new HashMap<>();
        for (Section section : sections) {
            String key = section.getCourseId() + "_" + section.getSemester() + "_" + section.getSecYear();
            sectionGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(section);
        }
        
        List<Section> mergedSections = new ArrayList<>();
        for (List<Section> group : sectionGroups.values()) {
            if (group.size() == 1) {
                mergedSections.add(group.get(0));
            } else {
                // 合并多个 section
                Section mergedSection = group.get(0); // 使用第一个作为基础
                StringBuilder mergedTime = new StringBuilder(mergedSection.getSecTime());
                
                for (int i = 1; i < group.size(); i++) {
                    mergedTime.append("; ").append(group.get(i).getSecTime());
                }
                
                mergedSection.setSecTime(mergedTime.toString());
                mergedSections.add(mergedSection);
            }
        }
        logger.info("合并相同课程的section完成");
        
        return mergedSections;
    }

    private void addSections(List<Section> sections){
        for(Section section : sections){
            addSchedule(section);
        }
    }

    @Override
    public void generateSchedule(LessonScheduleFilter filter) {

        List<Integer> courseIds = filter.getCourses();
        List<Course> undistributedCourses = new ArrayList<>();
        for(Integer courseId : courseIds){
            //从course表中查询courseId对应的课程
            String sql = "SELECT * FROM course WHERE course_id = ?";
            Course course = jdbcTemplate.queryForObject(sql, new CourseRowMapper(), courseId);
            undistributedCourses.add(course);
        }
        var semester = filter.getSemester();
        var secYear = filter.getSecYear();
        // 排课之前，删除掉当前学期、学年的课程安排
        deleteCurrentSchedule(semester, secYear);
        List<Course>courses = new ArrayList<>();

        //获取排课约束
        boolean avoidConsecutive = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidConsecutive);
        boolean teacherGap = filter.getConstraints().contains(LessonScheduleFilter.Constraint.teacherGap);
        boolean classroomConflict = filter.getConstraints().contains(LessonScheduleFilter.Constraint.classroomConflict);
        boolean classroomGap = filter.getConstraints().contains(LessonScheduleFilter.Constraint.classroomGap);
        boolean avoidSingle = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidSingle);
        boolean avoidWeekend = filter.getConstraints().contains(LessonScheduleFilter.Constraint.avoidWeekend);
        boolean teacherPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.teacher);
        boolean equipmentPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.equipment);
        boolean continuityPriority = filter.getPriority().contains(LessonScheduleFilter.Priority.continuity);

        for (Course course : undistributedCourses) {
            if(avoidConsecutive && course.getHoursPerWeek() == 4){
                courses.add(new Course(course, 2));
                courses.add(new Course(course, 2));
            }
            else if(avoidConsecutive && course.getHoursPerWeek() == 5){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 2));
            }
            else if(course.getHoursPerWeek() == 6){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 3));
            }
            else if(course.getHoursPerWeek() == 7){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 2));
                courses.add(new Course(course, 2));
            }
            else {
                courses.add(course);
            }
        }
        
        try{
            List<Section> sections = arrangeCourse(courses, filter);
            addSections(sections);
            return;
        }
        catch(Exception e){
            logger.error("第一次排课失败: {}", e.getMessage());
        }
        
        //如果第一次排课失败，则尝试调整课程安排
        courses.clear();

        for (Course course : undistributedCourses) {
            if(course.getHoursPerWeek() == 4){
                courses.add(new Course(course, 2));
                courses.add(new Course(course, 2));
            }
            else if(course.getHoursPerWeek() == 5){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 2));
            }
            else if(course.getHoursPerWeek() == 6){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 3));
            }
            else if(course.getHoursPerWeek() == 7){
                courses.add(new Course(course, 3));
                courses.add(new Course(course, 2));
                courses.add(new Course(course, 2));
            }
            else {
                courses.add(course);
            }
        }

        try{
            List<Section> sections = arrangeCourse(courses, filter);
            addSections(sections);
            return;
        }
        catch(Exception e){
            logger.error("第二次排课失败: {}", e.getMessage());
        }

        throw new RuntimeException("排课失败");
    }

    private String getCourseCategory(int courseId){
        String sql = "SELECT category FROM course WHERE course_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, courseId);
    }

    public List<Course> showCourses(){
        String sql = "SELECT * FROM course";
        return jdbcTemplate.query(sql, new CourseRowMapper());
    }

    @Override
    public void addSchedule(Section section) {
        String sql = "INSERT INTO section (course_id, classroom_id, capacity, semester, sec_year, sec_time, available_capacity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            section.getCourseId(),
            section.getClassroomId(),
            section.getCapacity(),
            section.getSemester(),
            section.getSecYear(),
            section.getSecTime(),
            section.getAvailableCapacity()
        );
    }

    @Override
    public void deleteSchedule(int sectionId) {
        String sql = "DELETE FROM section WHERE section_id = ?";
        jdbcTemplate.update(sql, sectionId);
    }

    @Override
    public void updateSchedule(int sectionId, Section updateInfo) {
        String sql = "UPDATE section SET course_id = ?, classroom_id = ?, capacity = ?, semester = ?, sec_year = ?, sec_time = ?, available_capacity = ? WHERE section_id = ?";
        jdbcTemplate.update(sql,
            updateInfo.getCourseId(),
            updateInfo.getClassroomId(),
            updateInfo.getCapacity(),
            updateInfo.getSemester(),
            updateInfo.getSecYear(),
            updateInfo.getSecTime(),
            updateInfo.getAvailableCapacity(),
            sectionId
        );
    }

    @Override
    public String checkSchedule(String semester, int secYear) {
        //检查教师和教室是否有同一时间上两节课的冲突
        var sectionFilter = new SectionDTO(new Section());
        sectionFilter.setSemester(semester);
        sectionFilter.setSecYear(secYear);
        var sectionList = showSchedule(sectionFilter);

        Map<Integer, boolean[][]> teacherTimeMap = new HashMap<>();
        Map<Integer, boolean[][]> classroomTimeMap = new HashMap<>();
        for(Section section : sectionList){
            var teacherId = section.getCourseId();
            var classroomId = section.getClassroomId();
            if(!teacherTimeMap.containsKey(teacherId)){
                teacherTimeMap.put(teacherId, new boolean[8][20]);
                boolean[][] teacherTime = teacherTimeMap.get(teacherId);
                for(int i = 0; i < 8; i++){
                    for(int j = 0; j < 20; j++){
                        teacherTime[i][j] = false;
                    }
                }
            }
            if(!classroomTimeMap.containsKey(classroomId)){
                classroomTimeMap.put(classroomId, new boolean[8][20]);
                boolean[][] classroomTime = classroomTimeMap.get(classroomId);
                for(int i = 0; i < 8; i++){
                    for(int j = 0; j < 20; j++){
                        classroomTime[i][j] = false;
                    }
                }
            }
            var secTime = section.getSecTime();
            var dayStringList = secTime.split("; ");
            for(String dayString : dayStringList){
                var day = Arrangement.Week.fromString(dayString.split(" ")[0]).getValue();
                var timeList = dayString.split(" ")[1].split(",");
                for(String time : timeList){
                    if(teacherTimeMap.get(teacherId)[day][Integer.parseInt(time)]){
                        logger.error("教师{}在星期{},第{}节课有冲突", teacherId, day, time);
                        return "教师" + getTeacherNameByCourseId(teacherId) + "在星期" + day + "第" + time + "节课有冲突";
                    }
                    if(classroomTimeMap.get(classroomId)[day][Integer.parseInt(time)]){
                        logger.error("教室{}在星期{},第{}节课有冲突", classroomId, day, time);
                        return "教室" + getClassroomNameByClassroomId(classroomId) + "在星期" + day + "第" + time + "节课有冲突";
                    }
                    teacherTimeMap.get(teacherId)[day][Integer.parseInt(time)] = true;
                    classroomTimeMap.get(classroomId)[day][Integer.parseInt(time)] = true;
                }
            }
        }
        return "没有冲突";
    }

    private String getCourseNameByCourseId(int courseId){
        String sql = "SELECT course_name FROM course WHERE course_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, courseId);
    }

    private String getTeacherNameByCourseId(int courseId){
        String sql = "SELECT u.name FROM user u JOIN course c ON u.user_id = c.teacher_id WHERE c.course_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, courseId);
    }

    private int getTeacherIdByCourseId(int courseId){
        logger.info("getTeacherIdByCourseId: {}", courseId);
        String sql = "SELECT teacher_id FROM course WHERE course_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, courseId);
    }

    private String getClassroomNameByClassroomId(int classroomId){
        String sql = "SELECT location FROM classroom WHERE classroom_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, classroomId);
    }

    @Override
    public List<SectionDTO> showSchedule(SectionDTO sectionFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM section WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (sectionFilter.getId() != -1) {
            sql.append(" AND section_id = ?");
            params.add(sectionFilter.getId());
        }
        if (sectionFilter.getCourseId() != -1) {
            sql.append(" AND course_id = ?");
            params.add(sectionFilter.getCourseId());
        }
        if (sectionFilter.getClassroomId() != -1) {
            sql.append(" AND classroom_id = ?");
            params.add(sectionFilter.getClassroomId());
        }
        if (sectionFilter.getSemester() != null) {
            sql.append(" AND semester = ?");
            params.add(sectionFilter.getSemester());
        }
        if (sectionFilter.getSecYear() != -1) {
            sql.append(" AND sec_year = ?");
            params.add(sectionFilter.getSecYear());
        }
        if (sectionFilter.getSecTime() != null) {
            sql.append(" AND sec_time = ?");
            params.add(sectionFilter.getSecTime());
        }
        if (sectionFilter.getCapacity() != -1) {
            sql.append(" AND capacity = ?");
            params.add(sectionFilter.getCapacity());
        }
        List<Section> sections = jdbcTemplate.query(sql.toString(), params.toArray(), new SectionRowMapper());
        List<SectionDTO> sectionDTOs = new ArrayList<>();
        for(Section section : sections){
            SectionDTO sectionDTO = new SectionDTO(section);
            sectionDTO.setCourseName(getCourseNameByCourseId(section.getCourseId()));
            sectionDTO.setTeacherName(getTeacherNameByCourseId(section.getCourseId()));
            sectionDTO.setClassroomName(getClassroomNameByClassroomId(section.getClassroomId()));
            sectionDTOs.add(sectionDTO);
        }
        return sectionDTOs;
    }

    @Override
    public List<SectionDTO> showSchedule(SectionDTO sectionFilter,int teacherId) {
        List<SectionDTO> sectionDTOs = showSchedule(sectionFilter);
        List<SectionDTO> filteredSectionDTOs = new ArrayList<>();
        for(SectionDTO sectionDTO : sectionDTOs){
            if(getTeacherIdByCourseId(sectionDTO.getCourseId()) == teacherId){
                filteredSectionDTOs.add(sectionDTO);
            }
        }
        return filteredSectionDTOs;
    }

    @Override
    public void addClassroom(Classroom classroom) {
        String sql = "INSERT INTO classroom (location, capacity, category) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, classroom.getLocation(), classroom.getCapacity(), classroom.getCategory());
    }

    @Override
    public void deleteClassroom(int classroomId) {
        String sql = "DELETE FROM classroom WHERE classroom_id = ?";
        jdbcTemplate.update(sql, classroomId);
    }

    @Override
    public void updateClassroom(int classroomId, Classroom updateInfo) {
        String sql = "UPDATE classroom SET location = ?, capacity = ?, category = ? WHERE classroom_id = ?";
        jdbcTemplate.update(sql, updateInfo.getLocation(), updateInfo.getCapacity(), updateInfo.getCategory(), classroomId);
    }

    @Override
    public List<Classroom> queryClassrooms(Classroom classroomFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM classroom WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (classroomFilter.getId() != -1) {
            sql.append(" AND classroom_id = ?");
            params.add(classroomFilter.getId());
        }
        if (classroomFilter.getLocation() != null) {
            sql.append(" AND location LIKE ?");
            params.add("%" + classroomFilter.getLocation() + "%");
        }
        if (classroomFilter.getCapacity() > 0) {
            sql.append(" AND capacity >= ?");
            params.add(classroomFilter.getCapacity());
        }
        if (classroomFilter.getCategory() != null) {
            sql.append(" AND category = ?");
            params.add(classroomFilter.getCategory());
        }
        return jdbcTemplate.query(sql.toString(), params.toArray(), new ClassroomRowMapper());
    }
}
