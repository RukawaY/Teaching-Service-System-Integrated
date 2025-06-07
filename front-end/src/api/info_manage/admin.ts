import apiClient from "./request";

// 获取用户列表接口
export const getUsers = async (params: any) => {
  return await apiClient.request({
    url: "admin/users",
    method: "GET",
    params: params,
  });
};

// 创建用户接口
export const createUser = async (data: any) => {
  return await apiClient.request({
    url: "admin/users",
    method: "POST",
    data: data,
  });
};

// 批量导入用户接口 未实现

// 获取指定用户信息
export const getUsersById = async (user_id: string) => {
  return await apiClient.request({
    url: `admin/users/${user_id}`,
    method: "GET",
  });
};

// 修改指定用户信息
export const updateUser = async (user_id: string, data: any) => {
  return await apiClient.request({
    url: `admin/users/${user_id}`,
    method: "PUT",
    data: data,
  });
};

// 删除指定用户信息
export const deleteUser = async (user_id: string) => {
  return await apiClient.request({
    url: `admin/users/${user_id}`,
    method: "DELETE",
  });
};

// 重置用户密码 未实现

// 5.9.3 管理员查询成绩修改申请列表
export const getAdminGradeApplies = async (params = {}) => {
  return await apiClient.request({
    url: "/admin/grade-applies",
    method: "GET",
    params: params,
  });
};

// 5.9.4 管理员审核成绩修改申请
export const reviewGradeApply = async (apply_id: string, data: any) => {
  return await apiClient.request({
    url: `/admin/grade-applies/${apply_id}/review`,
    method: "PUT",
    data: data,
  });
};
