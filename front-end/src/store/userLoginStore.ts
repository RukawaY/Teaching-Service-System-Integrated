import { defineStore } from "pinia";
import { getCurrentUserProfile } from "../api/info_manage/account";
import { Ref, ref } from "vue";
import { AxiosResponse } from "axios";

export const useuserLoginStore = defineStore("userLogin", {
  state: () => {
    const getInitialUser = () => {
      const defaultUser = {
        user_id: "null",
        name: "未登录",
        account: "null",
        role: "null",
        department: "null",
        contact: "null",
        avatar_path: "null",
        token: "null",
      };

      const itemStr = localStorage.getItem("loginUser");
      if (!itemStr) {
        return defaultUser;
      }

      try {
        const item = JSON.parse(itemStr);
        const now = new Date();
        // 检查时间戳是否过期
        if (now.getTime() < item.expiry && item.user) {
          return item.user;
        }
      } catch (e) {
        console.error("从localStorage解析用户数据时出错:", e);
      }

      localStorage.removeItem("loginUser");
      return defaultUser;
    };
    const loginUser: Ref<any, any> = ref(getInitialUser());

    const unLoginUser: Ref<any, any> = ref({
      user_id: "null",
      name: "未登录",
      account: "null",
      role: "null",
      department: "null",
      contact: "null",
      avatar_path: "null",
      token: "null",
    });

    // async function fetchLoginUser() {
    //   const res: AxiosResponse<any, any> = await getCurrentUserProfile();
    //   if (res.data.code === 200 && res.data.user_id) {
    //     loginUser.value.user_id = res.data.userInfo.user_id;
    //     loginUser.value.name = res.data.userInfo.name;
    //     loginUser.value.role = res.data.userInfo.role;
    //     loginUser.value.department = res.data.userInfo.department;
    //     loginUser.value.contact = res.data.userInfo.contact;
    //     loginUser.value.avatar_path = res.data.userInfo.avatar_path;
    //     loginUser.value.token = res.data.token;
    //   } else await setLoginUserUnlogin();
    // }

    async function setLoginUser(response: AxiosResponse<any, any>) {
      const res = response;
      try {
        // 确保响应状态和数据都是有效的
        if (
          res.status === 200 &&
          res.data.code === 200 &&
          res.data.data &&
          res.data.data.userInfo
        ) {
          loginUser.value.user_id = res.data.data.userInfo.user_id;
          loginUser.value.name = res.data.data.userInfo.name;
          loginUser.value.role = res.data.data.userInfo.role;
          loginUser.value.department = res.data.data.userInfo.department;
          loginUser.value.contact = res.data.data.userInfo.contact;
          loginUser.value.avatar_path = res.data.data.userInfo.avatarPath;
          loginUser.value.token = res.data.data.token;
          const now = new Date();
          // 设置10分钟后过期
          const expiry = now.getTime() + 10 * 60 * 1000;
          const itemToStore = {
            user: loginUser.value,
            expiry: expiry,
          };
          localStorage.setItem("loginUser", JSON.stringify(itemToStore));
          console.log("用户登录状态已更新:", loginUser.value);
          return true;
        } else {
          console.error("登录响应数据无效:", res.data);
          await setLoginUserUnlogin();
          return false;
        }
      } catch (error) {
        console.error("处理登录数据时出错:", error);
        await setLoginUserUnlogin();
        return false;
      }
    }

    async function setLoginUserUnlogin() {
      // loginUser.value = unLoginUser.value; // Old implementation
      // Explicitly reset all fields to their initial unlogged state
      loginUser.value.user_id = "null";
      loginUser.value.name = "未登录";
      loginUser.value.account = "null";
      loginUser.value.role = "null";
      loginUser.value.department = "null";
      loginUser.value.contact = "null";
      loginUser.value.avatar_path = "null";
      loginUser.value.token = "null";
      localStorage.removeItem("loginUser");
    }

    return { loginUser, setLoginUser, setLoginUserUnlogin };
  },
});
