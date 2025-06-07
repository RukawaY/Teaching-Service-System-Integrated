package com.Main.util.rss;

public class AttendanceUtils {
    /**
     * 计算考勤贡献的分数（考勤得分 * 比例%）
     * @param attendanceScore 考勤得分（0-100）
     * @param attendanceRatio 考勤比例（0-100）
     * @return 实际贡献分数（整数）
     */
    public static int calculateContributedScore(int attendanceScore, int attendanceRatio) {
        return (int) (attendanceScore * (attendanceRatio / 100.0));
    }

    /**
     * 校验基础参数是否合法
     * @param studentId 学生ID（非空）
     * @param courseName 课程名称（非空）
     */
    public static void validateBaseParams(Integer studentId, String courseName) {  // 修改参数类型
        if (studentId == null) {  // 校验学生ID非空
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
    }
}