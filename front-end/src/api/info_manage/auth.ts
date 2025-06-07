import apiClient from "./request";

export const login = async (credential: any) => {
  return await apiClient.request({
    url: "auth/login",
    method: "POST",
    data: credential,
  });
};
