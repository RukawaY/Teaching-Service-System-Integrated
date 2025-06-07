<template>
  <div class="search-analysis-container">
    <!-- 选择课程 -->
    <div class="selection-area">
      <el-form label-width="100px" inline>
        <!-- 选择课程 -->
        <el-form-item label="选择课程">
          <el-select
              v-model="selectedCourse"
              placeholder="请选择课程"
              style="width: 300px;"
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

        <!-- 选择考试 -->
        <el-form-item label="选择考试">
          <el-select
              v-model="selectedTest"
              placeholder="请选择考试"
              style="width: 300px;"
              @change="applyFilters"
          >
            <el-option
                v-for="test in tests"
                :key="test.test_id"
                :label="test.test_name"
                :value="test.test_id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="scores.length" class="title">
      <h2>查询结果</h2>
    </div>

    <!-- 成绩展示 -->
    <div v-if="scores.length" class="table">
      <el-table :data="[statisticsSummary]" border>
        <el-table-column prop="average" label="平均分" />
        <el-table-column prop="max" label="最高分" />
        <el-table-column prop="min" label="最低分" />
      </el-table>
    </div>

    <div v-if="scores.length" class="title">
      <h2>学生成绩</h2>
    </div>

    <div v-if="scores.length" class="table1">
      <el-table :data="scores" border>
        <el-table-column prop="rank" label="排名" align="center" />
        <el-table-column prop="student_id" label="学号" align="center" />
        <el-table-column prop="student_name" label="姓名" align="center" />
        <el-table-column prop="total_score" label="总分" align="center" />
        <!-- 每题得分列 -->
        <el-table-column
            v-for="(qid, index) in questionHeaders"
            :key="qid"
            :label="`题目 ${index+1}`"
            align="center"
        >
          <template #default="{ row }">
            <template v-if="row.question_scores">
              <template v-for="q in row.question_scores">
          <span v-if="q.question_id === qid">
            {{ q.score_obtained }}
          </span>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="questionStats.length" class="title">
      <h2>单题得分统计</h2>
    </div>
    <div v-if="questionStats.length" class="table">
      <el-table :data="questionStats" border>
        <el-table-column
            label="题号"
            align="center">
          <template #default="{ $index }">
            {{ $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="correct" label="答对人数" align="center" />
        <el-table-column prop="wrong" label="答错人数" align="center" />
        <el-table-column prop="correctRate" label="正确率" align="center" />
      </el-table>
    </div>

    <!-- 无查询结果-->
    <el-empty v-if="hasQueried && !scores.length && !questionStats.length" description="暂无查询结果"></el-empty>

  </div>

</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { computed } from 'vue'
import { useuserLoginStore } from '../../../store/userLoginStore'

const userStore = useuserLoginStore()

// 创建api实例，与其他online_test模块使用相同的配置
const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// 从用户状态获取教师ID
const teacherId = ref(Number(userStore.loginUser.user_id) || 29)

// 数据绑定
const courses = ref([])
const selectedCourse = ref(null)
const tests = ref([])
const selectedTest = ref(null)
const scores = ref([])
const questionHeaders = ref([])
const questionStats = ref([])
const hasQueried = ref(false)

// 获取教师所有课程
async function fetchCourses() {
  try {
    const res = await api.get(`/test/questions/course/${teacherId.value}`)
    courses.value = res.data || []
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

// 处理课程切换
async function handleCourseChange(courseId) {
  selectedTest.value = null
  scores.value = []
  questionHeaders.value = []
  questionStats.value = []
  hasQueried.value = false
  await fetchTests(courseId)
}

// 获取该课程下的考试
async function fetchTests(courseId) {
  if (!courseId) return
  try {
    const res = await api.get('/test/testPublish/getTestByCourseId', {
      params: { courseId },
    })
    console.log('考试：', res.data)
    tests.value = res.data || []
  } catch (error) {
    console.error('获取考试失败:', error)
  }
}

function calculateQuestionStats(scoreList) {
  if (!scoreList.length) return []

  const totalStudents = scoreList.length
  const statsMap = {}

  scoreList.forEach(student => {
    student.question_scores.forEach(q => {
      const qid = q.question_id
      if (!statsMap[qid]) {
        statsMap[qid] = { question_id: qid, correct: 0, wrong: 0 }
      }
      if (q.score_obtained === q.score) {
        statsMap[qid].correct += 1
      } else {
        statsMap[qid].wrong += 1
      }
    })
  })

  return Object.values(statsMap).map(stat => ({
    ...stat,
    correctRate: ((stat.correct / totalStudents) * 100).toFixed(2) + '%'
  }))
}

// 查询成绩
async function applyFilters() {
  if (!selectedCourse.value || !selectedTest.value) return

  try {
    // 第一步：获取总成绩列表
    const res = await api.get('/test/testPublish/getScoresByTestId', {
      params: {
        courseId: selectedCourse.value,
        testId: selectedTest.value,
      }
    })

    const rawScores = res.data || []

    // 第二步：逐个获取学生答题记录
    const withAnswers = await Promise.all(
        rawScores.map(async (student) => {
          try {
            const ansRes = await api.get('/test/testPublish/getQuestionsByTestIdIfAnswered', {
              params: {
                testId: selectedTest.value,
                studentId: student.student_id
              }
            })

            return {
              student_id: student.student_id,
              student_name: student.student_name,
              total_score: Number(student.total_score),
              question_scores: ansRes.data || []
            }
          } catch (err) {
            console.error(`获取学生 ${student.student_id} 答题记录失败`, err)
            hasQueried.value = true
            return {
              student_id: student.student_id,
              student_name: student.student_name,
              total_score: Number(student.total_score),
              question_scores: []
            }
          }
        })
    )

    scores.value = withAnswers

    // 第三步：生成所有题目 ID，用于表格列
    if (withAnswers.length > 0) {
      questionHeaders.value = withAnswers[0].question_scores.map(q => q.question_id)
    }

    questionStats.value = calculateQuestionStats(withAnswers)

    console.log('题目统计:', questionStats.value)

    console.log('所有题目 ID:', questionHeaders.value)
    computeRankings()
    hasQueried.value = true

  } catch (error) {
    console.error('查询成绩失败:', error)
  }
}

const statisticsSummary = computed(() => ({
  average: averageScore.value,
  max: maxScore.value,
  min: minScore.value
}))

// 平均分
const averageScore = computed(() => {
  if (!scores.value.length) return 0
  const total = scores.value.reduce((sum, s) => {
    const num = Number(s.total_score)
    return sum + (isNaN(num) ? 0 : num)
  }, 0)
  return (total / scores.value.length).toFixed(2)
})

// 最高分
const maxScore = computed(() => {
  const scoreNums = scores.value.map(s => Number(s.total_score)).filter(n => !isNaN(n))
  return scoreNums.length ? Math.max(...scoreNums) : 0
})

// 最低分
const minScore = computed(() => {
  const scoreNums = scores.value.map(s => Number(s.total_score)).filter(n => !isNaN(n))
  return scoreNums.length ? Math.min(...scoreNums) : 0
})

// 排名计算，基于 total_score
function computeRankings() {
  const sorted = [...scores.value].sort((a, b) => Number(b.total_score) - Number(a.total_score))
  sorted.forEach((student, index) => {
    student.rank = index + 1
  })

  scores.value.forEach(s => {
    const found = sorted.find(item => item.student_id === s.student_id)
    if (found) {
      s.rank = found.rank
    }
  })
}

// 初始化加载课程
fetchCourses()
</script>

<style scoped>
.search-analysis-container {
  padding: 20px;
}
.selection-area {
  margin-bottom: 20px;
}
.title{
  display: flex;
  justify-content: center;
}
.table{
  width: 80%;
  margin: 0 auto 20px auto
}
.table1{
  width: 95%;
  margin: 0 auto 20px auto
}
</style>
