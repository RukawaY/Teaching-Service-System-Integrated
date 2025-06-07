<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container analysis-container">
        <div class="card filter-card-analysis">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-cogs" /> 分析设置
          </h2>
          <form
            @submit.stop.prevent="submitAnalysisForm"
            id="analysisFilterForm"
            class="filter-form-grid"
          >
            <div class="form-group semester-range-group-label">
              <label for="analysisSemesterRangeStart">选择学期范围:</label>
              <div class="semester-selects-container">
                <select
                  v-model="startSem"
                  id="analysisSemesterRangeStart"
                  name="semesterStart"
                >
                  <option value="all">从最早</option>
                  <option
                    v-for="semester in semesters"
                    :key="semester"
                    :value="semester"
                  >
                    {{ formatSemester(semester) }}
                  </option>
                </select>
                <span class="range-separator"> 至 </span>
                <select
                  v-model="endSem"
                  id="analysisSemesterRangeEnd"
                  name="semesterEnd"
                >
                  <option value="all">至今</option>
                  <option
                    v-for="semester in semesters"
                    :key="semester"
                    :value="semester"
                  >
                    {{ formatSemester(semester) }}
                  </option>
                </select>
              </div>
            </div>
            <!-- <div class="form-group">
              <label for="analysisCourseType">课程性质:</label>
              <select
                v-model="courseType"
                id="analysisCourseType"
                name="courseType"
              >
                <option value="all">全部性质</option>
                <option value="compulsory">必修课</option>
                <option value="elective">选修课</option>
              </select>
            </div> -->
            <div class="form-action-group">
              <button
                @click.prevent="submitAnalysisForm"
                type="submit"
                class="btn btn-primary"
              >
                <FontAwesomeIcon icon="fas fa-sync-alt" /> 生成分析报告
              </button>
            </div>
          </form>
        </div>

        <div class="analysis-results-grid">
          <div class="card stats-summary-card">
            <h3 class="sub-card-title">
              <FontAwesomeIcon icon="fas fa-calculator" /> 总体学情概览
            </h3>
            <div class="overall-stats">
              <div class="stat-item">
                <span class="stat-label">总获得学分:</span>
                <span class="stat-value">{{ totalCreditsEarned }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">平均绩点 (GPA):</span>
                <span class="stat-value">{{ overallGpa }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">平均分:</span>
                <span class="stat-value">{{ averageScore }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已修课程数:</span>
                <span class="stat-value">{{ coursesTaken }}</span>
              </div>
            </div>
          </div>

          <div class="card chart-card">
            <h3 class="sub-card-title">
              <FontAwesomeIcon icon="fas fa-chart-line" /> 学期平均绩点趋势
            </h3>
            <div class="chart-container" ref="gpaChartContainer">
              <canvas id="gpaTrendChart"></canvas>
              <p class="chart-no-data-message" v-if="!hasGpaTrendData">
                暂无数据
              </p>
            </div>
          </div>

          <div class="card chart-card">
            <h3 class="sub-card-title">
              <FontAwesomeIcon icon="fas fa-chart-pie" /> 分数段分布
            </h3>
            <div class="chart-container" ref="scoreChartContainer">
              <canvas id="scoreDistributionChart"></canvas>
              <p class="chart-no-data-message" v-if="!hasScoreDistributionData">
                暂无数据
              </p>
            </div>
          </div>

          <!-- <div class="card chart-card">
            <h3 class="sub-card-title">
              <FontAwesomeIcon icon="fas fa-chart-doughnut" /> 学分构成
            </h3>
            <div class="chart-container" ref="creditsChartContainer">
              <canvas id="creditsByTypeChart"></canvas>
              <p class="chart-no-data-message" v-if="!hasCreditsByTypeData">
                暂无数据
              </p>
            </div>
          </div> -->
        </div>
      </div>
    </main>

    <!-- Notification Area -->
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
import { ref, onMounted, nextTick } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useuserLoginStore } from "../../../store/userLoginStore";
import { getStudentGrades, getStudentGradeAnalysis } from "../../../api/info_manage/student";
import { Chart } from "chart.js/auto";
import { useRouter } from "vue-router";

const loginUserStore = useuserLoginStore();
const router = useRouter();
// 响应式数据
const isUserDropdownOpen = ref(false);
const gradesFilterAll = {
  semester: "",
  sec_year: "",
  course_name: "",
};
const allGradesData = ref<any[]>([]);
const semesters = ref<string[]>([]);
const startSem = ref<string>("all");
const endSem = ref<string>("all");
const courseType = ref<string>("all");
const totalCreditsEarned = ref<string>("--");
const overallGpa = ref<string>("--");
const averageScore = ref<string>("--");
const coursesTaken = ref<string>("--");
const hasGpaTrendData = ref<boolean>(false);
const hasScoreDistributionData = ref<boolean>(false);
const hasCreditsByTypeData = ref<boolean>(false);
const showNotification = ref<boolean>(false);
const notificationMessage = ref<string>("");

// 图表实例引用
let gpaTrendChart: Chart | null = null;
let scoreDistributionChart: Chart | null = null;
let creditsByTypeChart: Chart | null = null;

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  isUserDropdownOpen.value = !isUserDropdownOpen.value;
};

// 处理退出登录
const handleLogout = () => {
  showMainNotification("正在退出登录...", "info");
  setTimeout(() => {
    loginUserStore.setLoginUserUnlogin();
    //window.location.href = "../login";
    router.push("/login");
  }, 1500);
};

// 处理修改密码
const handleChangePassword = () => {
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

// 销毁现有图表
const destroyCharts = () => {
  if (gpaTrendChart) {
    gpaTrendChart.destroy();
    gpaTrendChart = null;
  }
  if (scoreDistributionChart) {
    scoreDistributionChart.destroy();
    scoreDistributionChart = null;
  }
  if (creditsByTypeChart) {
    creditsByTypeChart.destroy();
    creditsByTypeChart = null;
  }
};

// 获取成绩数据
const fetchGradesData = async () => {
  try {
    // 创建普通对象传递给API
    const params = {
      semester: gradesFilterAll.semester,
      sec_year: gradesFilterAll.sec_year,
      course_name: gradesFilterAll.course_name,
    };
    console.log("获取成绩列表参数:", params);
    const response = await getStudentGrades(params);
    console.log("获取成绩列表响应:", response);
    if (response.data.code === 200) {
      allGradesData.value = response.data.data;
      // 提取所有学期信息
      if (allGradesData.value && Array.isArray(allGradesData.value)) {
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

        // 设置默认学期范围
        if (semesters.value.length > 0) {
          startSem.value = semesters.value[0];
          endSem.value = semesters.value[semesters.value.length - 1];
        }

        // 初始加载时自动生成分析
        nextTick(() => {
          submitAnalysisForm();
        });
      } else {
        showMainNotification("没有可用的成绩数据", "info");
      }
    } else {
      showMainNotification(`获取成绩失败: ${response.data.message}`, "error");
    }
  } catch (error) {
    console.error("获取成绩数据失败:", error);
    showMainNotification("获取成绩数据失败", "error");
  }
};

// 提交分析表单
const submitAnalysisForm = async () => {
  console.log("submitAnalysisForm函数被调用");
  showMainNotification("正在生成分析报告...", "info");
  try {
    // 检查学期范围
    if (startSem.value !== "all" && endSem.value !== "all") {
      // 使用新的比较函数
      const compareResult = compareSemesters(startSem.value, endSem.value);
      if (compareResult > 0) {
        showMainNotification("开始学期不能晚于结束学期。", "error");
        return;
      }
    }

    const startYear =
      startSem.value === "all" ? 0 : parseInt(startSem.value.split("-")[0]);
    const startSemester =
      startSem.value === "all" ? "" : startSem.value.split("-")[1];
    const endYear =
      endSem.value === "all" ? 0 : parseInt(endSem.value.split("-")[0]);
    const endSemester =
      endSem.value === "all" ? "" : endSem.value.split("-")[1];

    const params = {
      start_semester: startSemester,
      start_sec_year: startYear,
      end_semester: endSemester,
      end_sec_year: endYear,
    };

    console.log("发送分析请求参数:", params);

    try {
      console.log("正在调用getStudentGradeAnalysis API...");

      // 临时注释掉API调用，直接使用前端分析

      const response = await getStudentGradeAnalysis(params);
      console.log("获取分析响应:", response);

      if (response.data.code === 200) {
        // 使用nextTick确保DOM更新完成后再绘制图表
        nextTick(() => {
          calculateAndDisplayAnalytics(response.data.data);
        });
      } else {
        showMainNotification(
          `后端分析API返回错误: ${response.data.message}，尝试前端分析`,
          "warn"
        );
        // 当后端API失败时，尝试前端数据分析
        generateAndDisplayLocalAnalysis();
      }

      // 直接使用前端分析
      // console.log("跳过API调用，直接使用前端分析");
      // generateAndDisplayLocalAnalysis();
    } catch (error) {
      console.error("后端分析API调用失败:", error);
      console.log("当前allGradesData值:", allGradesData.value);
      showMainNotification("后端分析API调用失败，尝试前端分析", "warn");
      // 当后端API失败时，尝试前端数据分析
      generateAndDisplayLocalAnalysis();
    }
  } catch (error) {
    console.error("获取成绩分析数据失败:", error);
    showMainNotification("获取成绩分析数据失败", "error");
  }
};

// 从本地成绩数据生成分析数据
const generateAndDisplayLocalAnalysis = () => {
  console.log("开始前端本地分析数据生成");
  try {
    if (
      !allGradesData.value ||
      !Array.isArray(allGradesData.value) ||
      allGradesData.value.length === 0
    ) {
      showMainNotification("没有可用的成绩数据进行分析", "error");
      return;
    }

    // 筛选符合条件的成绩
    let filteredGrades = [...allGradesData.value];

    // 根据学期范围筛选
    if (startSem.value !== "all" || endSem.value !== "all") {
      filteredGrades = filteredGrades.filter((grade) => {
        const gradeSemester = `${grade.sec_year}-${grade.semester}`;

        if (startSem.value !== "all") {
          // 使用比较函数：如果成绩学期早于开始学期，则排除
          if (compareSemesters(gradeSemester, startSem.value) < 0) {
            return false;
          }
        }

        if (endSem.value !== "all") {
          // 使用比较函数：如果成绩学期晚于结束学期，则排除
          if (compareSemesters(gradeSemester, endSem.value) > 0) {
            return false;
          }
        }

        return true;
      });
    }

    // 根据课程类型筛选
    if (courseType.value !== "all") {
      // 假设课程类型在数据中存在
      filteredGrades = filteredGrades.filter(
        (grade) => grade.course_type === courseType.value
      );
    }

    // 创建分析数据结构
    const analysisData = {
      overall_gpa: 0,
      average_score: 0,
      total_credits_earned: 0,
      total_credits_taken: 0,
      grade_distribution_by_course: filteredGrades.map((g) => ({
        course_name: g.course_name,
        score: g.score,
        gpa: g.gpa,
        credit: g.credit,
        course_type: g.course_type || "未分类",
      })),
      performance_trend: [] as Array<{
        sec_year: number;
        semester: string;
        gpa: number;
        average_score: number;
      }>,
    };

    // 计算总体数据
    let totalGpa = 0;
    let totalScore = 0;
    let earnedCredits = 0;
    let totalCourses = filteredGrades.length;

    filteredGrades.forEach((grade) => {
      totalGpa += grade.gpa;
      totalScore += grade.score;
      // 假设submit_status为1表示已获得学分
      if (grade.submit_status === 1) {
        earnedCredits += grade.credit;
      }
    });

    const totalCredits = filteredGrades.reduce(
      (sum, grade) => sum + grade.credit,
      0
    );
    analysisData.overall_gpa =
      totalCourses > 0 ? parseFloat((totalGpa / totalCourses).toFixed(2)) : 0;
    analysisData.average_score =
      totalCourses > 0 ? parseFloat((totalScore / totalCourses).toFixed(2)) : 0;
    analysisData.total_credits_earned = earnedCredits;
    analysisData.total_credits_taken = totalCredits;

    // 生成学期表现趋势
    const semesterData: Record<
      string,
      {
        sec_year: number;
        semester: string;
        gpa: number;
        average_score: number;
        count: number;
      }
    > = {};

    filteredGrades.forEach((grade) => {
      const key = `${grade.sec_year}-${grade.semester}`;
      if (!semesterData[key]) {
        semesterData[key] = {
          sec_year: grade.sec_year,
          semester: grade.semester,
          gpa: 0,
          average_score: 0,
          count: 0,
        };
      }

      semesterData[key].gpa += grade.gpa;
      semesterData[key].average_score += grade.score;
      semesterData[key].count += 1;
    });

    // 计算每个学期的平均GPA和成绩
    analysisData.performance_trend = Object.values(semesterData).map((sem) => ({
      sec_year: sem.sec_year,
      semester: sem.semester,
      gpa: sem.count > 0 ? parseFloat((sem.gpa / sem.count).toFixed(2)) : 0,
      average_score:
        sem.count > 0
          ? parseFloat((sem.average_score / sem.count).toFixed(2))
          : 0,
    }));

    // 按学期排序
    analysisData.performance_trend.sort((a, b) => {
      // 使用compareSemesters函数进行排序
      return compareSemesters(
        `${a.sec_year}-${a.semester}`,
        `${b.sec_year}-${b.semester}`
      );
    });

    console.log("前端生成的分析数据:", analysisData);
    calculateAndDisplayAnalytics(analysisData);
  } catch (error) {
    console.error("前端分析数据生成失败:", error);
    showMainNotification("前端分析数据生成失败", "error");
  }
};

// 计算并显示分析结果
const calculateAndDisplayAnalytics = (data: any) => {
  console.log("开始处理分析数据:", data);
  console.log(
    "数据类型:",
    typeof data,
    "是否为数组:",
    Array.isArray(data),
    "是否为对象:",
    typeof data === "object" && data !== null
  );
  // 先销毁现有图表
  destroyCharts();

  // 防御性检查确保数据存在
  if (!data) {
    showMainNotification("分析数据为空，请重试。", "error");
    return;
  }

  // 添加防御性检查，确保所有必要的字段都存在
  totalCreditsEarned.value = data.total_credits_earned?.toString() || "0";
  overallGpa.value = data.overall_gpa
    ? parseFloat(data.overall_gpa).toFixed(2)
    : "0.00";
  averageScore.value = data.average_score
    ? parseFloat(data.average_score).toFixed(1)
    : "0.0";
  coursesTaken.value =
    data.grade_distribution_by_course?.length?.toString() || "0";

  // 处理学期平均绩点趋势
  const gpaTrendLabels: string[] = [];
  const gpaTrendData: number[] = [];

  if (data.performance_trend && Array.isArray(data.performance_trend)) {
    data.performance_trend.forEach((trend: any) => {
      gpaTrendLabels.push(
        trend.semester || (trend.sec_year ? trend.sec_year.toString() : "未知")
      );
      const gpaValue =
        parseFloat(trend.gpa) || parseFloat(trend.average_score) || 0;
      gpaTrendData.push(parseFloat(gpaValue.toFixed(2)));
    });
  }
  hasGpaTrendData.value = gpaTrendLabels.length > 0;

  // 处理分数段分布
  const scoreRanges: { [key: string]: number } = {
    "90-100": 0,
    "80-89": 0,
    "70-79": 0,
    "60-69": 0,
    "<60": 0,
  };

  let hasAnyScores = false;
  if (
    data.grade_distribution_by_course &&
    Array.isArray(data.grade_distribution_by_course)
  ) {
    data.grade_distribution_by_course.forEach((course: any) => {
      if (course) {
        hasAnyScores = true;
        const score = parseFloat(course.score) || 0;
        if (score >= 90) scoreRanges["90-100"]++;
        else if (score >= 80) scoreRanges["80-89"]++;
        else if (score >= 70) scoreRanges["70-79"]++;
        else if (score >= 60) scoreRanges["60-69"]++;
        else scoreRanges["<60"]++;
      }
    });
  }
  hasScoreDistributionData.value = hasAnyScores;

  // 处理学分构成
  const creditsByType: { [key: string]: number } = {};
  if (
    data.grade_distribution_by_course &&
    Array.isArray(data.grade_distribution_by_course)
  ) {
    data.grade_distribution_by_course.forEach((course: any) => {
      if (course && course.score >= 60) {
        const courseType = course.course_type || "未分类";
        creditsByType[courseType] =
          (creditsByType[courseType] || 0) + (parseFloat(course.credit) || 0);
      }
    });
  }
  hasCreditsByTypeData.value = Object.keys(creditsByType).length > 0;

  showMainNotification("成绩分析报告已生成。", "success");

  // 绘制学期平均绩点趋势图
  const gpaTrendChartCtx = document.getElementById(
    "gpaTrendChart"
  ) as HTMLCanvasElement;
  if (gpaTrendChartCtx) {
    gpaTrendChart = new Chart(gpaTrendChartCtx, {
      type: "line",
      data: {
        labels: gpaTrendLabels,
        datasets: [
          {
            label: "学期平均绩点趋势",
            data: gpaTrendData,
            borderColor: "rgb(75, 192, 192)",
            backgroundColor: "rgba(75, 192, 192, 0.5)",
            fill: true,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  // 绘制分数段分布图
  const scoreDistributionChartCtx = document.getElementById(
    "scoreDistributionChart"
  ) as HTMLCanvasElement;
  if (scoreDistributionChartCtx) {
    scoreDistributionChart = new Chart(scoreDistributionChartCtx, {
      type: "pie",
      data: {
        labels: Object.keys(scoreRanges),
        datasets: [
          {
            label: "分数段分布",
            data: Object.values(scoreRanges),
            backgroundColor: [
              "rgb(255, 99, 132)",
              "rgb(54, 162, 235)",
              "rgb(255, 205, 86)",
              "rgb(75, 192, 192)",
              "rgb(153, 102, 255)",
              "rgb(201, 203, 207)",
            ],
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }

  // 绘制学分构成图
  const creditsByTypeChartCtx = document.getElementById(
    "creditsByTypeChart"
  ) as HTMLCanvasElement;
  if (creditsByTypeChartCtx) {
    creditsByTypeChart = new Chart(creditsByTypeChartCtx, {
      type: "doughnut",
      data: {
        labels: Object.keys(creditsByType),
        datasets: [
          {
            data: Object.values(creditsByType),
            backgroundColor: [
              "rgb(54, 162, 235)",
              "rgb(255, 99, 132)",
              "rgb(255, 205, 86)",
              "rgb(75, 192, 192)",
              "rgb(153, 102, 255)",
            ],
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
      },
    });
  }
};

onMounted(() => {
  fetchGradesData();
});

// 格式化学期
const formatSemester = (semester: string) => {
  const [year, term] = semester.split("-");
  return `${year}学年 ${term}学期`;
};

// 比较两个学期的先后顺序
// 返回值: 1表示semester1在semester2之后，-1表示semester1在semester2之前，0表示相等
const compareSemesters = (semester1: string, semester2: string) => {
  const [year1, term1] = semester1.split("-");
  const [year2, term2] = semester2.split("-");

  const yearNum1 = parseInt(year1);
  const yearNum2 = parseInt(year2);

  if (yearNum1 !== yearNum2) {
    return yearNum1 > yearNum2 ? 1 : -1;
  }

  // 同一学年，秋冬学期在春夏学期之后
  if (term1 === term2) return 0;
  return term1 === "秋冬" ? 1 : -1;
};
</script>

<style scoped>
/* 原有的样式代码保持不变 */
</style>
<style scoped>
/* Global Styles */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8;
  color: #333;
  line-height: 1.6;
  padding-top: 60px;
  padding-bottom: 50px; /* Increased padding-bottom */
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
  background-color: #3a8ee6; /* Darker blue for hover */
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
  font-size: 18px; /* Adjusted for better fit */
  color: #409eff;
  line-height: 1;
}
.top-bar .user-name {
  margin: 0 8px 0 0;
  font-size: 14px;
  color: white;
  line-height: 1; /* Align with icons */
}
.top-bar .user-info .fa-angle-down {
  font-size: 12px;
  color: white;
  margin-left: 5px;
  transition: transform 0.2s;
}
.top-bar .user-info.is-open .fa-angle-down {
  /* JS would add/remove 'is-open' */
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
.container.analysis-container {
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
  font-size: 1.05em;
}
.sub-card-title {
  font-size: 17px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 18px;
  font-weight: 500;
  display: flex;
  align-items: center;
}
.sub-card-title i {
  margin-right: 8px;
  color: #67c23a;
}
.stats-summary-card .sub-card-title i {
  color: #e6a23c;
}

/* Filter Form Grid */
.filter-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  align-items: flex-end;
}
.filter-form-grid .form-group,
.filter-form-grid .semester-range-group-label {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
}
.filter-form-grid .form-group label,
.filter-form-grid .semester-range-group-label > label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}
.filter-form-grid .semester-selects-container {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.filter-form-grid .semester-selects-container select,
.filter-form-grid .form-group select {
  flex-grow: 1;
  min-width: 100px;
  padding: 9px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg class='icon' viewBox='0 0 1024 1024' xmlns='http://www.w3.org/2000/svg' width='12' height='12'%3E%3Cpath d='M512 714.666667c-8.533333 0-17.066667-2.133333-23.466667-8.533334l-307.2-268.8c-12.8-10.666667-14.933333-29.866667-4.266666-42.666666 10.666667-12.8 29.866667-14.933333 42.666666-4.266667l292.266667 256 292.266667-256c12.8-10.666667 32-8.533333 42.666667 4.266667s8.533333 32-4.266667 42.666666l-307.2 268.8c-6.4 4.266667-12.8 8.533333-21.333333 8.533334z' fill='%23C0C4CC'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 12px;
  padding-right: 30px;
}
.filter-form-grid .semester-selects-container select:focus,
.filter-form-grid .form-group select:focus {
  border-color: #409eff;
  outline: 0;
}
.filter-form-grid .range-separator {
  white-space: nowrap;
  color: #606266;
  margin: 0 5px;
}

.filter-form-grid .form-action-group {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}
@media (min-width: 900px) {
  .filter-form-grid .form-action-group {
    grid-column: auto;
  }
}

/* Buttons */
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

/* Analysis Results Grid */
.analysis-results-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 25px;
}
@media (min-width: 992px) {
  .analysis-results-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stats-summary-card {
    grid-column: 1 / -1;
  }
}

/* Stats Summary Card */
.overall-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 15px;
  padding-top: 10px;
}
.stat-item {
  background-color: #f9fafc;
  padding: 15px;
  border-radius: 6px;
  text-align: center;
  border: 1px solid #ebeef5;
}
.stat-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.stat-value {
  display: block;
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
}

/* Chart Card & Container */
.chart-card {
  padding-bottom: 25px;
}
.chart-container {
  position: relative;
  width: 100%;
  height: 280px;
  margin-top: 10px;
}
.chart-container canvas {
  display: block;
  width: 100% !important;
  height: 100% !important;
}
.chart-no-data-message {
  text-align: center;
  color: #909399;
  font-style: italic;
  padding: 50px 0;
  font-size: 15px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
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

/* Bottom Bar */
.bottom-bar {
  background-color: #f5f5f5b7;
  padding: 12px 0;
  text-align: center; /* This centers the inline or inline-block child */
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
  margin: 0; /* Crucial for text-align on parent to work as expected for block elements */
  line-height: 1.5;
  /* display: inline-block; */ /* Not strictly necessary if .bottom-bar has text-align: center */
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
    display: none;
  }
  .page-main {
    padding: 15px;
  }
  .filter-form-grid {
    grid-template-columns: 1fr;
  }
  .filter-form-grid .semester-selects-container {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  .filter-form-grid .semester-selects-container select {
    width: 100%;
  }
  .filter-form-grid .range-separator {
    margin: 5px 0;
    text-align: center;
  }
  .analysis-results-grid {
    grid-template-columns: 1fr;
  }
  .stat-value {
    font-size: 20px;
  }
  .chart-container {
    height: 250px;
  }
}
</style>
