<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container course-analysis-container">
        <div class="card filter-card-course-analysis">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-filter" /> 选择分析课程
          </h2>
          <form
            @submit.prevent="submitAnalysisForm"
            id="selectCourseForAnalysisForm"
            class="filter-form-grid"
          >
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
              >
                <option :value="null">-- 请选择开课时间 --</option>
                <option
                  v-for="section in sections"
                  :key="section.section_id"
                  :value="section"
                >
                  {{ section.sec_year }} 学年 {{ section.semester }} 学期
                  {{ section.sec_time }}
                </option>
              </select>
            </div>
            <div class="form-action-group">
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="!selectedSection || !selectedCourse"
              >
                <FontAwesomeIcon icon="fas fa-chart-pie" /> 生成分析报告
              </button>
            </div>
          </form>
        </div>

        <div
          id="courseAnalysisResultsSection"
          :style="{ display: showResults ? 'block' : 'none' }"
        >
          <div class="card course-overview-card">
            <h3 class="sub-card-title">
              <FontAwesomeIcon icon="fas fa-info-circle" /> 课程成绩概览
              <!--              《<span>{{-->
              <!--                selectedCourse.course_name-->
              <!--              }}</span-->
              <!--              >》-->
            </h3>
            <div class="overall-course-stats">
              <div class="stat-item">
                <span class="stat-label">总人数:</span>
                <span class="stat-value">{{
                  totalStudents !== undefined && totalStudents !== null
                    ? totalStudents
                    : "--"
                }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">最高分:</span>
                <span class="stat-value">{{
                  highestScore !== undefined && highestScore !== null
                    ? highestScore
                    : "--"
                }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">最低分:</span>
                <span class="stat-value">{{
                  lowestScore !== undefined && lowestScore !== null
                    ? lowestScore
                    : "--"
                }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">平均分:</span>
                <span class="stat-value">{{
                  medianScore !== undefined && medianScore !== null
                    ? medianScore
                    : "--"
                }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">及格率:</span>
                <span class="stat-value">{{
                  passRate !== undefined && passRate !== null
                    ? `${passRate.toFixed(2)}%`
                    : "--"
                }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">优秀率 (>=90):</span>
                <span class="stat-value">{{
                  excellentRate !== undefined && excellentRate !== null
                    ? `${excellentRate.toFixed(2)}%`
                    : "--"
                }}</span>
              </div>
            </div>
          </div>

          <div class="analysis-charts-grid">
            <div class="card chart-card">
              <h3 class="sub-card-title">
                <FontAwesomeIcon icon="fas fa-percentage" /> 分数段人数分布
              </h3>
              <div class="chart-container">
                <canvas id="courseScoreDistributionChart"></canvas>
              </div>
            </div>
            <div class="card chart-card">
              <h3 class="sub-card-title">
                <FontAwesomeIcon icon="fas fa-chart-pie" /> 不同成绩段所占百分比
              </h3>
              <div class="chart-container">
                <canvas id="gradePercentageChart"></canvas>
              </div>
            </div>
            <div class="card chart-card">
              <h3 class="sub-card-title">
                <FontAwesomeIcon icon="fas fa-list-ol" /> 学生成绩排名
              </h3>
              <div class="chart-container">
                <table>
                  <thead>
                    <tr>
                      <th>排名</th>
                      <th>学生姓名</th>
                      <th>成绩</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(student, index) in rankedStudents" :key="index">
                      <td>{{ student.rank }}</td>
                      <td>{{ student.studentName }}</td>
                      <td>{{ student.score }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
        <div
          class="placeholder-no-selection"
          :style="{ display: !showResults ? 'block' : 'none' }"
        >
          <FontAwesomeIcon icon="fas fa-info-circle fa-3x" />
          <p>请先选择学期和课程以生成成绩分析报告。</p>
        </div>

        <div
          id="notificationArea"
          class="notification"
          :style="{ display: showNotification ? 'block' : 'none' }"
        >
          {{ notificationMessage }}
        </div>
      </div>
    </main>


  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { Chart } from "chart.js/auto";
import { useuserLoginStore } from "../../../store/userLoginStore";
import {
  getMyCourses,
  getMyCourseSections,
  getSectionGradeAnalysis,
} from "../../../api/info_manage/teacher";
import router from "../../../router";

const loginUserStore = useuserLoginStore();

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
  sec_time: string;
  // 其他可能的属性
}

// 分析数据相关接口
interface SectionInfo {
  courseId: number;
  classroomId: number;
  capacity: number;
  semester: string;
  secYear: number;
  secTime: string;
  id: number;
  availableCapacity: number;
}

interface GradeSegment {
  segment: string;
  count: number;
  percentage: number;
}

interface StudentRanking {
  studentId: number;
  studentName: string;
  score: number;
  rank: number;
}

interface GradeAnalysisData {
  section: SectionInfo;
  averageScore: number;
  highestScore: number;
  lowestScore: number;
  medianScore: number;
  passRate: number;
  excellentRate: number;
  gradeDistributionSegments: GradeSegment[];
  scoreRanking: StudentRanking[];
}

// 响应式数据
const userDropdownVisible = ref(false);
const selectedSection = ref<Section | null>(null);
const selectedCourse = ref<Course | null>(null);
const sections = ref<Section[]>([]);
const courses = ref<Course[]>([]);
const showResults = ref<boolean>(false);
const analyzedCourseName = ref<string>("");
const totalStudents = ref<number>(0);
const highestScore = ref<number>(0);
const lowestScore = ref<number>(0);
const medianScore = ref<number>(0);
const passRate = ref<number>(0);
const excellentRate = ref<number>(0);
const rankedStudents = ref<any[]>([]);
const showNotification = ref<boolean>(false);
const notificationMessage = ref<string>("");

let scoreDistChartInstance: Chart | null = null;
let gradePercentageChartInstance: Chart | null = null;

//   item: [
//     {
//       course_id: "121",
//       course_name: "课1",
//       course_description: "adwd",
//       category: "adawd",
//       credit: 12,
//     },
//     {
//       course_id: "122",
//       course_name: "课2",
//     },
//     {
//       course_id: "123",
//       course_name: "课3",
//     },
//   ],
// };
//
// const getMyCourses = async (params: any) => {
//   return MockCourses;
// };
//
// const MockSections = {
//   data: [
//     {
//       section_id: "212",
//       sec_year: 1999,
//       semester: "awdawd",
//     },
//     {
//       section_id: "awdwd",
//       sec_year: 2000,
//       semester: "aw22awd",
//     },
//   ],
// };
//
// const getMyCourseSections = async (params: any) => {
//   return MockSections;
// };
//
// const MockCouresGradeAnalysis = {
//   code: 200,
//   message: "分析成功",
//   data: {
//     section_info: {
//       capacity: 5,
//       available_capacity: 3,
//     },
//     average_score: 82.5,
//     highest_score: 95,
//     lowest_score: 55,
//     median_score: 83,
//     pass_rate: 92.3,
//     excellent_rate: 25.0,
//     grade_distribution_segments: [
//       { segment: "90-100", count: 5, percentage: 25.0 },
//       { segment: "80-89", count: 8, percentage: 40.0 },
//       { segment: "70-79", count: 4, percentage: 20.0 },
//       { segment: "60-69", count: 2, percentage: 10.0 },
//       { segment: "<60", count: 1, percentage: 5.0 },
//     ],
//     score_ranking: [
//       { student_id: "S001", student_name: "张三", score: 95, rank: 1 },
//       { student_id: "S002", student_name: "李四", score: 92, rank: 2 },
//       { student_id: "S003", student_name: "王五", score: 88, rank: 3 },
//     ],
//   },
// };
//
// const getSectionGradeAnalysis = async (params: any) => {
//   return MockCouresGradeAnalysis;
// };

// 显示通知
const showMainNotification = (message: string, type = "info") => {
  notificationMessage.value = message;
  showNotification.value = true;
  setTimeout(() => {
    showNotification.value = false;
  }, 3000);
};

// 切换用户下拉菜单
const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

const handleLogout = () => {
  showMainNotification("正在退出登录...", "info");
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

// 销毁现有图表
const destroyCharts = () => {
  if (scoreDistChartInstance) {
    scoreDistChartInstance.destroy();
    scoreDistChartInstance = null;
  }
  if (gradePercentageChartInstance) {
    gradePercentageChartInstance.destroy();
    gradePercentageChartInstance = null;
  }
};

const populateCourseSelect = async () => {
  courses.value = [];
  sections.value = [];
  selectedCourse.value = null;
  selectedSection.value = null;
  showResults.value = false;

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
    showMainNotification("获取课程列表失败，请稍后重试。", "error");
    console.error(error);
  }
};

// 选择课程后填充开课列表
const populateSectionSelect = async () => {
  sections.value = [];
  selectedSection.value = null;
  showResults.value = false;

  if (!selectedCourse.value) {
    showMainNotification("请先选择课程", "error");
    return;
  }

  try {
    console.log("选择的课程ID:", selectedCourse.value.course_id);

    const response = await getMyCourseSections(selectedCourse.value.course_id, {
      semester: "",
      // 不传递sec_year参数，避免后端将其设为0
    });

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
        showMainNotification("API返回的开课数据结构不符合预期", "error");
        return;
      }

      // 将返回的数据映射到我们的Section接口
      sections.value = sectionsData.map((item: any) => ({
        section_id: item.section_id || item.sectionId,
        sec_year: item.sec_year || item.secYear || 0,
        semester: item.semester || "",
        sec_time: item.sec_time || item.secTime,
      }));

      console.log("处理后的开课列表:", sections.value);

      if (sections.value.length === 0) {
        showMainNotification("该课程暂无开课信息", "info");
      }
    } else {
      showMainNotification("获取开课列表失败，返回数据为空", "error");
    }
  } catch (error) {
    showMainNotification("获取开课列表失败，请稍后重试。", "error");
    console.error("获取开课列表错误:", error);
  }
};

// 提交分析表单
const submitAnalysisForm = async () => {
  if (!selectedSection.value || !selectedCourse.value) {
    showMainNotification("请先选择课程和开课时间", "error");
    return;
  }

  showMainNotification("正在生成分析报告...", "info");

  try {
    console.log("选择的开课ID:", selectedSection.value.section_id);

    destroyCharts();
    const result = await getSectionGradeAnalysis(
      selectedSection.value.section_id
    );

    console.log("API返回的分析数据:", result);
    console.log("API返回的data内容:", result.data);

    // 尝试确定数据结构
    let analysisData: GradeAnalysisData | null = null;

    // 检查是否有额外的嵌套
    if (result.data && result.data.data) {
      console.log("检测到数据在data.data中，使用嵌套数据");
      analysisData = result.data.data as GradeAnalysisData;
    } else if (result.data) {
      analysisData = result.data as GradeAnalysisData;
    }

    if (analysisData) {
      try {
        console.log("处理的分析数据结构:", analysisData);

        // 检查必要的数据是否存在
        if (!analysisData.section) {
          console.error(
            "找不到section字段，可用字段:",
            Object.keys(analysisData)
          );
          throw new Error("缺少课程部分信息(section)");
        }

        if (
          analysisData.section.capacity === undefined ||
          analysisData.section.availableCapacity === undefined
        ) {
          console.error("section字段内容:", analysisData.section);
          throw new Error("缺少课程容量信息");
        }

        showResults.value = true;
        analyzedCourseName.value = selectedCourse.value.course_name || "";

        // 更新统计信息，根据后端返回的实际数据结构
        console.log("容量信息:", {
          capacity: analysisData.section.capacity,
          availableCapacity: analysisData.section.availableCapacity,
          type: typeof analysisData.section.capacity,
        });

        // 确保使用数值而非字符串进行计算
        const capacity = Number(analysisData.section.capacity || 0);
        const availableCapacity = Number(
          analysisData.section.availableCapacity || 0
        );
        const calculatedStudents = capacity - availableCapacity;

        console.log("计算的学生人数:", calculatedStudents);

        // 确保总人数至少为0（防止负数）
        totalStudents.value = Math.max(0, calculatedStudents);
        highestScore.value = analysisData.highestScore || 0;
        lowestScore.value = analysisData.lowestScore || 0;
        medianScore.value = analysisData.medianScore || 0;
        passRate.value = analysisData.passRate || 0;
        excellentRate.value = analysisData.excellentRate || 0;

        // 检查排名数据
        if (!analysisData.scoreRanking) {
          console.warn("缺少成绩排名数据");
          rankedStudents.value = [];
        } else {
          rankedStudents.value = analysisData.scoreRanking;
        }

        // 检查是否有分数段数据
        if (
          !analysisData.gradeDistributionSegments ||
          analysisData.gradeDistributionSegments.length === 0
        ) {
          console.warn("缺少分数段分布数据");
          showMainNotification("无法显示分数段分布图表，数据不完整", "error");
          return;
        }

        // 检查是否所有分数段的count都为0
        const hasNonZeroCount = analysisData.gradeDistributionSegments.some(
          (item) => item.count > 0
        );

        if (!hasNonZeroCount) {
          console.warn("所有分数段的人数都为0");
          showMainNotification("当前暂无学生成绩数据", "info");
        }

        // 准备图表数据
        const labels = analysisData.gradeDistributionSegments.map(
          (item: any) => item.segment
        );
        const counts = analysisData.gradeDistributionSegments.map(
          (item: any) => item.count
        );
        const percentages = analysisData.gradeDistributionSegments.map(
          (item: any) => item.percentage
        );

        // 绘制分数段分布图
        const scoreDistChartCtx = document.getElementById(
          "courseScoreDistributionChart"
        ) as HTMLCanvasElement;
        if (scoreDistChartCtx) {
          scoreDistChartInstance = new Chart(scoreDistChartCtx, {
            type: "bar",
            data: {
              labels: labels,
              datasets: [
                {
                  label: "学生人数",
                  data: counts,
                  backgroundColor: [
                    "#4CAF50",
                    "#8BC34A",
                    "#CDDC39",
                    "#FFEB3B",
                    "#F44336",
                  ],
                  borderColor: [
                    "#388E3C",
                    "#689F38",
                    "#AFB42B",
                    "#FBC02D",
                    "#D32F2F",
                  ],
                  borderWidth: 1,
                },
              ],
            },
            options: {
              responsive: true,
              maintainAspectRatio: false,
              scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
            },
          });
        } else {
          console.warn("无法找到图表容器: courseScoreDistributionChart");
        }

        // 绘制成绩段百分比饼状图
        const gradePercentageChartCtx = document.getElementById(
          "gradePercentageChart"
        ) as HTMLCanvasElement;
        if (gradePercentageChartCtx) {
          gradePercentageChartInstance = new Chart(gradePercentageChartCtx, {
            type: "pie",
            data: {
              labels: labels,
              datasets: [
                {
                  label: "成绩段百分比",
                  data: percentages,
                  backgroundColor: [
                    "rgb(255, 99, 132)",
                    "rgb(54, 162, 235)",
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
        } else {
          console.warn("无法找到图表容器: gradePercentageChart");
        }

        showMainNotification("课程成绩分析报告已生成。", "success");
      } catch (error) {
        showResults.value = false;
        console.error("分析数据处理失败:", error);
        showMainNotification(
          `分析数据处理失败: ${
            error instanceof Error ? error.message : String(error)
          }`,
          "error"
        );
      }
    } else {
      showResults.value = false;
      showMainNotification("未找到所选课程的成绩数据。", "error");
    }
  } catch (error) {
    showResults.value = false;
    console.error("生成分析报告失败:", error);
    showMainNotification(
      `生成分析报告失败: ${
        error instanceof Error ? error.message : String(error)
      }`,
      "error"
    );
  }
};

onMounted(() => {
  populateCourseSelect();
});

// // 监听学期选择变化
// watch(selectedSemester, (newSemester) => {
//   selectedCourseId.value = "";
//   populateCourses(newSemester);
// });
</script>

<style scoped>
/* Global Styles */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8;
  color: #333;
  line-height: 1.6;
  padding-top: 60px; /* For fixed top-bar */
  padding-bottom: 50px; /* For fixed bottom-bar, increased for safety */
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

/* Top Bar - ENSURING CONSISTENCY */
.top-bar {
  height: 60px;
  background-color: #409eff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center; /* Vertical centering for left and right sections */
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
  align-items: center; /* Vertical centering for user-info block */
}
.top-bar .user-info {
  display: flex;
  align-items: center; /* Vertical centering for avatar, name, arrow */
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background-color 0.2s;
  position: relative;
}
.top-bar .user-info:hover {
  background-color: #3a8ee6; /* Consistent hover color */
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
  /* Font Awesome icon inside avatar */
  font-size: 18px; /* Corrected icon size */
  color: #409eff;
}
.top-bar .user-name {
  margin: 0 8px 0 0;
  font-size: 14px; /* Consistent font size */
  color: white;
  line-height: 1; /* Crucial for vertical alignment */
}
.top-bar .user-info .fa-angle-down {
  /* Dropdown arrow */
  font-size: 12px; /* Standard size */
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

/* Main Content Specifics for this page */
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
.course-overview-card .sub-card-title i {
  color: #e6a23c;
}
#analyzedCourseName {
  color: #409eff;
  font-style: italic;
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
.filter-form-grid .form-group select[disabled],
.filter-form-grid button[disabled] {
  /* Moved button[disabled] here */
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
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
/* .btn-primary[disabled] already handled with .filter-form-grid button[disabled] */

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
.overall-course-stats {
  display: grid;
  grid-template-columns: repeat(
    auto-fit,
    minmax(150px, 1fr)
  ); /* Adjusted min */
  gap: 15px;
  padding-top: 10px;
  text-align: center;
}
.stat-item {
  background-color: #f9fafc;
  padding: 12px 15px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
.stat-label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.stat-value {
  display: block;
  font-size: 20px;
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

/* Placeholder for no selection */
.placeholder-no-selection {
  text-align: center;
  padding: 50px 20px;
  color: #909399;
  background-color: #fff;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  margin-top: 20px;
}
.placeholder-no-selection i {
  font-size: 2.5em;
  margin-bottom: 15px;
  color: #c0c4cc;
}
.placeholder-no-selection p {
  font-size: 16px;
  margin: 0;
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
  text-align: center; /* This centers the .copyright-text */
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
  .overall-course-stats {
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  }
  .stat-value {
    font-size: 18px;
  }
  .chart-container {
    height: 250px;
  }
}
</style>
