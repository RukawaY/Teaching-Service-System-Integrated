<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container">
        <div class="card select-course-card">
          <h2 class="card-title">选择课程进行成绩提交</h2>
          <form id="selectCourseForm">
            <div class="form-row">
              <div class="form-group">
                <label for="requestCourse">选择课程:</label>
                <select
                  id="requestCourse"
                  name="courseId"
                  v-model="selectedCourse"
                  @change="populateSectionSelect"
                >
                  <option value="">-- 请选择课程 --</option>
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
                <label for="requestStudent">选择开课时间:</label>
                <select
                  id="requestStudent"
                  name="studentId"
                  v-model="selectedSection"
                  :disabled="!selectedCourse"
                >
                  <option value="">-- 请选择开课时间 --</option>
                  <option
                    v-for="section in sections"
                    :key="section.sectionId"
                    :value="section"
                  >
                    {{ section.secYear }} 学年 {{ section.semester }} 学期 -
                    {{ section.classroom_location || "未知教室" }}
                    {{ section.secTime ? "(" + section.secTime + ")" : "" }}
                  </option>
                </select>
              </div>
            </div>
            <div class="form-action-group">
              <button
                type="button"
                class="btn btn-primary"
                id="loadStudentsBtn"
                :disabled="!selectedSection"
                @click="loadStudents"
              >
                <FontAwesomeIcon icon="fas fa-search" /> 加载学生名单
              </button>
            </div>
          </form>
        </div>

        <div
          class="card grade-entry-card"
          id="gradeEntryCard"
          :style="{ display: showGradeEntryCard ? 'block' : 'none' }"
        >
          <h2 class="card-title" id="gradeEntryTitle">成绩录入</h2>
          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>序号</th>
                  <th>姓名</th>
                  <th>总评成绩</th>
                  <th>GPA</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody id="studentListBody">
                <tr v-if="loadingStudents">
                  <td colspan="5" style="text-align: center; padding: 20px">
                    正在加载学生数据... <i class="fas fa-spinner fa-spin"></i>
                  </td>
                </tr>
                <tr
                  v-else
                  v-for="(studentGrade, index) in studentGrades"
                  :key="studentGrade.student_id"
                >
                  <td>{{ index + 1 }}</td>
                  <td>{{ studentGrade.name }}</td>
                  <td>
                    <input
                      type="number"
                      v-model="studentGrade.score"
                      :disabled="studentGrade.submit_status === 1"
                      @input="validateScoreInput(studentGrade)"
                    />
                  </td>
                  <td>
                    <input type="number" v-model="studentGrade.gpa" readonly />
                  </td>
                  <td>
                    <span
                      v-if="studentGrade.submit_status === 1"
                      class="status-submitted"
                      >已提交</span
                    >
                    <span v-else>
                      <input
                        type="checkbox"
                        v-model="studentGrade.selected"
                        :id="`select-student-${studentGrade.student_id}`"
                      />
                      <label :for="`select-student-${studentGrade.student_id}`"
                        >未提交</label
                      >
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="action-buttons">
            <button
              type="button"
              class="btn btn-success"
              id="submitAllGradesBtn"
              @click="submitSelectedGrades"
              :disabled="!hasSelectedGrades"
            >
              <FontAwesomeIcon icon="fas fa-check-circle" /> 提交选中成绩
            </button>
            <button
              type="button"
              class="btn btn-primary"
              id="selectAllBtn"
              @click="selectAllUnsubmitted"
              :disabled="allSubmitted"
            >
              <FontAwesomeIcon icon="fas fa-check-square" /> 全选未提交
            </button>
          </div>
        </div>

        <div
          id="notificationArea"
          class="notification"
          :class="notification.type"
          :style="{ display: notification.message ? 'block' : 'none' }"
        >
          {{ notification.message }}
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import {
  getMyCourses,
  getMyCourseSections,
  getSectionGrades,
  submitStudentGrades,
} from "../../../api/info_manage/teacher";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { useRouter } from "vue-router";
import { isNullOrUndef } from "chart.js/helpers";

