import apiClient from "./request";

// 学生查询自己的成绩
export const getStudentGrades = async (params: any) => {
  return await apiClient.request({
    url: "/student/grades",
    method: "GET",
    params: params, 
  });
};

// 学生查询单科成绩详情
export const getStudentGradeDetails = async (gradeId: any) => {
  return await apiClient.request({
    url: `/student/grades/${gradeId}`,
    method: "GET",
  });
};

// 学生个人成绩分析
export const getStudentGradeAnalysis = async (params: any) => {
  return await apiClient.request({
    url: "/student/grades/analysis",
    method: "GET",
    params: params,
  });
};
