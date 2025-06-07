package com.Main.dto.information;

import java.util.List;

import com.Main.entity.information.Section;

public class SectionAnalyseDTO {

    private Section section;
    private double averageScore; // 平均分
    private double highestScore; // 最高分
    private double lowestScore; // 最低分
    private double medianScore; // 中位数
    private double passRate; // 及格率
    private double excellentRate; // 优秀率
    private List<GradeDistributionSegmentDTO> gradeDistributionSegments; // 成绩分布区间
    private List<ScoreRankingDTO> scoreRanking; // 学生排名

    public SectionAnalyseDTO() {}

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(double highestScore) {
        this.highestScore = highestScore;
    }

    public double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public double getMedianScore() {
        return medianScore;
    }

    public void setMedianScore(double medianScore) {
        this.medianScore = medianScore;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public double getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(double excellentRate) {
        this.excellentRate = excellentRate;
    }

    public List<GradeDistributionSegmentDTO> getGradeDistributionSegments() {
        return gradeDistributionSegments;
    }

    public void setGradeDistributionSegments(List<GradeDistributionSegmentDTO> gradeDistributionSegments) {
        this.gradeDistributionSegments = gradeDistributionSegments;
    }

    public List<ScoreRankingDTO> getScoreRanking() {
        return scoreRanking;
    }

    public void setScoreRanking(List<ScoreRankingDTO> scoreRanking) {
        this.scoreRanking = scoreRanking;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}

