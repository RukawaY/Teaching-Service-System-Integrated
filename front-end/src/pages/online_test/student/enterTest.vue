<template>
  <div class="enter-test-container">
    <h1>考前准备 - 考试ID: {{ test_id }}</h1>
    
    <div v-if="loading" class="loading">加载考试信息中...</div>
    
    <div v-else-if="exam" class="exam-info">
      <h2>{{ exam.test_name }}</h2>
      <p>考试开始时间: {{ formatDate(exam.publish_time) }}</p>
      <p>考试结束时间: {{ formatDate(exam.deadline) }}</p>
      <p>试卷题量: {{ exam.question_count }}</p>
      <p>考试模式: {{ exam.mode }}</p>
      
      <button @click="startExam" class="start-button">开始考试</button>
      <button @click="goBack" class="back-button">返回</button>
    </div>
    
    <div v-else class="error" v-if="error">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { useuserLoginStore } from '../../../store/userLoginStore'

const route = useRoute()
const router = useRouter()
const userStore = useuserLoginStore()

// 创建api实例，与其他online_test模块使用相同的配置
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

const test_id = ref(route.params.test_id) // 保持变量名一致
const loading = ref(false)
const exam = ref(null)
const error = ref(null)
const studentId = ref(Number(userStore.loginUser.user_id) || 1) // 从用户状态获取学生ID

// 格式化日期
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取考试数据
const fetchExamData = async () => {
  loading.value = true
  console.log('正在请求考试数据，testId:', test_id.value) // 调试
  try {
    const response = await api.get(`/test/testPublish/getTestByTestId`, {
      params: { testId: test_id.value }
    })
    console.log('API响应数据:', response.data) // 调试
    exam.value = {
      test_name: response.data[0].test_name,
      publish_time: response.data[0].publish_time,
      deadline: response.data[0].deadline,
      question_count: response.data[0].question_count || 0,
      mode: response.data[0].mode || (response.data[0].is_open_book ? '开卷' : '闭卷')
    };
  } catch (err) {
    console.error('请求详情:', {
      url: err.config?.url,
      params: err.config?.params,
      status: err.response?.status,
      data: err.response?.data
    })
    error.value = err.response?.data?.message || '获取考试信息失败'
  } finally {
    loading.value = false
  }
}

// 开始考试
const startExam = () => {
  if (!exam.value) return
  router.push({ 
    name: 'Test', 
    params: { test_id: test_id.value },
    query: {
      testName: exam.value.test_name,
      deadline: exam.value.deadline
    }
  })
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 组件挂载时获取考试数据
onMounted(() => {
  if (!test_id.value) {
    error.value = '缺少考试ID参数'
    return
  }
  fetchExamData()
})
</script>

<style scoped>
/* 保持原有样式不变 */
.enter-test-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  margin: 20px 0;
}

.loading {
  background-color: #e6f7ff;
  color: #1890ff;
}

.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.exam-info {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.start-button, .back-button {
  display: inline-block;
  margin: 20px 10px 0;
  padding: 10px 25px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.start-button {
  background-color: #1890ff;
  color: white;
}

.start-button:hover {
  background-color: #40a9ff;
}

.back-button {
  background-color: #f5f5f5;
  color: #666;
}

.back-button:hover {
  background-color: #e6e6e6;
}
</style>