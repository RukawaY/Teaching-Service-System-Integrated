package com.Main.dto.information;

import java.util.List;

import com.Main.entity.information.Section;

public class SectionSearchListDTO {
    private List<String> classroom_location;
    private List<Section> SectionInfo;

    // 构造函数
    public SectionSearchListDTO() {
    }

    public SectionSearchListDTO(List<Section> sectionInfo, List<String> classroom_location) {
        this.SectionInfo = sectionInfo;
        this.classroom_location = classroom_location;
    }

    //get and set
    public List<Section> getSection() {
        return this.SectionInfo;
    }

    public void setSection(List<Section> sectionInfo) {
        this.SectionInfo = sectionInfo;
    }

    public List<String> getClassroom_location() {
        return classroom_location;
    }

    public void setClassroom_location(List<String> classroom_location) {
        this.classroom_location = classroom_location;
    }
}
