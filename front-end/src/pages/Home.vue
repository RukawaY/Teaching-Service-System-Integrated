<template>
  <div>
    <!-- 欢迎用户 -->
     <div class="welcome-banner">
       <el-icon class="welcome-icon"><Bell /></el-icon>
       <span class="welcome-text">欢迎登录教学服务系统，{{ user_name }}！今天是{{ currentDate }}</span>
     </div>

     <!-- 我的收藏模块 -->
     <div v-if="activeModule.get() === 'favorite'">
      <div v-if="favoriteCardDetails.length === 0" class="module-content-placeholder">
        还没有任何收藏的功能，请点击卡片收藏
      </div>
      <div v-else style="display: flex;flex-wrap: wrap; justify-content: start; padding-left: 50px;">
        <el-button
          v-for="card in favoriteCardDetails"
          :key="card.id"
          class="cardBox"
          @click="card.action"
          :style="{ position: 'relative' }"
        >
          <el-icon class="favorite-button" @click.stop="toggleFavorite(card.id)">
            <component :is="StarFilled" />
          </el-icon>
          <div>
            <div :style="card.titleStyle || { fontSize: '28px', fontWeight: 'bold' }" v-html="card.title"></div>
            <el-divider />
            <el-icon size="150px" :color="card.iconColor"><component :is="card.icon" /></el-icon>
          </div>
        </el-button>
      </div>
    </div>

    <!-- 通用模块渲染 -->
    <div v-for="moduleInfo in modules" :key="moduleInfo.name">
      <div v-if="activeModule.get() === moduleInfo.name && moduleInfo.name !== 'favorite'">
        <div style="display: flex; flex-wrap: wrap; justify-content: start; padding-left: 50px;">
          <el-button
            v-for="card in getModuleCards(moduleInfo.name)"
            :key="card.id"
            class="cardBox"
            @click="card.action"
            :style="{ position: 'relative' }"
          >
            <el-icon class="favorite-button" @click.stop="toggleFavorite(card.id)">
              <component :is="isFavorite(card.id) ? StarFilled : Star" />
            </el-icon>
            <div>
              <div :style="card.titleStyle || { fontSize: '28px', fontWeight: 'bold' }" v-html="card.title"></div>
              <el-divider />
              <el-icon size="150px" :color="card.iconColor"><component :is="card.icon" /></el-icon>
            </div>
          </el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import {
  AddLocation, Bell, Calendar, Check, CircleCheck, Collection, CollectionTag, Compass, DataAnalysis, DCaret,
  DocumentAdd, DocumentChecked, Edit, EditPen, Files, Histogram, InfoFilled, List, Monitor, Operation,
  Promotion, Search, SetUp, Star, StarFilled, Tickets, Tools, TrendCharts, Upload, User, Picture // Added Picture as a fallback/example
} from '@element-plus/icons-vue';
import { inject, onMounted, ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';

const activeModule = inject('activeModule');
const user = inject('user');
const router = useRouter();
const is_homepage = inject('is_homepage');
const user_name = inject('user_name');
const currentDate = ref('');

onMounted(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  currentDate.value = `${year}年${month}月${day}日`;

  // Load favorites from localStorage
  const savedFavorites = localStorage.getItem('favoritedCardIds');
  if (savedFavorites) {
    favoritedCardIds.value = JSON.parse(savedFavorites);
  }
});

// Define all navigation functions (assuming they are already defined below)
// ... (goToProfile, goToQueryGrades, etc. functions should be here)

