<template>
  <div class="course-auto-arrange container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>自动排课</h2>
          <p>排课参数配置</p>
        </div>
      </template>

      <el-form :model="config" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="学年">
              <el-input v-model="config.year" placeholder="请输入学年" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学期">
              <el-select v-model="config.semester" placeholder="请选择学期">
                <el-option label="春夏" value="春夏" />
                <el-option label="秋冬" value="秋冬" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排课优先级">
              <el-select v-model="config.priority" multiple placeholder="请选择优先级">
                <el-option label="教师偏好" value="teacher" />
                <el-option label="教室设备" value="equipment" />
                <el-option label="班级连续性" value="continuity" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="特殊约束">
          <el-checkbox-group v-model="config.constraints">
            <el-checkbox label="avoidConsecutive">避免连堂</el-checkbox>
            <el-checkbox label="teacherGap">教师课间休息</el-checkbox>
            <el-checkbox label="classroomConflict">避免教室冲突</el-checkbox>
            <el-checkbox label="classroomGap">教室课间休息</el-checkbox>
            <el-checkbox label="avoidSingle">避免单节课程</el-checkbox>
            <el-checkbox label="avoidWeekend">避免周末排课</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="待排课程">
          <div class="course-selection-container">
            <div class="selection-actions">
              <el-input
                v-model="courseSearch"
                placeholder="按课程名称或ID搜索"
                clearable
                @clear="handleSearchClear"
                style="width: 240px; margin-right: 20px;"
              />
              <el-button type="primary" size="small" @click="toggleAllSelection">
                {{ selectAll ? '取消全选' : '全选' }}
              </el-button>
              <span class="selected-count">已选 {{ selectedCourses.length }} 门课程</span>
            </div>

            <div class="table-container">
              <el-table
                :data="filteredCourses"
                style="width: 100%; font-size: 15px;"
                empty-text="暂无课程数据"
                @selection-change="handleSelectionChange"
                ref="courseTable"
                border highlight-current-row
              >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column prop="id" label="课程ID" width="120" align="center" />
                <el-table-column prop="name" label="课程名称" width="180" align="center" />
                <el-table-column prop="credit" label="学分" width="80" align="center" />
                <el-table-column prop="category" label="类型" width="100" align="center" />
                <el-table-column prop="hoursPerWeek" label="每周课时" width="100" align="center" />
                <el-table-column prop="description" label="课程描述" min-width="200" align="center" show-overflow-tooltip />
              </el-table>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="generateSchedule" :loading="generating">
            生成排课方案
          </el-button>
          <el-button @click="resetConfig">重置参数</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      v-model="showProgressDialog"
      title="排课进度"
      width="30%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <el-progress
        :percentage="progressPercent"
        :status="progressStatus"
        :text-inside="true"
        :stroke-width="20"
      />
      <div class="progress-message">{{ progressMessage }}</div>
      <template #footer>
        <el-button
          v-if="progressStatus === 'success' || progressStatus === 'error'"
          type="primary"
          @click="showProgressDialog = false"
        >
          完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 配置数据
const config = reactive({
  year: '2025',
  semester: '春夏',
  priority: ['teacher', 'equipment'],
  constraints: ['avoidConsecutive', 'teacherGap']
})

// 课程数据
const courses = ref([])
const courseSearch = ref('')
const selectAll = ref(false)
const selectedCourses = ref([])
const courseTable = ref(null)

// 排课进度
const generating = ref(false)
const showProgressDialog = ref(false)
const progressPercent = ref(0)
const progressStatus = ref('')
const progressMessage = ref('')

// 【修改】补全课程筛选的计算属性
const filteredCourses = computed(() => {
  if (!courseSearch.value) {
    return courses.value;
  }
  return courses.value.filter(course =>
    // 按课程名称或ID进行不区分大小写的搜索
    course.name.toLowerCase().includes(courseSearch.value.toLowerCase()) ||
    String(course.id).includes(courseSearch.value)
  );
});


const toggleAllSelection = () => {
  selectAll.value = !selectAll.value;
  if (courseTable.value) {
    if (selectAll.value) {
      filteredCourses.value.forEach(row => {
        courseTable.value.toggleRowSelection(row, true);
      });
    } else {
      courseTable.value.clearSelection();
    }
  }
};

const handleSelectionChange = (selection) => {
  selectedCourses.value = selection
  selectAll.value = selection.length === filteredCourses.value.length && filteredCourses.value.length > 0;
};

const handleSearchClear = () => {
  courseSearch.value = ''
}


const generateSchedule = async () => {
  if (selectedCourses.value.length === 0) {
    ElMessage.warning('请至少选择一门课程进行排课');
    return;
  }

  generating.value = true;
  showProgressDialog.value = true;
  progressPercent.value = 0;
  progressStatus.value = '';
  progressMessage.value = '正在准备排课数据...';

  try {
    const scheduleConfig = {
      secYear: config.year,
      semester: config.semester,
      priority: config.priority,
      constraints: config.constraints,
      courses: selectedCourses.value.map(course => course.id) 
    };
    
    progressPercent.value = 30;
    progressMessage.value = '正在向服务器发送请求...';
    
    const response = await axios.post('/arrange/api/schedules/generate', scheduleConfig);

    progressPercent.value = 70;
    progressMessage.value = '服务器正在处理排课...';

    if (response.status === 200) {
      setTimeout(() => {
          progressPercent.value = 100;
          progressStatus.value = 'success';
          progressMessage.value = '排课方案生成成功';
          ElMessage.success('排课方案生成成功');
      }, 500);
    } else {
      throw new Error('API returned non-200 status');
    }
  } catch (error) {
    progressPercent.value = 100;
    progressStatus.value = 'exception';
    progressMessage.value = `排课发生错误: ${error.message || '未知错误'}`;
    ElMessage.error('排课发生错误');
    console.error('生成排课方案失败:', error);
  } finally {
    generating.value = false;
  }
};

const resetConfig = () => {
  Object.assign(config, {
    year: '2025',
    semester: '春夏',
    priority: ['teacher', 'equipment'],
    constraints: ['avoidConsecutive', 'teacherGap']
  })
  if (courseTable.value) {
    courseTable.value.clearSelection();
  }
  selectedCourses.value = [];
  selectAll.value = false;
  courseSearch.value = '';
};

onMounted(async () => {
  try {
    const response = await axios.get('/arrange/api/courses');
    courses.value = response.data;
  } catch (error) {
    ElMessage.error('获取课程数据失败');
    console.error('获取课程数据失败:', error);
  }
})
</script>

<style scoped>
.course-auto-arrange {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.form-card {
  width: 95%;
  margin: 0px auto 20px auto;
}
.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.card-header h2 {
  margin-bottom: 5px;
}
.card-header p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
.time-separator {
  margin: 0 10px;
  color: var(--el-text-color-secondary);
}
.course-selection-container {
  width: 100%;
}
.selection-actions {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.selected-count {
  margin-left: 15px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
.progress-message {
  margin-top: 10px;
  text-align: center;
  color: var(--el-text-color-secondary);
}
</style>