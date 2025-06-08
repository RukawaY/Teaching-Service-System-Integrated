<template>
  <div>
    <!-- Main Content -->
    <main class="page-main">
      <div class="container user-management-container">
        <div class="card user-filter-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-search" /> 查询用户
          </h2>
          <form
            @submit.prevent="filterAndDisplayUsers"
            class="filter-form-grid"
          >
            <div class="form-group">
              <label for="filterUserId">用户ID/学工号:</label>
              <input
                type="text"
                id="filterUserId"
                v-model="filterUserId"
                placeholder="输入ID或学工号"
              />
            </div>
            <div class="form-group">
              <label for="filterUserName">用户姓名:</label>
              <input
                type="text"
                id="filterUserName"
                v-model="filterUserName"
                placeholder="输入姓名关键词"
              />
            </div>
            <div class="form-group">
              <label for="filterUserRole">用户角色:</label>
              <select id="filterUserRole" v-model="filterUserRole">
                <option value="">所有角色</option>
                <option value="s">学生</option>
                <option value="t">教师</option>
                <option value="a">管理员</option>
              </select>
            </div>
            <div class="form-group">
              <label for="filterUserDepartment">所属院系/部门:</label>
              <input
                type="text"
                id="filterUserDepartment"
                v-model="filterUserDepartment"
                placeholder="输入院系或部门"
              />
            </div>
            <div class="divider"></div>
            <div class="divider"></div>
            <div class="divider"></div>
            <div class="form-action-group">
              <button type="submit" class="btn btn-primary">
                <FontAwesomeIcon icon="fas fa-filter" /> 应用筛选
              </button>
              <button
                type="reset"
                class="btn btn-secondary"
                @click="resetUserFilters"
              >
                <FontAwesomeIcon icon="fas fa-undo" /> 重置
              </button>
              <button
                type="button"
                class="btn btn-success"
                @click="openUserModal('add')"
              >
                <FontAwesomeIcon icon="fas fa-user-plus" /> 添加新用户
              </button>
              <button
                type="button"
                class="btn btn-warning"
                @click="triggerFileUpload"
              >
                <FontAwesomeIcon icon="fas fa-upload" /> 批量上传用户
              </button>
              <input
                type="file"
                id="jsonFileInput"
                ref="jsonFileInput"
                accept=".json"
                style="display: none"
                @change="handleFileUpload"
              />
            </div>
          </form>
        </div>

        <div class="card user-list-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-users" /> 用户列表
          </h2>
          <div class="table-responsive">
            <table id="usersTable">
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>姓名</th>
                  <th>角色</th>
                  <th>院系/部门</th>
                  <th>手机号</th>
                  <!--                  <th>账号状态</th>-->
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="7" class="placeholder-row">
                    正在加载用户信息...
                  </td>
                </tr>
                <tr v-else-if="users.length === 0">
                  <td colspan="7" class="no-users-row">
                    未查询到符合条件的用户。
                  </td>
                </tr>
                <tr v-else v-for="user in users" :key="user.id">
                  <td>{{ user.account }}</td>
                  <td>{{ user.name }}</td>
                  <td>
                    {{
                      user.role === "s"
                        ? "学生"
                        : user.role === "t"
                        ? "教师"
                        : "管理员"
                    }}
                  </td>
                  <td>{{ user.department || "-" }}</td>
                  <td>{{ user.contact }}</td>
                  <!--                  <td class="user-status">-->
                  <!--                    <span-->
                  <!--                      :class="{-->
                  <!--                        'status-active': user.status === 'active',-->
                  <!--                        'status-disabled': user.status === 'disabled',-->
                  <!--                      }"-->
                  <!--                    >-->
                  <!--                      {{ user.status === "active" ? "激活" : "禁用" }}-->
                  <!--                    </span>-->
                  <!--                  </td>-->
                  <td class="actions-cell">
                    <button
                      class="btn btn-sm btn-primary"
                      @click="openUserModal('edit', user)"
                    >
                      <FontAwesomeIcon icon="fas fa-edit" />
                    </button>
                    <button
                      class="btn btn-sm btn-danger"
                      @click="handleDeleteUser(user.user_id, user.name)"
                    >
                      <FontAwesomeIcon icon="fas fa-trash-alt" />
                    </button>
                    <!--                    <button-->
                    <!--                      class="btn btn-sm btn-secondary"-->
                    <!--                      @click="handleResetPassword(user.id, user.name)"-->
                    <!--                    >-->
                    <!--                      <FontAwesomeIcon icon="fas fa-key" />-->
                    <!--                    </button>-->
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <!-- Pagination (Optional, implement if needed) -->
          <div class="pagination-controls" v-if="users.length > itemsPerPage">
            <button
              class="btn btn-sm btn-default"
              :disabled="currentPage === 1"
              @click="prevPage"
            >
              « 上一页
            </button>
            <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
            <button
              class="btn btn-sm btn-default"
              :disabled="currentPage === totalPages"
              @click="nextPage"
            >
              下一页 »
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal for Add/Edit User (Initially Hidden) -->
    <div class="modal" :style="{ display: isUserModalOpen ? 'flex' : 'none' }">
      <div class="modal-content card">
        <div class="modal-header">
          <h3 class="modal-title">{{ modalTitle }}</h3>
          <button type="button" class="close-modal-btn" @click="closeUserModal">
            ×
          </button>
        </div>
        <form @submit.prevent="saveUser">
          <input type="hidden" v-model="modalUserId" />
          <!-- For editing existing user -->
          <div class="form-group">
            <label for="modalUserFullName">姓名:</label>
            <input
              type="text"
              id="modalUserFullName"
              v-model="modalUser.name"
              required
              placeholder="请输入用户姓名"
              maxlength="20"
            />
            <small
              >姓名长度不能超过20个字符 (当前:
              {{ modalUser.name ? modalUser.name.length : 0 }}/20)</small
            >
          </div>
          <div class="form-group">
            <label for="modalUserLoginId">登录ID (学/工号):</label>
            <input
              type="text"
              id="modalUserLoginId"
              v-model="modalUser.account"
              :readonly="modalMode === 'edit'"
              required
              placeholder="学生为学号，教师为工号"
            />
          </div>
          <div class="form-group">
            <label for="modalUserRoleSelect">角色:</label>
            <select id="modalUserRoleSelect" v-model="modalUser.role" required>
              <option value="">--选择角色--</option>
              <option value="s">学生</option>
              <option value="t">教师</option>
              <option value="a">管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label for="modalUserDepartment">院系/部门:</label>
            <input
              type="text"
              id="modalUserDepartment"
              v-model="modalUser.department"
              placeholder="例如：计算机学院"
            />
          </div>
          <div class="form-group">
            <label for="modalUserMajorId">专业ID:</label>
            <input
              type="number"
              id="modalUserMajorId"
              v-model="modalUser.majorId"
              placeholder="可选，输入专业ID"
            />
          </div>
          <div class="form-group">
            <label for="modalUserEmail">手机号:</label>
            <input
              type="text"
              id="modalUserEmail"
              v-model="modalUser.contact"
              required
              placeholder="请输入手机号码"
            />
          </div>
          <div
            class="form-group"
            v-if="modalMode === 'add' || modalShowPassword"
          >
            <label for="modalUserPassword">初始密码:</label>
            <input
              type="password"
              id="modalUserPassword"
              v-model="modalUser.password"
              :placeholder="
                modalMode === 'edit' ? '留空则不修改密码' : '请输入初始密码'
              "
            />
            <small v-if="modalMode === 'edit'">
              编辑用户时，留空则不修改密码。
            </small>
          </div>
          <!--          <div class="form-group">-->
          <!--            <label for="modalUserStatus">账号状态:</label>-->
          <!--            <select id="modalUserStatus" v-model="modalUser.status">-->
          <!--              <option value="active">激活</option>-->
          <!--              <option value="disabled">禁用</option>-->
          <!--            </select>-->
          <!--          </div>-->
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary">
              {{ modalMode === "add" ? "添加用户" : "保存更改" }}
            </button>
            <button
              type="button"
              class="btn btn-default"
              @click="closeUserModal"
            >
              取消
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      id="notificationArea"
      class="notification"
      :style="{ display: showNotification ? 'block' : 'none' }"
    >
      {{ notificationMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { getUsers, createUser, updateUser, deleteUser, batchCreateUsers } from "../../../api/info_manage/admin";
import { useRouter } from "vue-router";

type User = {
  user_id: string;
  name: string;
  account: string;
  role: string;
  department: string;
  contact: string;
  password: string;
  majorId: number | null;
};

// 响应式数据
const router = useRouter();
const loginUserStore = useuserLoginStore();
const isUserDropdownOpen = ref(false);
const isUserModalOpen = ref(false);
const loading = ref(false);
const showNotification = ref(false);
const notificationMessage = ref("");
const users = ref([]);
const filterUserId = ref("");
const filterUserName = ref("");
const filterUserRole = ref("");
const filterUserDepartment = ref("");
const currentPage = ref(1);
const itemsPerPage = ref(10);
const totalPages = ref(1);
const modalMode = ref("add");
const modalTitle = ref("添加新用户");
const modalUserId = ref("");
const modalUser = ref({
  name: "",
  account: "",
  role: "",
  department: "",
  contact: "",
  password: "",
  majorId: null,
});
const modalShowPassword = ref(false);
const jsonFileInput = ref(null);

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  isUserDropdownOpen.value = !isUserDropdownOpen.value;
};

// 退出登录
const logout = () => {
  showMainNotification("正在退出登录...", "info");
  setTimeout(() => {
    loginUserStore.setLoginUserUnlogin();
    window.location.href = "../login";
  }, 1500);
};

// 修改密码
const logChange = () => {
  //window.location.href = "../change-password";
  router.push("/change-password");
};

// 显示通知
const showMainNotification = (message: string, type = "info") => {
  notificationMessage.value = message;
  showNotification.value = true;
  setTimeout(() => {
    showNotification.value = false;
  }, 3000);
};

// 获取用户列表
// const fetchUsers = async () => {
//   loading.value = true;
//   try {
//     // 模拟返回样例数据
//     const sampleUsers = [
//       {
//         id: "1",
//         name: "张三",
//         account: "2023001",
//         role: "student",
//         department: "计算机学院",
//         contact: "zhangsan@example.com",
//         password: "123456",
//       },
//       {
//         id: "2",
//         name: "李四",
//         account: "2023002",
//         role: "student",
//         department: "计算机学院",
//         contact: "lisi@example.com",
//         password: "123456",
//       },
//       {
//         id: "3",
//         name: "王老师",
//         account: "T2023001",
//         role: "teacher",
//         department: "计算机学院",
//         contact: "wanglaoshi@example.com",
//         password: "123456",
//       },
//       {
//         id: "4",
//         name: "管理员",
//         account: "A2023001",
//         role: "admin",
//         department: "管理部门",
//         contact: "admin@example.com",
//         password: "123456",
//       },
//     ];
//
//     users.value = sampleUsers;
//     totalPages.value = Math.ceil(users.value.length / itemsPerPage.value);
//     showMainNotification(
//       `查询到 ${users.value.length} 条符合条件的用户。`,
//       "success"
//     );
//   } catch (error) {
//     console.error("获取用户列表失败:", error);
//     showMainNotification("获取用户列表失败", "error");
//   } finally {
//     loading.value = false;
//   }
// };
//
const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await getUsers({
      account: filterUserId.value,
      name: filterUserName.value,
      role: filterUserRole.value,
      department: filterUserDepartment.value,
    });
    users.value = response.data.data.items;
    totalPages.value = Math.ceil(users.value.length / itemsPerPage.value);
    showMainNotification(
      `查询到 ${users.value.length} 条符合条件的用户。`,
      "success"
    );
  } catch (error) {
    console.error("获取用户列表失败:", error);
    showMainNotification("获取用户列表失败", "error");
  } finally {
    loading.value = false;
  }
};

