<template>
  <div class="teacher-schedule">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <h2>教师课表查询</h2>
        </div>
      </template>

      <div class="filter-form">
        <el-form :model="filterForm">
          <div class="filter-row">
            <el-form-item label="教师" class="filter-item">
              <el-input v-model="currentTeacher" disabled style="width: 200px" />
            </el-form-item>
            <el-form-item label="学期" class="filter-item">
              <el-select v-model="filterForm.semester" placeholder="请选择学期" style="width: 200px">
                <el-option label="春夏" value="春夏" />
                <el-option label="秋冬" value="秋冬" />
              </el-select>
            </el-form-item>
            <el-form-item label="学年" class="filter-item">
              <el-input v-model="filterForm.sec_year" placeholder="请输入学年" style="width: 200px" />
            </el-form-item>
            <el-button 
              type="primary" 
              @click="handleQuery" 
              :loading="loading"
              :disabled="!currentTeacherId || userStore.loginUser.role !== 't'"
            >
              查询
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>

    <el-card class="schedule-card">
      <!-- 未登录或非教师提示 -->
      <div v-if="!currentTeacherId || userStore.loginUser.role !== 't'" class="no-permission-tip">
        <el-alert
          title="权限提示"
          description="请使用教师账号登录后查看课程安排"
          type="warning"
          show-icon
          :closable="false"
        />
      </div>
      
      <div v-else class="schedule-container">
        <table class="schedule-table">
          <thead>
            <tr>
              <th>时间</th>
              <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="(timeSlot, index) in timeSlots" :key="timeSlot.time">
              <tr>
                <td class="time-slot">{{ timeSlot.time }}</td>
                <td
                  v-for="day in weekDays"
                  :key="day.value"
                  :rowspan="getCourseRowspan(day.value, timeSlot)"
                  :class="{
                    'course-cell': true,
                    'occupied': isCourseStart(day.value, timeSlot),
                    'empty': !isCourseStart(day.value, timeSlot)
                  }"
                  v-show="isCourseStart(day.value, timeSlot) || !getCourse(day.value, timeSlot)"
                >
                  <div v-if="isCourseStart(day.value, timeSlot)" class="course-info">
                    <div class="course-name">{{ getCourse(day.value, timeSlot).courseName }}</div>
                    <div class="classroom">教室: {{ getCourse(day.value, timeSlot).classroomName }}</div>
                    <div class="time-range">{{ getCourseTimeRange(getCourse(day.value, timeSlot)) }}</div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { useuserLoginStore } from '../../../store/userLoginStore';

// 获取用户登录状态
const userStore = useuserLoginStore();

// 从登录用户信息中获取教师信息
const currentTeacher = computed(() => userStore.loginUser.name || '未登录');
const currentTeacherId = computed(() => {
  const userId = userStore.loginUser.user_id;
  return userId !== "null" ? parseInt(userId) : null;
});
const filterForm = reactive({
  semester: '春夏', // 默认学期
  sec_year: new Date().getFullYear().toString(), // 默认当前学年
});

const loading = ref(false);

const weekDays = ref([
  { value: 'Monday', label: '周一' },
  { value: 'Tuesday', label: '周二' },
  { value: 'Wednesday', label: '周三' },
  { value: 'Thursday', label: '周四' },
  { value: 'Friday', label: '周五' },
  { value: 'Saturday', label: '周六' },
  { value: 'Sunday', label: '周日' }
]);

const timeSlots = ref([
  { time: '08:00-08:45', start: 1, end: 1 },
  { time: '08:50-09:35', start: 2, end: 2 },
  { time: '10:00-10:45', start: 3, end: 3 },
  { time: '10:50-11:35', start: 4, end: 4 },
  { time: '11:40-12:25', start: 5, end: 5 },
  { time: '13:25-14:10', start: 6, end: 6 },
  { time: '14:15-15:00', start: 7, end: 7 },
  { time: '15:05-15:50', start: 8, end: 8 },
  { time: '16:15-17:00', start: 9, end: 9 },
  { time: '17:05-17:50', start: 10, end: 10 },
  { time: '18:50-19:35', start: 11, end: 11 },
  { time: '19:40-20:25', start: 12, end: 12 },
  { time: '20:30-21:15', start: 13, end: 13 }
]);

const scheduleData = ref([]);

// 获取课程
const getCourse = (day, timeSlot) => {
  return scheduleData.value.find(course =>
    course.day === day &&
    course.timeSlot.start <= timeSlot.start &&
    course.timeSlot.end >= timeSlot.end &&
    course.teacherId === currentTeacherId.value
  );
};

const isCourseStart = (day, timeSlot) => {
  const course = getCourse(day, timeSlot);
  if (!course) return false;
  return course.timeSlot.start === timeSlot.start;
};

const getCourseRowspan = (day, timeSlot) => {
  const course = getCourse(day, timeSlot);
  if (!course) return 1;

  const startIndex = timeSlots.value.findIndex(slot => slot.start === course.timeSlot.start);
  const endIndex = timeSlots.value.findIndex(slot => slot.end === course.timeSlot.end);

  return (endIndex - startIndex) + 1;
};

