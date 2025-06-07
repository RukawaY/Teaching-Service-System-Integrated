import axios from 'axios';

// 设置后端的基础 API 地址
const API_BASE_URL = 'http://localhost:8080/user';

// 登录接口
export const login = (email, password) => {
    return axios.post(`${API_BASE_URL}/signin`, { email, password });
};

// 注册接口
export const register = (email, password, username, confirmpassword) => {
    return axios.post(`${API_BASE_URL}/register`, { email, password, username, confirmpassword });
};

// 退出登录接口
export const signout = () => {
    return axios.get(`${API_BASE_URL}/signout`);
};
