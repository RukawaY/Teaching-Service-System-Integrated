<template>
  <div class="homework-teacher-container">
    <el-card class="homework-card">
      <template #header>
        <div class="card-header">
          <h2>作业管理（教师端）</h2>
          <div>
            <el-button type="primary" @click="openAssignDialog">
              布置作业
            </el-button>
          </div>
        </div>
      </template>
      <div v-loading="loading">
        <el-table :data="courseHomeworkList" style="width: 100%" v-if="courseHomeworkList.length">
          <el-table-column prop="course_name" label="课程" />
          <el-table-column prop="homework_list" label="作业">
            <template #default="{ row }">
              <div v-for="hw in row.homework_list" :key="hw.homework_id" style="margin-bottom: 6px;">
                <span style="margin-right: 8px;">{{ hw.title }}</span>
                <el-tag size="small" :type="getStatusTagType(hw)">
                  {{ getHomeworkStatus(hw) }}
                </el-tag>
                <el-tag
                  v-if="hw.weight !== undefined && hw.weight !== null"
                  size="small"
                  style="margin-left: 8px;"
                  type="success"
                >{{ hw.weight }}%</el-tag>
                <el-button size="small" style="margin-left: 8px;" @click="openMarkDialog(hw, row.course_name)">
                  批改/下载
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div v-else style="text-align: center;color: #aaa;padding: 40px 0;">
          暂无作业
        </div>
      </div>
    </el-card>

    <!-- 布置作业弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="布置新作业" width="500px" :close-on-click-modal="false">
      <el-form
        :model="assignForm"
        :rules="assignRules"
        ref="assignFormRef"
        label-width="100px"
        class="assign-form"
      >
        <el-form-item label="课程" prop="course_id">
          <el-select v-model="assignForm.course_id" placeholder="请选择课程">
            <el-option
              v-for="course in allCourses"
              :key="course.course_id"
              :label="course.course_name"
              :value="course.course_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="assignForm.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="assignForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="assignForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="作业占比(%)" prop="weight">
          <el-input-number
            v-model="assignForm.weight"
            :min="0"
            :max="100"
            :step="1"
            placeholder="占比"
          />
        </el-form-item>
        <el-form-item label="要求" prop="requirements">
          <el-input v-model="assignForm.requirements" placeholder="作业要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="assigning">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批改/下载弹窗 -->
    <el-dialog
      v-model="markDialogVisible"
      title="批改作业/下载"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentHomework">
        <div style="margin-bottom: 12px;">
          <strong>作业标题：</strong>{{ currentHomework.title }}
          <el-tag
            v-if="currentHomework.weight !== undefined && currentHomework.weight !== null"
            size="small"
            style="margin-left: 8px;"
            type="success"
          >{{ currentHomework.weight }}%</el-tag>
        </div>
        <div style="margin-bottom: 12px;">
          <strong>所属课程：</strong>{{ currentCourseName }}
        </div>
        <div style="margin-bottom: 12px;">
          <strong>说明：</strong>{{ currentHomework.description }}
        </div>
        <el-table :data="submissionList" size="small" style="margin: 18px 0;">
          <el-table-column prop="student_id" label="学生id" />
          <el-table-column prop="submit_time" label="提交时间" />
          <el-table-column prop="file_name" label="文件名" />
          <el-table-column prop="score" label="分数" width="80" />
          <el-table-column prop="comment" label="评语" width="120" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="mini" type="primary" @click="download(row)">下载</el-button>
              <el-button size="mini" @click="openGradeDialog(row)">评分</el-button>
              <el-button size="mini" @click="viewSubmission(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="submissionList.length > pageSize"
          layout="prev, pager, next"
          :total="submissionList.length"
          :page-size="pageSize"
          v-model:current-page="page"
        ></el-pagination>
        <!-- 作业详情弹窗 -->
        <el-dialog v-model="submissionDetailVisible" title="作业详情" width="400px">
          <div v-if="selectedSubmission">
            <p>学生id：{{ selectedSubmission.student_id }}</p>
            <p>提交时间：{{ selectedSubmission.submit_time }}</p>
            <p>文件名：{{ selectedSubmission.file_name }}</p>
            <p>分数：{{ selectedSubmission.score }}</p>
            <p>评语：{{ selectedSubmission.comment }}</p>
            <a :href="getDownloadUrl(selectedSubmission)" target="_blank">下载文件</a>
          </div>
        </el-dialog>
        <!-- 评分弹窗 -->
        <el-dialog v-model="gradeDialogVisible" :append-to-body="true" title="评分" width="350px">
          <el-form :model="gradeForm">
            <el-form-item label="分数">
              <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="评语">
              <el-input v-model="gradeForm.comment" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="gradeDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGrade">提交评分</el-button>
          </template>
        </el-dialog>
      </div>
      <template #footer>
        <el-button @click="markDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { teacherAPI } from '../../../api/resource_share/teacher'

