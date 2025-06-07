<template>
  <div>
    <!-- Main Content -->
    <main class="page-main">
      <div class="find-password-container">
        <div class="card find-password-card">
          <h2 class="card-title">修改密码</h2>
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label for="oldPassword">旧密码:</label>
              <input
                type="text"
                id="oldPassword"
                v-model="oldPassword"
                placeholder="请输入旧密码"
                required
              />
            </div>
            <div class="form-group">
              <label for="newPassword">新密码:</label>
              <input
                type="password"
                id="newPassword"
                v-model="newPassword"
                placeholder="请输入新密码 (6-20位字符)"
                required
              />
            </div>
            <div class="form-group">
              <label for="confirmPassword">确认新密码:</label>
              <input
                type="password"
                id="confirmPassword"
                v-model="confirmPassword"
                placeholder="请再次输入新密码"
                required
              />
            </div>
            <button type="submit" class="btn btn-primary btn-block">
              修改并重新登录
            </button>
          </form>
          <div
            :class="['notification', notificationType]"
            v-if="showNotification"
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
import { updateCurrentUserPassword } from "../api/info_manage/account";
import { useuserLoginStore } from "../store/userLoginStore";
import { useRouter } from "vue-router";

const loginUserStore = useuserLoginStore();
const router = useRouter();

// 表单数据
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");

// 通知相关
const showNotification = ref(false);
const notificationMessage = ref("");
const notificationType = ref("info");

const showNotificationFunc = (message: string, type = "info") => {
  notificationMessage.value = message;
  notificationType.value = type;
  showNotification.value = true;
  setTimeout(() => {
    showNotification.value = false;
  }, 5000);
};

const handleSubmit = async () => {
  // Basic frontend validation
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    showNotificationFunc("所有字段均为必填项。", "error");
    return;
  }
  if (newPassword.value.length < 6 || newPassword.value.length > 20) {
    showNotificationFunc("新密码长度必须在6到20位之间。", "error");
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    showNotificationFunc("两次输入的新密码不一致。", "error");
    return;
  }

  showNotificationFunc("正在处理您的请求...", "info");

  // 提取响应式对象的值
  const data = {
    oldPassword: oldPassword.value,
    newPassword: newPassword.value,
  };

  try {
    const response = await updateCurrentUserPassword(data);
    if (response.data.code == 400) {
      showNotificationFunc("旧密码错误", "error");
    } else if (response.data.code != 200) {
      showNotificationFunc("密码重置失败，请重试。", "error");
    } else {
      showNotificationFunc("密码重置成功！正在跳转到登录页面...", "success");
      loginUserStore.setLoginUserUnlogin();
      setTimeout(() => {
        router.push('/login'); // Redirect to login page using Vue router
      }, 2000);
    }
  } catch (error) {
    console.error("密码重置失败:", error);
    showNotificationFunc("密码重置失败，请重试。", "error");
  }
};

const backToDashboard = () => {
  const userRole = loginUserStore.loginUser.role;

  // 根据角色决定跳转的dashboard
  let dashboardPath = "/";
  if (userRole === "s") {
    dashboardPath = "/info_manage/student/Student_Dashboard";
  } else if (userRole === "t") {
    dashboardPath = "/info_manage/teacher/Teacher_Dashboard";
  } else if (userRole === "a") {
    dashboardPath = "/info_manage/admin/Admin_Dashboard";
  }

  // 使用router进行导航
  router.push(dashboardPath);
};
</script>

<style scoped>
/* Global Styles (与 login_style.css 和 submit_grades_style.css 中的全局部分基本一致) */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji",
    "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  margin: 0;
  background-color: #f8f9fa;
  color: #333;
  line-height: 1.6;
  padding-bottom: 40px;
  box-sizing: border-box;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

a {
  text-decoration: none;
  color: #409eff;
}
a:hover {
  text-decoration: underline;
  color: #1370eb;
}

/* Main Content */
.page-main {
  flex-grow: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

.find-password-container {
  /* 类似 login-container */
  width: 100%;
  display: flex;
  justify-content: center;
}

/* Cards (与 login_style.css 中的 card 样式类似) */
.card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  padding: 30px 35px;
  width: 100%;
  max-width: 450px; /* 可略宽一点 */
  box-sizing: border-box;
}
.card-title {
  font-size: 22px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 30px;
  text-align: center;
  font-weight: 500;
}

/* Forms (与 login_style.css 中的 form 样式类似) */
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}
.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}
.form-group input[type="text"]:focus,
.form-group input[type="password"]:focus {
  border-color: #409eff;
  outline: 0;
}

/* Buttons (与 login_style.css 中的 btn 样式类似) */
.btn {
  padding: 10px 20px;
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

/* Notification Area (与之前页面一致) */
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
}
</style>
