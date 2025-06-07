import apiClient from "./request";

// 获取课程列表
export const getCourses = async (params: any) => {
  return await apiClient.request({  
    url: "/courses",
    method: "GET",
    params: params,
  });
};

// 获取课程详情
export const getCourseDetails = async (courseId: any) => {
  return await apiClient.request({
    url: `/courses/${courseId}`,
    method: "GET",
  });
};

// 获取某课程的开课信息列表
export const getCourseSections = async (courseId: any, params: any) => {
  return await apiClient.request({
    url: `/courses/${courseId}/sections`,
    method: "GET",
    params: params,
  });
};