// 过滤并显示用户
const filterAndDisplayUsers = async () => {
  currentPage.value = 1;
  await fetchUsers();
};

// 重置筛选条件
const resetUserFilters = () => {
  filterUserId.value = "";
  filterUserName.value = "";
  filterUserRole.value = "";
  filterUserDepartment.value = "";
  filterAndDisplayUsers();
};

// 打开用户模态框
const openUserModal = (mode = "add", userData: User) => {
  modalMode.value = mode;
  if (mode === "add") {
    modalTitle.value = "添加新用户";
    modalUserId.value = "";
    modalUser.value = {
      name: "",
      account: "",
      role: "",
      department: "",
      contact: "",
      password: "",
      majorId: null,
    };
    modalShowPassword.value = true;
  } else if (mode === "edit" && userData) {
    modalTitle.value = `编辑用户 - ${userData.name}`;
    modalUserId.value = userData.user_id;
    modalUser.value = { ...userData };
    modalShowPassword.value = false;
  }
  isUserModalOpen.value = true;
};

// 关闭用户模态框
const closeUserModal = () => {
  isUserModalOpen.value = false;
};

// 保存用户信息
const saveUser = async () => {
  if (
    !modalUser.value.name ||
    !modalUser.value.account ||
    !modalUser.value.role ||
    !modalUser.value.contact
  ) {
    showMainNotification("姓名、登录ID、角色和手机号为必填项。", "error");
    return;
  }
  if (modalMode.value === "add" && !modalUser.value.password) {
    showMainNotification("新用户必须设置初始密码。", "error");
    return;
  }
  // 定义各字段长度限制
  const MAX_NAME_LENGTH = 20;
  const MAX_DEPARTMENT_LENGTH = 20;
  const MAX_PHONE_LENGTH = 20;
  const MIN_LOGIN_ID_LENGTH = 6;
  const MAX_LOGIN_ID_LENGTH = 20;
  const MIN_PASSWORD_LENGTH = 6;
  const MAX_PASSWORD_LENGTH = 20;

  // 用户名称长度检查
  if (modalUser.value.name.length > MAX_NAME_LENGTH) {
    showMainNotification(
      `用户姓名长度不能超过 ${MAX_NAME_LENGTH} 个字符。`,
      "error"
    );
    return;
  }

  // 院系/部门长度检查
  if (modalUser.value.department.length > MAX_DEPARTMENT_LENGTH) {
    showMainNotification(
      `院系/部门长度不能超过 ${MAX_DEPARTMENT_LENGTH} 个字符。`,
      "error"
    );
    return;
  }
  // 手机号码长度检查
  if (modalUser.value.contact.length > MAX_PHONE_LENGTH) {
    showMainNotification(
      `手机号码长度不能超过 ${MAX_PHONE_LENGTH} 个字符。`,
      "error"
    );
    return;
  }
  // 登录ID长度检查
  if (
    modalUser.value.account.length < MIN_LOGIN_ID_LENGTH ||
    modalUser.value.account.length > MAX_LOGIN_ID_LENGTH
  ) {
    showMainNotification(
      `初始登录ID长度必须在 ${MIN_LOGIN_ID_LENGTH} 到 ${MAX_LOGIN_ID_LENGTH} 个字符之间。`,
      "error"
    );
    return;
  }
  // 密码长度检查
  if (
    (modalMode.value === "add" || modalUser.value.password) &&
    (modalUser.value.password.length < MIN_PASSWORD_LENGTH ||
      modalUser.value.password.length > MAX_PASSWORD_LENGTH)
  ) {
    showMainNotification(
      `初始密码长度必须在 ${MIN_PASSWORD_LENGTH} 到 ${MAX_PASSWORD_LENGTH} 个字符之间。`,
      "error"
    );
    return;
  }

  try {
    if (modalMode.value === "add") {
      const response = await createUser(modalUser.value);
      showMainNotification(
        `新用户 ${response.data.data.name} 已成功添加。`,
        "success"
      );
    } else {
      const response = await updateUser(modalUserId.value, modalUser.value);
      showMainNotification(
        `用户 ${response.data.data.name} 信息已更新。`,
        "success"
      );
    }
    closeUserModal();
    await fetchUsers();
  } catch (error) {
    console.error("保存用户信息失败:", error);
    showMainNotification("保存用户信息失败", "error");
  }
};

