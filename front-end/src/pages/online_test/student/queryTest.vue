<template>
  <div class="exam-query-container">
    <!-- 顶部标题和查询区域 -->
    <div class="header-section">
      <h1>考试查询</h1>
      
      <div class="query-section">
        <div class="query-header">
          <h2 class="course-title">课程</h2>
          <el-select
            v-model="selectedCourse"
            placeholder="请选择课程"
            :loading="loadingCourses"
            :disabled="loadingCourses"
            style="width: 300px; margin-right: 10px;"
          >
            <el-option
              v-for="course in courses"
              :key="course.course_id"
              :label="course.course_name"
              :value="course.course_id"
            />
          </el-select>
          <el-button
            type="primary"
            @click="fetchExams"
            :loading="loadingExams"
            :disabled="!selectedCourse"
          >
            查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 结果表格区域 -->
    <div class="result-section" v-if="exams.length > 0">
      <h2>共查询到{{ exams.length }}条结果</h2>
      
      <el-table :data="exams" border style="width: 100%">
        <el-table-column prop="test_name" label="考试名称" width="180">
        <template #default="{row}">
            {{ row.test_name || '未命名考试' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{row}">
            {{ formatDate(row.publish_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="结束时间" width="180">
          <template #default="{row}">
            {{ formatDate(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="isOpenBook" label="是否开卷" width="120">
          <template #default="{row}">
            {{ row.isOpenBook ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{row}">
            {{ getExamStatus(row) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button
              v-if="getExamStatus(row) === '进行中'"
              type="text"
              @click="enterExam(row)"
            >
              进入考试
            </el-button>
            <span v-else>{{  }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 空状态 -->
    <el-empty v-if="!loadingCourses && !loadingExams && exams.length === 0 && selectedCourse" description="暂无考试数据" />
    
    <!-- 错误信息 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      style="margin-top: 20px;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useuserLoginStore } from '../../../store/userLoginStore'

const router = useRouter()
const userStore = useuserLoginStore()

// 创建api实例，与其他online_test模块使用相同的配置
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// 响应式数据
const loadingCourses = ref(false)
const loadingExams = ref(false)
const courses = ref([])
const selectedCourse = ref('')
const exams = ref([])
const error = ref(null)
const studentId = ref(Number(userStore.loginUser.user_id) || 1) // 从store获取user_id并转换为数字

// 格式化日期
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取考试状态
const getExamStatus = (exam) => {
  const now = new Date()
  const startTime = new Date(exam.publish_time)
  const endTime = new Date(startTime.getTime() + exam.duration * 60 * 1000)
  
  if (now < startTime) return '未开始'
  if (now > endTime) return '已结束'
  return '进行中'
}

// 获取课程列表 - 对接/test/testPublish/getStudentCourses/${studentId}接口
const fetchCourses = async () => {
  loadingCourses.value = true
  error.value = null
  try {
    const response = await api.get(`/test/testPublish/getStudentCourses`,{
      params: {
        studentId: studentId.value,
      }
    })
    console.log("响应数据：", response.data)
    courses.value = response.data
  } catch (err) {
    console.error('获取课程列表失败:', err)
    error.value = '获取课程列表失败，请稍后重试'
    ElMessage.error('获取课程列表失败')
  } finally {
    loadingCourses.value = false
  }
}

// 获取考试数据 - 对接/test/testPublish/getTestForStudent/{studentId,courseId}接口
const fetchExams = async () => {
  if (!selectedCourse.value) return
  
  loadingExams.value = true
  error.value = null
  try {
    const response = await api.get('/test/testPublish/getTestForStudent', {
      params: {
        studentId: studentId.value,
        courseId: selectedCourse.value
      }
    })
    console.log("响应数据：", response.data)
    exams.value = response.data
  } catch (err) {
    console.error('查询考试失败:', err)
    error.value = '查询考试失败，请稍后重试'
    exams.value = []
    ElMessage.error('查询考试失败')
  } finally {
    loadingExams.value = false
  }
}

// 进入考试
const enterExam = (exam) => {
  if (getExamStatus(exam) === '进行中') {
    router.push({ name: 'enterTest', params: { test_id: exam.test_id }})
  }
}

// 添加监听，当用户ID变化时重新获取课程
onMounted(() => {
  // 确认用户ID可用后再获取课程
  if (userStore.loginUser && userStore.loginUser.user_id !== 'null') {
    studentId.value = Number(userStore.loginUser.user_id)
    console.log('当前用户ID:', studentId.value)
    fetchCourses()
  } else {
    console.warn('用户未登录或ID不可用')
    ElMessage.warning('请先登录系统')
  }
})
</script>

<style scoped>
.exam-query-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  background-color: #f5f7fa;
}

h1 {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
  color: #303133;
}

.query-section {
  margin-top: 20px;
}

.query-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.course-title {
  margin: 0 10px 0 0;
  font-size: 16px;
  font-weight: normal;
  color: #606266;
}

.result-section {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
  background-color: #f5f7fa;
  margin-top: 20px;
}

h2 {
  text-align: center;
  font-size: 18px;
  font-weight: normal;
  margin-bottom: 15px;
  color: #303133;
}
</style>