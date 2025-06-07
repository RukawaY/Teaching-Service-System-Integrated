<template>
    <div>
      <!-- Main Content -->
      <main class="page-main">
        <div class="login-container">
          <div class="card login-card">
            <h2 class="card-title">用户登录</h2>
            <form @submit.prevent="handleLogin" id="loginForm">
              <div class="form-group">
                <label for="loginRole">登录身份:</label>
                <select v-model="formData.role" id="loginRole" name="role">
                  <option value="s">学生</option>
                  <option value="t">教师</option>
                  <option value="a">管理员</option>
                </select>
              </div>
              <div class="form-group">
                <label for="account">账号:</label>
                <input
                  v-model="formData.account"
                  type="text"
                  id="account"
                  name="account"
                  placeholder="请输入您的账号"
                  required
                />
              </div>
              <div class="form-group">
                <label for="password">密码:</label>
                <input
                  v-model="formData.password"
                  type="password"
                  id="password"
                  name="password"
                  placeholder="请输入密码"
                  required
                />
              </div>
              <button type="submit" class="btn btn-primary btn-block">
                登 录
              </button>
            </form>
            <div
              id="notificationArea"
              class="notification"
              :style="{ display: notificationVisible ? 'block' : 'none' }"
              :class="notificationType"
            >
              {{ notificationMessage }}
            </div>
          </div>
        </div>
      </main>
  
      <!-- Bottom Bar - REMOVED as global BottomBar is used -->
      <!-- 
      <div class="bottom-bar">
        <p class="copyright-text">
          版权所有© Copyright 2025 浙江大学 软件工程基础课程 教学服务系统课程设计
          信息管理子系统
        </p>
      </div>
      -->
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from "vue";
  import { useRouter } from "vue-router";
  import { login } from "../api/info_manage/auth";
  import { useuserLoginStore } from "../store/userLoginStore";
  
  const loginUserStore = useuserLoginStore();
  
  const router = useRouter();
  // 表单数据
  const formData = ref({
    role: "s",
    account: "",
    password: "",
  });
  
  // 通知相关状态
  const notificationVisible = ref(false);
  const notificationMessage = ref("");
  const notificationType = ref("info");
  
  // 显示通知的函数
  const showNotification = (
    message: string,
    type: "success" | "error" | "info" = "info"
  ) => {
    notificationMessage.value = message;
    notificationType.value = `notification ${type}`;
    notificationVisible.value = true;
  
    setTimeout(() => {
      notificationVisible.value = false;
    }, 5000);
  };
  
  // 处理登录表单提交
  const handleLogin = async () => {
    const { role, account, password } = formData.value;
  
    // 检查账号和密码是否为空
    if (!account || !password) {
      showNotification("账号和密码不能为空！", "error");
      return;
    }
  
    // 检查账号长度
    if (account.length > 20) {
      showNotification("账号长度必须小于 20 个字符！", "error");
      return;
    }
  
    // 检查密码长度
    if (password.length > 20) {
      showNotification("密码长度必须小于 20 个字符！", "error");
      return;
    }
  
    showNotification("正在登录，请稍候...", "info");
  
    try {
      const response = await login(formData.value);
      console.log("登录响应:", response);
  
      // 检查响应状态和数据
      if (response.status === 200 && response.data.code === 200) {
        // 使用setLoginUser的返回值判断登录状态设置是否成功
        const loginSuccess = await loginUserStore.setLoginUser(response);
  
        if (loginSuccess) {
          showNotification("登录成功！正在跳转...", "success");
          setTimeout(() => {
            router.push('/home');
          }, 1000);
        } else {
          showNotification("登录失败：无法获取用户信息。", "error");
        }
      } else {
        // 登录失败但API请求成功的情况
        showNotification("登录失败：账号或密码错误，或身份不匹配。", "error");
        console.error("登录失败，服务器响应:", response.data);
        // 确保用户处于未登录状态
        loginUserStore.setLoginUserUnlogin();
      }
    } catch (error) {
      // API请求失败的情况
      showNotification("登录失败：网络错误，请稍后重试。", "error");
      console.error("登录请求出错:", error);
      // 确保用户处于未登录状态
      loginUserStore.setLoginUserUnlogin();
    }
  };
  </script>
  
  <style scoped>
  /* Global Styles */
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
      "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji",
      "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
    margin: 0;
    background-color: #f8f9fa; /* 主背景色 */
    color: #333;
    line-height: 1.6;
    /* 为固定的顶栏和底栏留出空间 */
    padding-bottom: 40px; /* 大致等于 bottom-bar 高度，可微调 */
    box-sizing: border-box;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
  
  a {
    text-decoration: none;
    color: #409eff; /* 统一使用 Element Plus 主题蓝 */
  }
  
  a:hover {
    text-decoration: underline;
    color: #1370eb; /* 悬停颜色加深 */
  }
  
  /* Main Content */
  .page-main {
    flex-grow: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px; /* 内边距 */
    width: 100%;
    box-sizing: border-box;
  }
  
  .login-container {
    width: 100%;
    display: flex;
    justify-content: center;
  }
  
  /* Cards (基本保持不变，微调) */
  .card {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08); /* 阴影微调 */
    padding: 30px 35px; /* 调整内边距 */
    width: 100%;
    max-width: 420px; /* 稍微增大 */
    box-sizing: border-box;
  }
  
  .card-title {
    font-size: 22px; /* 调整卡片标题大小 */
    color: #303133; /* Element Plus 文本主色 */
    margin-top: 0;
    margin-bottom: 30px; /* 增大间距 */
    text-align: center;
    font-weight: 500;
  }
  
  /* Forms (基本保持不变，颜色和焦点效果可参考Element Plus) */
  .form-group {
    margin-bottom: 22px;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    color: #606266; /* Element Plus 文本常规色 */
    font-size: 14px;
  }
  
  .form-group select,
  .form-group input[type="text"],
  .form-group input[type="password"] {
    width: 100%;
    padding: 10px 12px; /* 调整输入框padding */
    border: 1px solid #dcdfe6; /* Element Plus 输入框边框色 */
    border-radius: 4px; /* Element Plus 圆角 */
    box-sizing: border-box;
    font-size: 14px;
    color: #606266;
    transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  }
  
  .form-group select:focus,
  .form-group input[type="text"]:focus,
  .form-group input[type="password"]:focus {
    border-color: #409eff; /* Element Plus 焦点边框色 */
    outline: 0;
    /* box-shadow: 0 0 0 2px rgba(64,158,255,.2); /* 可选：模拟焦点阴影 */
  }
  
  .form-group select {
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg class='icon' viewBox='0 0 1024 1024' xmlns='http://www.w3.org/2000/svg' width='12' height='12'%3E%3Cpath d='M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-307.2-268.8c-12.8-10.666667-14.933333-29.866667-4.266666-42.666666 10.666667-12.8 29.866667-14.933333 42.666666-4.266667l292.266667 256 292.266667-256c12.8-10.666667 32-8.533333 42.666667 4.266667s8.533333 32-4.266667 42.666666l-307.2 268.8c-6.4 4.266667-12.8 8.533333-21.333333 8.533334z' fill='%23C0C4CC'%3E%3C/path%3E%3C/svg%3E"); /* Element Plus select箭头 */
    background-repeat: no-repeat;
    background-position: right 10px center;
    background-size: 12px;
    padding-right: 30px;
  }
  
  /* Buttons (与Element Plus按钮风格对齐) */
  .btn {
    padding: 10px 20px; /* Element Plus 默认按钮padding */
    border: 1px solid transparent;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: 0.1s;
    text-align: center;
    user-select: none;
    white-space: nowrap;
  }
  
  .btn-primary {
    background-color: #409eff;
    border-color: #409eff;
    color: white;
  }
  .btn-primary:hover {
    background-color: #66b1ff;
    border-color: #66b1ff;
  }
  .btn-primary:active {
    background-color: #3a8ee6;
    border-color: #3a8ee6;
  }
  
  .btn-block {
    display: block;
    width: 100%;
    margin-top: 25px;
  }
  
  .form-links {
    text-align: right;
    margin-top: 15px;
    font-size: 14px;
  }
  
  /* Notification Area (保持原有，颜色可微调) */
  .notification {
    padding: 10px 15px;
    margin-top: 20px;
    border-radius: 4px;
    text-align: center;
    font-size: 14px;
    border: 1px solid transparent;
  }
  .notification.success {
    background-color: #f0f9eb;
    color: #67c23a;
    border-color: #e1f3d8;
  }
  .notification.error {
    background-color: #fef0f0;
    color: #f56c6c;
    border-color: #fde2e2;
  }
  .notification.info {
    background-color: #edf2fc;
    color: #909399;
    border-color: #e4e7ed;
  }
  
  /* Responsive Adjustments */
  @media (max-width: 768px) {
    body {
      /* padding-top: 50px; */ /* REMOVED */
    }
    .page-main {
      padding: 15px;
    }
    .card {
      padding: 20px 25px;
    }
    .card-title {
      font-size: 20px;
      margin-bottom: 25px;
    }
    .btn {
      padding: 9px 15px;
    }
    /* .bottom-bar {
      padding: 10px 0;
    } */ /* REMOVED */
    /* .copyright-text {
      font-size: 11.5px;
    } */ /* REMOVED */
  }
  </style>
  