const router = useRouter();
const loginUserStore = useuserLoginStore();
// 响应式数据
const userDropdownVisible = ref(false);
const selectedCourse = ref<any>(null);
const selectedSection = ref<any>(null);
const courses = ref<any[]>([]);
const sections = ref<any[]>([]);
const students = ref<any[]>([]);
const grades = ref<any[]>([]);
const loadingStudents = ref(false);
const showGradeEntryCard = ref(false);
const notification = ref({
  message: "",
  type: "info",
});
const studentGrades = ref<any[]>([]);

// 生命周期钩子
onMounted(() => {
  populateCourseSelect();
});

const validateScoreInput = (studentGrade: any) => {
  const gradeValue = parseInt(studentGrade.score);
  if (gradeValue > 100) {
    studentGrade.score = 100;
  } else if (gradeValue < 0) {
    studentGrade.score = 0;
  }
  if (gradeValue >= 90) {
    studentGrade.gpa = 4.0;
  } else if (gradeValue >= 80) {
    studentGrade.gpa = 3.0;
  } else if (gradeValue >= 70) {
    studentGrade.gpa = 2.0;
  } else if (gradeValue >= 60) {
    studentGrade.gpa = 1.0;
  } else {
    studentGrade.gpa = 0.0;
  }
};

// 检查是否有选中的成绩
const hasSelectedGrades = computed(() => {
  return studentGrades.value.some(
    (grade) => !grade.submit_status && grade.selected
  );
});

// 检查是否所有成绩都已提交
const allSubmitted = computed(() => {
  return studentGrades.value.every((grade) => grade.submit_status === 1);
});

// 全选未提交的成绩
const selectAllUnsubmitted = () => {
  studentGrades.value.forEach((grade) => {
    if (grade.submit_status !== 1) {
      grade.selected = true;
    }
  });
};

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

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

const populateCourseSelect = async () => {
  courses.value = [];
  sections.value = [];
  students.value = [];
  grades.value = [];
  selectedCourse.value = null;
  selectedSection.value = null;
  showGradeEntryCard.value = false;

  try {
    const response = await getMyCourses({
      page: null,
      size: null,
      course_name: "",
      teacher_id: "",
      teacher_name: "",
      category: "",
    });

    console.log("获取到的课程数据:", response);

    // 防御性编程：检查数据结构
    if (response && response.data) {
      if (response.data.data && response.data.data.items) {
        courses.value = response.data.data.items;
      } else if (response.data.items) {
        courses.value = response.data.items;
      } else if (Array.isArray(response.data)) {
        courses.value = response.data;
      } else {
        showNotification("API返回的课程数据结构不符合预期", "error");
      }
    } else {
      showNotification("获取课程列表失败，返回数据为空", "error");
    }
  } catch (error) {
    showNotification("获取课程列表失败，请稍后重试。", "error");
    console.error("获取课程列表错误:", error);
  }
};

// 选择课程后填充开课列表
const populateSectionSelect = async () => {
  sections.value = [];
  students.value = [];
  grades.value = [];
  selectedSection.value = null;
  showGradeEntryCard.value = false;

  if (!selectedCourse.value) return;

  try {
    console.log("选择的课程:", selectedCourse.value);

    // 获取课程ID，处理可能的不同属性名
    const courseId =
      selectedCourse.value.course_id ||
      selectedCourse.value.courseId ||
      selectedCourse.value.id;

    if (!courseId) {
      console.error("无法获取课程ID，选中的课程对象:", selectedCourse.value);
      showNotification("无法获取课程ID，请重新选择课程", "error");
      return;
    }

    const response = await getMyCourseSections(courseId, {
      semester: "",
      secYear: null,
    });

    console.log("获取到的开课数据:", response);

    // 防御性编程：检查数据结构
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        sections.value = response.data;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        sections.value = response.data.data;
      } else {
        showNotification("API返回的开课数据结构不符合预期", "error");
      }
    } else {
      showNotification("获取开课列表失败，返回数据为空", "error");
    }
  } catch (error) {
    showNotification("获取开课列表失败，请稍后重试。", "error");
    console.error("获取开课列表错误:", error);
  }
};

