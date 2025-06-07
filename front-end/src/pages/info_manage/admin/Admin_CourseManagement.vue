<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container course-management-container">
        <div class="card course-filter-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-filter" /> 筛选课程
          </h2>
          <form
            @submit.prevent="filterAndDisplayCourses"
            class="filter-form-grid"
          >
            <div class="form-group">
              <label for="filterCourseIdAdmin">课程编号:</label>
              <input
                type="text"
                id="filterCourseIdAdmin"
                v-model="filter.course_id"
                placeholder="输入课程编号"
              />
            </div>
            <div class="form-group">
              <label for="filterCourseNameAdmin">课程名称:</label>
              <input
                type="text"
                id="filterCourseNameAdmin"
                v-model="filter.course_name"
                placeholder="输入课程名称关键词"
              />
            </div>
            <div class="form-group">
              <label for="filterCourseTeacherAdmin">授课教师:</label>
              <input
                type="text"
                id="filterCourseTeacherAdmin"
                v-model="filter.teacher_name"
                placeholder="输入教师姓名"
              />
            </div>
            <!--            <div class="form-group">-->
            <!--              <label for="filterCourseDepartmentAdmin">开课院系:</label>-->
            <!--              <input-->
            <!--                type="text"-->
            <!--                id="filterCourseDepartmentAdmin"-->
            <!--                v-model="filter.department"-->
            <!--                placeholder="输入院系名称"-->
            <!--              />-->
            <!--            </div>-->
            <div class="divider"></div>

            <div class="form-action-group">
              <button type="submit" class="btn btn-primary">
                <FontAwesomeIcon icon="fas fa-search" /> 查询
              </button>
              <button
                type="reset"
                class="btn btn-secondary"
                @click="resetFilters"
              >
                <FontAwesomeIcon icon="fas fa-undo" /> 重置
              </button>
              <!--              <button-->
              <!--                type="button"-->
              <!--                class="btn btn-success"-->
              <!--                @click="openCourseModal('add')"-->
              <!--              >-->
              <!--                <FontAwesomeIcon icon="fas fa-plus-circle" /> 添加新课程-->
              <!--              </button>-->
            </div>
          </form>
        </div>

        <div class="card course-list-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-book-open" /> 课程列表
          </h2>
          <div class="table-responsive">
            <table id="coursesTable">
              <thead>
                <tr>
                  <th>课程编号</th>
                  <th>课程名称</th>
                  <th>授课教师</th>
                  <th>学分</th>
                  <th>周学时</th>
                  <th>课程类别</th>
                  <!-- <th>操作</th> -->
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="8" class="placeholder-row">
                    正在加载课程信息...
                  </td>
                </tr>
                <tr v-else-if="filteredCourses.length === 0">
                  <td colspan="8" class="no-courses-row">
                    未查询到符合条件的课程。
                  </td>
                </tr>
                <tr
                  v-else
                  v-for="course in paginatedCourses"
                  :key="course.course_id"
                >
                  <td>{{ course.course_id }}</td>
                  <td>{{ course.course_name }}</td>
                  <td>{{ course.teacher_name || course.teacher_id || "-" }}</td>
                  <td>{{ course.credit }}</td>
                  <td>{{ course.hours_per_week }}</td>
                  <td>{{ course.category }}</td>
                  <!-- <td>{{ course.department }}</td>-->
                  <!-- <td class="actions-cell">
                    <button
                      class="btn btn-sm btn-primary edit-course-btn"
                      @click="openCourseModal('edit', course)"
                      title="编辑课程"
                    >
                      <FontAwesomeIcon icon="fas fa-edit" />
                    </button>
                    <button
                      class="btn btn-sm btn-danger delete-course-btn"
                      @click="handleDeleteCourse(course.course_id)"
                      title="删除课程"
                    >
                      <FontAwesomeIcon icon="fas fa-trash-alt" />
                    </button>
                  </td> -->
                </tr>
              </tbody>
            </table>
          </div>
          <!-- Pagination (Optional) -->
          <div
            class="pagination-controls"
            id="coursePaginationControls"
            :style="{
              display: filteredCourses.length > 0 ? 'block' : 'none',
            }"
          >
            <button
              class="btn btn-sm btn-default"
              id="prevCoursePageBtn"
              :disabled="currentPage === 1"
              @click="prevPage"
            >
              « 上一页
            </button>
            <span id="coursePageInfo"
              >第 {{ currentPage }} / {{ totalPages }} 页</span
            >
            <button
              class="btn btn-sm btn-default"
              id="nextCoursePageBtn"
              :disabled="currentPage === totalPages"
              @click="nextPage"
            >
              下一页 »
            </button>
          </div>
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { getCourses } from "../../../api/info_manage/course";
import { createCourse, updateCourse, deleteCourse } from "../../../api/info_manage/teacher";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { useRouter } from "vue-router";

