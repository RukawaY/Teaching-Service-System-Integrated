<template>
  <div class="homework-student-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的作业</h2>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom: 18px">
        <el-form-item label="学生ID">
          <el-input
            v-model="studentId"
            style="width: 120px"
            placeholder="请输入学生ID"
            @keyup.enter="onStudentIdConfirm"
            clearable
          />
          <el-button type="primary" style="margin-left:8px;" @click="onStudentIdConfirm">确定</el-button>
        </el-form-item>
        <el-form-item label="课程">
          <el-select
            v-model="courseId"
            placeholder="请选择课程"
            style="width: 180px"
            :disabled="allCourses.length===0"
            :loading="courseLoading"
          >
            <el-option
              v-for="c in allCourses"
              :key="c.course_id"
              :label="c.course_name"
              :value="c.course_id"
            />
          </el-select>
          <span v-if="!courseLoading && allCourses.length === 0" style="color: #888; margin-left: 10px;">无可选课程</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchHomeworkList" :disabled="!studentId || !courseId">查 询</el-button>
        </el-form-item>
      </el-form>

      <el-empty v-if="!loading && homeworkList.length===0" description="暂无作业" />

      <el-table
        v-else
        :data="safeHomeworkList"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="title" label="作业标题" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="weight" label="作业占比(%)" width="110">
          <template #default="{ row }">
            <span v-if="row && row.weight !== undefined && row.weight !== null">{{ row.weight }}</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间">
          <template #default="{ row }">
            <span v-if="row && row.deadline">{{ formatDeadline(row.deadline) }}</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="完成状态">
          <template #default="{ row }">
            <el-tag :type="row && row.isSubmitted ? 'success' : 'info'">
              {{ row && row.isSubmitted ? '已提交' : '未提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 成绩栏 -->
        <el-table-column prop="score" label="成绩">
          <template #default="{ row }">
            <span v-if="row && row.score !== undefined && row.score !== null">{{ row.score }}</span>
            <span v-else-if="row && row.isSubmitted">未评分</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <!-- 评语栏 -->
        <el-table-column prop="comment" label="评语">
          <template #default="{ row }">
            <span v-if="row && row.isSubmitted && row.comment">{{ row.comment }}</span>
            <span v-else-if="row && row.isSubmitted">--</span>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              size="small"
              :disabled="!row || row.isSubmitted || isAfterDeadline(row.deadline)"
              @click="openSubmitDialog(row)"
            >提交作业</el-button>
            <el-button size="small" :disabled="!row" @click="openDetailDialog(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交作业弹窗 -->
    <el-dialog v-model="submitDialogVisible" title="提交作业" width="450px">
      <el-form :model="submitForm">
        <el-form-item label="作业标题">
          <el-input v-model="submitForm.title" disabled />
        </el-form-item>
        <el-form-item label="上传文件" prop="file">
          <input type="file" @change="handleFileChange" />
        </el-form-item>
      </el-form>
      <el-alert
        v-if="isAfterDeadline(submitForm.deadline)"
        title="已超过截止时间，不能提交作业"
        type="warning"
        show-icon
        style="margin-bottom: 16px;"
      />
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitHomework"
          :disabled="isAfterDeadline(submitForm.deadline)"
        >提交</el-button>
      </template>
    </el-dialog>

    <!-- 作业详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="作业详情" width="400px">
      <div v-if="selectedHomework">
        <p><strong>作业标题：</strong>{{ selectedHomework.title }}</p>
        <p><strong>描述：</strong>{{ selectedHomework.description }}</p>
        <p><strong>作业占比：</strong>{{ selectedHomework.weight !== undefined && selectedHomework.weight !== null ? selectedHomework.weight : '--' }}%</p>
        <p><strong>截止时间：</strong>{{ formatDeadline(selectedHomework.deadline) }}</p>
        <p><strong>完成状态：</strong>
          <el-tag :type="selectedHomework.isSubmitted ? 'success' : 'info'">
            {{ selectedHomework.isSubmitted ? '已提交' : '未提交' }}
          </el-tag>
        </p>
        <p><strong>成绩：</strong>
          <span v-if="selectedHomework.score !== undefined && selectedHomework.score !== null">
            {{ selectedHomework.score }}
          </span>
          <span v-else-if="selectedHomework.isSubmitted">未评分</span>
          <span v-else>--</span>
        </p>
        <p><strong>评语：</strong>
          <span v-if="selectedHomework.comment">{{ selectedHomework.comment }}</span>
          <span v-else-if="selectedHomework.isSubmitted">--</span>
          <span v-else>--</span>
        </p>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 定义 Homework 接口，新增 weight 字段
export interface Homework {
  homework_id: number;
  course_id: number;
  title: string;
  description: string;
  deadline: string | number;
  isSubmitted: boolean;
  selectedFile?: File | null;
  score?: number | null;
  comment?: string | null;
  weight?: number | null; // 新增作业占比
}

import studentAPI from '../../../api/resource_share/student'

const studentId = ref('')
const courseId = ref('')
const allCourses = ref<any[]>([])
const homeworkList = ref<Homework[]>([])
const loading = ref(false)
const courseLoading = ref(false)

// 防止表格数据为null/undefined/null项
const safeHomeworkList = computed(() =>
  Array.isArray(homeworkList.value) ? homeworkList.value.filter(hw => hw && typeof hw === 'object') : []
);

// 时间戳/字符串格式化
function formatDeadline(deadline: string | number) {
  if (!deadline) return '--'
  // 支持后端给字符串/时间戳
  const d = typeof deadline === 'number'
    ? new Date(deadline)
    : new Date((deadline as string).replace(/-/g, '/'))
  if (isNaN(d.getTime())) return deadline
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

// 判断是否超过截止时间
function isAfterDeadline(deadline: string | number) {
  if (!deadline) return false
  let d
  if (typeof deadline === 'number') d = new Date(deadline)
  else d = new Date((deadline as string).replace(/-/g, '/'))
  return Date.now() > d.getTime()
}

// “确定”按钮或回车触发
const onStudentIdConfirm = async () => {
  courseId.value = ''
  allCourses.value = []
  homeworkList.value = []
  if (studentId.value.trim()) {
    if (isNaN(Number(studentId.value))) {
      ElMessage.warning('学生ID只能为数字')
      return
    }
    await fetchCourses()
  }
}

// 获取学生课程列表
const fetchCourses = async () => {
  courseLoading.value = true
  try {
    const res = await studentAPI.getCourseList({ student_id: Number(studentId.value) })
    if (res.code === '200') {
      allCourses.value = Array.isArray(res.data.course_list) ? res.data.course_list : []
      if (allCourses.value.length > 0) {
        courseId.value = allCourses.value[0].course_id // 默认选中第一个
        await fetchHomeworkList()
      } else {
        courseId.value = ''
        homeworkList.value = []
        ElMessage.info('该学生没有可选课程，请联系管理员或检查选课信息。')
      }
    } else {
      allCourses.value = []
      courseId.value = ''
      homeworkList.value = []
      ElMessage.error(res.message || '获取课程失败')
    }
  } catch (e) {
    allCourses.value = []
    courseId.value = ''
    homeworkList.value = []
    ElMessage.error('获取课程失败，请检查网络或联系管理员。')
  } finally {
    courseLoading.value = false
  }
}

const fetchHomeworkList = async () => {
  if (!studentId.value) {
    ElMessage.warning('请填写学生ID')
    return
  }
  if (!courseId.value) {
    ElMessage.warning('请选择课程')
    return
  }
  loading.value = true
  try {
    // 先获取作业列表
    const res = await studentAPI.getHomeworkList(Number(courseId.value), Number(studentId.value))
    // 强制 homeworkList.value 为数组且无null
    const originList = (res.code === '200' && Array.isArray(res.data?.homework_list))
      ? res.data.homework_list.filter(hw => hw && typeof hw === 'object')
      : []
    homeworkList.value = originList.map(hw => ({
      ...hw,
      score: null,
      comment: null,
      isSubmitted: false,
      weight: hw.weight ?? null, // 显式支持 weight 字段
    }))
    // 并发补充每条作业的成绩、评语、提交状态
    await Promise.all(
      homeworkList.value.map(async hw => {
        try {
          // 调用 getSubmissionDetail 获取详情
          const detailRes = await studentAPI.getSubmissionDetail(hw.homework_id, Number(studentId.value))
          if (detailRes.data.code === "200" && detailRes.data.data) {
            // 有提交记录，补充成绩和评语
            hw.score = detailRes.data.data.score
            hw.comment = detailRes.data.data.comment
            hw.isSubmitted = true
          } else {
            // 没有提交
            hw.score = null
            hw.comment = null
            hw.isSubmitted = false
          }
        } catch (e) {
          // 网络或接口异常也视为未提交
          hw.score = null
          hw.comment = null
          hw.isSubmitted = false
        }
      })
    )
  } catch {
    homeworkList.value = []
    ElMessage.error('查询作业失败')
  } finally {
    loading.value = false
  }
}

// 提交作业弹窗
const submitDialogVisible = ref(false)
const submitForm = reactive({
  homework_id: 0,
  student_id: 0,
  title: '',
  deadline: '',
  file: null as File | null
})
const submitting = ref(false)

const openSubmitDialog = (row: Homework) => {
  if (!row) return
  submitForm.homework_id = row.homework_id
  submitForm.student_id = Number(studentId.value)
  submitForm.title = row.title
  submitForm.deadline = row.deadline
  submitForm.file = null
  submitDialogVisible.value = true
}
const handleFileChange = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) submitForm.file = files[0]
}
const submitHomework = async () => {
  // 查找当前作业
  const current = homeworkList.value.find(hw => hw.homework_id === submitForm.homework_id)
  if (current && isAfterDeadline(current.deadline)) {
    ElMessage.warning('已超过截止时间，不能提交作业')
    return
  }
  if (!submitForm.file) {
    ElMessage.warning('请选择文件')
    return
  }
  submitting.value = true
  try {
    await studentAPI.submitHomework({
      homework_id: submitForm.homework_id,
      student_id: submitForm.student_id,
      file: submitForm.file
    })
    ElMessage.success('提交成功')
    await fetchHomeworkList()
    submitDialogVisible.value = false
  } catch (err) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 详情弹窗
const detailDialogVisible = ref(false)
const selectedHomework = ref<Homework | null>(null)
const openDetailDialog = (row: Homework) => {
  if (!row) return
  selectedHomework.value = row
  detailDialogVisible.value = true
}
</script>