// 加载学生名单
const loadStudents = async () => {
  loadingStudents.value = true;
  showNotification("正在加载学生名单...", "info");

  if (!selectedSection.value) {
    loadingStudents.value = false;
    showNotification("请先选择开课时间", "error");
    return;
  }

  // 记录选中的开课section信息，帮助调试
  console.log("选中的开课section:", selectedSection.value);
  console.log("section属性:", Object.keys(selectedSection.value));

  // 使用sectionId或fallback到section_id
  const sectionId =
    selectedSection.value.sectionId || selectedSection.value.section_id;

  if (!sectionId) {
    loadingStudents.value = false;
    showNotification("无法获取开课ID，请重新选择", "error");
    console.error(
      "选中的section没有sectionId或section_id属性:",
      selectedSection.value
    );
    return;
  }

  try {
    const response = await getSectionGrades(sectionId, {
      student_id: "",
      student_name: "",
    });

    console.log("获取到的学生成绩数据:", response);

    // 防御性编程：检查数据结构
    if (response && response.data) {
      if (response.data.user) {
        students.value = response.data.user;
      } else if (response.data.data && response.data.data.user) {
        // 处理可能的嵌套结构
        students.value = response.data.data.user;
      } else {
        students.value = [];
        showNotification("未找到学生数据", "error");
      }

      if (response.data.grade) {
        grades.value = response.data.grade;
      } else if (response.data.data && response.data.data.grade) {
        // 处理可能的嵌套结构
        grades.value = response.data.data.grade;
      } else {
        grades.value = [];
        showNotification("未找到成绩数据", "error");
      }

      // 计算studentGrades
      if (students.value.length > 0) {
        studentGrades.value = students.value.map((student, index) => {
          // 找到与学生匹配的成绩记录
          const gradeRecord = grades.value.find(
            (g) => g.gradeBase && g.gradeBase.studentId === student.user_id
          );

          if (!gradeRecord || !gradeRecord.gradeBase) {
            console.warn(
              `未找到学生(${student.name}, ID: ${student.user_id})的成绩记录`
            );
            return {
              student_id: student.user_id,
              name: student.name,
              score: 0,
              gpa: 0,
              submit_status: 0,
              selected: false,
            };
          }

          return {
            student_id: student.user_id,
            name: student.name,
            score: gradeRecord.gradeBase.score,
            gpa: gradeRecord.gradeBase.gpa,
            submit_status: gradeRecord.gradeBase.submitStatus,
            selected: false,
          };
        });

        // 只有在有数据时才显示成绩输入卡片
        showGradeEntryCard.value = true;
        showNotification("学生名单加载完毕。", "success");
      } else {
        showNotification("学生数据为空", "info");
      }
    } else {
      showNotification("API返回数据结构不符合预期", "error");
    }
  } catch (error) {
    showNotification("加载学生名单失败，请稍后重试。", "error");
    console.error("加载学生名单错误:", error);
  } finally {
    loadingStudents.value = false;
  }
};

