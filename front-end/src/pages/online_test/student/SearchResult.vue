<template>
  <div class="student-score-container">
    <!-- 选择课程与考试 -->
    <div class="selection-area">
      <el-form label-width="100px" inline>
        <el-form-item label="选择课程">
          <el-select
              v-model="selectedCourse"
              placeholder="请选择课程"
              style="width: 300px"
              @change="handleCourseChange"
          >
            <el-option
                v-for="course in courses"
                :key="course.course_id"
                :label="course.course_name"
                :value="course.course_id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择考试">
          <el-select
              v-model="selectedTest"
              placeholder="请选择考试"
              style="width: 300px"
              @change="fetchStudentScore"
          >
            <el-option
                v-for="test in tests"
                :key="test.test_id"
                :label="`${test.test_name}`"
                :value="test.test_id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="studentScore" class="title">
      <h2>我的成绩</h2>
    </div>

    <!-- 成绩展示 -->
    <div v-if="studentScore" class="table1">
      <el-table :data="[studentScore]" border>
        <el-table-column prop="rank" label="排名" align="center" />
        <el-table-column prop="total_score" label="总分" align="center" />
      </el-table>
    </div>

    <!-- 查询后但没有结果 -->
    <el-empty description="暂无查询结果" v-if="hasQueried && !studentScore" />


    <!-- 单题得分详情 -->
    <div v-if="detailedScores.length" class="title">
      <h2>单题得分详情</h2>
    </div>

    <div v-if="detailedScores.length" class="table1">
      <el-table :data="detailedScores" border>
        <el-table-column
            label="题号"
            align="center">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题干" align="left" min-width="300" />
        <el-table-column prop="score_obtained" label="得分" align="center" width="80" />
        <el-table-column prop="score" label="满分" align="center" width="80" />
        <el-table-column prop="student_answer" label="学生答案" align="center" />
        <el-table-column prop="correct_answer" label="正确答案" align="center" />
      </el-table>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useuserLoginStore } from '../../../store/userLoginStore'

const userStore = useuserLoginStore()

// 创建api实例，与其他online_test模块使用相同的配置
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

const courses = ref([])
const selectedCourse = ref(null)
const tests = ref([])
const selectedTest = ref(null)
const studentScore = ref(null)
const questionHeaders = ref([])
const studentId = ref(Number(userStore.loginUser.user_id) || 1) // 从用户状态获取学生ID
const detailedScores = ref([])
const hasQueried = ref(false)

function unwrapRef(r) {
  // 如果是ref对象（有value属性），返回r.value，否则原样返回
  return r && typeof r === 'object' && 'value' in r ? r.value : r
}

async function fetchCourses() {
  try {
    const res = await api.get(`/test/testPublish/getStudentCourses`,{
      params:{ studentId: unwrapRef(studentId) }
    })
    courses.value = res.data || []
    console.log(res.data)
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

async function handleCourseChange(courseId) {
  selectedTest.value = null
  studentScore.value = null
  detailedScores.value = []
  hasQueried.value = null
  await fetchTests(courseId)
}

async function fetchTests(courseId) {
  if (!courseId) return
  try {
    const res = await api.get('/test/testPublish/getTestByCourseId', {
      params: { courseId }
    })
    tests.value = res.data || []
  } catch (error) {
    console.error('获取考试失败:', error)
  }
}

function buildDetailedScores(rawScores) {
  return rawScores.map(q => {
    const options = JSON.parse(q.options || "[]")
    const studentAnswerIndex = q.student_answer?.charCodeAt(0) - 65
    const correctAnswerIndex = q.answer?.charCodeAt(0) - 65

    return {
      question_id: q.question_id,
      content: q.content,
      score_obtained: q.score_obtained,
      score: q.score,
      student_answer: `${q.student_answer}：${options[studentAnswerIndex] ?? '无'}`,
      correct_answer: `${q.answer}：${options[correctAnswerIndex] ?? '无'}`
    }
  })
}

async function fetchStudentScore() {
  studentScore.value = null
  detailedScores.value = []
  questionHeaders.value = []
  hasQueried.value = false

  if (!selectedCourse.value || !selectedTest.value || !studentId.value) return

  try {
    // 获取所有学生分数用于计算排名
    const res = await api.get('/test/testPublish/getScoresByTestId', {
      params: {
        courseId: selectedCourse.value,
        testId: selectedTest.value,
      }
    })

    const allScores = res.data || []
    const sorted = allScores
        .map(s => ({
          ...s,
          student_id: Number(s.student_id),
          total_score: Number(s.total_score)
        }))
        .sort((a, b) => b.total_score - a.total_score)

    sorted.forEach((s, index) => {
      s.rank = index + 1
    })

    const student = sorted.find(s => s.student_id === studentId.value)
    if (!student) {
      console.warn('未找到该学生成绩')
      hasQueried.value = true
      return
    }

    // 获取该学生的答题记录
    const ansRes = await api.get('/test/testPublish/getQuestionsByTestIdIfAnswered', {
      params: {
        testId: selectedTest.value,
        studentId: studentId.value
      }
    })

    const rawAnswers = ansRes.data || []
    student.question_scores = rawAnswers

    // 题号列表
    questionHeaders.value = rawAnswers.map(q => q.question_id)

    // 设置单题详细得分
    detailedScores.value = buildDetailedScores(rawAnswers)

    // 设置总成绩
    studentScore.value = student
    hasQueried.value = true

  } catch (error) {
    console.error('查询学生成绩失败:', error)
  }
}


fetchCourses()
</script>

<style scoped>
.student-score-container {
  padding: 20px;
}
.selection-area {
  margin-bottom: 20px;
}
.title {
  text-align: center;
  margin-top: 20px;
}
.table1 {
  width: 90%;
  margin: 0 auto;
}
</style>