const allCards = ref([
  // 信息管理模块 (infoManagement)
  { id: 'profile', title: '个人信息', icon: Compass, iconColor: '#409EFF', action: () => goToProfile(), userTypes: ['student', 'teacher', 'admin'], module: 'infoManagement' },
  { id: 'queryGrades', title: '成绩查询', icon: Histogram, iconColor: '#67C23A', action: () => goToQueryGrades(), userTypes: ['student'], module: 'infoManagement' },
  { id: 'queryGradeAnalysis', title: '学情分析', icon: Promotion, iconColor: '#909399', action: () => goToQueryGradeAnalysis(), userTypes: ['student'], module: 'infoManagement' },
  { id: 'userManagement', title: '用户管理', icon: User, iconColor: '#F56C6C', action: () => goToUserManagement(), userTypes: ['admin'], module: 'infoManagement' },
  { id: 'courseManagement', title: '课程管理', icon: Operation, iconColor: '#67C23A', action: () => goToCourseManagement(), userTypes: ['admin'], module: 'infoManagement' },
  { id: 'approveGradeChange', title: '审核成绩修改', icon: Check, iconColor: '#909399', action: () => goToApproveGradeChange(), userTypes: ['admin'], module: 'infoManagement' },
  { id: 'submitGrades', title: '提交成绩', icon: CollectionTag, iconColor: '#67C23A', action: () => goToSubmitGrades(), userTypes: ['teacher'], module: 'infoManagement' },
  { id: 'taughtCourses', title: '我的课程', icon: Files, iconColor: '#409EFF', action: () => goToTaughtCourses(), userTypes: ['teacher'], module: 'infoManagement' },
  { id: 'editCourse', title: '修改课程信息', icon: DCaret, iconColor: '#E6A23C', action: () => goToEditCourse(), userTypes: ['teacher'], module: 'infoManagement' },
  { id: 'requestGradeChange', title: '修改已提交成绩', icon: CircleCheck, iconColor: '#F56C6C', action: () => goToRequestGradeChange(), userTypes: ['teacher'], module: 'infoManagement' },
  { id: 'courseGradeAnalysis', title: '课程成绩分析', icon: AddLocation, iconColor: '#909399', action: () => goToCourseGradeAnalysis(), userTypes: ['teacher'], module: 'infoManagement' },

  // 课程安排模块 (courseArrangement)
  { id: 'classroomBrowseAdmin', title: '浏览教室信息<br>（管理员端）', icon: Calendar, iconColor: '#67C23A', action: () => goToClassroomBrowseAdmin(), userTypes: ['admin'], module: 'courseArrangement' },
  { id: 'classroomBrowseTeacher', title: '浏览教室信息<br>（教师端）', icon: Calendar, iconColor: '#67C23A', action: () => goToClassroomBrowseTeacher(), userTypes: ['teacher'], module: 'courseArrangement' },
  { id: 'classroomAddition', title: '添加教室', icon: Edit, iconColor: '#909399', action: () => goToClassroomAddition(), userTypes: ['admin'], module: 'courseArrangement' },
  { id: 'classroomModify', title: '修改教室信息', icon: InfoFilled, iconColor: '#000000', action: () => goToClassroomModify(), userTypes: ['admin'], module: 'courseArrangement' },
  { id: 'classroomDeletion', title: '删除教室', icon: Collection, iconColor: '#FFC0CB', action: () => goToClassroomDeletion(), userTypes: ['admin'], module: 'courseArrangement' }, // Pink color
  { id: 'courseAutoArrange', title: '自动排课', icon: Tickets, iconColor: '#E6A23C', action: () => goToCourseAutoArrange(), userTypes: ['admin'], module: 'courseArrangement' },
  { id: 'courseArrangement', title: '手动调整课程', icon: Search, iconColor: '#409EFF', action: () => goToCourseArrangement(), userTypes: ['admin'], module: 'courseArrangement' },
  { id: 'courseArrangementQueryClassroom', title: '教室课表查询', icon: Tools, iconColor: '#F56C6C', action: () => goToCourseArrangementQueryClassroom(), userTypes: ['teacher'], module: 'courseArrangement' },
  { id: 'courseArrangementQueryTeacher', title: '教师课表查询', icon: SetUp, iconColor: '#909399', action: () => goToCourseArrangementQueryTeacher(), userTypes: ['teacher'], module: 'courseArrangement' },

  // 课程选择模块 (courseSelection)
  { id: 'chooseCurriculum', title: '定制培养方案', icon: Edit, iconColor: '#909399', action: () => goToChooseCurriculum(), userTypes: ['student'], module: 'courseSelection' },
  { id: 'showCurriculum', title: '查看培养方案', icon: InfoFilled, iconColor: '#000000', action: () => goToShowCurriculum(), userTypes: ['student'], module: 'courseSelection' },
  { id: 'searchCourse', title: '搜索课程信息', icon: Search, iconColor: '#409EFF', action: () => goToSearchCourse(), userTypes: ['student'], module: 'courseSelection' },
  { id: 'chooseCourse', title: '课程初选', icon: Calendar, iconColor: '#67C23A', action: () => goToChooseCourse(), userTypes: ['student'], module: 'courseSelection' },
  { id: 'chooseCourseSupp', title: '课程补选', icon: Collection, iconColor: '#FFC0CB', action: () => goToChooseCourseSupp(), userTypes: ['student'], module: 'courseSelection' }, // Pink color
  { id: 'showResult', title: '查看选课结果', icon: Tickets, iconColor: '#E6A23C', action: () => goToShowResult(), userTypes: ['student'], module: 'courseSelection' },
  { id: 'showResultTeacher', title: '查看选课结果<br>（教师端）', titleStyle: { fontSize: '26px', fontWeight: 'bold' }, icon: Tickets, iconColor: '#E6A23C', action: () => goToShowResultTeacher(), userTypes: ['teacher'], module: 'courseSelection' },
  { id: 'manageCourseSelection', title: '课程选择管理', icon: Tools, iconColor: '#F56C6C', action: () => goToManage(), userTypes: ['admin'], module: 'courseSelection' },
  { id: 'manualChooseCourse', title: '手动选择课程', icon: SetUp, iconColor: '#909399', action: () => goToManualChoose(), userTypes: ['admin'], module: 'courseSelection' },
  { id: 'setCurriculum', title: '制订培养方案', icon: List, iconColor: '#409EFF', action: () => goToSetCurriculum(), userTypes: ['admin'], module: 'courseSelection' },
  { id: 'manageSuppCourse', title: '课程补选管理', icon: EditPen, iconColor: '#E6A23C', action: () => goToManageSupp(), userTypes: ['admin'], module: 'courseSelection' },

  // 在线测验模块 (onlineQuiz)
  { id: 'queryTest', title: '考试查询', icon: Search, iconColor: '#909399', action: () => goToQueryTest(), userTypes: ['student'], module: 'onlineQuiz' },
  { id: 'searchResultTest', title: '成绩查询', icon: DocumentChecked, iconColor: '#409EFF', action: () => goToSearchResult(), userTypes: ['student'], module: 'onlineQuiz' },
  { id: 'setQuestionBank', title: '题库管理', titleStyle: { fontSize: '26px', fontWeight: 'bold' }, icon: Tools, iconColor: '#909399', action: () => goToSetQuestionBank(), userTypes: ['teacher'], module: 'onlineQuiz' },
  { id: 'manageTest', title: '考试管理', icon: DocumentAdd, iconColor: '#E6A23C', action: () => goToManageTest(), userTypes: ['teacher'], module: 'onlineQuiz' },
  { id: 'searchAnalysisTest', title: '成绩查询分析', icon: Search, iconColor: '#409EFF', action: () => goToSearchAnalysis(), userTypes: ['teacher'], module: 'onlineQuiz' },

  // 课程资源共享模块 (resourceSharing)
  { id: 'uploadResource', title: '课程资源上传', icon: Upload, iconColor: '#909399', action: () => goToUploadResource(), userTypes: ['student', 'teacher'], module: 'resourceSharing' }, // Assuming teachers can also upload
  { id: 'viewResource', title: '课程资源查看', icon: Monitor, iconColor: '#409EFF', action: () => goToviewResource(), userTypes: ['student', 'teacher'], module: 'resourceSharing' }, // Assuming teachers can also view
  { id: 'homeworkStudent', title: '作业', icon: DataAnalysis, iconColor: '#67C23A', action: () => goToHomework(), userTypes: ['student'], module: 'resourceSharing' },
  { id: 'homeworkTeacher', title: '作业（教师端）', icon: SetUp, iconColor: '#67C23A', action: () => goToHomeworkTeacher(), userTypes: ['teacher'], module: 'resourceSharing' },
  { id: 'attendanceTeacher', title: '考勤（教师端）', icon: TrendCharts, iconColor: '#909399', action: () => goToAttendance(), userTypes: ['teacher'], module: 'resourceSharing' },
]);