const allCourses = ref<any[]>([])
const courseHomeworkList = ref<any[]>([])
const assignDialogVisible = ref(false)
const assignFormRef = ref()
const assigning = ref(false)
const assignForm = reactive({
  course_id: '',
  title: '',
  description: '',
  deadline: '',
  weight: 10, // 作业占比
  requirements: ''
})
const assignRules = {
  course_id: [{ required: true, message: '请选择课程', trigger: 'change' }],
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入作业描述', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  weight: [{ required: true, type: 'number', min: 0, max: 100, message: '占比0~100', trigger: 'blur' }],
  requirements: [{ required: true, message: '请输入作业要求', trigger: 'blur' }]
}

const loading = ref(false)
const page = ref(1)
const pageSize = 10

const fetchCourses = async () => {
  try {
    const res = await teacherAPI.getTeacherCourses('张老师')
    if (res.code === '200') {
      allCourses.value = res.data.map((item: any) => ({
        course_id: item.course_id ?? item,
        course_name: item.course_name ?? item
      }))
    }
  } catch (e) { allCourses.value = [] }
}
const fetchCourseHomeworkList = async () => {
  loading.value = true
  try {
    const res = await teacherAPI.getHomeworkList()
    if (res.code === '200') {
      // 确保每个作业有 weight 字段
      courseHomeworkList.value = res.data.map((course: any) => ({
        ...course,
        homework_list: Array.isArray(course.homework_list)
          ? course.homework_list.map((hw: any) => ({
              ...hw,
              weight: hw.weight ?? null
            }))
          : []
      }))
    }
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  fetchCourses()
  fetchCourseHomeworkList()
})

const openAssignDialog = () => {
  Object.assign(assignForm, {
    course_id: '',
    title: '',
    description: '',
    deadline: '',
    weight: 10,
    requirements: ''
  })
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!assignFormRef.value) return
  try {
    await assignFormRef.value.validate()
    assigning.value = true
    // POST时确保 weight 字段为数字
    await teacherAPI.assignHomework({ ...assignForm, weight: Number(assignForm.weight) })
    ElMessage.success('作业布置成功')
    assignDialogVisible.value = false
    fetchCourseHomeworkList()
  } catch (e: any) {
    ElMessage.error(e.message || '作业布置失败')
  } finally {
    assigning.value = false
  }
}

const getHomeworkStatus = (hw: any): string => {
  const now = new Date()
  const deadline = hw.deadline ? new Date(hw.deadline) : null
  if (!deadline) return '未开始'
  if (now < deadline) return '进行中'
  return '已截止'
}
const getStatusTagType = (hw: any): string => {
  const status = getHomeworkStatus(hw)
  if (status === '未开始') return 'info'
  if (status === '进行中') return 'primary'
  if (status === '已截止') return 'danger'
  return 'default'
}

// ========== 批改相关 ==========
const markDialogVisible = ref(false)
const currentHomework = ref<any>(null)
const currentCourseName = ref('')
const submissionList = ref<any[]>([])

const openMarkDialog = async (hw: any, courseName: string) => {
  currentHomework.value = hw
  currentCourseName.value = courseName
  markDialogVisible.value = true
  try {
    const res = await teacherAPI.getHomeworkSubmissions(hw.homework_id)
    submissionList.value = res.code === '200' ? res.data : []
  } catch {
    ElMessage.error('获取提交列表失败')
    submissionList.value = []
  }
}

const download = async (row: any) => {
  try {
    const blob = await teacherAPI.downloadHomework(row.submission_id)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.file_name
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch {
    ElMessage.error('下载失败')
  }
}

const gradeDialogVisible = ref(false)
const gradeForm = reactive({
  submission_id: 0,
  score: 0,
  comment: ''
})

const openGradeDialog = (row: any) => {
  gradeForm.submission_id = row.submission_id
  gradeForm.score = row.score || 0
  gradeForm.comment = row.comment || ''
  gradeDialogVisible.value = true
}

const submitGrade = async () => {
  try {
    await teacherAPI.submitGrading({ ...gradeForm })
    ElMessage.success('评分成功')
    gradeDialogVisible.value = false
    if (currentHomework.value) {
      openMarkDialog(currentHomework.value, currentCourseName.value)
    }
  } catch (err) {
    ElMessage.error('评分失败')
  }
}

// 作业详情
const submissionDetailVisible = ref(false)
const selectedSubmission = ref<any>(null)
const viewSubmission = (row: any) => {
  selectedSubmission.value = row
  submissionDetailVisible.value = true
}
const getDownloadUrl = (row: any) => {
  // 假设后端可以通过文件url直链访问，否则可调用下载接口
  return row.file_url || '#'
}
</script>