// 提交选中的成绩
const submitSelectedGrades = async () => {
  if (!selectedSection.value) {
    showNotification("请先选择开课时间", "error");
    return;
  }

  const selectedGrades = studentGrades.value.filter(
    (grade) => grade.selected && grade.submit_status !== 1
  );

  if (selectedGrades.length === 0) {
    showNotification("请选择至少一个未提交的成绩", "error");
    return;
  }

  // 记录选中的开课section信息，帮助调试
  console.log("提交成绩时选中的section:", selectedSection.value);

  // 使用sectionId或fallback到section_id
  const sectionId =
    selectedSection.value.sectionId || selectedSection.value.section_id;

  if (!sectionId) {
    showNotification("无法获取开课ID，请重新选择", "error");
    console.error(
      "选中的section没有sectionId或section_id属性:",
      selectedSection.value
    );
    return;
  }

  try {
    let successSubmissions = 0;
    const totalSubmissions = selectedGrades.length;

    for (const studentGrade of selectedGrades) {
      // 将数据包装在数组中，符合后端期望的格式
      console.log(`${studentGrade.score === ""}`);
      if (studentGrade.score === "") {
        showNotification("总评成绩不能为空", "info");
        return;
      }
      const data = [
        {
          student_id: studentGrade.student_id,
          score: studentGrade.score,
          gpa: studentGrade.gpa,
        },
      ];
      console.log(`提交学生(${studentGrade.name})的成绩:`, data);

      try {
        await submitStudentGrades(sectionId, data);
        successSubmissions++;
        // 更新学生成绩状态为已提交
        const index = studentGrades.value.findIndex(
          (grade) => grade.student_id === studentGrade.student_id
        );
        if (index !== -1) {
          studentGrades.value[index].submit_status = 1;
          studentGrades.value[index].selected = false;
        }
      } catch (submissionError) {
        console.error(
          `提交学生(${studentGrade.name})的成绩失败:`,
          submissionError
        );
      }
    }

    if (successSubmissions === totalSubmissions) {
      showNotification(
        `选中的成绩(${successSubmissions}条)已成功提交！`,
        "success"
      );
    } else {
      showNotification(
        `部分成绩提交成功(${successSubmissions}/${totalSubmissions})，请检查日志`,
        "info"
      );
    }
  } catch (error) {
    showNotification("提交成绩失败，请稍后重试。", "error");
    console.error("提交成绩错误:", error);
  }
};

// 显示通知
const showNotification = (
  message: string,
  type: "info" | "error" | "success" = "info"
) => {
  notification.value = {
    message,
    type,
  };
  setTimeout(() => {
    notification.value = {
      message: "",
      type: "info",
    };
  }, 5000);
};
</script>

<style scoped>
/* 这里可以直接复用原有的 CSS 样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji",
    "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  margin: 0;
  background-color: #f8f9fa;
  color: #333;
  line-height: 1.6;
  padding-top: 60px; /* For fixed top-bar */
  padding-bottom: 40px; /* For fixed bottom-bar */
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

/* Top Bar (与 login_style.css 中的 top-bar 样式一致) */
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
  /* 返回按钮样式 */
  color: white;
  font-size: 20px; /* 调整图标大小 */
  margin-right: 20px; /* 与系统名称的间距 */
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
  transition: all 0.2s;
  position: relative;
}
.top-bar .user-info:hover {
  background-color: #1370eb;
}
.top-bar .user-avatar {
  font-size: 22px; /* 调整图标大小 */
  width: 32px; /* 调整头像容器大小 */
  height: 32px;
  border-radius: 50%;
  background-color: #fff;
  color: #409eff;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
}
.top-bar .user-name {
  margin: 0 8px 0 0; /* 调整右边距 */
  font-size: 15px;
  color: white;
}
.top-bar .user-info .fa-angle-down {
  font-size: 12px;
  color: white;
}
.top-bar .user-dropdown-menu {
  display: none;
  position: absolute;
  top: calc(100% + 5px); /* 调整与用户信息的间距 */
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
  text-decoration: none;
}
.top-bar .user-dropdown-menu .divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 5px 0;
}

/* Main Content */
.page-main {
  flex-grow: 1;
  padding: 25px; /* 调整主内容区padding */
  width: 100%;
  box-sizing: border-box;
}
.container {
  max-width: 1100px; /* 调整容器最大宽度以适应表格 */
  margin: 0 auto;
}

/* Cards (与 login_style.css 中的 card 样式类似，可微调) */
.card {
  background-color: white;
  border-radius: 6px; /* 细微调整圆角 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
  padding: 20px 25px;
  margin-bottom: 25px;
}
.card-title {
  font-size: 18px; /* 调整卡片标题大小 */
  color: #303133;
  margin-top: 0;
  margin-bottom: 20px;
  font-weight: 500;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5; /* 添加分隔线 */
}