const router = useRouter();

type Course = {
  course_id: string;
  course_name: string;
  credit: number;
  hours_per_week: number;
  category: string;
  course_description: string;
  teacher_id: string;
  teacher_name: string;
};

const loginUserStore = useuserLoginStore();

// 响应式数据
const loading = ref(false);
const courses = ref<Course[]>([]);
const filter = ref({
  course_id: "",
  course_name: "",
  teacher_name: "",
});
const userDropdownVisible = ref(false);
const currentPage = ref(1);
const coursesPerPage = ref(10);
const courseData = ref<Course>({
  course_id: "",
  course_name: "",
  credit: 0,
  hours_per_week: 0,
  category: "",
  course_description: "",
  teacher_id: "",
  teacher_name: "",
});
const notification = ref({
  message: "",
  type: "info",
});

const filteredCourses = computed(() => {
  return courses.value.filter((course) => {
    return (
      String(course.course_id).includes(filter.value.course_id) &&
      String(course.course_name).includes(filter.value.course_name) &&
      String(course.teacher_name || course.teacher_id || "").includes(
        filter.value.teacher_name
      )
    );
  });
});

const totalPages = computed(() => {
  return Math.ceil(filteredCourses.value.length / coursesPerPage.value);
});

const paginatedCourses = computed(() => {
  const startIndex = (currentPage.value - 1) * coursesPerPage.value;
  const endIndex = startIndex + coursesPerPage.value;
  return filteredCourses.value.slice(startIndex, endIndex);
});

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

const filterAndDisplayCourses = async () => {
  loading.value = true;
  showNotification("正在查询课程列表...", "info");
  try {
    const response = await getCourses(filter.value);
    courses.value = response.data.data.items;
    loading.value = false;
    if (courses.value.length > 0) {
      showNotification("课程列表加载完毕。", "success");
    } else {
      showNotification("没有找到符合条件的课程。", "info");
    }
  } catch (error) {
    loading.value = false;
    showNotification("查询课程列表失败，请稍后重试。", "error");
    console.error(error);
  }
};

const resetFilters = () => {
  filter.value = {
    course_id: "",
    course_name: "",
    teacher_name: "",
  };
  setTimeout(filterAndDisplayCourses, 100);
};

const showNotification = (message: string, type = "info") => {
  notification.value = {
    message,
    type,
  };
  setTimeout(() => {
    notification.value = {
      message: "",
      type: "info",
    };
  }, 3000);
};

// 生命周期钩子
onMounted(() => {
  filterAndDisplayCourses();
});

const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

const handleLogout = () => {
  showNotification("正在退出登录...", "info");
  setTimeout(() => {
    loginUserStore.setLoginUserUnlogin();
    router.push("/login");
  }, 3000);
};

const handleChangePassword = () => {
  //window.location.href = "../change-password";
  router.push("/change-password");
};
</script>

<style scoped>
/* admin_course_management_style.css */

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

