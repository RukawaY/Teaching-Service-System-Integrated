<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container request-grade-change-container">
        <!-- 筛选表单 -->
        <div class="card filter-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-filter" /> 筛选申请列表
          </h2>
          <form id="filterForm" class="filter-form-grid">
            <div class="form-group">
              <label for="auditStatus">审核状态:</label>
              <select
                id="auditStatus"
                name="audit_status"
                v-model="auditStatus"
                @change="fetchGradeApplies"
              >
                <option value="">全部</option>
                <option value="1">待审核</option>
                <option value="2">已通过</option>
                <option value="3">已拒绝</option>
              </select>
            </div>
          </form>
        </div>

        <!-- 申请列表 -->
        <div class="card grade-apply-list-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-list" /> 历史申请列表
          </h2>
          <table class="table">
            <thead>
              <tr>
                <th>学生姓名</th>
                <th>课程名称</th>
                <th>原成绩</th>
                <th>新成绩</th>
                <th>审核状态</th>
                <th>审核答复</th>
                <th>申请时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="apply in gradeApplies" :key="apply.id">
                <td>{{ apply.student_name }}</td>
                <td>{{ apply.course_name }}</td>
                <td>{{ apply.apply.oldScore }}</td>
                <td>{{ apply.apply.newScore }}</td>
                <td>{{ mapStatus(apply.apply.auditStatus) }}</td>
                <td>{{ apply.apply.audit_reason }}</td>
                <td>{{ formatTimestamp(apply.apply.applyTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="card course-student-selection-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-list-ol" /> 第一步：选择课程与学生
          </h2>
          <form id="selectCourseStudentForm" class="filter-form-grid">
            <div class="form-group">
              <label for="requestCourse">选择课程:</label>
              <select
                id="requestCourse"
                name="courseId"
                v-model="selectedCourse"
                @change="populateSectionSelect"
              >
                <option :value="null">-- 请选择课程 --</option>
                <option
                  v-for="course in courses"
                  :key="course.course_id"
                  :value="course"
                >
                  {{ course.course_name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="requestSection">选择开课时间:</label>
              <select
                id="requestSection"
                name="sectionId"
                v-model="selectedSection"
                :disabled="!selectedCourse"
                @change="populateStudentSelect"
              >
                <option :value="null">-- 请先选择课程 --</option>
                <option
                  v-for="section in sections"
                  :key="section.section_id"
                  :value="section"
                >
                  {{ section.sec_year || "未知学年" }} 学年
                  {{ section.semester || "未知学期" }} 学期
                  {{
                    section.classroom_location
                      ? `- ${section.classroom_location}`
                      : ""
                  }}
                  {{ section.sec_time ? `(${section.sec_time})` : "" }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="requestStudent">选择学生:</label>
              <select
                id="requestStudent"
                name="studentId"
                v-model="selectedStudent"
                :disabled="!selectedSection"
              >
                <option :value="null">-- 请先选择开课 --</option>
                <option
                  v-for="student in students"
                  :key="student.user_id"
                  :value="student"
                >
                  {{ student.name }}
                </option>
              </select>
            </div>
            <div class="divider"></div>
            <div class="divider"></div>
            <div class="form-action-group">
              <button
                type="button"
                class="btn btn-primary"
                id="loadGradeInfoBtn"
                :disabled="!selectedStudent"
                @click="loadGradeInfo"
              >
                <FontAwesomeIcon icon="fas fa-search-dollar" /> 加载成绩信息
              </button>
            </div>
          </form>
        </div>

        <div
          class="card grade-modification-card"
          id="gradeModificationCard"
          :style="{ display: gradeModificationCardVisible ? 'block' : 'none' }"
        >
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-edit" /> 第二步：填写修改信息
          </h2>
          <form
            id="gradeModificationForm"
            @submit.prevent="submitGradeChangeRequest"
          >
            <div class="readonly-info-grid">
              <div class="info-item">
                <label>学生姓名:</label
                ><span id="displayStudentName">{{
                  selectedStudent?.name || "--"
                }}</span>
              </div>
              <div class="info-item">
                <label>课程名称:</label
                ><span id="displayCourseName">{{
                  selectedCourse?.course_name || "--"
                }}</span>
              </div>
              <div class="info-item">
                <label>当前总评成绩:</label>
                <strong id="displayCurrentGrade">{{
                  selectedGradeInfo?.grade_base?.score !== null
                    ? selectedGradeInfo?.grade_base?.score
                    : "--"
                }}</strong>
              </div>
            </div>

            <hr class="section-divider" />

            <div class="form-group">
              <label for="newGrade">修改后总评成绩:</label>
              <input
                type="number"
                id="newGrade"
                name="newGrade"
                v-model="newGrade"
                min="0"
                max="100"
                step="0.1"
                placeholder="输入0-100之间的数字"
                required
              />
            </div>
            <div class="form-group">
              <label for="modificationReason">修改原因 (详细说明):</label>
              <textarea
                id="modificationReason"
                name="reason"
                v-model="modificationReason"
                rows="5"
                placeholder="请详细填写修改成绩的理由，例如：录入错误、补交作业调整等..."
                required
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary">
                <FontAwesomeIcon icon="fas fa-paper-plane" /> 提交修改申请
              </button>
              <button
                type="button"
                class="btn btn-default"
                id="cancelModificationBtn"
                @click="cancelModification"
              >
                <FontAwesomeIcon icon="fas fa-times" /> 取消
              </button>
            </div>
          </form>
        </div>
        <div
          id="notificationArea"
          class="notification"
          :style="{ display: notificationVisible ? 'block' : 'none' }"
          :class="notificationType"
        >
          {{ notificationMessage }}
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  getMyCourses,
  getMyCourseSections,
  getSectionGrades,
  submitGradeApply,
  getTeacherGradeApplies,
} from "../../../api/info_manage/teacher";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { useRouter } from "vue-router";

// 定义接口
interface Course {
  course_id: string;
  course_name: string;
  // 其他可能的属性
}

interface Section {
  section_id: string;
  sec_year: number;
  semester: string;
  classroom_location?: string;
  sec_time?: string;
  // 其他可能的属性
}

interface Student {
  user_id: string;
  name: string;
  account: string;
  role: string;
  department: string;
  contact: string;
  avatarPath: string;
  // 其他可能的属性
}

interface GradeApply {
  id: number;
  studentName: string;
  courseName: string;
  old_score: number;
  new_score: number;
  audit_status: string;
  apply_time: string;
}

const router = useRouter();
const loginUserStore = useuserLoginStore();
// 响应式数据
const userDropdownVisible = ref(false);
const selectedCourse = ref<Course | null>(null);
const selectedSection = ref<Section | null>(null);
const selectedStudent = ref<Student | null>(null);
const courses = ref<Course[]>([]);
const sections = ref<Section[]>([]);
const students = ref<Student[]>([]);
const gradeModificationCardVisible = ref(false);
const selectedGradeInfo = ref<any>(null);
const newGrade = ref("");
const modificationReason = ref("");
const notificationVisible = ref(false);
const notificationMessage = ref("");
const notificationType = ref("info");
const formatTimestamp = (timestamp: number) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");

  // 返回你需要的时间格式，这里是 YYYY-MM-DD HH:MM:SS
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 申请列表相关数据
const gradeApplies = ref<GradeApply[]>([]);
const auditStatus = ref("");
const mapStatus = (status: number) => {
  if (status == 0) return "待审核";
  else if (status == 1) return "已通过";
  else return "已拒绝";
};

// 填充课程列表
const populateCourseSelect = async () => {
  courses.value = [];
  sections.value = [];
  students.value = [];
  selectedCourse.value = null;
  selectedSection.value = null;
  selectedStudent.value = null;
  gradeModificationCardVisible.value = false;

  try {
    const response = await getMyCourses({
      page: null,
      size: null,
      course_name: "",
      teacher_id: "",
      teacher_name: "",
      category: "",
    });
    courses.value = response.data.data.items;
  } catch (error) {
    showNotification("获取课程列表失败，请稍后重试。", "error");
    console.error(error);
  }
};

// 选择课程后填充开课列表
const populateSectionSelect = async () => {
  sections.value = [];
  students.value = [];
  selectedSection.value = null;
  selectedStudent.value = null;
  gradeModificationCardVisible.value = false;

  if (!selectedCourse.value) {
    showNotification("请先选择课程", "error");
    return;
  }

  try {
    console.log("选择的课程ID:", selectedCourse.value.course_id);

    const response = await getMyCourseSections(
      selectedCourse.value?.course_id,
      {
        semester: "",
        // 不传递sec_year参数
      }
    );

    console.log("API返回的开课数据:", response);

    // 检查并处理API返回的数据
    if (response && response.data) {
      let sectionsData = [];

      // 尝试从不同可能的数据结构中获取开课列表
      if (Array.isArray(response.data)) {
        sectionsData = response.data;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        sectionsData = response.data.data;
      } else if (
        response.data.data &&
        response.data.data.items &&
        Array.isArray(response.data.data.items)
      ) {
        sectionsData = response.data.data.items;
      } else {
        console.error("API返回的开课数据结构不符合预期:", response.data);
        showNotification("API返回的开课数据结构不符合预期", "error");
        return;
      }

      // 将返回的数据映射到我们的Section接口
      sections.value = sectionsData.map((item: any) => ({
        section_id: item.section_id || item.sectionId,
        sec_year: item.sec_year || item.secYear || 0,
        semester: item.semester || "",
        classroom_location: item.classroom_location || "",
        sec_time: item.sec_time || item.secTime || "",
      }));

      console.log("处理后的开课列表:", sections.value);

      if (sections.value.length === 0) {
        showNotification("该课程暂无开课信息", "info");
      }
    } else {
      showNotification("获取开课列表失败，返回数据为空", "error");
    }
  } catch (error) {
    showNotification("获取开课列表失败，请稍后重试。", "error");
    console.error("获取开课列表错误:", error);
  }
};

// 选择开课后填充学生列表
const populateStudentSelect = async () => {
  students.value = [];
  selectedStudent.value = null;
  gradeModificationCardVisible.value = false;

  if (!selectedSection.value) {
    showNotification("请先选择开课时间", "error");
    return;
  }

  try {
    console.log("选择的开课ID:", selectedSection.value.section_id);

    const response = await getSectionGrades(selectedSection.value?.section_id, {
      student_name: "",
      student_id: "",
    });

    console.log("API返回的学生数据:", response);

    // 确保API返回了有效数据
    if (response && response.data.data && response.data.data.user) {
      // 将返回的数据映射到我们的Student接口
      students.value = response.data.data.user.map((item: any) => ({
        user_id: item.user_id,
        name: item.name || "",
        account: item.account,
        role: item.role,
        department: item.department,
        contact: item.contact,
        avatarPath: item.avatarPath,
      }));

      console.log("处理后的学生列表:", students.value);

      if (students.value.length === 0) {
        showNotification("该开课暂无学生信息", "info");
      }
    } else {
      showNotification("获取学生列表失败，返回数据格式不符合预期", "error");
      console.error("API返回的学生数据结构不符合预期:", response);
    }
  } catch (error) {
    showNotification("获取学生列表失败，请稍后重试。", "error");
    console.error("获取学生列表错误:", error);
  }
};

// 加载成绩信息
const loadGradeInfo = async () => {
  if (
    !selectedSection.value ||
    !selectedCourse.value ||
    !selectedStudent.value
  ) {
    showNotification("请先完整选择课程,开课和学生。", "error");
    return;
  }

  try {
    console.log("加载成绩信息，学生ID:", selectedStudent.value?.user_id);

    const response = await getSectionGrades(selectedSection.value?.section_id, {
      student_name: selectedStudent.value?.name,
      student_id: selectedStudent.value?.user_id,
    });

    console.log("获取到的成绩数据:", response);

    // 确保API返回了有效数据
    if (
      response &&
      response.data &&
      response.data.data.grade &&
      response.data.data.grade.length > 0
    ) {
      selectedGradeInfo.value = {
        grade_id: response.data.data.grade[0].gradeBase.gradeId,
        score: response.data.data.grade[0].gradeBase.score,
        grade_base: {
          score: response.data.data.grade[0].gradeBase.score,
        },
        // 保存原始数据，以便可能需要的其他字段
        original: response.data.data.grade[0],
      };

      console.log("处理后的成绩信息:", selectedGradeInfo.value);

      newGrade.value = "";
      modificationReason.value = "";
      gradeModificationCardVisible.value = true;
      showNotification("已加载成绩信息，请填写修改详情。", "info");
    } else {
      showNotification("未找到学生成绩信息，或数据格式不符合预期", "error");
      console.error("API返回的成绩数据不符合预期:", response);
    }
  } catch (error) {
    showNotification("加载成绩信息失败，请稍后重试。", "error");
    console.error("获取成绩信息错误:", error);
  }
};

// 取消修改
const cancelModification = () => {
  gradeModificationCardVisible.value = false;
  selectedStudent.value = null;
  showNotification("修改已取消。", "info");
};

// 显示通知
const showNotification = (message: string, type: string) => {
  notificationMessage.value = message;
  notificationType.value = type;
  notificationVisible.value = true;
  setTimeout(() => {
    notificationVisible.value = false;
  }, 3000);
};

// 提交成绩修改申请
const submitGradeChangeRequest = async () => {
  if (!selectedGradeInfo.value) {
    showNotification("请先加载学生成绩信息。", "error");
    return;
  }

  const newGradeValue = parseFloat(newGrade.value);
  if (isNaN(newGradeValue) || newGradeValue < 0 || newGradeValue > 100) {
    showNotification("新成绩必须是0到100之间的数字。", "error");
    return;
  }

  if (!modificationReason.value.trim()) {
    showNotification("请填写详细的修改原因。", "error");
    return;
  }

  // 构建请求数据
  const requestData = {
    grade_id: selectedGradeInfo.value.grade_id, // 成绩ID
    old_score: selectedGradeInfo.value.score, // 原始成绩
    new_score: newGradeValue, // 新成绩
    reason: modificationReason.value, // 修改原因
    studentName: selectedStudent.value?.name || "",
    courseName: selectedCourse.value?.course_name || "",
    // 可能需要其他数据
    student_id: selectedStudent.value?.user_id,
    course_id: selectedCourse.value?.course_id,
    section_id: selectedSection.value?.section_id,
  };

  console.log("提交的申请数据:", requestData);

  try {
    showNotification("正在提交修改申请...", "info");
    const response = await submitGradeApply(requestData);

    console.log("申请提交响应:", response);

    showNotification(
      `已成功为 ${selectedStudent.value?.name || ""} 的《${
        selectedCourse.value?.course_name || ""
      }》提交成绩修改申请，等待管理员审核。`,
      "success"
    );

    // 重置状态
    gradeModificationCardVisible.value = false;
    selectedSection.value = null;
    selectedCourse.value = null;
    selectedStudent.value = null;
    sections.value = [];
    courses.value = [];
    students.value = [];
    selectedGradeInfo.value = null;
    newGrade.value = "";
    modificationReason.value = "";

    // 重新获取申请列表
    fetchGradeApplies();
  } catch (error) {
    showNotification("提交申请失败，请稍后重试。", "error");
    console.error("提交申请错误:", error);
  }
};

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

// 处理退出登录
const handleLogout = () => {
  showNotification("正在退出登录...", "info");
  setTimeout(() => {
    loginUserStore.setLoginUserUnlogin();
    window.location.href = "../login";
  }, 1500);
};

// 处理修改密码
const handleChangePassword = () => {
  //window.location.href = "../change-password";
  router.push("/change-password");
};

// 获取申请列表
const fetchGradeApplies = async () => {
  try {
    const response = await getTeacherGradeApplies({
      audit_status: auditStatus.value,
    });
    gradeApplies.value = response.data.data;
  } catch (error) {
    showNotification("获取申请列表失败，请稍后重试。", "error");
    console.error(error);
  }
};

onMounted(() => {
  populateCourseSelect();
  fetchGradeApplies();
});
</script>

<style scoped>
/* Global Styles - ENSURING CONSISTENCY */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8; /* Consistent page background */
  color: #333;
  line-height: 1.6;
  padding-top: 60px; /* For fixed top-bar */
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

/* Main Content Specifics for this page */
.page-main {
  flex-grow: 1;
  padding: 25px;
  width: 100%;
  box-sizing: border-box;
}
.container.request-grade-change-container {
  max-width: 900px;
  margin: 0 auto;
}

/* Card Styles */
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
}

/* Form Styles */
.filter-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px 20px;
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
  font-weight: 500;
}
.form-group input[type="text"],
.form-group input[type="number"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}
.form-group input[type="text"]:focus,
.form-group input[type="number"]:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #409eff;
  outline: 0;
}
.form-group select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg class='icon' viewBox='0 0 1024 1024' xmlns='http://www.w3.org/2000/svg' width='12' height='12'%3E%3Cpath d='M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-307.2-268.8c-12.8-10.666667-14.933333-29.866667-4.266666-42.666666 10.666667-12.8 29.866667-14.933333 42.666667-4.266667l292.266667 256 292.266667-256c12.8-10.666667 32-8.533333 42.666667 4.266667s8.533333 32-4.266667 42.666666l-307.2 268.8c-6.4 4.266667-12.8 8.533333-21.333333 8.533334z' fill='%23C0C4CC'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 12px;
  padding-right: 30px;
}
.form-group select[disabled],
.form-group input[disabled],
.form-group button[disabled] {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}
.form-text.text-muted {
  font-size: 0.85em;
  color: #909399;
  display: block;
  margin-top: 4px;
}

/* Readonly Info Grid */
.readonly-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px 20px;
  margin-bottom: 20px;
  font-size: 14px;
}
.info-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.info-item label {
  color: #606266;
  font-weight: 500;
  margin-right: 8px;
}
.info-item span,
.info-item strong {
  color: #303133;
}
.info-item strong {
  font-weight: bold;
  color: #e6a23c;
}

