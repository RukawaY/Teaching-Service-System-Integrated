<template>
  <div class="homework-teacher-container">
    <el-card class="homework-card">
      <template #header>
        <div class="card-header">
          <h2>作业批改</h2>
          <el-button type="primary" @click="exportAllGrades" :loading="exporting">
            导出所有成绩
          </el-button>
        </div>
      </template>

      <!-- 作业列表 -->
      <div v-loading="loading">
        <el-collapse v-model="activeNames">
          <el-collapse-item 
            v-for="course in homeworkList" 
            :key="course.course_id"
            :title="course.course_name"
            :name="course.course_id"
          >
            <el-table :data="course.homework_list" style="width: 100%">
              <el-table-column prop="title" label="作业标题" min-width="200" />
              <el-table-column label="成绩比例" width="200">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.weight"
                    :min="0"
                    :max="1"
                    :step="0.1"
                    :precision="2"
                    @change="(value) => handleWeightChange(row, value)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="提交情况" width="200">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="(row.submitted_count / row.total_count) * 100"
                    :format="(percentage) => `${row.submitted_count}/${row.total_count}`"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="deadline" label="截止日期" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <div class="operation-buttons">
                    <el-button 
                      type="primary" 
                      @click="openGradingDialog(row)"
                      :disabled="row.submitted_count === 0"
                    >
                      批改作业
                    </el-button>
                    <el-button 
                      type="success" 
                      @click="exportGrades(row)"
                      :disabled="row.submitted_count === 0"
                    >
                      导出成绩
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>
        </el-collapse>

        <el-empty v-if="!loading && homeworkList.length === 0" description="暂无待批改作业" />
      </div>
    </el-card>

    <!-- 批改作业弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="作业批改"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="currentSubmission" class="grading-dialog">
        <!-- 添加搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchStudent"
            placeholder="输入学生ID或姓名搜索"
            class="search-input"
            clearable
            @keyup.enter="searchAndJumpToStudent"
          >
            <template #append>
              <el-button @click="searchAndJumpToStudent">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- 学生信息 -->
        <el-descriptions title="学生信息" :column="2" border>
          <el-descriptions-item label="学生ID">{{ currentSubmission.student_id }}</el-descriptions-item>
          <el-descriptions-item label="学生姓名">{{ currentSubmission.student_name }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentSubmission.submit_time }}</el-descriptions-item>
          <el-descriptions-item label="成绩比例">{{ (currentHomework?.weight || 0) * 100 }}%</el-descriptions-item>
        </el-descriptions>

        <!-- 作业内容 -->
        <el-descriptions title="作业内容" :column="1" border class="mt-4">
          <el-descriptions-item label="文件名">{{ currentSubmission.file_name }}</el-descriptions-item>
        </el-descriptions>
        <div class="text-right mt-2">
          <el-button type="primary" @click="downloadHomework(currentSubmission)">
            下载作业
          </el-button>
        </div>

        <!-- 评分表单 -->
        <el-form 
          :model="gradingForm" 
          :rules="gradingRules" 
          ref="gradingFormRef" 
          label-width="100px"
          class="mt-4"
        >
          <el-form-item label="分数" prop="score">
            <el-input-number 
              v-model="gradingForm.score" 
              :min="0" 
              :max="100"
              :precision="1"
              :step="0.5"
            />
          </el-form-item>
          <el-form-item label="评语" prop="comment">
            <el-input
              v-model="gradingForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入评语"
            />
          </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="dialog-footer">
          <el-button @click="prevSubmission" :disabled="!hasPrevSubmission">
            上一份
          </el-button>
          <el-button @click="nextSubmission" :disabled="!hasNextSubmission">
            下一份
          </el-button>
          <el-button type="primary" @click="submitGrading" :loading="submitting">
            提交评分
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

// 状态管理
const loading = ref(false)
const submitting = ref(false)
const exporting = ref(false)
const activeNames = ref(['1'])
const dialogVisible = ref(false)
const currentSubmission = ref(null)
const currentSubmissionIndex = ref(0)
const currentHomework = ref(null)
const gradingFormRef = ref(null)
const homeworkList = ref([])

// 评分表单
const gradingForm = reactive({
  score: 0,
  comment: ''
})

// 评分表单验证规则
const gradingRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur' }
  ],
  comment: [
    { required: true, message: '请输入评语', trigger: 'blur' }
  ]
}

// 计算属性
const hasPrevSubmission = computed(() => {
  if (!currentHomework.value || !currentSubmissionIndex.value) return false
  return currentSubmissionIndex.value > 0
})

const hasNextSubmission = computed(() => {
  if (!currentHomework.value || !currentSubmissionIndex.value) return false
  return currentSubmissionIndex.value < currentHomework.value.submissions.length - 1
})

