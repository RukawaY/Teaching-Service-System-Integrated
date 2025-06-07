import apiClient from "./request";

//获取当前用户信息
export const getCurrentUserProfile = async () => {
  return await apiClient.request({
    url: "account/profile",
    method: "GET",
  });
};

// 修改当前用户信息
export const updateCurrentUserProfile = async (params: any) => {
  return await apiClient.request({
    url: "account/profile",
    method: "PUT",
    data: params,
  });
};

// 修改当前用户密码
export const updateCurrentUserPassword = async (params: any) => {
  return await apiClient.request({
    url: "account/password",
    method: "PUT",
    data: params,
  });
};

export const uploadAvatar = async (params: any) => {
  return await apiClient.request({
    headers: {
      "Content-Type": "multipart/form-data",
    },
    url: "account/avatar",
    method: "POST",
    data: params,
  });
};