// 删除用户
const handleDeleteUser = async (userId: string, userName: string) => {
  if (
    confirm(`确定要删除用户 "${userName}" (ID: ${userId}) 吗？此操作不可恢复。`)
  ) {
    try {
      await deleteUser(userId);
      showMainNotification(`用户 "${userName}" 已被删除。`, "success");
      await fetchUsers();
    } catch (error) {
      console.error("删除用户失败:", error);
      showMainNotification("删除用户失败", "error");
    }
  }
};

// // 重置密码
// const handleResetPassword = async (userId: string, userName: string) => {
//   if (confirm(`确定要为用户 "${userName}" 重置密码吗？`)) {
//     try {
//       await resetPassword(userId);
//       showMainNotification(`用户 "${userName}" 的密码已重置。`, "success");
//     } catch (error) {
//       console.error("重置密码失败:", error);
//       showMainNotification("重置密码失败", "error");
//     }
//   }
// };

// 触发文件上传
const triggerFileUpload = () => {
  jsonFileInput.value?.click();
};

// 处理文件上传
const handleFileUpload = async (event: Event) => {
  const file = (event.target as HTMLInputElement)?.files?.[0];
  if (!file) {
    showMainNotification("请选择一个JSON文件", "error");
    return;
  }

  try {
    const formData = new FormData();
    formData.append("file", file);

    const response = await batchCreateUsers(formData);
    showMainNotification(
      `成功批量添加 ${response.data.data.length} 个用户。`,
      "success"
    );
    await fetchUsers();
  } catch (error) {
    console.error("批量上传用户失败:", error);
    showMainNotification("批量上传用户失败", "error");
  }
};

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
/* admin_user_management_style.css */