/* Top Bar - RE-CONFIRMED STYLES */
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
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
  overflow: hidden;
}
.top-bar .user-avatar i {
  font-size: 18px;
  color: #409eff;
  line-height: 1;
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
.top-bar .user-dropdown-menu a {
  display: block;
  padding: 8px 15px;
  color: #606266;
  font-size: 14px;
}
.top-bar .user-dropdown-menu a:hover {
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

/* General Button Styles */
.btn {
  padding: 8px 15px;
  border: 1px solid #dcdfe6;
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
  justify-content: center;
  gap: 6px;
  background-color: #fff;
  color: #606266;
}
.btn i {
  font-size: 1em;
  line-height: 1;
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
.btn-secondary {
  background-color: #909399;
  border-color: #909399;
  color: white;
}
.btn-secondary:hover {
  background-color: #a6a9ad;
  border-color: #a6a9ad;
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
.btn-danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}
.btn-danger:hover {
  background-color: #f78989;
  border-color: #f78989;
}
.btn-default:hover {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
}
.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
}

/* Notification Area (General) */
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 10px 15px;
  border-radius: 4px;
  color: white;
  z-index: 1000;
  opacity: 0;
  transition: opacity 0.3s;
}
.notification.success {
  background-color: #67c23a;
}
.notification.error {
  background-color: #f56c6c;
}
.notification.info {
  background-color: #409eff;
}
.notification.show {
  opacity: 1;
}

/* Page Specific Styles for Course Management */
.page-main {
  flex-grow: 1;
  padding: 25px;
  width: 100%;
  box-sizing: border-box;
}
.container.course-management-container {
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
  margin-right: 10px;
  color: #409eff;
  font-size: 1.05em;
}

/* Filter Form Grid */
.filter-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px 20px;
  align-items: end;
}
.filter-form-grid .form-group {
  margin-bottom: 0;
}
.filter-form-grid .form-action-group {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}
@media (min-width: 1024px) {
  .filter-form-grid .form-action-group {
    grid-column: auto;
    align-self: end;
    margin-top: 0;
  }
}

/* Forms */
.form-group {
  margin-bottom: 18px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}
.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="url"],
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
}
.form-group input[type="text"]:focus,
.form-group input[type="number"]:focus,
.form-group input[type="url"]:focus,
.form-group select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* Table */
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
  white-space: normal;
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
.no-courses-row td {
  text-align: center;
  color: #909399;
  padding: 30px 15px;
  font-style: italic;
}
td.actions-cell .btn {
  margin-right: 5px;
}
td.actions-cell .btn:last-child {
  margin-right: 0;
}

/* Pagination Controls */
.pagination-controls {
  margin-top: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.pagination-controls button {
  margin: 0 5px;
}
.pagination-controls #coursePageInfo {
  font-size: 14px;
  color: #606266;
}

/* Modal Styles - CRITICAL FIXES FOR POSITIONING */
.modal {
  display: none; /* Hidden by default, JS will change to 'flex' */
  position: fixed; /* Cover the whole screen */
  z-index: 1050; /* Sit on top of other content */
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto; /* Enable scroll if modal content is too long */
  background-color: rgba(0, 0, 0, 0.5); /* Semi-transparent backdrop */
  /* Flexbox for centering the modal-content */
  justify-content: center; /* Horizontally center */
  align-items: center; /* Vertically center */
}
.modal-content {
  /* This is the actual modal box */
  background-color: #fff;
  margin: auto; /* Fallback for older browsers / if flex not perfectly working */
  padding: 0; /* Card will provide padding */
  border: 1px solid #888;
  width: 90%;
  max-width: 700px; /* Adjust max-width as needed for course form */
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  animation: modal-appear 0.3s ease-out;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}
@keyframes modal-appear {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
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
  font-weight: 500;
}
.close-modal-btn {
  background: none;
  border: none;
  font-size: 1.8rem;
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
.modal-body {
  padding: 20px 25px;
  overflow-y: auto;
  flex-grow: 1;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 25px;
  border-top: 1px solid #ebeef5;
  margin-top: auto;
  background-color: #f9fafc;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}
</style>
