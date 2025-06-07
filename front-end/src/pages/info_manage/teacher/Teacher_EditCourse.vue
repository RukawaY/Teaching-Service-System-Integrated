<template>
  <div>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container edit-course-container">
        <div class="card course-selection-card">
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-tasks" /> 选择要修改的课程
          </h2>
          <form id="selectCourseToEditForm" class="filter-form-grid">
            <div class="form-group">
              <label for="requestCourse">选择课程:</label>
              <select
                  id="requestCourse"
                  name="courseId"
                  v-model="selectedCourseId"
                  @change="populateSectionSelect"
              >
                <option
                    v-for="course in courses"
                    :key="course.course_id"
                    :value="course.course_id"
                >
                  {{ course.course_name }}
                </option>
              </select>
            </div>
            <div class="form-action-group">
              <button
                  type="button"
                  class="btn btn-primary"
                  :disabled="!selectedCourseId"
                  @click="loadCourseDetails"
              >
                <FontAwesomeIcon icon="fas fa-cogs" /> 加载课程详情
              </button>
            </div>
          </form>
        </div>

        <div
            class="card course-details-card"
            id="courseDetailsCard"
            :style="{ display: showCourseDetails ? 'block' : 'none' }"
        >
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-book-open" /> 课程《<span
              id="editingCourseNameTitle"
          >{{ selectedCourse }}</span
          >》信息编辑
          </h2>
          <form id="editCourseDetailsForm" @submit.prevent="saveCourseDetails">
            <div class="form-group">
              <label for="courseCodeDisplay">课程名称:</label>
              <input
                  type="text"
                  id="courseCodeDisplay"
                  v-model="currentCourse.course_name"
              />
              <span
                  class="character-count"
                  :class="{
                  'near-limit':
                    currentCourse.course_name &&
                    currentCourse.course_name.length > 60,
                }"
              >
                {{
                  currentCourse.course_name
                      ? currentCourse.course_name.length
                      : 0
                }}/80
              </span>
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">学分:</label>
              <input
                  type="number"
                  id="courseCreditsDisplay"
                  v-model="currentCourse.credit"
                  min="0.1"
                  step="0.1"
              />
              <span
                  class="character-count"
                  :class="{
                  'near-limit':
                    currentCourse.credit && Number(currentCourse.credit) > 15,
                }"
              >
                可输入小数，最小值: 0.1，最大值: 20
              </span>
            </div>
            <div class="form-group">
              <label for="courseIntroduction">课程类别:</label>
              <select
                  id="courseCategory"
                  name="Category"
                  v-model="currentCourse.category"
                  readonly
              >
                <option>{{ currentCourse.category }}</option>
                <option>体育</option>
                <option>普通</option>
                <option>实验</option>
              </select>
            </div>

            <hr class="section-divider" />

            <div class="form-group">
              <label for="courseIntroduction">课程简介:</label>
              <textarea
                  id="courseIntroduction"
                  name="introduction"
                  rows="5"
                  v-model="currentCourse.course_description"
                  placeholder="请输入课程简介..."
              ></textarea>
              <span
                  class="character-count"
                  :class="{
                  'near-limit':
                    currentCourse.course_description &&
                    currentCourse.course_description.length > 800,
                }"
              >
                {{
                  currentCourse.course_description
                      ? currentCourse.course_description.length
                      : 0
                }}/1000
              </span>
            </div>

            <div class="form-actions">
              <button
                  type="submit"
                  class="btn btn-primary"
                  @click="saveCourseDetails"
              >
                <FontAwesomeIcon icon="fas fa-save" /> 保存并提交修改
              </button>
              <button
                  type="button"
                  class="btn btn-default"
                  @click="resetCourseEdit"
              >
                <FontAwesomeIcon icon="fas fa-circle" /> 重置
              </button>
              <button
                  type="button"
                  class="btn btn-default"
                  @click="cancelCourseEdit"
              >
                <FontAwesomeIcon icon="fas fa-times" /> 取消
              </button>
            </div>
            <div class="form-group">
              <label for="requestStudent">选择开课时间:</label>
              <select
                  id="requestStudent"
                  name="studentId"
                  v-model="selectedSection"
                  :disabled="!selectedCourseId"
              >
                <option value="">-- 请选择开课时间 --</option>
                <option
                    v-for="section in sections"
                    :key="section.section_id || section.sectionId"
                    :value="section.section_id || section.sectionId"
                >
                  {{ section.sec_year || section.secYear }} 学年
                  {{ section.semester }} 学期
                  {{
                    section.classroom_location
                        ? ` - ${section.classroom_location}`
                        : ""
                  }}
                  {{
                    section.sec_time || section.secTime
                        ? `(${section.sec_time || section.secTime})`
                        : ""
                  }}
                </option>
              </select>
            </div>
            <div class="form-action-group">
              <button
                  type="button"
                  class="btn btn-primary"
                  :disabled="!selectedSection"
                  @click="loadSectionDetails"
              >
                <FontAwesomeIcon icon="fas fa-cogs" /> 加载开课详情
              </button>
            </div>
          </form>
        </div>

        <div
            class="card course-details-card"
            id="courseDetailsCard"
            :style="{ display: showSectionDetails ? 'block' : 'none' }"
        >
          <h2 class="card-title">
            <FontAwesomeIcon icon="fas fa-book-open" /> 课程《<span
              id="editingCourseNameTitle"
          >{{ selectedCourse }}</span
          >》开课信息编辑
          </h2>
          <form id="editCourseDetailsForm" @submit.prevent="saveSectionDetails">
            <div class="form-group">
              <label for="courseCodeDisplay">所在学年:</label>
              <input
                  type="number"
                  id="courseYearDisplay"
                  v-model="currentSection.sec_year"
                  required
              />
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">所在学期</label>
              <select
                  id="courseSemesterDisplay"
                  name="Category"
                  v-model="currentSection.semester"
                  required
              >
                <option value="">请选择学期</option>
                <option value="春夏">春夏</option>
                <option value="秋冬">秋冬</option>
              </select>
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">上课时间:</label>
              <input
                  type="text"
                  id="courseTimeDisplay"
                  v-model="currentSection.sec_time"
                  placeholder="例如: 周一 1-2节;周三 6-8节"
                  required
              />
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">课程容量:</label>
              <input
                  type="number"
                  id="courseCapacityDisplay"
                  v-model="currentSection.capacity"
                  min="1"
                  required
              />
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">教室地址:</label>
              <input
                  type="text"
                  id="courseClassroomDisplay"
                  readonly
                  v-model="currentSection.classroom_location"
              />
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">教室容量:</label>
              <input
                  type="text"
                  id="courseAvailableCapacityDisplay"
                  readonly
                  v-model="currentSection.classroom_capacity"
              />
            </div>
            <div class="form-group">
              <label for="courseCreditsDisplay">剩余容量:</label>
              <input
                  type="text"
                  id="courseAvailableCapacityDisplay"
                  readonly
                  v-model="currentSection.available_capacity"
              />
            </div>

            <div class="form-actions">
              <button
                  type="submit"
                  class="btn btn-primary"
                  @click="saveSectionDetails"
              >
                <FontAwesomeIcon icon="fas fa-save" /> 保存并提交修改
              </button>
              <button
                  type="button"
                  class="btn btn-default"
                  @click="resetSectionEdit"
              >
                <FontAwesomeIcon icon="fas fa-circle" /> 重置
              </button>
              <button
                  type="button"
                  class="btn btn-default"
                  @click="cancelSectionEdit"
              >
                <FontAwesomeIcon icon="fas fa-times" /> 取消
              </button>
            </div>
          </form>
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
import { ref, onMounted } from "vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { useuserLoginStore } from "../../../store/userLoginStore";
import {
  getMyCourses,
  getMyCourseSections,
  updateCourse,
  updateSection,
} from "../../../api/info_manage/teacher";
import { getSectionDetails } from "../../../api/info_manage/section";
import { getCourseDetails } from "../../../api/info_manage/course";
import { useRouter } from "vue-router";