/* ==========================================================================
   CRITICAL: Global, Top Bar, and Bottom Bar Styles - ENSURE CONSISTENCY
   ========================================================================== */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8;
  color: #333;
  line-height: 1.6;
  padding-top: 60px;
  padding-bottom: 50px;
  box-sizing: border-box;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
router-link {
  text-decoration: none;
  color: #409eff;
}
router-link:hover {
  text-decoration: none;
  color: #1370eb;
}

/* Top Bar */
.top-bar {
  height: 60px;
  background-color: #409eff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-sizing: border-box;
}
.top-bar .left-section {
  display: flex;
  align-items: center;
}
.top-bar .back-icon {
  color: white;
  font-size: 20px;
  margin-right: 20px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.top-bar .back-icon:hover {
  opacity: 0.7;
}
.top-bar .system-name {
  font-size: 24px;
  font-weight: bold;
  color: white;
}
.top-bar .system-subname {
  font-size: 16px;
  color: white;
  margin-left: 15px;
}
.top-bar .right-section {
  display: flex;
  align-items: center;
}
.top-bar .user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background-color 0.2s;
  position: relative;
}
.top-bar .user-info:hover {
  background-color: #3a8ee6;
}
.top-bar .user-avatar {
  /* This div WRAPS the icon */
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #fff; /* Background for the circle */
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
  overflow: hidden;
}
.top-bar .user-avatar i {
  /* The Font Awesome icon itself */
  font-size: 18px; /* Size of the icon */
  color: #409eff; /* Color of the icon */
  line-height: 1; /* Important for precise centering */
}
.top-bar .user-name {
  margin: 0 8px 0 0;
  font-size: 14px;
  color: white;
  line-height: 1;
}
.top-bar .user-info .fa-angle-down {
  font-size: 12px;
  color: white;
  margin-left: 5px;
  transition: transform 0.2s;
}
.top-bar .user-info.is-open .fa-angle-down {
  transform: rotate(180deg);
}
.top-bar .user-dropdown-menu {
  display: none;
  position: absolute;
  top: calc(100% + 5px);
  right: 0;
  background-color: white;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 130px;
  z-index: 1001;
}
.top-bar .user-dropdown-menu router-link {
  display: block;
  padding: 8px 15px;
  color: #606266;
  font-size: 14px;
}
.top-bar .user-dropdown-menu router-link:hover {
  background-color: #ecf5ff;
  color: #409eff;
}
.top-bar .user-dropdown-menu .divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 5px 0;
}

