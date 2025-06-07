package com.Main.dto.information;


import java.util.List;

import com.Main.entity.information.User;

public class SectionGradeDTO {
    public List<User> user;
    public List<GradeDTO> grade;

    public SectionGradeDTO() {
    }

    public List<User> getUser() {
        return user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    public List<GradeDTO> getGrade() {
        return grade;
    }

    public void setGrade(List<GradeDTO> grade) {
        this.grade = grade;
    }


}