.section-divider {
  grid-column: 1 / -1;
  border: none;
  border-top: 1px dashed #ebeef5;
  margin: 20px 0;
}

/* Form Actions */
.form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn {
  padding: 9px 18px;
  border: 1px solid transparent;
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
  gap: 6px;
}
.btn i {
  font-size: 1em;
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
.btn-default {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}
.btn-default:hover {
  border-color: #409eff;
  color: #409eff;
}

/* Notification Area */
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

/* Bottom Bar - ENSURING CONSISTENCY */
.bottom-bar {
  background-color: #f5f5f5b7;
  padding: 12px 0;
  text-align: center; /* Ensures the .copyright-text is centered */
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
  margin: 0; /* Remove default paragraph margins */
  line-height: 1.5;
}

/* Responsive Adjustments */
@media (max-width: 992px) {
  .filter-form-grid {
    grid-template-columns: 1fr;
  }
  .filter-form-grid .form-action-group {
    justify-content: center;
  }
}
@media (max-width: 768px) {
  .top-bar {
    padding: 0 15px;
    height: 50px;
  }
  body {
    padding-top: 50px;
  }
  .top-bar .system-name {
    font-size: 18px;
  }
  .top-bar .system-subname {
    display: none;
  }
  .page-main {
    padding: 15px;
  }
  .readonly-info-grid {
    grid-template-columns: 1fr;
  }
  .form-actions {
    justify-content: center;
  }
}

/* 表格样式 */
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  text-align: left;
}
.table th {
  background-color: #f5f7fa;
  font-weight: 500;
}
</style>