/* Forms (与 login_style.css 中的 form 样式类似) */
.form-row {
  /* 用于横向排列表单组 */
  display: flex;
  gap: 20px; /* 表单组之间的间隙 */
  margin-bottom: 15px;
}
.form-row .form-group {
  flex: 1; /* 让表单组平分空间 */
  margin-bottom: 0; /* 移除单个表单组的底部外边距 */
}
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
.form-group select,
.form-group input[type="text"],
.form-group input[type="number"] {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}
.form-group select:focus,
.form-group input[type="text"]:focus,
.form-group input[type="number"]:focus {
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
.form-group select[disabled],
.form-group input[disabled] {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}
.form-action-group {
  text-align: right; /* 使按钮靠右 */
  margin-top: 10px;
}

/* Buttons (与 login_style.css 中的 btn 样式类似) */
.btn {
  padding: 9px 18px; /* 调整按钮padding */
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: 0.1s;
  text-align: center;
  user-select: none;
  white-space: nowrap;
  display: inline-flex; /* 用于图标和文字对齐 */
  align-items: center;
  gap: 6px; /* 图标和文字的间距 */
}
.btn i {
  font-size: 1em; /* 使图标与文字大小一致 */
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
.btn-primary[disabled] {
  background-color: #a0cfff;
  border-color: #a0cfff;
  cursor: not-allowed;
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
  /* 新增 Element Plus success 按钮样式 */
  background-color: #67c23a;
  border-color: #67c23a;
  color: white;
}
.btn-success:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}
.action-buttons {
  margin-top: 25px;
  text-align: right;
}
.action-buttons .btn {
  margin-left: 10px;
}

/* Tables (Element Plus 风格表格) */
.table-responsive {
  overflow-x: auto;
  margin-top: 15px;
  border: 1px solid #ebeef5; /* 表格外边框 */
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
  border-bottom: 1px solid #ebeef5; /* 行分隔线 */
  padding: 12px 15px; /* 单元格内边距 */
  text-align: left;
  vertical-align: middle;
}
thead {
  background-color: #f5f7fa; /* 表头背景 */
  color: #909399; /* 表头文字颜色 */
}
th {
  font-weight: 500;
}
tbody tr:hover {
  background-color: #f5f7fa; /* 悬停行背景 */
}
tbody td input[type="number"],
tbody td input[type="text"] {
  width: calc(100% - 10px); /* 适应单元格 */
  padding: 6px 8px; /* 输入框内边距 */
  font-size: 13px;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
}
tbody td input[type="number"]:focus,
tbody td input[type="text"]:focus {
  border-color: #409eff;
}
tbody td input[readonly] {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

/* Notification Area (与 login_style.css 中的一致) */
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

/* Bottom Bar (与 login_style.css 中的一致) */
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

/* Responsive Adjustments */
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
    font-size: 13px;
    margin-left: 10px;
  }
  .top-bar .user-name {
    display: none;
  } /* 小屏幕隐藏用户名，只留头像 */
  .top-bar .user-avatar {
    margin-right: 0;
  }

  .page-main {
    padding: 15px;
  }
  .container {
    max-width: 100%;
  }
  .card-title {
    font-size: 16px;
  }
  .form-row {
    flex-direction: column;
    gap: 0;
  } /* 小屏幕下筛选条件垂直排列 */
  .form-row .form-group {
    margin-bottom: 15px;
  }
  .action-buttons .btn {
    font-size: 13px;
    padding: 8px 12px;
  }
  th,
  td {
    padding: 10px 8px;
    font-size: 13px;
  }
  tbody td input[type="number"],
  tbody td input[type="text"] {
    font-size: 12px;
    padding: 5px;
  }
}

.status-submitted {
  background-color: #67c23a;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-draft {
  background-color: #909399;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