// Corrected original userTypes based on template v-if conditions
// For 'uploadResource' and 'viewResource', the original template showed them only for students.
// I've updated them to be student-only as per the original template, but this can be adjusted if teachers also need access.
allCards.value = allCards.value.map(card => {
  if (card.id === 'uploadResource' || card.id === 'viewResource') {
    return { ...card, userTypes: ['student'] };
  }
  return card;
});


const modules = ref([
  { name: 'infoManagement', displayName: '信息管理' },
  { name: 'courseArrangement', displayName: '课程安排' },
  { name: 'courseSelection', displayName: '课程选择' },
  { name: 'onlineQuiz', displayName: '在线测验' },
  { name: 'resourceSharing', displayName: '课程资源共享' },
  // 'favorite' module is handled separately in the template
]);

const favoritedCardIds = ref([]);

watch(favoritedCardIds, (newFavorites) => {
  localStorage.setItem('favoritedCardIds', JSON.stringify(newFavorites));
}, { deep: true });

const toggleFavorite = (cardId) => {
  const index = favoritedCardIds.value.indexOf(cardId);
  if (index === -1) {
    favoritedCardIds.value.push(cardId);
  } else {
    favoritedCardIds.value.splice(index, 1);
  }
};

const isFavorite = (cardId) => {
  return favoritedCardIds.value.includes(cardId);
};

