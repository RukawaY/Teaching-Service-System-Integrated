<template>
  <div class="course-modify container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>调整课程安排</h2>
          <p>请输入筛选条件查找要调整的课程信息</p>
        </div>
      </template>
      <el-form :model="filterForm" label-width="120px" class="input-form">
        <el-form-item label="开课ID">
          <el-input v-model="filterForm.section_id" placeholder="请输入开课ID" />
        </el-form-item>
        <el-form-item label="课程ID">
          <el-input v-model="filterForm.course_id" placeholder="请输入课程ID" />
        </el-form-item>
        <el-form-item label="教室ID">
          <el-input v-model="filterForm.classroom_id" placeholder="请输入教室ID" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input v-model.number="filterForm.capacity" placeholder="请输入容量" type="number" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="filterForm.semester" placeholder="请选择学期" style="width: 100%">
            <el-option label="春夏" value="春夏" />
            <el-option label="秋冬" value="秋冬" />
          </el-select>
        </el-form-item>
        <el-form-item label="开课年份">
          <el-input v-model.number="filterForm.sec_year" placeholder="请输入开课年份" type="number" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter" :loading="filterLoading">
            查询课程
          </el-button>
          <el-button @click="resetFilterForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="course-card">
      <template #header>
        <div class="card-header-flex">
          <h2 v-if="courseList.length > 0">
            共查询到 {{ courseList.length }} 门课程
          </h2>
          <h2 v-else-if="hasSearched">
            未查询到符合条件的课程
          </h2>
          <h2 v-else>
            请输入查询条件
          </h2>
          <div class="button-group">
            <el-button type="success" @click="openAddDialog" plain>
              <el-icon><Plus /></el-icon>
              添加课程
            </el-button>
            <el-button type="warning" @click="handleCheckButtonClick" plain>
              <el-icon><Check /></el-icon>
              检查排课
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="courseList" style="font-size: 15px;" empty-text="暂无课程数据">
        <el-table-column prop="section_id" label="开课ID" />
        <el-table-column prop="course_id" label="课程ID" />
        <el-table-column prop="classroom_id" label="教室ID" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="semester" label="学期" />
        <el-table-column prop="sec_year" label="开课年份" />
        <el-table-column prop="sec_time" label="开课时间" />
        <el-table-column prop="available_capacity" label="剩余容量" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              修改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="formDialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开课ID" v-if="dialogMode === 'edit'">
              <el-input v-model="formData.section_id" :disabled="dialogMode === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="dialogMode === 'edit'">
            <el-form-item label="课程ID">
              <el-input v-model="formData.course_id" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="dialogMode !== 'edit'">
            <el-form-item label="课程ID">
              <el-input v-model="formData.course_id" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教室ID">
              <el-input v-model="formData.classroom_id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量">
              <el-input-number v-model="formData.capacity" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="剩余容量">
              <el-input-number v-model="formData.available_capacity" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期">
              <el-select v-model="formData.semester" placeholder="请选择学期" style="width: 100%">
                <el-option label="春夏" value="春夏" />
                <el-option label="秋冬" value="秋冬" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开课年份">
              <el-input v-model="formData.sec_year" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="开课时间" style="display: flex; align-items: center;">
              <el-input :value="humanReadableTime" placeholder="请点击按钮选择时间" :readonly="true" style="flex: 1;" />
              <el-button @click="openTimeSelectionDialog" style="margin-left: 10px;">
                选择时间
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="formLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="timeSelectionDialogVisible" title="选择课程时间" width="800px" append-to-body>
        <div class="timetable-container">
            <div class="timetable-header">
                <div class="timetable-cell time-header-cell">时间</div>
                <div v-for="period in 13" :key="`header-${period}`" class="timetable-cell period-header-cell">第{{ period }}节</div>
            </div>
            <div v-for="(day, dayIndex) in weekDays" :key="day" class="timetable-row">
                <div class="timetable-cell day-header-cell">{{ day }}</div>
                <div v-for="period in 13" :key="`${dayIndex}-${period}`" class="timetable-cell time-slot-cell" :class="{ 'selected': isTimeSlotSelected(dayIndex + 1, period) }" @click="toggleTimeSlot(dayIndex + 1, period)"></div>
            </div>
        </div>
        <template #footer>
            <el-button @click="timeSelectionDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmTimeSelection">确定</el-button>
        </template>
    </el-dialog>

    <!-- 检查排课对话框 -->
    <el-dialog
      v-model="checkDialogVisible"
      title="检查排课"
      width="500px"
    >
      <el-form :model="checkForm" label-width="100px">
        <el-form-item label="学期">
          <el-select v-model="checkForm.semester" placeholder="请选择学期" style="width: 100%">
            <el-option label="春夏" value="春夏" />
            <el-option label="秋冬" value="秋冬" />
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-input v-model.number="checkForm.year" placeholder="请输入学年" type="number" />
        </el-form-item>
        <el-form-item v-if="checkResult" label="检查结果">
          <div class="check-result">{{ checkResult }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCheckArrangement" :loading="checkLoading">
          确认检查
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
  import { ref, reactive, computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Plus, Check } from '@element-plus/icons-vue'
  import axios from 'axios'

  const filterForm = reactive({
    section_id: '', course_id: '', classroom_id: '',
    capacity: '', semester: '', sec_year: ''
  })
  const filterLoading = ref(false)
  const hasSearched = ref(false)
  const courseList = ref([])
  const formDialogVisible = ref(false)
  const formLoading = ref(false)
  const dialogMode = ref('add')
  const formData = reactive({
    section_id: '', course_id: '', classroom_id: '',
    capacity: null, semester: '', sec_year: null, sec_time: '', available_capacity: 0
  })

  const dialogTitle = computed(() => {
    return dialogMode.value === 'add' ? '添加课程' : `修改课程 - ${formData.section_id}`
  })

  const dayMapping = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const humanReadableTime = computed(() => {
    return formData.sec_time;
  });

  const timeSelectionDialogVisible = ref(false)
  const selectedSlots = ref(new Set())
  const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  const openTimeSelectionDialog = () => {
    selectedSlots.value.clear()
    if (formData.sec_time && typeof formData.sec_time === 'string') {
      const slots = formData.sec_time.split(',').filter(s => s.trim() !== '')
      slots.forEach(slot => selectedSlots.value.add(slot))
    }
    timeSelectionDialogVisible.value = true
  }

  const toggleTimeSlot = (day, period) => {
    const slotKey = `${day}-${period}`
    if (selectedSlots.value.has(slotKey)) {
      selectedSlots.value.delete(slotKey)
    } else {
      selectedSlots.value.add(slotKey)
    }
  }

  const isTimeSlotSelected = (day, period) => {
    return selectedSlots.value.has(`${day}-${period}`)
  }

  const confirmTimeSelection = () => {
    const sortedSlots = Array.from(selectedSlots.value).sort((a, b) => {
      const [dayA, periodA] = a.split('-').map(Number);
      const [dayB, periodB] = b.split('-').map(Number);
      if (dayA !== dayB) return dayA - dayB;
      return periodA - periodB;
    });
    
    // 将时间槽转换为新的格式
    const formattedSlots = sortedSlots.map(slot => {
      const [day, period] = slot.split('-').map(Number);
      // 确保day在有效范围内（1-7）
      if (day >= 1 && day <= 7) {
        return `${dayMapping[day-1]} ${period}`;
      }
      return null;
    }).filter(slot => slot !== null); // 过滤掉无效的时间槽
    
    formData.sec_time = formattedSlots.join('; ');
    // console.log('Selected time slots:', formData.sec_time);
    timeSelectionDialogVisible.value = false;
    ElMessage.success('时间已更新！');
  }

  const handleFilter = async () => {
    filterLoading.value = true
    hasSearched.value = true
    try {
      const queryParams = {}
      for (const key in filterForm) {
        if (filterForm[key] !== '' && filterForm[key] !== null) {
          queryParams[key] = filterForm[key]
        }
      }
      const response = await axios.get('/arrange/api/sections/query', {
        params: queryParams
      })
      if (response.data) {
        courseList.value = response.data.map(item => ({
          section_id: item.id,
          course_id: item.courseId,
          classroom_id: item.classroomId,
          capacity: item.capacity,
          semester: item.semester,
          sec_year: item.secYear,
          sec_time: item.secTime,
          available_capacity: item.availableCapacity
        }))
        if (courseList.value.length === 0) {
          ElMessage.info('未查询到符合条件的课程安排')
        }
      } else {
        ElMessage.error('获取课程安排失败')
        courseList.value = []
      }
    } catch (error) {
      ElMessage.error('网络错误或服务器不可用')
      courseList.value = []
      console.error('筛选课程安排失败:', error)
    } finally {
      filterLoading.value = false
    }
  }

  const resetFilterForm = () => {
    Object.keys(filterForm).forEach(key => { filterForm[key] = '' })
    courseList.value = []
    hasSearched.value = false
    ElMessage.success('筛选条件已重置')
  }
  
  const resetFormData = () => {
    Object.assign(formData, {
        section_id: '', course_id: '', classroom_id: '',
        capacity: 80, semester: '春夏', sec_year: new Date().getFullYear(), 
        sec_time: '', available_capacity: 50
    })
  }

  const openAddDialog = () => {
    dialogMode.value = 'add'
    resetFormData()
    formDialogVisible.value = true
  }
  
  const openEditDialog = (row) => {
    dialogMode.value = 'edit'
    Object.assign(formData, row)
    formDialogVisible.value = true
  }

  // 保存函数
  const handleSave = async () => {
    formLoading.value = true
    try {
      const payload = {
        courseId: parseInt(formData.course_id),
        classroomId: parseInt(formData.classroom_id),
        capacity: parseInt(formData.capacity),
        semester: formData.semester,
        secYear: parseInt(formData.sec_year),
        secTime: formData.sec_time,
        availableCapacity: parseInt(formData.available_capacity)
      }
      if (dialogMode.value === 'add') {
        await axios.post('/arrange/api/sections', payload)
      } else {
        payload.Id = parseInt(formData.section_id)
        await axios.put(`/arrange/api/sections/${formData.section_id}`, payload)
      }
      ElMessage.success(`${dialogMode.value === 'add' ? '添加' : '修改'}成功！`)
      formDialogVisible.value = false
      await handleFilter()
    } catch (error) {
      ElMessage.error('网络错误或服务器不可用')
      console.error('保存课程安排失败:', error)
    } finally {
      formLoading.value = false
    }
  }

  // 检查排课相关
  const checkDialogVisible = ref(false)
  const checkLoading = ref(false)
  const checkResult = ref('')
  const checkForm = reactive({
    semester: '',
    year: new Date().getFullYear()
  })

  // 打开检查排课对话框
  const openCheckDialog = () => {
    checkDialogVisible.value = true
    checkResult.value = ''
  }

  // 检查排课函数
  const handleCheckArrangement = async () => {
    if (!checkForm.semester || !checkForm.year) {
      ElMessage.warning('请填写完整的学期和学年信息')
      return
    }

    checkLoading.value = true
    try {
      const response = await axios.get('/arrange/api/sections/check', {
        params: {
          semester: checkForm.semester,
          year: checkForm.year
        }
      })
      if (response.data) {
        checkResult.value = response.data
        ElMessage.success('排课检查完成')
      }
    } catch (error) {
      ElMessage.error('检查排课失败')
      console.error('检查排课失败:', error)
    } finally {
      checkLoading.value = false
    }
  }

  // 修改按钮点击事件
  const handleCheckButtonClick = () => {
    openCheckDialog()
  }
</script>

<style scoped>
  .course-modify { max-width: 1200px; margin: 0 auto; padding: 20px; }
  .container { display: flex; flex-direction: column; gap: 20px; }
  .form-card { width: 95%; margin: 0px auto 20px auto; }
  .card-header { display: flex; flex-direction: column; align-items: center; }
  .input-form { max-width: 500px; margin: 0 auto; }
  .course-card { width: 95%; margin: 0px auto 20px auto; }
  .card-header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  .timetable-container { display: flex; flex-direction: column; font-family: sans-serif; }
  .timetable-header, .timetable-row { display: flex; }
  .timetable-cell { border: 1px solid #e0e0e0; text-align: center; box-sizing: border-box; }
  .time-header-cell { flex: 0 0 80px; background-color: #f5f7fa; line-height: 40px; }
  .period-header-cell { flex: 1; background-color: #f5f7fa; line-height: 40px; font-size: 12px; }
  .day-header-cell { flex: 0 0 80px; background-color: #f5f7fa; writing-mode: vertical-rl; line-height: 80px; padding: 10px 0; height: 60px; display: flex; align-items: center; justify-content: center; }
  .time-slot-cell { flex: 1; height: 60px; cursor: pointer; transition: background-color 0.2s; }
  .time-slot-cell:hover { background-color: #ecf5ff; }
  .time-slot-cell.selected { background-color: #409eff; border-color: #409eff; }
  .button-group {
    display: flex;
    gap: 10px;
  }
  .check-result {
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    white-space: pre-wrap;
    word-break: break-all;
  }
</style>