/* Bottom Bar */
.bottom-bar {
  background-color: #f5f5f5b7;
  padding: 12px 0;
  text-align: center;
  width: 100%;
  position: fixed;
  bottom: 0;
  left: 0;
  z-index: 999;
  box-sizing: border-box;
}
.copyright-text {
  color: #666;
  font-size: 12.5px;
  margin: 0;
  line-height: 1.5;
}

/* Notification Area (General) */
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
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #d9ecff;
}

/* ==========================================================================
   Page Specific Styles for User Management
   ========================================================================== */

.page-main {
  flex-grow: 1;
  padding: 25px;
  width: 100%;
  box-sizing: border-box;
}
.container.user-management-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 20px 25px;
  margin-bottom: 25px;
}
.card-title {
  font-size: 18px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 500;
  display: flex;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.card-title i {
  font-size: 1em;
  margin-right: 10px;
}

/* Forms (Common styles for label, input, select) */
.form-group {
  margin-bottom: 18px;
  padding-left: 12px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}
.form-group input[type="text"],
.form-group input[type="contact"],
.form-group input[type="password"],
.form-group select {
  width: 90%;
  padding: 9px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}
.form-group input:focus,
.form-group select:focus {
  border-color: #409eff;
  outline: 0;
}
.form-group select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg class='icon' viewBox='0 0 1024 1024' xmlns='http://www.w3.org/2000/svg' width='12' height='12'%3E%3Cpath d='M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-307.2-268.8c-12.8-10.666667-14.933333-29.866667-4.266666-42.666666 10.666667-12.8 29.866667-14.933333 42.666666-4.266667l292.266667 256 292.266667-256c12.8-10.666667 32-8.533333 42.666667 4.266667s8.533333 32-4.266667 42.666666l-307.2 268.8c-6.4 4.266667-12.8 8.533333-21.333333 8.533334z' fill='%23C0C4CC'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 12px;
  padding-right: 30px;
}
.form-group small {
  font-size: 0.85em;
  color: #909399;
  display: block;
  margin-top: 4px;
}
.form-group input[readonly] {
  background-color: #f5f7fa; /* Readonly fields slightly different background */
  color: #909399;
  cursor: default;
  border-color: #e4e7ed;
}
.form-group input:not([readonly]):focus {
  border-color: #409eff;
  outline: 0;
}

