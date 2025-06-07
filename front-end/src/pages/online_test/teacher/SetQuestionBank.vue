<template>
  <div class="question-bank-container">
    <!-- 选择课程 -->
    <div class="selection-area">
      <el-form :model="selectedCourse" label-width="100px" inline>
        <el-form-item label="选择课程">
          <el-select v-model="selectedCourse" placeholder="请选择课程" style="width: 300px;" @change="fetchChapters">
            <el-option
              v-for="course in courses"
              :key="course.course_id"
              :label="course.course_name"
              :value="course.course_id"
            />
          </el-select>
        </el-form-item>

        <!-- 选择章节 -->
        <el-form-item label="选择章节">
          <el-select v-model="selectedChapter" placeholder="请选择章节" style="width: 300px;" @change="applyFilters">
            <el-option
              v-for="chapter in chapters"
              :key="chapter.chapter_id"
              :label="`第 ${chapter.chapter_id} 章`"
              :value="chapter.chapter_id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 筛选条件 -->
      <el-form :model="filterOptions" label-width="80px" inline class="filter-area">
        <el-form-item label="题干">
          <el-input v-model="filterOptions.questionText" placeholder="请输入题干内容" style="width: 600px;"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterOptions.type" placeholder="请选择题目类型" style="width: 150px;">
            <el-option label="选择题" value="MC" />
            <el-option label="判断题" value="TF" />
          </el-select>
        </el-form-item>
        <el-button type="success" @click="applyFilters">应用筛选</el-button>
        <el-button icon="plus" circle @click="openAddDialog" style="float: right;" />
      </el-form>

    </div>

    <!-- 题目列表展示 -->
    <el-table :data="questions" border style="width: 100%">
      <el-table-column prop="content" label="题干" />
      <el-table-column prop="question_type" label="类型" />
      <el-table-column prop="score" label="分数" />
      <el-table-column prop="answer" label="答案" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteQuestion(row.question_id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
<el-dialog v-model="editDialogVisible" title="编辑题目">
  <el-form :model="currentEditQuestion">
    <el-form-item label="题干">
      <el-input v-model="currentEditQuestion.content" />
    </el-form-item>
    <el-form-item label="题目类型">
      <el-select v-model="currentEditQuestion.question_type" placeholder="请选择类型">
        <el-option label="选择题" value="MC" />
        <el-option label="判断题" value="TF" />
      </el-select>
    </el-form-item>

    <el-form-item label="题目难度">
      <el-select v-model="currentEditQuestion.difficulty" placeholder="请选择难度">
        <el-option label="1" value="1" />
        <el-option label="2" value="2" />
        <el-option label="3" value="3" />
        <el-option label="4" value="4" />
        <el-option label="5" value="5" />
      </el-select>
    </el-form-item>

    <!-- 添加选项输入框，仅当题目类型为选择题时显示 -->
    <el-form-item label="选项" v-if="currentEditQuestion.question_type === 'MC'">
      <el-input
        v-model="currentEditQuestion.options"
        type="textarea"
        :rows="4"
        placeholder="请输入选项，每行一个（例如：A.选项内容）"
      />
    </el-form-item>

    <el-form-item label="分数">
      <el-input-number v-model="currentEditQuestion.score" :min="0" />
    </el-form-item>
    <el-form-item label="答案">
      <el-input v-model="currentEditQuestion.answer" />
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="editDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="saveEdit">保存</el-button>
  </template>
</el-dialog>

    <!-- 新增弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增题目">
      <el-form :model="newQuestion">
        <el-form-item label="题干">
          <el-input v-model="newQuestion.content" />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="newQuestion.question_type" placeholder="请选择类型">
            <el-option label="选择题" value="MC" />
            <el-option label="判断题" value="TF" />
          </el-select>
        </el-form-item>

        <el-form-item label="题目难度">
          <el-select v-model="newQuestion.difficulty" placeholder="请选择难度">
            <el-option label="1" value="1" />
            <el-option label="2" value="2" />
            <el-option label="3" value="3" />
            <el-option label="4" value="4" />
            <el-option label="5" value="5" />
          </el-select>
        </el-form-item>

        <el-form-item label="选项" v-if="newQuestion.question_type === 'MC'">
          <el-input
            v-model="newQuestion.options"
            type="textarea"
            :rows="4"
            placeholder="请输入选项，每行一个（例如：A.选项内容）"
          />
        </el-form-item>
        <el-form-item label="分数">
          <el-input-number v-model="newQuestion.score" :min="0" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="newQuestion.answer" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNewQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
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
const chapters = ref([])
const selectedChapter = ref(null)
const questions = ref([])


// 筛选条件
const filterOptions = ref({
  questionText: '',
  type: ''
})

// 弹窗控制
const editDialogVisible = ref(false)
const addDialogVisible = ref(false)
const currentEditQuestion = ref({})
const newQuestion = ref({
  content: '',
  question_type: '',
  score: 0,
  answer: '',
  options: '' 
})

// 获取教师所有课程
async function fetchCourses() {
  try {
    const res = await api.get(`/test/questions/course/${teacherId.value}`)
    courses.value = res.data || []
  } catch (error) {
    console.error('获取课程失败:', error)
    ElMessage.error('获取课程失败')
  }
}

// 获取该课程下的章节
async function fetchChapters(courseId) {
  if (!courseId) return;
  try {
    const res = await api.get('/test/questions/getQuestionByCourse', {
      params: { courseId },
    });

    // 使用 Set 去重 chapter_id
    const uniqueChapterIds = [...new Set(res.data.map(q => q.chapter_id))];

    // 转换为对象数组
    chapters.value = uniqueChapterIds.map(id => ({ chapter_id: id }));

    selectedChapter.value = null;
    questions.value = [];
  } catch (error) {
    console.error('获取章节失败:', error);
    ElMessage.error('获取章节失败')
  }
}

// 查询题目
async function applyFilters() {
  if (!selectedCourse.value || !selectedChapter.value) return
  try {
    const res = await api.get('/test/questions/searchQuestions', {
      params: {
        courseId: selectedCourse.value,
        bankId: selectedChapter.value,
        queName: filterOptions.value.questionText,
        queType: filterOptions.value.type
      }
    })
    questions.value = res.data.map(item => ({
      question_id: item.question_id,
      content: item.content,
      question_type: item.question_type,
      difficulty: item.difficulty,
      score: item.score,
      answer: item.answer
    }))
  } catch (error) {
    console.error('查询题目失败:', error)
    ElMessage.error('查询题目失败')
  }
}

// 删除题目
async function deleteQuestion(questionId) {
  try {
    await api.delete('/test/questions/delQuestion', {
      params: { question_id: questionId }
    })
    questions.value = questions.value.filter(q => q.question_id !== questionId)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除题目失败:', error)
    ElMessage.error('删除题目失败')
  }
}

// 打开编辑弹窗
function openEditDialog(row) {
  currentEditQuestion.value = { ...row }
  editDialogVisible.value = true
}

// 保存编辑
async function saveEdit() {
  try {
    let optionsArray = null;
    if (currentEditQuestion.value.question_type === 'MC') {
      const lines = currentEditQuestion.value.options.split('\n');
      optionsArray = lines.map(line => line.trim()).filter(line => line !== '');
    }

    // 构建更新数据
    const payload = {
      questionId: currentEditQuestion.value.question_id, // 添加要更新的题目ID
      courseId: selectedCourse.value,
      chapterId: selectedChapter.value,
      questionType: currentEditQuestion.value.question_type,
      content: currentEditQuestion.value.content,
      answer: currentEditQuestion.value.answer,
      score: currentEditQuestion.value.score,
      difficulty: currentEditQuestion.value.difficulty,
      options: optionsArray // 选择题的选项数组
    };

    // 使用PUT请求直接更新题目
    await api.put('/test/questions/updateQuestion', payload);
    
    editDialogVisible.value = false;
    applyFilters();
    ElMessage.success('编辑成功')
  } catch (error) {
    console.error('编辑题目失败:', error);
    ElMessage.error('编辑题目失败')
  }
}


// 打开新增弹窗
function openAddDialog() {
  newQuestion.value = {
    content: '',
    question_type: '',
    score: 0,
    answer: ''
  }
  addDialogVisible.value = true
}

// 保存新增题目
async function saveNewQuestion() {
  try {
    let optionsArray = null;
    if (newQuestion.value.question_type === 'MC') {
      const lines = newQuestion.value.options.split('\n');
      optionsArray = lines.map(line => line.trim()).filter(line => line !== '');
    }

    const payload = {
      courseId: selectedCourse.value,
      chapterId: selectedChapter.value,
      questionType: newQuestion.value.question_type,
      content: newQuestion.value.content,
      answer: newQuestion.value.answer,
      score: newQuestion.value.score,
      difficulty: newQuestion.value.difficulty,
      options: optionsArray // 添加选项字段
    };

    await api.post('/test/questions/addQuestion', payload);
    addDialogVisible.value = false;
    applyFilters();
    ElMessage.success('添加成功')
  } catch (error) {
    console.error('新增题目失败:', error);
    ElMessage.error('新增题目失败')
  }
}

// 初始化加载课程
fetchCourses()
</script>

<style scoped>
.question-bank-container {
  padding: 20px;
}
.selection-area {
  margin-bottom: 20px;
}
.filter-area {
  margin-top: 15px;
}
</style>