package com.Main.dto.information;

import com.Main.entity.information.GradeBase;
import com.Main.entity.information.GradeComponent;

import java.util.List;

public class GradeDTO {
    public GradeBase gradeBase;
    public List<GradeComponent> gradeComponent;

    public GradeDTO() {}
    public GradeBase getGradeBase() {
        return gradeBase;
    }

    public void setGradeBase(GradeBase gradeBase) {
        this.gradeBase = gradeBase;
    }

    public List<GradeComponent> getGradeComponent() {
        return gradeComponent;
    }

    public void setGradeComponent(List<GradeComponent> gradeComponent) {
        this.gradeComponent = gradeComponent;
    }


}