const getModuleCards = (moduleName) => {
  return allCards.value.filter(card =>
    card.module === moduleName &&
    (card.userTypes.includes(user.value) || card.userTypes.includes('all')) // 'all' or current user type
  );
};

const favoriteCardDetails = computed(() => {
  return favoritedCardIds.value
    .map(id => allCards.value.find(card => card.id === id))
    .filter(card => card && (card.userTypes.includes(user.value) || card.userTypes.includes('all')));
});


// Navigation functions
function goToChooseCurriculum() {
  is_homepage.set(false);
  router.push('/course_selection/chooseCurriculum');
}
function goToShowCurriculum() {
  is_homepage.set(false);
  router.push('/course_selection/showCurriculum');
}
function goToSearchCourse() {
  is_homepage.set(false);
  router.push('/course_selection/searchCourse');
}
function goToChooseCourse() {
  is_homepage.set(false);
  router.push('/course_selection/chooseCourse');
}
function goToShowResult() {
  is_homepage.set(false);
  router.push('/course_selection/showResult');
}
function goToShowResultTeacher() {
  is_homepage.set(false);
  router.push('/course_selection/showResultTeacher');
}
function goToManage() {
  is_homepage.set(false);
  router.push('/course_selection/manage');
}
function goToManualChoose() {
  is_homepage.set(false);
  router.push('/course_selection/manualChoose');
}
function goToSetCurriculum() {
  is_homepage.set(false);
  router.push('/course_selection/setCurriculum');
}
function goToChooseCourseSupp() {
  is_homepage.set(false);
  router.push('/course_selection/chooseCourseSupplementary');
}
function goToManageSupp() {
  is_homepage.set(false);
  router.push('/course_selection/manageSupplementary');
}
function goToClassroomBrowseAdmin() {
  is_homepage.set(false);
  router.push('/course_schedule/classroomBrowseAdmin');
}
function goToClassroomBrowseTeacher() {
  is_homepage.set(false);
  router.push('/course_schedule/classroomBrowseTeacher');
}
function goToClassroomAddition() {
  is_homepage.set(false);
  router.push('/course_schedule/classroomAddition');
}
function goToClassroomModify() {
  is_homepage.set(false);
  router.push('/course_schedule/classroomModify');
}
function goToClassroomDeletion() {
  is_homepage.set(false);
  router.push('/course_schedule/classroomDeletion');
}
function goToCourseAutoArrange() {
  is_homepage.set(false);
  router.push('/course_schedule/courseAutoArrange');
}
function goToCourseArrangement() {
  is_homepage.set(false);
  router.push('/course_schedule/courseArrangement');
}
function goToCourseArrangementQueryClassroom() {
  is_homepage.set(false);
  router.push('/course_schedule/courseArrangementQueryClassroom');
}
function goToCourseArrangementQueryTeacher() {
  is_homepage.set(false);
  router.push('/course_schedule/courseArrangementQueryTeacher');
}
function goToSetQuestionBank() {
  is_homepage.set(false);
  router.push('/online_test/setQuestionBank');
}
function goToManageTest() {
  is_homepage.set(false);
  router.push('/online_test/manageTest');
}
function goToSearchAnalysis() {
  is_homepage.set(false);
  router.push('/online_test/searchAnalysis');
}
function goToSearchResult() {
  is_homepage.set(false);
  router.push('/online_test/searchResult');
}
function goToQueryTest() {
  is_homepage.set(false);
  router.push('/online_test/queryTest');
}
function goToUploadResource() {
  is_homepage.set(false);
  router.push('/resource_sharing/uploadResource');
}
function goToviewResource() {
  is_homepage.set(false);
  router.push('/resource_sharing/viewResource');
}
function goToHomework() {
  is_homepage.set(false);
  router.push('/resource_sharing/homework');
}
function goToHomeworkTeacher() {
  is_homepage.set(false);
  router.push('/resource_sharing/homeworkTeacher');
}
function goToAttendance() {
  is_homepage.set(false);
  router.push('/resource_sharing/attendance');
}
function goToProfile() {
  is_homepage.set(false);
  router.push('/info_manage/profile');
}
function goToQueryGrades() {
  is_homepage.set(false);
  router.push('/info_manage/queryGrades');
}
function goToQueryGradeAnalysis() {
  is_homepage.set(false);
  router.push('/info_manage/queryGradeAnalysis');
}
function goToUserManagement() {
  is_homepage.set(false);
  router.push('/info_manage/userManagement');
}
function goToCourseManagement() {
  is_homepage.set(false);
  router.push('/info_manage/courseManagement');
}
function goToApproveGradeChange() {
  is_homepage.set(false);
  router.push('/info_manage/approveGradeChange');
}
function goToSubmitGrades() {
  is_homepage.set(false);
  router.push('/info_manage/submitGrades');
}
function goToTaughtCourses() {
  is_homepage.set(false);
  router.push('/info_manage/taughtCourses');
}
function goToEditCourse() {
  is_homepage.set(false);
  router.push('/info_manage/editCourse');
}
function goToRequestGradeChange() {
  is_homepage.set(false);
  router.push('/info_manage/requestGradeChange');
}
function goToCourseGradeAnalysis() {
  is_homepage.set(false);
  router.push('/info_manage/courseGradeAnalysis');
}
</script>

