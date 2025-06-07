package com.Main.entity.arrange;

import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

public class LessonScheduleFilter {

    public enum Priority {
        teacher,
        equipment,
        continuity,
    } 

    public enum Constraint {
        avoidConsecutive,
        teacherGap,
        classroomConflict,
        classroomGap,
        avoidSingle,
        avoidWeekend,
    }
    
    private String semester;
    private int secYear;

 
    @JsonDeserialize(converter = StringToPriorityConverter.class)
    private List<Priority> priority;

    @JsonDeserialize(converter = StringToConstraintConverter.class)
    private List<Constraint> constraints;

    private List<Integer> courses;

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getSecYear() {
        return secYear;
    }

    public void setSecYear(int secYear) {
        this.secYear = secYear;
    }

    public List<Priority> getPriority() {
        return priority;
    }

    public void setPriority(List<Priority> priority) {
        this.priority = priority;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public List<Integer> getCourses() {
        return courses;
    }

    public void setCourses(List<Integer> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return "LessonScheduleFilter{" +
                "semester='" + semester + '\'' +
                ", secYear=" + secYear +
                ", priority=" + priority +
                ", constraints=" + constraints +
                ", courses=" + courses +
                '}';
    }
    

}

class StringToPriorityConverter extends StdConverter<List<String>, List<LessonScheduleFilter.Priority>> {
    @Override
    public List<LessonScheduleFilter.Priority> convert(List<String> value) {
        return value.stream()
            .map(LessonScheduleFilter.Priority::valueOf)
            .toList();
    }
}

class StringToConstraintConverter extends StdConverter<List<String>, List<LessonScheduleFilter.Constraint>> {
    @Override
    public List<LessonScheduleFilter.Constraint> convert(List<String> value) {
        return value.stream()
            .map(LessonScheduleFilter.Constraint::valueOf)
            .toList();
    }
}