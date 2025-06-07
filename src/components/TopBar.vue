<template>
  <div class="top-bar">
    <!-- 左侧Logo/标题 -->
    <div class="left-section">
      <el-icon size="30px" @click="isSidebarVisible.toggle()" class="sidebar-toggle-icon">
        <Operation />
      </el-icon>
      <el-icon size="30px" v-if="!router.currentRoute.value.path.startsWith('/online_test/test') && router.currentRoute.value.path !== '/home' && user !== 'invalid'" @click="goToHome()" class="back-icon"><HomeFilled /></el-icon>
      <el-icon size="30px" v-if="router.currentRoute.value.path !== '/home'" @click="goBack()" class="back-icon"><Back /></el-icon>
      <span class="system-name">{{ pageTitle }}</span>
      <span class="sub-system-name" v-if="router.currentRoute.value.path === '/home' && user !== 'invalid'">{{ relfect_name(activeModule.get()) }}</span>
    </div>

    <!-- 右侧用户信息 -->
    <div class="right-section">
      <div class="search-area" v-if="user !== 'invalid'">
        <transition name="search-input-transition">
          <el-input
            v-if="isSearchVisible"
            ref="searchInputRef"
            v-model="searchQuery"
            placeholder="请输入需要的功能"
            class="search-input-field"
            clearable
            size="small"
          />
        </transition>
        <el-icon @click="toggleSearch" class="search-toggle-icon" size="22">
          <Close v-if="isSearchVisible" />
          <Search v-else />
        </el-icon>
        <div v-if="isSearchVisible && filteredFunctions.length > 0" class="search-results-dropdown">
          <el-card shadow="never" :body-style="{ padding: '0px', border: 'none' }">
            <div
              v-for="funcItem in filteredFunctions"
              :key="funcItem.name"
              class="search-result-item"
              @click="selectFunction(funcItem)"
            >
              {{ funcItem.name }}
            </div>
          </el-card>
        </div>
      </div>

      <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">

        <div class="user-info">
          <el-avatar :size="36" :src="userAvatar" />
          <span class="user-name">{{ userName }}</span>
          <el-icon><arrow-down /></el-icon>
        </div>

        <template #dropdown v-if="user !== 'invalid'">
          <el-dropdown-menu>
            <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>

      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Back, Operation, Search, Close, HomeFilled } from '@element-plus/icons-vue'
import { inject, computed, ref, nextTick } from 'vue'
import { useuserLoginStore } from '../store/userLoginStore'

const router = useRouter()
const is_homepage = inject('is_homepage')
const isSidebarVisible = inject('is_sidebar_visible')
const userName = inject('user_name')
const userAvatar = inject('user_avatar')
const user = inject('user')
const activeModule = inject('activeModule')
const userLoginStore = useuserLoginStore()

const isSearchVisible = ref(false)
const searchQuery = ref('')
const searchInputRef = ref(null)