<style scoped>
.cardBox {
    height: 315px;
    width: 210px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    text-align: center;
    margin-top: 40px;
    margin-left: 11px;
    margin-right: 30px;
    margin-bottom: 20px;
    padding: 7.5px;
    padding-right: 10px;
    padding-top: 15px;
    border-radius: 15px;
    position: relative; /* Added for positioning favorite button */
}

.cardBox:hover {
  transform: translateY(-7px);
  transition: transform 0.3s ease;
}

.favorite-button {
  position: absolute;
  top: 12px; /* Adjusted for better placement */
  right: 12px; /* Adjusted for better placement */
  font-size: 27px; /* Made icon larger */
  cursor: pointer;
  z-index: 10;
  color: #FFD700; /* Gold color for star */
}

.favorite-button .el-icon svg { /* Ensure icon inside button is also colored if needed */
    vertical-align: middle; /* Align icon properly */
}


.module-content-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  font-size: 20px;
  color: #909399;
  margin: 20px; 
}

.welcome-banner {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background-color: #e6f7ff; 
  border-top: 1px solid #abe0ff; 
  border-bottom: 1px solid #abe0ff; 
  margin: 0px 0 10px 0; 
  border-radius: 4px;
  box-sizing: border-box;
}

.welcome-icon {
  margin-left: 15px;
  margin-right: 30px;
  font-size: 22px;
  color: #409EFF; 
}

.welcome-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
</style>