const getCourseTimeRange = (course) => {
  if (!course) return '';
  const startSlot = timeSlots.value.find(slot => slot.start === course.timeSlot.start);
  const endSlot = timeSlots.value.find(slot => slot.end === course.timeSlot.end);

  return `${startSlot.time.split('-')[0]}~${endSlot.time.split('-')[1]}`;
};

const getTimeSlotByPeriod = (period) => {
  return timeSlots.value.find(slot => slot.start === period);
};

const handleQuery = async () => {
  // 检查是否已登录且为教师
  if (!currentTeacherId.value || userStore.loginUser.role !== 't') {
    ElMessage.error('请先登录教师账号');
    return;
  }
  
  loading.value = true;
  scheduleData.value = [];
  try {
    const response = await axios.get('/arrange/api/sections/query', {
      params: {
        teacher_id: currentTeacherId.value,
        semester: filterForm.semester,
        sec_year: filterForm.sec_year
      }
    });
    const responseData = response.data;

    if (responseData && responseData.length > 0) {

      // 处理课程数据
      scheduleData.value = responseData.flatMap(item => {
        const { secTime, courseName, classroomId, classroomName } = item;
        const timeEntries = secTime.split(';').map(entry => entry.trim());
        
        // 按天分组处理时间段
        const dayGroups = {};
        timeEntries.forEach(entry => {
          const [dayStr, periodStr] = entry.split(' ');
          const period = parseInt(periodStr);
          if (!dayGroups[dayStr]) {
            dayGroups[dayStr] = [];
          }
          dayGroups[dayStr].push(period);
        });

        // 处理每天的时间段
        return Object.entries(dayGroups).map(([dayStr, periods]) => {
          // 对时间段进行排序
          periods.sort((a, b) => a - b);
          
          // 找出连续的时间段
          const continuousPeriods = [];
          let currentGroup = [periods[0]];
          
          for (let i = 1; i < periods.length; i++) {
            // 检查是否跨过午休时间（第5节和第6节之间）或晚上休息时间（第10节和第11节之间）
            const isLunchBreak = periods[i-1] === 5 && periods[i] === 6;
            const isEveningBreak = periods[i-1] === 10 && periods[i] === 11;
            
            if (periods[i] === periods[i-1] + 1 && !isLunchBreak && !isEveningBreak) {
              currentGroup.push(periods[i]);
            } else {
              continuousPeriods.push([...currentGroup]);
              currentGroup = [periods[i]];
            }
          }
          continuousPeriods.push(currentGroup);

          // 为每个连续时间段创建课程记录
          return continuousPeriods.map(periodGroup => {
            const startPeriod = periodGroup[0];
            const endPeriod = periodGroup[periodGroup.length - 1];
            const day = getDayOfWeekFromString(dayStr);

            if (day) {
              return {
                teacherId: currentTeacherId.value,
                day: day,
                timeSlot: { start: startPeriod, end: endPeriod },
                courseName: courseName,
                classroomId: classroomId,
                classroomName: classroomName,
              };
            }
            return null;
          });
        }).flat().filter(Boolean);
      });

      // 移除处理连堂课程的逻辑，因为已经在上面处理过了
      scheduleData.value.forEach(course => {
        if (course) {
          course._rowspan = course.timeSlot.end - course.timeSlot.start + 1;
        }
      });

      ElMessage.success('查询成功');
    } else {
      ElMessage.info('没有课程安排数据');
    }
  } catch (error) {
    ElMessage.error('获取教师课表失败，请检查 public 目录下是否存在 teacherSchedule.json 文件');
    console.error('获取教师课表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 关键修改：根据英文星期标识获取 weekDays 中的 label
const getDayOfWeekFromString = (dayStr) => {
  const dayMap = {
    'Monday': 'Monday',
    'Tuesday': 'Tuesday',
    'Wednesday': 'Wednesday',
    'Thursday': 'Thursday',
    'Friday': 'Friday',
    'Saturday': 'Saturday',
    'Sunday': 'Sunday'
  };
  return dayMap[dayStr] || '';
};

onMounted(() => {
  // 只有在用户已登录且为教师时才自动查询课表
  if (currentTeacherId.value && userStore.loginUser.role === 't') {
    handleQuery();
  }
});
</script>

<style scoped>
.teacher-schedule {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  text-align: center;
}

.filter-form {
  display: flex;
  justify-content: center;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-item {
  margin-bottom: 0;
}

.schedule-container {
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e0e0e0;
  padding: 8px;
  text-align: center;
}

.schedule-table th {
  background-color: #f5f5f5;
}

.time-slot {
  background-color: #f5f5f5;
  width: 100px;
}

.course-cell {
  height: 80px;
  vertical-align: middle;
}

.empty {
  background-color: #f9f9f9;
}

.occupied {
  background-color: #e3f2fd;
}

.course-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.course-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.classroom {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 4px;
}

.time-range {
  font-size: 0.8em;
  color: #888;
}

.no-permission-tip {
  padding: 20px;
  text-align: center;
}
</style>