/* Buttons - Corrected to match Element Plus style more closely */
.btn {
  padding: 8px 15px; /* Element Plus default button size padding */
  border: 1px solid #dcdfe6; /* Default border */
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: 0.1s;
  text-align: center;
  user-select: none;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  justify-content: center; /* Center icon and text */
  gap: 6px;
  background-color: #fff; /* Default background */
  color: #606266; /* Default text color */
}
.btn i {
  font-size: 1em;
  line-height: 1; /* Ensure icon aligns well */
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

.btn-secondary {
  background-color: #909399;
  border-color: #909399;
  color: white;
}
.btn-secondary:hover {
  background-color: #a6a9ad;
  border-color: #a6a9ad;
}
.btn-secondary:active {
  background-color: #82848a;
  border-color: #82848a;
}

.btn-success {
  background-color: #67c23a;
  border-color: #67c23a;
  color: white;
}
.btn-success:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}
.btn-success:active {
  background-color: #5daf34;
  border-color: #5daf34;
}

.btn-danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}
.btn-danger:hover {
  background-color: #f78989;
  border-color: #f78989;
}
.btn-danger:active {
  background-color: #f14f4f;
  border-color: #f14f4f;
}

.btn-default {
  /* This is the base .btn style now */
}
.btn-default:hover {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
}
.btn-default:active {
  border-color: #3a8ee6;
  color: #3a8ee6;
}

.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
} /* Element Plus small button size */

/* Users Table */
.table-responsive {
  overflow-x: auto;
  margin-top: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
table {
  width: 100%;
  border-collapse: collapse;
  color: #606266;
  font-size: 14px;
}
th,
td {
  border-bottom: 1px solid #ebeef5;
  padding: 10px 12px;
  text-align: left;
  vertical-align: middle;
  white-space: nowrap;
}
thead {
  background-color: #f5f7fa;
  color: #909399;
}
th {
  font-weight: 500;
}
tbody tr:hover {
  background-color: #f5f7fa;
}
.placeholder-row td,
.no-users-row td {
  text-align: center;
  color: #909399;
  padding: 30px 15px;
  font-style: italic;
}
td.user-status span {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
  display: inline-block;
} /* More pill-like */
td.user-status .status-active {
  background-color: #f0f9eb;
  color: #67c23a;
}
td.user-status .status-disabled {
  background-color: #fef0f0;
  color: #f56c6c;
}
td.actions-cell .btn {
  margin-right: 5px;
} /* Use general .btn for table actions now */

/* Pagination Controls */
.pagination-controls {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.pagination-controls #userPageInfo {
  font-size: 14px;
  color: #606266;
}

/* Modal Styles */
.modal {
  display: none;
  position: fixed;
  z-index: 1050;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.5);
  align-items: center;
  justify-content: center;
}
.modal-content {
  position: relative;
  margin: auto;
  padding: 0;
  width: 90%;
  max-width: 600px;
  animation: modal-appear 0.3s ease-out;
}
@keyframes modal-appear {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  border-bottom: 1px solid #ebeef5;
}
.modal-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.close-modal-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  font-weight: bold;
  line-height: 1;
  color: #909399;
  opacity: 0.7;
  cursor: pointer;
  padding: 0;
}
.close-modal-btn:hover {
  opacity: 1;
  color: #000;
}
#userModalForm {
  padding: 20px 25px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 25px;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}
</style>
