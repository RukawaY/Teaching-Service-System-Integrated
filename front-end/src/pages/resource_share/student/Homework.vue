<template>
  <div class="homework-container">
    <el-card class="homework-card">
      <template #header>
        <div class="card-header">
          <h2>作业管理</h2>
        </div>
      </template>

      <!-- 作业列表 -->
      <div class="homework-list" v-if="homeworkList.length > 0">
        <el-collapse v-model="activeNames">
          <el-collapse-item 
            v-for="course in homeworkList" 
            :key="course.course_id"
            :title="course.course_name"
            :name="course.course_id"
          >
            <el-timeline>
              <el-timeline-item
                v-for="homework in course.homework_list"
                :key="homework.homework_id"
                :type="getHomeworkStatus(homework)"
                :timestamp="homework.deadline"
                placement="top"
              >
                <el-card class="homework-item">
                  <template #header>
                    <div class="homework-header">
                      <h3>{{ homework.title }}</h3>
                      <div class="homework-status">
                        <el-tag v-if="homework.isSubmitted" type="success">已完成</el-tag>
                        <el-tag v-else-if="isOverdue(homework)" type="danger">已截止</el-tag>
                        <el-tag v-else type="warning">进行中</el-tag>
                      </div>
                    </div>
                  </template>
                  <div class="homework-content">
                    <p class="description">{{ homework.description }}</p>
                    <div class="homework-actions" v-if="!isOverdue(homework) && !homework.isSubmitted">
                      <el-upload
                        class="upload-demo"
                        action="#"
                        :auto-upload="false"
                        :on-change="(file) => handleFileChange(file, homework)"
                        :before-upload="beforeUpload"
                        :limit="1"
                      >
                        <template #trigger>
                          <el-button type="primary">选择文件</el-button>
                        </template>
                        <template #tip>
                          <div class="el-upload__tip">
                            只能上传 PDF 或 ZIP 文件
                          </div>
                        </template>
                      </el-upload>
                      <el-button 
                        type="success" 
                        @click="submitHomework(homework)"
                        :loading="submitting === homework.homework_id"
                        :disabled="!homework.selectedFile"
                      >
                        提交作业
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 无数据提示 -->
      <el-empty v-else-if="!loading" description="暂无作业数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getHomeworkList, submitHomework as submitHomeworkAPI } from '../../../api/resource_share/student'

const loading = ref(false)
const submitting = ref(null)
const activeNames = ref(['1']) // 默认展开第一个课程
const homeworkList = ref([])

// 示例数据
const mockHomeworkList = [
  {
    course_id: 1,
    course_name: '软件工程基础',
    homework_list: [
      {
        homework_id: 1,
        title: '第一章作业：软件工程概述',
        description: '完成第一章的课后习题，包括选择题和简答题。重点掌握软件工程的基本概念和开发模型。',
        deadline: '2024-03-20 23:59:59',
        isSubmitted: false,
        selectedFile: null
      },
      {
        homework_id: 2,
        title: '第二章作业：需求分析',
        description: '完成第二章的实践项目，包括需求分析文档的编写和用例图的绘制。要求使用UML工具完成用例图。',
        deadline: '2024-03-25 23:59:59',
        isSubmitted: true,
        selectedFile: null
      },
      {
        homework_id: 3,
        title: '第三章作业：系统设计',
        description: '完成第三章的案例分析，包括系统架构设计和数据库设计。要求提交设计文档和ER图。',
        deadline: '2024-03-30 23:59:59',
        isSubmitted: false,
        selectedFile: null
      }
    ]
  },
  {
    course_id: 2,
    course_name: '数据库系统',
    homework_list: [
      {
        homework_id: 4,
        title: '第一章作业：数据库基础',
        description: '完成第一章的课后习题，包括SQL语句练习和数据库设计题。',
        deadline: '2024-03-22 23:59:59',
        isSubmitted: false,
        selectedFile: null
      },
      {
        homework_id: 5,
        title: '第二章作业：关系数据库',
        description: '完成第二章的实践项目，包括关系代数和SQL查询优化。',
        deadline: '2024-03-27 23:59:59',
        isSubmitted: false,
        selectedFile: null
      }
    ]
  },
  {
    course_id: 3,
    course_name: '操作系统',
    homework_list: [
      {
        homework_id: 6,
        title: '第一章作业：操作系统概述',
        description: '完成第一章的课后习题，包括进程管理和内存管理的概念题。',
        deadline: '2024-03-24 23:59:59',
        isSubmitted: true,
        selectedFile: null
      },
      {
        homework_id: 7,
        title: '第二章作业：进程调度',
        description: '完成第二章的实践项目，包括进程调度算法的实现和性能分析。',
        deadline: '2024-03-29 23:59:59',
        isSubmitted: false,
        selectedFile: null
      }
    ]
  }
]

// 初始化加载作业列表
const initHomeworkList = async () => {
  loading.value = true
  try {
    // 这里应该调用实际的API获取所有课程的作业
    // const response = await getHomeworkList()
    // if (response.code === '200' && response.data) {
    //   homeworkList.value = response.data
    // }
    
    // 使用示例数据
    homeworkList.value = mockHomeworkList
  } catch (error) {
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

// 检查作业是否已截止
const isOverdue = (homework) => {
  return new Date(homework.deadline) < new Date()
}

// 获取作业状态
const getHomeworkStatus = (homework) => {
  if (homework.isSubmitted) return 'success'
  if (isOverdue(homework)) return 'danger'
  return 'warning'
}

// 文件上传前的验证
const beforeUpload = (file) => {
  const isValidType = file.type === 'application/pdf' || file.type === 'application/zip'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传 PDF 或 ZIP 文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！')
    return false
  }
  return true
}

// 文件改变时的处理
const handleFileChange = (file, homework) => {
  homework.selectedFile = file.raw
}

// 提交作业
const submitHomework = async (homework) => {
  if (!homework.selectedFile) {
    ElMessage.warning('请先选择要提交的文件')
    return
  }

  try {
    submitting.value = homework.homework_id
    const response = await submitHomeworkAPI({
      homework_id: homework.homework_id,
      student_id: 1001, // 这里应该从用户信息中获取
      file: homework.selectedFile
    })

    if (response.code === '200') {
      ElMessage.success('作业提交成功')
      homework.isSubmitted = true
      homework.selectedFile = null
    } else {
      ElMessage.error(response.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = null
  }
}

// 页面加载时初始化数据
initHomeworkList()
</script>

<style scoped>
.homework-container {
  padding: 20px;
  max-width: 1000px;
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

.homework-list {
  margin-top: 20px;
}

.homework-item {
  margin-bottom: 10px;
}

.homework-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.homework-header h3 {
  margin: 0;
  font-size: 1.2em;
  color: #303133;
}

.homework-content {
  padding: 10px 0;
}

.description {
  margin: 10px 0;
  line-height: 1.5;
  color: #606266;
}

.homework-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

:deep(.el-timeline-item__node) {
  background-color: var(--el-color-primary);
}

:deep(.el-timeline-item__node--success) {
  background-color: var(--el-color-success);
}

:deep(.el-timeline-item__node--warning) {
  background-color: var(--el-color-warning);
}

:deep(.el-timeline-item__node--danger) {
  background-color: var(--el-color-danger);
}

:deep(.el-upload) {
  width: auto;
}

:deep(.el-upload-dragger) {
  width: auto;
  height: auto;
}

:deep(.el-collapse-item__header) {
  font-size: 1.1em;
  font-weight: bold;
}
</style>