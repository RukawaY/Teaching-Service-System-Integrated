import axios from "axios";
// import { useuserLoginStore } from "@/store/userLoginStore";
//
// const loginUser = useuserLoginStore();

const apiClient = axios.create({
  baseURL: "http://localhost:8080/information/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

// 添加请求拦截器，自动添加Authorization头
apiClient.interceptors.request.use(
  async (config) => {
    const { useuserLoginStore } = await import("../../store/userLoginStore");
    const loginUserStore = useuserLoginStore();
    const token = loginUserStore.loginUser.token;
    const url = config.url || "";

    if (token && !url.includes("login")) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  function (response) {
    console.log("API响应:", response);

    // 处理各种认证错误和会话过期情况
    if (
      response.data.code === 401 ||
      (response.data.code !== 200 &&
        response.config?.url?.includes("auth/login"))
    ) {
      // 如果是登录接口返回的非成功状态码，直接返回响应，让登录页面处理
      if (response.config?.url?.includes("auth/login")) {
        console.warn("登录失败:", response.data);
        return response;
      }

      // 其他API请求的认证错误，重定向到登录页
      if (
        !window.location.pathname.includes("/login") &&
        !window.location.pathname.includes("/change-password")
      ) {
        console.warn("认证失败，重定向到登录页");

        // 使用异步导入router
        import("../../router").then(({ default: router }) => {
          router.push("/login");
        });
      }
    }
    return response;
  },
  function (error) {
    // 请求错误处理
    console.error("API请求错误:", error);
    return Promise.reject(error);
  }
);

export default apiClient;
