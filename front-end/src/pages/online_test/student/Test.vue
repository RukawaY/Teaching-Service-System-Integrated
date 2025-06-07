<template>
  <div class="exam-container">
    <!-- 左侧固定导航栏 -->
    <div class="question-sidebar">
      <div class="sidebar-header">题目列表</div>
      <div class="question-numbers">
        <div 
          v-for="q in questions" 
          :key="q.id"
          class="question-number"
          :class="{ 
            'active': currentQuestion === q.id,
            'answered': q.selectedAnswer 
          }"
          @click="scrollToQuestion(q.id)"
        >
          {{ q.id }}
        </div>
      </div>
    </div>

    <!-- 右侧可滚动内容区 -->
    <div class="question-main" ref="mainContent">
      <template v-if="loading">
        <div class="loading-spinner">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载题目中...
        </div>
      </template>
      <template v-else-if="error">
        <el-alert :title="error" type="error" show-icon />
      </template>
      <template v-else>
        <div 
          v-for="q in questions"
          :key="q.id"
          :id="'question-' + q.id"
          class="question-item"
          :ref="el => { questionRefs[q.id] = el }"
        >
          <h3>{{ q.id }}. {{ q.text }}</h3>
          <ul class="options">
            <li
              v-for="(text, optId) in q.options"
              :key="optId"
              :class="{ 'selected': q.selectedAnswer === optId }"
              @click="selectAnswer(q.id, optId)"
            >
              {{ optId }}. {{ text }}
            </li>
          </ul>
        </div>
        
        <!-- 添加提交按钮 -->
        <div class="submit-container">
          <el-button 
            type="primary" 
            size="large" 
            :loading="submitting"
            @click="submitAllAnswers"
          >
            提交所有答案
          </el-button>
          <div v-if="submitResult" class="submit-result">
            <el-alert :title="submitResult" :type="submitSuccess ? 'success' : 'error'" show-icon />
          </div>
        </div>
      </template>
    </div>

    <!-- 左下角显示剩余时间 -->
    <div class="countdown-timer">
      剩余时间: {{ formattedTime }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
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

const testId = route.params.test_id
const studentId = ref(Number(userStore.loginUser.user_id) || 1) // 从用户状态获取学生ID
const isTeacher = false // 根据实际需求设置

// 考试数据相关
const questions = ref([])
const loading = ref(false)
const error = ref(null)

// 视图控制相关
const currentQuestion = ref(1)
const mainContent = ref(null)
const questionRefs = ref({})
let observer = null

// 提交相关状态
const submitting = ref(false)
const submitResult = ref(null)
const submitSuccess = ref(false)

// 定时提交和考试截止时间
const autoSaveInterval = ref(null)
const AUTO_SAVE_INTERVAL = 300000 // 5分钟（毫秒）
const deadline = ref(null) // 考试截止时间（时间戳）
let countdownInterval = null
const forceUpdate = ref(0) // 用于强制更新 formattedTime

// 剩余时间格式化
const formattedTime = computed(() => {
  forceUpdate.value
  if (!deadline.value) return '00:00:00'
  const remainingTime = Math.max(0, deadline.value - Date.now())
  const hours = Math.floor(remainingTime / 3600000)
  const minutes = Math.floor((remainingTime % 3600000) / 60000)
  const seconds = Math.floor((remainingTime % 60000) / 1000)
  const formatted = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  //console.log('Formatted time:', formatted) // 调试输出
  return formatted
})

// 从接口获取考试题目和考试信息
const fetchTestInfoAndQuestions = async () => {
  loading.value = true
  error.value = null
  try {
    // 获取考试信息
    const testInfoResponse = await api.get('/test/testPublish/getTestByTestId', {
      params: {
        testId: testId
      }
    })
    console.log('API响应数据:', testInfoResponse.data)
    
    // 确保 deadline 是有效的日期字符串
    if (testInfoResponse.data.length > 0 && testInfoResponse.data[0].deadline) {
      deadline.value = new Date(testInfoResponse.data[0].deadline).getTime()
      console.log('deadline:', deadline.value)
    } else {
      console.error('无效的 deadline 数据')
      deadline.value = Date.now() + 3600000 // 示例：设置为1小时后
    }

    // 获取考试题目
    const questionsResponse = await api.get('/test/testPublish/getQuestionsByTestId', {
      params: {
        testId: testId,
        isTeacher: isTeacher
      }
    })
    
    // 转换接口数据为前端需要的格式
    questions.value = questionsResponse.data.map((item, index) => {
      return {
        id: index + 1,
        originalId: item.question_id, // 保存原始题目ID用于提交答案
        text: item.content,
        questionType: item.question_type, // 保存题目类型
        options: (() => {
          if (item.question_type === 'TF') {
            // 判断题，只有两个选项：对和错
            return {
              T: '对',
              F: '错'
            };
          } else if (item.question_type === 'MC') {
            // 选择题，解析 JSON 字符串为四个选项
            try {
              const optionsArray = item.options ? JSON.parse(item.options) : [];
              return {
                A: optionsArray[0] || '',
                B: optionsArray[1] || '',
                C: optionsArray[2] || '',
                D: optionsArray[3] || ''
              };
            } catch (e) {
              console.error('解析 options 失败:', e);
              return {
                A: '',
                B: '',
                C: '',
                D: ''
              };
            }
          } else {
            // 默认情况，返回空选项
            return {
              A: '',
              B: '',
              C: '',
              D: ''
            };
          }
        })(),
        selectedAnswer: null // 初始未选择答案
      };
    });
  } catch (err) {
    console.error('获取题目或考试信息失败:', err)
    error.value = '获取考试题目或信息失败，请稍后重试'
    ElMessage.error('加载题目或考试信息失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

// 初始化 Intersection Observer
const initObserver = () => {
  const headerHeight = 80 // 顶部导航栏高度
  const viewportHeight = window.innerHeight
  
  const options = {
    root: null,
    rootMargin: `-${headerHeight}px 0px -35% 0px`,
    threshold: 1
  }

  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && entry.intersectionRatio >= 0.5) {
        const questionId = parseInt(entry.target.id.replace('question-', ''))
        const elementTop = entry.boundingClientRect.top
        const triggerZone = headerHeight + (viewportHeight / 4)
        
        if (elementTop < triggerZone) {
          currentQuestion.value = questionId
        }
      }
    })
  }, options)

  Object.values(questionRefs.value).forEach(el => el && observer.observe(el))
}

