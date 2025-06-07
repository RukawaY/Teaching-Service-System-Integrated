import apiClient from "./request";

// 教师提交学生成绩
export const submitStudentGrades = async (sectionId: any, data: any) => {
  return await apiClient.request({
    url: `/teacher/sections/${sectionId}/grades`,
    method: "POST",
    data: data,
  });
};

// 教师查询其开课班级的学生成绩列表
export const getSectionGrades = async (sectionId: any, params: any) => {
  return await apiClient.request({
    url: `/teacher/sections/${sectionId}/grades`,
    method: "GET",
    params: params,
  });
};

// 教师对某开课班级进行成绩分析
export const getSectionGradeAnalysis = async (sectionId: any) => {
  return await apiClient.request({
    url: `/teacher/sections/${sectionId}/grades/analysis`,
    method: "GET",
  });
};

// 教师提交成绩修改申请
export const submitGradeApply = async (data: any) => {
  return await apiClient.request({
    url: "/teacher/grade-applies",
    method: "POST",
    data: data,
  });
};

// 教师查询其提交的申请列表
export const getTeacherGradeApplies = async (params: any) => {
  return await apiClient.request({
    url: "/teacher/grade-applies",
    method: "GET",
    params: params,
  });
};

// 教师创建课程
export const createCourse = async (data: any) => {
  return await apiClient.request({
    url: "/teacher/courses",
    method: "POST",
    data: data,
  });
};

// 教师修改其课程信息
export const updateCourse = async (courseId: any, data: any) => {
  return await apiClient.request({
    url: `/teacher/courses/${courseId}`,
    method: "PUT",
    data: data,
  });
};

// 教师删除其课程
export const deleteCourse = async (courseId: any) => {
  return await apiClient.request({
    url: `/teacher/courses/${courseId}`,
    method: "DELETE",
  });
};

// 教师为其课程创建开课信息
export const createSection = async (courseId: any, data: any) => {
  return await apiClient.request({
    url: `/teacher/courses/${courseId}/sections`,
    method: "POST",
    data: data,
  });
};

// 教师修改其开课信息
export const updateSection = async (sectionId: any, data: any) => {
  return await apiClient.request({
    url: `/teacher/sections/${sectionId}`,
    method: "PUT",
    data: data,
  });
};

// 教师删除其开课信息
export const deleteSection = async (sectionId: any) => {
  return await apiClient.request({
    url: `/teacher/sections/${sectionId}`,
    method: "DELETE",
  });
};

// 教师获取其教授的课程列表
export const getMyCourses = async (params: any) => {
  return await apiClient.request({
    url: "/teacher/my-courses",
    method: "GET",
    params: params,
  });
};

// 教师获取其某个课程的开课列表
export const getMyCourseSections = async (courseId: any, params: any) => {
  return await apiClient.request({
    url: `/teacher/my-courses/${courseId}/sections`,
    method: "GET",
    params: params,
  });
};

// 教师获取可用的教室列表
export const getAvailableClassroom = async (filter: any) => {
  return await apiClient.request({
    url: "/teacher/classrooms",
    method: "GET",
    params: filter,
  });
}