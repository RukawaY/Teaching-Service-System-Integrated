<template>
  <div class="upload-resource-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <h2>上传课程资源</h2>
        </div>
      </template>

      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <!-- 学生ID输入 -->
        <el-form-item label="学生ID" prop="student_id">
          <el-input v-model="formData.student_id" placeholder="请输入学生ID"></el-input>
        </el-form-item>

        <!-- 课程ID输入 -->
        <el-form-item label="课程ID" prop="course_id">
          <el-input v-model="formData.course_id" placeholder="请输入课程ID"></el-input>
        </el-form-item>

        <!-- 目录ID输入（新增） -->
        <el-form-item label="目录ID" prop="directory_id">
          <el-input v-model="formData.directory_id" placeholder="请输入资源所属目录ID"></el-input>
        </el-form-item>

        <!-- 资源名称输入 -->
        <el-form-item label="资源名称" prop="resource_name">
          <el-input v-model="formData.resource_name" placeholder="请输入资源名称"></el-input>
        </el-form-item>

        <!-- 资源描述输入 -->
        <el-form-item label="资源描述" prop="resource_description">
          <el-input
            v-model="formData.resource_description"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          ></el-input>
        </el-form-item>

        <!-- 文件上传 -->
        <el-form-item label="上传文件" prop="file">
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :limit="1"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 PDF 或 ZIP 文件
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadResourceFile } from '../../../api/resource_share/student'  // 替换为实际API（需适配后端/resource/upload接口）

// 表单数据（新增directory_id）
const formData = reactive({
  student_id: '',
  course_id: '',
  directory_id: '',  // 新增目录ID字段
  resource_name: '',
  resource_description: '',
  file: null
})

// 表单验证规则（新增目录ID验证）
const rules = {
  student_id: [
    { required: true, message: '请输入学生ID', trigger: 'blur' },
    { pattern: /^\d+$/, message: '学生ID必须为数字', trigger: 'blur' }
  ],
  course_id: [
    { required: true, message: '请输入课程ID', trigger: 'blur' },
    { pattern: /^\d+$/, message: '课程ID必须为数字', trigger: 'blur' }
  ],
  directory_id: [  // 新增
    { required: true, message: '请输入目录ID', trigger: 'blur' },
    { pattern: /^\d+$/, message: '目录ID必须为数字', trigger: 'blur' }
  ],
  resource_name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  resource_description: [
    { required: true, message: '请输入资源描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请上传文件', trigger: 'change' }
  ]
}

const formRef = ref(null)
const submitting = ref(false)

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
const handleFileChange = (file) => {
  formData.file = file.raw
}

// 提交表单（调整上传参数）
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    submitting.value = true
    // 构建FormData（适配后端/resource/upload接口参数：file、directoryId、courseId、ownerId（student_id）、description）
    const formDataToUpload = new FormData()
    formDataToUpload.append('file', formData.file)
    formDataToUpload.append('directoryId', formData.directory_id)
    formDataToUpload.append('courseId', formData.course_id)
    formDataToUpload.append('ownerId', formData.student_id)
    formDataToUpload.append('description', formData.resource_description)

    const response = await uploadResourceFile(formDataToUpload)  // 调用适配后的API

    if (response.code === '200') {
      ElMessage.success('资源上传成功！')
      resetForm()
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败，请检查表单内容')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.file = null
}
</script>

<style scoped>
.upload-resource-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.upload-card {
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

.upload-demo {
  width: 100%;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 7px;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-form-item__content) {
  justify-content: flex-start;
}
</style>