// 跳转到指定题目
const scrollToQuestion = (questionId) => {
  currentQuestion.value = questionId
  const element = questionRefs.value[questionId]
  if (element) {
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
  }
}

// 选择答案
const selectAnswer = (questionId, optionId) => {
  const question = questions.value.find(q => q.id === questionId)
  if (question) {
    question.selectedAnswer = optionId
  }
}

// 自动保存或提交
const autoSaveOrSubmit = async () => {
  if (deadline.value && Date.now() >= deadline.value) {
    // 考试时间截止，自动提交
    await submitAllAnswers()
    clearInterval(autoSaveInterval.value)
      console.log('准备跳转到首页...') // 调试日志
      router.push('/home') // 确保执行
      console.log('跳转指令已发送') // 调试日志
  } else {
    // 定时保存（示例：可以保存到本地或直接提交）
    console.log('定时保存答案...')
    await submitAllAnswers()
    // 这里可以调用保存API（如果需要）
  }
}

// 提交所有答案
const submitAllAnswers = async () => {
  submitting.value = true
  submitResult.value = null
  
  try {
    // 准备提交的数据 - 确保格式与后端要求一致
    const answersToSubmit = questions.value
      .filter(q => q.selectedAnswer)
      .map(q => ({
        testId: Number(testId),
        studentId: studentId.value,
        questionId: q.originalId, // 使用原始题目ID
        studentAnswer: q.selectedAnswer.toString()
      }))
    
    if (answersToSubmit.length === 0) {
      ElMessage.warning('尚未作答')
      return
    }
    
    // 打印调试信息
    console.log('准备提交的数据:', JSON.stringify(answersToSubmit, null, 2))
    
    // 批量提交所有答案
    const response = await api.post('/test/answer/submitBatch', answersToSubmit, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('提交响应:', response.data)
    
    if (response.data && response.data.length > 0) {
      submitSuccess.value = true
      submitResult.value = '答案提交成功！'
      ElMessage.success('答案提交成功！')
    } else {
      submitSuccess.value = false
      submitResult.value = '提交失败: ' + (response.data.message || '未知错误')
      ElMessage.error('提交失败: ' + (response.data.message || '未知错误'))
    }
  } catch (err) {
    console.error('提交答案失败:', err)
    console.error('错误详情:', {
      message: err.message,
      response: err.response?.data,
      config: err.config
    })
    submitSuccess.value = false
    submitResult.value = '提交失败: ' + (err.response?.data?.message || err.message)
    ElMessage.error('提交失败: ' + (err.response?.data?.message || err.message))
  } finally {
    submitting.value = false
  }
}

// 启动倒计时（更新剩余时间）
const startCountdown = () => {
  if (!deadline.value) {
    console.error('未设置 deadline，无法启动倒计时')
    return
  }
  countdownInterval = setInterval(() => {
    forceUpdate.value += 1 // 强制更新 formattedTime
    //console.log('forceUpdate:', forceUpdate.value) // 调试输出
    if (Date.now() >= deadline.value) {
      clearInterval(countdownInterval)
      autoSaveOrSubmit()
    }
    //console.log('倒计时:', formattedTime)
  }, 1000)
}

// 组件挂载时获取数据
onMounted(() => {
  if (!testId) {
    error.value = '缺少考试ID参数'
    return
  }
  fetchTestInfoAndQuestions().then(() => {
    if (questions.value.length > 0) {
      initObserver()
      scrollToQuestion(1)
      
      // 初始化定时提交
      autoSaveInterval.value = setInterval(autoSaveOrSubmit, AUTO_SAVE_INTERVAL)
      
      // 启动倒计时
      startCountdown()
    }
  })
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  if (autoSaveInterval.value) clearInterval(autoSaveInterval.value)
  if (countdownInterval) clearInterval(countdownInterval)
})
</script>

<style scoped>
/* 主容器布局 */
.exam-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.question-sidebar {
  width: 240px;
  background-color: #f8f9fa;
  border-right: 1px solid #dee2e6;
  padding: 80px 0;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
}

.question-main {
  flex: 1;
  padding: 40px 40px;
  margin-left: 240px;
  height: 100vh;
  overflow-y: auto;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-color-primary);
  font-size: 18px;
}