// 添加示例作业数据
const exampleHomework = {
  course_id: 'CS001',
  course_name: '数据结构与算法',
  homework_list: [
    {
      homework_id: 'HW001',
      title: '链表实现学生成绩管理系统',
      weight: 0.3,
      submitted_count: 45,
      total_count: 50,
      deadline: '2024-03-20 23:59:59',
      submissions: [
        {
          student_id: '2024001',
          student_name: '张三',
          submit_time: '2024-03-19 15:30:00',
          file_name: 'student_management.cpp',
          score: 95,
          comment: '代码结构清晰，实现了所有要求的功能。链表操作正确，排序算法实现合理。建议：\n1. 可以添加输入验证\n2. 考虑使用智能指针管理内存\n3. 可以添加文件持久化功能'
        }
      ],
      requirements: `实现一个简单的学生成绩管理系统，要求：
1. 使用链表存储学生信息
2. 实现添加、删除、查询、修改学生信息的功能
3. 实现按成绩排序的功能
4. 使用C++语言实现

评分标准：
1. 基本功能实现（40分）
   - 链表实现正确（15分）
   - 增删改查功能完整（15分）
   - 排序功能正确（10分）
2. 代码质量（30分）
   - 代码结构清晰（10分）
   - 命名规范（5分）
   - 注释完整（5分）
   - 错误处理（10分）
3. 进阶特性（30分）
   - 输入验证（10分）
   - 内存管理（10分）
   - 用户界面友好（10分）`
    }
  ]
}

// 添加搜索相关的状态
const searchStudent = ref('')

// 方法
const getHomeworkList = async () => {
  loading.value = true
  try {
    // 直接使用示例作业数据
    homeworkList.value = [exampleHomework]
  } catch (error) {
    console.error('获取作业列表错误:', error)
    ElMessage.error(`获取作业列表失败: ${error.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

const handleWeightChange = async (homework, value) => {
  try {
    const response = await teacherAPI.setHomeworkWeight({
      homework_id: homework.homework_id,
      weight: value
    })
    if (response.code === '200') {
      ElMessage.success('成绩比例设置成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '设置成绩比例失败')
    homework.weight = homework._originalWeight
  }
}

const openGradingDialog = async (homework) => {
  currentHomework.value = homework
  // 直接使用示例作业中的提交数据
  if (homework.submissions && homework.submissions.length > 0) {
    currentSubmissionIndex.value = 0
    currentSubmission.value = homework.submissions[0]
    dialogVisible.value = true
    gradingForm.score = currentSubmission.value.score || 0
    gradingForm.comment = currentSubmission.value.comment || ''
  } else {
    ElMessage.warning('该作业暂无提交')
  }
}

const downloadHomework = async (submission) => {
  ElMessage.success('开始下载作业文件')
  // 这里可以添加模拟下载的逻辑
}

const prevSubmission = () => {
  if (hasPrevSubmission.value) {
    currentSubmissionIndex.value--
    currentSubmission.value = currentHomework.value.submissions[currentSubmissionIndex.value]
    gradingForm.score = currentSubmission.value.score || 0
    gradingForm.comment = currentSubmission.value.comment || ''
  }
}

const nextSubmission = () => {
  if (hasNextSubmission.value) {
    currentSubmissionIndex.value++
    currentSubmission.value = currentHomework.value.submissions[currentSubmissionIndex.value]
    gradingForm.score = currentSubmission.value.score || 0
    gradingForm.comment = currentSubmission.value.comment || ''
  }
}

const submitGrading = async () => {
  if (!gradingFormRef.value) return

  try {
    await gradingFormRef.value.validate()
    submitting.value = true

    // 模拟提交成功
    setTimeout(() => {
      ElMessage.success('评分提交成功')
      currentSubmission.value.score = gradingForm.score
      currentSubmission.value.comment = gradingForm.comment
      submitting.value = false
    }, 500)
  } catch (error) {
    ElMessage.error(error.message || '评分提交失败')
    submitting.value = false
  }
}

const exportGrades = async (homework) => {
  try {
    const blob = await teacherAPI.exportGrades(homework.homework_id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${homework.title}_成绩.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error.message || '导出成绩失败')
  }
}

const exportAllGrades = async () => {
  exporting.value = true
  try {
    const blob = await teacherAPI.exportAllGrades()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '所有作业成绩.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error.message || '导出所有成绩失败')
  } finally {
    exporting.value = false
  }
}

// 添加搜索和跳转方法
const searchAndJumpToStudent = () => {
  if (!searchStudent.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  const searchText = searchStudent.value.trim().toLowerCase()
  const index = currentHomework.value.submissions.findIndex(submission => 
    submission.student_id.toLowerCase().includes(searchText) ||
    submission.student_name.toLowerCase().includes(searchText)
  )

  if (index !== -1) {
    currentSubmissionIndex.value = index
    currentSubmission.value = currentHomework.value.submissions[index]
    gradingForm.score = currentSubmission.value.score || 0
    gradingForm.comment = currentSubmission.value.comment || ''
    ElMessage.success(`已跳转到学生 ${currentSubmission.value.student_name} 的作业`)
  } else {
    ElMessage.warning('未找到匹配的学生')
  }
}

// 生命周期钩子
onMounted(() => {
  getHomeworkList()
})
</script>

<style scoped>
.homework-teacher-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.homework-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5em;
  color: #303133;
}

.grading-dialog {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

:deep(.el-collapse-item__header) {
  font-size: 1.1em;
  font-weight: bold;
}

:deep(.el-input-number) {
  width: 150px;
}

:deep(.el-progress) {
  margin-right: 10px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}

.text-right {
  text-align: right;
}

.homework-requirements {
  white-space: pre-line;
  font-family: monospace;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 10px 0;
}

.operation-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-box {
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}
</style>