// 定义课程类型接口
interface Course {
  course_id: string | number;
  course_name: string;
  course_description: string;
  credit: number;
  category: string;
  hours_per_week: number;
  teacher_id?: string | number;
  teacher_name?: string;
}

// 定义开课信息类型接口
interface Section {
  section_id: string | number;
  sec_year: number;
  semester: string;
  sec_time: string;
  capacity: number;
  classroom_id: string | number;
  classroom_location?: string;
  available_capacity?: number;
  classroom_capacity: number;
  name?: string;
}

const router = useRouter();
const loginUserStore = useuserLoginStore();
const userDropdownVisible = ref(false);
const selectedCourseId = ref("");
const selectedCourse = ref("");
const selectedSection = ref("");
const courses = ref<any[]>([]);
const sections = ref<any[]>([]);
const currentCourse = ref<Course>({} as Course);
const showCourseDetails = ref(false);
const currentSection = ref<Section>({} as Section);
const showSectionDetails = ref(false);
const notification = ref({
  message: "",
  type: "info",
});

const toggleUserDropdown = () => {
  userDropdownVisible.value = !userDropdownVisible.value;
};

// 处理退出登录
const handleLogout = () => {
  showNotification("正在退出登录...", "info");
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



const populateCourseSelect = async () => {
  courses.value = [];
  sections.value = [];
  selectedCourseId.value = "";
  selectedSection.value = "";
  showCourseDetails.value = false;

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

const populateSectionSelect = async () => {
  sections.value = [];
  selectedSection.value = "";
  showCourseDetails.value = false;
  showSectionDetails.value = false;

  if (!selectedCourseId.value) {
    showNotification("请先选择课程", "error");
    return;
  }

  try {
    console.log("选择的课程ID:", selectedCourseId.value);

    // 正确传递参数：
    // 1. 不传递sec_year参数，后端会将其视为null并设为0
    // 2. 传递空字符串作为semester
    const params = {
      semester: "",
      // 不传sec_year参数
    };

    console.log("发送请求参数:", params);
    const response = await getMyCourseSections(selectedCourseId.value, params);

    console.log("获取到的开课数据:", response);

    // 防御性编程：检查数据结构
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        sections.value = response.data;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        sections.value = response.data.data;
      } else if (
          response.data.data &&
          Array.isArray(response.data.data.items)
      ) {
        sections.value = response.data.data.items;
      } else {
        console.error("API返回的开课数据结构不符合预期:", response.data);
        showNotification("API返回的开课数据结构不符合预期", "error");
      }

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

const loadCourseDetails = async () => {
  try {
    const response = await getCourseDetails(selectedCourseId.value);
    currentCourse.value = response.data.data;
    selectedCourse.value = response.data.data.course_name;
    showCourseDetails.value = true;
    showNotification(
        `已加载课程《${currentCourse.value.course_name}》的详细信息。`,
        "info"
    );
  } catch (error) {
    showNotification("无法加载课程详情。", "error");
    showCourseDetails.value = false;
    console.error(error);
  }
};

const saveCourseDetails = async () => {
  if (
      !currentCourse.value.course_name ||
      !currentCourse.value.course_description ||
      !currentCourse.value.credit
  ) {
    showNotification("课程名称,简介,学分不能为空", "error");
    return;
  }

  // 检查输入限制
  if (currentCourse.value.course_name.length > 80) {
    showNotification("课程名称不能超过80个字符", "error");
    return;
  }

  const creditValue = Number(currentCourse.value.credit);
  if (creditValue <= 0) {
    showNotification("课程学分必须大于0", "error");
    return;
  }

  if (creditValue > 20) {
    showNotification("课程学分不能超过20", "error");
    return;
  }

  if (currentCourse.value.course_description.length > 1000) {
    showNotification("课程简介不能超过1000个字符", "error");
    return;
  }

  try {
    showNotification("正在保存课程信息...", "info");
    const response = await updateCourse(currentCourse.value.course_id, {
      course_name: currentCourse.value.course_name,
      course_description: currentCourse.value.course_description,
      credit: currentCourse.value.credit,
      category: currentCourse.value.category,
      hours_per_week: currentCourse.value.hours_per_week,
    });
    selectedCourse.value = currentCourse.value.course_name;
    await populateCourseSelect();
    showNotification(
        `课程《${currentCourse.value.course_name}》的课程信息已成功更新！`,
        "success"
    );
  } catch (error) {
    showNotification("保存课程信息失败，请稍后重试。", "error");
    console.error(error);
  }
};

const resetCourseEdit = () => {
  loadCourseDetails();
  showNotification("修改已取消。", "info");
};

const cancelCourseEdit = () => {
  showCourseDetails.value = false;
};

const loadSectionDetails = async () => {
  if (!selectedSection.value) {
    showNotification("请先选择开课时间", "error");
    return;
  }

  try {
    console.log("选择的开课ID:", selectedSection.value);
    const response = await getSectionDetails(selectedSection.value);

    console.log("获取到的开课详情:", response);

    if (response && response.data) {
      let sectionData;

      // 处理不同的数据结构
      if (response.data.data) {
        sectionData = response.data.data;
      } else {
        sectionData = response.data;
      }

      console.log("解析后的开课详情:", sectionData);

      // 确保所有必要的字段都存在
      currentSection.value = {
        section_id: sectionData.sectionId || sectionData.section_id,
        sec_year: Number(sectionData.secYear || sectionData.sec_year || 0),
        semester: sectionData.semester || "",
        sec_time: sectionData.secTime || sectionData.sec_time || "",
        capacity: Number(sectionData.capacity || 0),
        classroom_id: sectionData.classroomId || sectionData.classroom_id,
        classroom_location: sectionData.classroom_location || "",
        classroom_capacity: Number(sectionData.classroom_capacity || 0),
        available_capacity: Number(sectionData.available_capacity || 0),
      };

      console.log("最终开课详情对象:", currentSection.value);

      showSectionDetails.value = true;
      showNotification(
          `已加载课程《${currentCourse.value.course_name}》的开课信息。`,
          "info"
      );
    } else {
      showNotification("获取开课详情失败，返回数据为空", "error");
    }
  } catch (error) {
    showNotification("无法加载开课详情。", "error");
    showSectionDetails.value = false;
    console.error("获取开课详情错误:", error);
  }
};

const saveSectionDetails = async () => {
  // 添加调试信息
  console.log("准备提交的开课信息:", currentSection.value);

  // 检查所有必填字段
  if (
      !currentSection.value.sec_year ||
      !currentSection.value.semester ||
      !currentSection.value.sec_time ||
      !currentSection.value.capacity
  ) {
    console.error("缺少必填字段:", {
      sec_year: currentSection.value.sec_year,
      semester: currentSection.value.semester,
      sec_time: currentSection.value.sec_time,
      capacity: currentSection.value.capacity,
    });
    showNotification("开课学年,开课学期,上课时间,课程容量不能为空！", "error");
    return;
  }

  if (currentSection.value.capacity > currentSection.value.classroom_capacity) {
    console.error("开课容量不能大于教室容量");
    showNotification("课程容量不得超过教室容量", "error");
    return;
  }

  try {
    showNotification("正在保存开课信息...", "info");

    // 构建API请求数据，确保使用正确的字段名
    const requestData = {
      classroom_id: currentSection.value.classroom_id,
      capacity: Number(currentSection.value.capacity), // 确保是数字类型
      semester: currentSection.value.semester,
      sec_year: Number(currentSection.value.sec_year), // 确保是数字类型
      sec_time: currentSection.value.sec_time,
    };

    console.log("发送到API的数据:", requestData);

    // 使用section_id
    const sectionId = currentSection.value.section_id;
    const response = await updateSection(sectionId, requestData);

    console.log("API响应:", response);
    if (response.data.code != 200) {
      await loadSectionDetails();
      showNotification(`更新失败，发生内部错误`, "error");
    } else {
      await populateSectionSelect();
      showNotification(
          `课程《${currentCourse.value.course_name}》的开课信息已成功更新！`,
          "success"
      );
    }
  } catch (error) {
    showNotification("保存开课信息失败，请稍后重试。", "error");
    console.error("保存开课信息错误:", error);
  }
};

const resetSectionEdit = () => {
  loadSectionDetails();
  showNotification("修改已取消。", "info");
};

const cancelSectionEdit = () => {
  showSectionDetails.value = false;
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
  }, 5000);
};

onMounted(() => {
  populateCourseSelect();
});
</script>

<style scoped>
/* 这里可以复用原有的 CSS 样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
  "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  margin: 0;
  background-color: #f4f6f8;
  color: #333;
  line-height: 1.6;
  padding-top: 60px;
  padding-bottom: 50px; /* Corrected bottom padding */
  box-sizing: border-box;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 新增样式：字符计数 */
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

/* Top Bar - ENSURING CONSISTENCY */
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
} /* Consistent hover */

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
} /* Corrected icon style */