/* 倒计时样式 */
.countdown-timer {
  position: fixed;
  bottom: 20px;
  left: 20px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 15px;
  border-radius: 4px;
  font-size: 16px;
  z-index: 1000;
}

/* 其他样式保持不变 */
.sidebar-header {
  font-weight: bold;
  padding: 0 15px 15px;
  margin-bottom: 10px;
  border-bottom: 1px solid #ddd;
  position: sticky;
  top: 0;
  background-color: #f8f9fa;
  z-index: 1;
}

.question-numbers {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.question-number {
  width: 100%;
  padding: 8px 0;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  font-size: 14px;
}

.question-number:hover {
  background-color: #e9ecef;
}

.question-number.active {
  background-color: #1890ff;
  color: white;
  font-weight: bold;
}

.question-item {
  scroll-margin-top: 80px;
  padding-top: 20px;
  margin-top: -20px;
}

.options {
  list-style-type: none;
  padding-left: 0;
}

.options li {
  padding: 10px 15px;
  margin: 8px 0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eee;
}

.options li:hover {
  background-color: #f5f5f5;
}

.options li.selected {
  background-color: #e6f7ff;
  border-color: #1890ff;
  border-left: 3px solid #1890ff;
}

.question-number.answered {
  background-color: #52c41a;
  color: white;
}

/* 提交按钮样式 */
.submit-container {
  margin-top: 40px;
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  position: sticky;
  bottom: 0;
}

.submit-result {
  margin-top: 15px;
}

html {
  scroll-behavior: smooth;
}
</style>