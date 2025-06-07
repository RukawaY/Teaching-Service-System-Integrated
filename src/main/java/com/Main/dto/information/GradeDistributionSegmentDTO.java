package com.Main.dto.information;

public class GradeDistributionSegmentDTO {
    private String segment; // 区间
    private int count; // 该区间人数
    private double percentage; // 该区间百分比

    public GradeDistributionSegmentDTO() {}

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
