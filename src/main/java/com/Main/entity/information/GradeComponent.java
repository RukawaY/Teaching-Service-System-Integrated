package com.Main.entity.information;

public class GradeComponent {
    private int componentId;    // 组成ID (主键)
    private String componentName;// 组成部分名称 (例如: 平时作业, 期中考试, 期末考试)
    private int gradeId;        // 成绩ID (外键, 关联GradeBase表)
    private int componentType; // 组成类型 (例如: 考勤/测试/作业)
    private int ratio;         // 所占比例 (例如: 30 代表30%)
    private int score;          // 该组成部分得分

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getComponentType() {
        return componentType;
    }

    public void setComponentType(int componentType) {
        this.componentType = componentType;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("GradeComponent[componentId=%d, componentName='%s', gradeId=%d, componentType='%s', ratio=%d, score=%d]",
                componentId, componentName, gradeId, componentType, ratio, score);
    }
}