// Function list based on Home.vue structure
const allFunctions = [
  // Info Management
  { name: '个人信息', module: 'infoManagement', roles: ['student', 'teacher', 'admin'] },
  { name: '成绩查询', module: 'infoManagement', roles: ['student'] },
  { name: '学情分析', module: 'infoManagement', roles: ['student'] },
  { name: '用户管理', module: 'infoManagement', roles: ['admin'] },
  { name: '课程管理', module: 'infoManagement', roles: ['admin'] },
  { name: '审核成绩修改', module: 'infoManagement', roles: ['admin'] },
  { name: '提交成绩', module: 'infoManagement', roles: ['teacher'] },
  { name: '我的课程', module: 'infoManagement', roles: ['teacher'] },
  { name: '修改课程信息', module: 'infoManagement', roles: ['teacher'] },
  { name: '修改已提交成绩', module: 'infoManagement', roles: ['teacher'] },
  { name: '课程成绩分析', module: 'infoManagement', roles: ['teacher'] },

  // Course Arrangement
  { name: '浏览教室信息（管理员端）', module: 'courseArrangement', roles: ['admin'] },
  { name: '浏览教室信息（教师端）', module: 'courseArrangement', roles: ['teacher'] },
  { name: '添加教室', module: 'courseArrangement', roles: ['admin'] },
  { name: '修改教室信息', module: 'courseArrangement', roles: ['admin'] },
  { name: '删除教室', module: 'courseArrangement', roles: ['admin'] },
  { name: '自动排课', module: 'courseArrangement', roles: ['admin'] },
  { name: '手动调整课程', module: 'courseArrangement', roles: ['admin'] },
  { name: '教室课表查询', module: 'courseArrangement', roles: ['teacher'] },
  { name: '教师课表查询', module: 'courseArrangement', roles: ['teacher'] },

  // Course Selection
  { name: '定制培养方案', module: 'courseSelection', roles: ['student'] },
  { name: '查看培养方案', module: 'courseSelection', roles: ['student'] },
  { name: '搜索课程信息', module: 'courseSelection', roles: ['student'] },
  { name: '课程初选', module: 'courseSelection', roles: ['student'] },
  { name: '课程补选', module: 'courseSelection', roles: ['student'] },
  { name: '查看选课结果', module: 'courseSelection', roles: ['student'] },
  { name: '查看选课结果（教师端）', module: 'courseSelection', roles: ['teacher'] },
  { name: '课程选择管理', module: 'courseSelection', roles: ['admin'] },
  { name: '手动选择课程', module: 'courseSelection', roles: ['admin'] },
  { name: '制订培养方案', module: 'courseSelection', roles: ['admin'] },
  { name: '课程补选管理', module: 'courseSelection', roles: ['admin'] },

  // Online Quiz
  { name: '考试查询', module: 'onlineQuiz', roles: ['student'] },
  { name: '在线测验成绩查询', module: 'onlineQuiz', roles: ['student'] }, // Differentiated from infoManagement grades
  { name: '题库管理', module: 'onlineQuiz', roles: ['teacher'] },
  { name: '考试管理', module: 'onlineQuiz', roles: ['teacher'] },
  { name: '成绩查询分析', module: 'onlineQuiz', roles: ['teacher'] },

  // Resource Sharing
  { name: '课程资源上传', module: 'resourceSharing', roles: ['student'] },
  { name: '课程资源查看', module: 'resourceSharing', roles: ['student'] },
  { name: '作业', module: 'resourceSharing', roles: ['student'] },
  { name: '作业（教师端）', module: 'resourceSharing', roles: ['teacher'] },
  { name: '考勤（教师端）', module: 'resourceSharing', roles: ['teacher'] }
];

const filteredFunctions = computed(() => {
  if (!searchQuery.value.trim()) {
    return [];
  }
  const currentUserRole = user.value;
  let result = allFunctions.filter(func =>
    func.roles.includes(currentUserRole) &&
    func.name.toLowerCase().includes(searchQuery.value.toLowerCase().trim())
  );
  if (result.length === 0) {
    return [{name: "没有找到相关功能", module: "invalid", roles: ["invalid"]}]
  }
  return result
});

const toggleSearch = async () => {
  isSearchVisible.value = !isSearchVisible.value;
  if (isSearchVisible.value) {
    await nextTick();
    searchInputRef.value?.focus();
  } else {
    searchQuery.value = ''; // Clear search query when hiding
  }
};

const selectFunction = (funcItem) => {
  if (funcItem.module === "invalid") {
    isSearchVisible.value = false;
    searchQuery.value = '';
    return
  }
  activeModule.set(funcItem.module);
  is_homepage.set(true); // Ensure we are on the homepage
  router.push('/home');
  searchQuery.value = '';
  isSearchVisible.value = false;
};

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userLoginStore.setLoginUserUnlogin()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch (error) {
      console.log('取消退出')
    }
  } else if (command === 'changePassword') {
    router.push('/changePassword')
    is_homepage.set(false);
  }
}

function goToHome() {
  is_homepage.set(true);
  router.push('/home')
}

