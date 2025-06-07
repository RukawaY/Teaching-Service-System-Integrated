package com.Main.service.arrange;

import com.Main.dto.arrange.SectionDTO;
import com.Main.entity.arrange.Section;
import com.Main.entity.arrange.LessonScheduleFilter;

import java.util.List;

public interface AutoManualScheduler {
    public void generateSchedule(LessonScheduleFilter filter);
    public void addSchedule(Section section);
    public void deleteSchedule(int sectionId);
    public void updateSchedule(int sectionId, Section updateInfo);
    public String checkSchedule(String semester, int secYear);
    public List<SectionDTO> showSchedule(SectionDTO sectionFilter);
    public List<SectionDTO> showSchedule(SectionDTO sectionFilter,int teacherId);
}
