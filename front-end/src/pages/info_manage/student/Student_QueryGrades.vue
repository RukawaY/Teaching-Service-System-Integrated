<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container query-grades-container">
        <div class="card filter-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-filter" /> 筛选成绩记录
          </h2>
          <form
            id="filterGradesForm"
            class="filter-form-grid"
            @submit.prevent="filterAndDisplayGrades"
          >
            <div class="form-group">
              <label for="filterSemesterGrades">选择学年:</label>
              <select id="filterSemesterGrades" v-model="filter.sec_year">
                <option value="">所有</option>
                <option v-for="year in uniqueYears" :key="year" :value="year">
                  {{ year }}学年
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="filterSemesterGrades">选择学期:</label>
              <select id="filterSemesterGrades" v-model="filter.semester">
                <option value="">所有</option>
                <option
                  v-for="semester in semesters"
                  :key="semester"
                  :value="semester.split('-')[1]"
                >
                  {{ formatSemester(semester) }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="filterCourseNameAdmin">课程名称:</label>
              <input
                type="text"
                id="filterCourseNameAdmin"
                v-model="filter.course_name"
                placeholder="输入课程名称关键词"
                maxlength="80"
              />
            </div>
            <div class="form-action-group">
              <button type="submit" class="btn btn-primary">
                <FontAwesomeIcon icon="fas fa-search" /> 查询成绩
              </button>
              <button
                type="reset"
                class="btn btn-secondary"
                id="resetGradeFiltersBtn"
                @click="resetFilters"
              >
                <FontAwesomeIcon icon="fas fa-undo" /> 重置筛选
              </button>
            </div>
          </form>
        </div>

        <div class="card grades-list-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-clipboard-list" /> 我的成绩单 (<span
              id="currentGradeFilterDisplay"
              >{{ filterText }}</span
            >)
          </h2>
          <div class="table-responsive">
            <table id="gradesTable">
              <thead>
                <tr>
                  <th>学年</th>
                  <th>学期</th>
                  <th>授课教师</th>
                  <th>课程名称</th>
                  <th>学分</th>
                  <th>成绩</th>
                  <th>绩点</th>
                </tr>
              </thead>
              <tbody id="gradesListBody">
                <template v-if="loading">
                  <tr>
                    <td colspan="8" class="placeholder-row">
                      正在加载成绩信息...
                    </td>
                  </tr>
                </template>
                <template v-else-if="grades.length === 0">
                  <tr>
                    <td colspan="8" class="no-grades-row">
                      没有找到符合条件的成绩记录。
                    </td>
                  </tr>
                </template>
                <template v-else>
                  <tr v-for="grade in grades" :key="grade.course_id">
                    <td>{{ grade.sec_year }}</td>
                    <td>{{ grade.semester }}</td>
                    <td>{{ grade.teacher_name }}</td>
                    <td>{{ grade.course_name }}</td>
                    <td>{{ grade.credit }}</td>
                    <td :class="getGradeClass(grade.score)">
                      {{ grade.score }}
                    </td>
                    <td class="gpa-value">{{ grade.gpa }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
          <div class="summary-stats">
            <div>
              总修读学分:
              <span id="totalCreditsAttempted">{{
                totalCreditsAttempted
              }}</span>
            </div>
            <div>
              平均绩点 (GPA): <span id="gpa">{{ gpa }}</span>
            </div>
          </div>
        </div>
        <div
          id="notificationArea"
          class="notification"
          :style="{ display: notification.message ? 'block' : 'none' }"
        >
          {{ notification.message }}
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { getStudentGrades } from "../../../api/info_manage/student";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { useRouter } from "vue-router";

type Grade = {
  grade_id: number;
  course_id: number;
  course_name: string;
  section_id: number;
  semester: string;
  sec_year: number;
  teacher_name: string;
  score: number;
  gpa: number;
  credit: number;
  submit_status: number;
  componets: [
    {
      component_id: number;
      component_name: string;
      grade_id: number;
      component_type: number;
      ratio: number;
      score: number;
    }
  ];
};

const loginUserStore = useuserLoginStore();
const router = useRouter();
// 响应式数据
const userDropdownVisible = ref(false);
const filter = ref({
  sec_year: "" as string | number | null,
  semester: "",
  course_name: "" as string | null,
});
const gradesFilterAll = ref({
  sec_year: "",
  semester: "",
  course_name: "",
});
const semesters = ref<string[]>([]);
const allGradesData = ref<Grade[]>([]);
const filterText = ref("全部学期");
const loading = ref(true);
const grades = ref<Grade[]>([]);
const notification = ref({
  message: "",
  type: "info",
});
const totalCreditsAttempted = ref(0);
const totalCreditsEarned = ref(0);
const gpa = ref("N/A");
const uniqueYears = ref<number[]>([]);

// 生命周期钩子
onMounted(() => {
  //filterAndDisplayGrades();
});

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

const formatYear = (semester: string) => {
  const [year, term] = semester.split("-");
  return `${year}学年`;
};

const formatSemester = (semester: string) => {
  const [year, term] = semester.split("-");
  return `${term}学期`;
};

// 处理退出登录
const handleLogout = () => {
  showNotification("正在退出登录...", "info");
  setTimeout(() => {
    loginUserStore.setLoginUserUnlogin();
    router.push("/login");
  }, 1500);
};

// 处理修改密码
const handleChangePassword = () => {
  router.push("/change-password");
};

const fetchGradesData = async () => {
  try {
    const response = await getStudentGrades(gradesFilterAll.value);
    allGradesData.value = response.data.data;
    // const response = sampleGradesData;
    // allGradesData.value = response;
    const combinedSemesters = allGradesData.value.map(
      (g) => `${g.sec_year}-${g.semester}`
    );
    semesters.value = [...new Set(combinedSemesters)].sort((a, b) => {
      const [yearA, termA] = a.split("-");
      const [yearB, termB] = b.split("-");
      if (parseInt(yearA) !== parseInt(yearB)) {
        return parseInt(yearA) - parseInt(yearB);
      }
      if (termA === "秋冬" && termB === "春夏") {
        return -1;
      } else if (termA === "春夏" && termB === "秋冬") {
        return 1;
      } else return 0;
    });
    if (semesters.value.length > 0) {
      filter.value.sec_year = parseInt(semesters.value[0].split("-")[0]);
      filter.value.semester = semesters.value[0].split("-")[1];
    }
    uniqueYears.value = [
      ...new Set(allGradesData.value.map((g) => g.sec_year)),
    ];
  } catch (error) {
    console.error("获取成绩数据失败:", error);
    showNotification("获取成绩数据失败", "error");
  }
};

const filterAndDisplayGrades = async () => {
  loading.value = true;
  showNotification("正在查询成绩列表...", "info");
  try {
    const response = await getStudentGrades(filter.value);
    grades.value = response.data.data;
    // const response = sampleGradesData;
    // grades.value = response;

    // 更新过滤文本
    if (filter.value.sec_year && filter.value.semester) {
      filterText.value = `${filter.value.sec_year}学年 ${filter.value.semester}学期`;
    } else if (filter.value.sec_year) {
      filterText.value = `${filter.value.sec_year}学年`;
    } else if (filter.value.semester) {
      filterText.value = `${filter.value.semester}学期`;
    } else {
      filterText.value = "全部学期";
    }

    loading.value = false;
    if (grades.value.length > 0) {
      showNotification("成绩列表加载完毕。", "success");
      calculateSummaryStats();
    } else {
      showNotification("没有找到符合条件的成绩记录。", "info");
    }
  } catch (error) {
    loading.value = false;
    showNotification("查询成绩列表失败，请稍后重试。", "error");
    console.error(error);
  }
};

// 重置筛选条件
const resetFilters = () => {
  filter.value = {
    sec_year: "",
    semester: "",
    course_name: "",
  };
  setTimeout(filterAndDisplayGrades, 100);
};

// 显示通知
const showNotification = (message: string, type: "info") => {
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

// 计算总修读学分和平均绩点
const calculateSummaryStats = () => {
  let attempted = 0;
  let earned = 0;
  let totalGpa = 0;
  let validGpaCount = 0;

  grades.value.forEach((grade) => {
    attempted += grade.credit;
    if (typeof grade.gpa === "number") {
      totalGpa += grade.gpa;
      validGpaCount++;
    }
  });

  totalCreditsAttempted.value = attempted;
  totalCreditsEarned.value = earned;
  gpa.value = validGpaCount > 0 ? (totalGpa / validGpaCount).toFixed(2) : "N/A";
};

// 获取成绩的 CSS 类
const getGradeClass = (score: number) => {
  if (typeof score === "number") {
    if (score >= 60) {
      return "grade-value pass";
    } else {
      return "grade-value fail";
    }
  }
  return "grade-value pending";
};

onMounted(() => {
  fetchGradesData();
});
</script>

<style scoped>
/* 样式部分可以直接复用原有的 CSS 代码 */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8;
  color: #333;
  line-height: 1.6;
  padding-top: 60px;
  padding-bottom: 40px;
  box-sizing: border-box;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 字符计数样式 */
.character-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.character-count.near-limit {
  color: #e6a23c;
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

/* Main Content */
.page-main {
  flex-grow: 1;
  padding: 25px;
  width: 100%;
  box-sizing: border-box;
}

.container.query-grades-container {
  max-width: 1200px;
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

/* Filter Form */
.filter-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group select {
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg class='icon' viewBox='0 0 1024 1024' xmlns='http://www.w3.org/2000/svg' width='12' height='12'%3E%3Cpath d='M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-307.2-268.8c-12.8-10.666667-14.933333-29.866667-4.266666-42.666666 10.666667-12.8 29.866667-14.933333 42.666666-4.266667l292.266667 256 292.266667-256c12.8-10.666667 32-8.533333 42.666667 4.266667s8.533333 32-4.266667 42.666666l-307.2 268.8c-6.4 4.266667-12.8 8.533333-21.333333 8.533334z' fill='%23C0C4CC'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 12px;
  padding-right: 30px;
}

.form-action-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

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

/* Grades Table */
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
.no-grades-row td {
  text-align: center;
  color: #909399;
  padding: 30px 15px;
  font-style: italic;
}

td.grade-value {
  font-weight: bold;
}

td.grade-value.pass {
  color: #67c23a;
}

td.grade-value.fail {
  color: #f56c6c;
}

td.grade-value.pending {
  color: #e6a23c;
}

td.gpa-value {
  font-style: italic;
  color: #409eff;
}

/* Summary Stats */
.summary-stats {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 15px;
  color: #303133;
  text-align: center;
}

.summary-stats div {
  padding: 5px 10px;
}

.summary-stats span {
  font-weight: bold;
  color: #409eff;
  font-size: 1.1em;
}

/* Notification Area */
.notification {
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  display: none;
}

.notification.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.notification.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.notification.info {
  background-color: #f4f4f5;
  color: #909399;
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

/* Responsive Adjustments */
@media (max-width: 992px) {
  .filter-form-grid {
    grid-template-columns: 1fr;
  }

  .filter-form-grid .form-action-group {
    justify-content: center;
  }

  .summary-stats {
    flex-direction: column;
    align-items: center;
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

  th,
  td {
    padding: 8px 10px;
    font-size: 13px;
  }
}
</style>
