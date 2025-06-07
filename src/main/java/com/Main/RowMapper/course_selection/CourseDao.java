package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.Course;
import java.util.List;

public interface CourseDao {
    Course findById(Integer courseId);
    boolean existsById(Integer courseId);
    
    /**
     * Search course table with optional filters
     * @param courseId course ID filter (optional, exact match)
     * @param courseName course name filter (optional, fuzzy match)
     * @param category category filter (optional, fuzzy match)
     * @return list of courses matching the criteria
     */
    List<Course> searchCourseTable(Integer courseId, String courseName, String category);
}