.top-bar .user-name {
  margin: 0 8px 0 0;
  font-size: 14px;
  color: white;
  line-height: 1;
} /* Corrected name style */

.top-bar .user-info .fa-angle-down {
  font-size: 12px;
  color: white;
  margin-left: 5px;
  transition: transform 0.2s;
} /* Corrected arrow */

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

.container.edit-course-container {
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
  font-size: 1.05em;
}

#editingCourseNameTitle {
  color: #e6a23c;
  font-weight: bold;
}

/* Filter Form Grid */
.filter-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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

@media (min-width: 800px) {
  .filter-form-grid .form-action-group {
    grid-column: auto;
    align-self: end;
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

.form-group select,
.form-group input[type="text"],
.form-group input[type="file"],
.form-group textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  background-color: #fff;
  line-height: 1.5;
}

.form-group textarea {
  resize: vertical;
}

.form-group input[readonly] {
  background-color: #f5f7fa;
  color: #909399;
  cursor: default;
  border-color: #e4e7ed;
}

.form-group select:focus,
.form-group input[type="text"]:focus,
.form-group input[type="file"]:focus,
.form-group textarea:focus {
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
.form-group input[disabled],
.form-group button[disabled] {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

.section-divider {
  grid-column: 1 / -1;
  border: none;
  border-top: 1px dashed #ebeef5;
  margin: 15px 0;
}

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

  .filter-form-grid .form-action-group {
    justify-content: center;
  }

  .form-actions {
    justify-content: center;
  }
}
</style>