const pageTitle = computed(() => {
  const currentPath = router.currentRoute.value.path
  if (currentPath === '/home' || currentPath === '/' || currentPath === '/login') {
    return '教学服务系统'
  } else {
    if (currentPath.startsWith('/online_test/enterTest')) {
      return '考试准备'
    }
    if (currentPath.startsWith('/online_test/test')) {
      return '在线考试'
    }
    // 返回对应页面的标题
    switch (currentPath) {
      case '/course_selection/chooseCurriculum':
        return '定制培养方案'
      case '/course_selection/showCurriculum':
        return '查看培养方案'
      case '/course_selection/searchCourse':
        return '搜索课程信息'
      case '/course_selection/chooseCourse':
        return '课程初选'
      case '/course_selection/showResult':
        return '查看选课结果'
      case '/course_selection/showResultTeacher':
        return '查看选课结果（教师端）'
      case '/course_selection/manage':
        return '课程选择管理'
      case '/course_selection/manualChoose':
        return '手动选择课程'
      case '/course_selection/setCurriculum':
        return '制订培养方案'
      case '/course_selection/chooseCourseSupplementary':
        return '课程补选'
      case '/course_selection/manageSupplementary':
        return '课程补选管理'
      case '/course_schedule/classroomBrowseAdmin':
        return '浏览教室信息（管理员端）'
      case '/course_schedule/classroomBrowseTeacher':
        return '浏览教室信息（教师端）'
      case '/course_schedule/classroomAddition':
        return '添加教室'
      case '/course_schedule/classroomModify':
        return '修改教室信息'
      case '/course_schedule/classroomDeletion':
        return '删除教室'
      case '/course_schedule/courseArrangement':
        return '手动调整课程'
      case '/course_schedule/courseAutoArrange':
        return '自动排课'
      case '/course_schedule/courseArrangementQueryClassroom':
        return '教室课表查询'
      case '/course_schedule/courseArrangementQueryTeacher':
        return '教师课表查询'
      case '/online_test/setQuestionBank':
        return '题库管理'
      case '/online_test/manageTest':
        return '考试管理'
      case '/online_test/searchAnalysis':
        return '成绩查询分析'
      case '/online_test/searchResult':
        return '成绩查询'
      case '/online_test/queryTest':
        return '考试查询'
      case '/resource_sharing/uploadResource':
        return '课程资源上传'
      case '/resource_sharing/viewResource':
        return '课程资源查看'
      case '/resource_sharing/homework':
        return '作业'
      case '/resource_sharing/homeworkTeacher':
        return '作业（教师端）'
      case '/resource_sharing/attendance':
        return '考勤（教师端）'
      case '/info_manage/profile':
        return '个人信息'
      case '/info_manage/queryGrades':
        return '成绩查询'
      case '/info_manage/queryGradeAnalysis':
        return '学情分析'
      case '/info_manage/userManagement':
        return '用户管理'
      case '/info_manage/courseManagement':
        return '课程管理'
      case '/info_manage/approveGradeChange':
        return '审核成绩修改'
      case '/info_manage/submitGrades':
        return '提交成绩'
      case '/info_manage/taughtCourses':
        return '我的课程'
      case '/info_manage/editCourse':
        return '修改课程信息'
      case '/info_manage/requestGradeChange':
        return '修改已提交成绩'
      case '/info_manage/courseGradeAnalysis':
        return '课程成绩分析'
      case '/changePassword':
        return '修改密码'
    }
  }
})

const relfect_name = (module) => {
  switch (module) {
    case 'favorite':
      return '我的收藏'
    case 'infoManagement':
      return '信息管理子模块'
    case 'courseArrangement':
      return '课程安排子模块'
    case 'courseSelection':
      return '课程选择子模块'
    case 'onlineQuiz':
      return '在线测验子模块'
    case 'resourceSharing':
      return '课程资源共享子模块'
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.top-bar {
  height: 60px;
  background-color: #409EFF;
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
}

.left-section {
  display: flex;
  align-items: center;
}

.sidebar-toggle-icon {
  cursor: pointer;
  margin-right: 15px;
  color: white;
}

.sidebar-toggle-icon:hover {
  opacity: 0.7;
  transition: all 0.2s;
}

.system-name {
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin-left: 10px;
}

.system-subname {
  font-size: 16px;
  font-weight: bold;
  color: white;
  margin-left: 20px;
}

.right-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: all 0.2s;
}

.user-info:hover {
  background-color: #1370eb;
}

.user-name {
  margin: 0 8px;
  font-size: 15px;
  color: white;
}

.el-icon {
  font-size: 12px;
  color: white;
}

.back-icon {
  cursor: pointer;
  margin-right: 15px;
  margin-left: 7px;
}

.back-icon:hover {
  opacity: 0.5;
  transition: all 0.2s;
}

.search-area {
  display: flex;
  align-items: center;
  position: relative;
  margin-right: 20px;
}

.search-input-field {
  width: 200px;
  font-size: 13.5px;
  height: 33px;
  margin-right: 8px;
  transition: width 0.2s ease-in-out;
}

.search-input-field .el-input__inner {
  color: #303133;
}

.search-toggle-icon {
  cursor: pointer;
  color: white !important;
  font-size: 25px !important;
}

.search-toggle-icon:hover {
  opacity: 0.7;
  transition: all 0.2s;
}

.search-results-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 220px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  z-index: 2000;
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  padding: 10px 15px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.search-input-transition-enter-active,
.search-input-transition-leave-active {
  transition: all 0.2s ease-out;
}
.search-input-transition-enter-from,
.search-input-transition-leave-to {
  width: 0;
  opacity: 0;
  margin-right: 0;
  padding-left: 0;
  padding-right: 0;
}

.sub-system-name {
  font-size: 17px;
  font-weight: bold;
  color: white;
  margin-left: 20px;
}
</style>